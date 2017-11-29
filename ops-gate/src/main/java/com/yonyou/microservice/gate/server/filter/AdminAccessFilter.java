package com.yonyou.microservice.gate.server.filter;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yonyou.cloud.common.jwt.IJWTInfo;
import com.yonyou.cloud.common.service.utils.ClientUtil;
import com.yonyou.microservice.auth.client.config.ServiceAuthConfig;
import com.yonyou.microservice.auth.client.config.UserAuthConfig;
import com.yonyou.microservice.auth.client.jwt.ServiceAuthUtil;
import com.yonyou.microservice.auth.client.jwt.UserAuthUtil;
import com.yonyou.microservice.gate.common.context.BaseContextHandler;
import com.yonyou.microservice.gate.common.msg.TokenErrorResponse;
import com.yonyou.microservice.gate.common.msg.TokenForbiddenResponse;
import com.yonyou.microservice.gate.common.vo.authority.PermissionInfo;
import com.yonyou.microservice.gate.common.vo.log.LogInfo;
import com.yonyou.microservice.gate.common.vo.user.UserInfo;
import com.yonyou.microservice.gate.server.feign.ILogService;
import com.yonyou.microservice.gate.server.feign.IUserService;
import com.yonyou.microservice.gate.server.utils.DBLog;

import lombok.extern.slf4j.Slf4j;

/**
 * ${DESCRIPTION}
 *
 * @author joy
 * @create 2017-06-23 8:25
 */
@Component
@Slf4j
public class AdminAccessFilter extends ZuulFilter {
	private static final String USER_HEAD_ID="userId";
	private static final String USER_HEAD_NAME="userName";
	private static Logger logger=Logger.getLogger(AdminAccessFilter.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private ILogService logService;

    @Value("${gate.ignore.startWith}")
    private String startWith;

    @Value("${zuul.prefix}")
    private String zuulPrefix;
    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    private ServiceAuthConfig serviceAuthConfig;

    @Autowired
    private UserAuthConfig userAuthConfig;

    @Autowired
    private ServiceAuthUtil serviceAuthUtil;
    
    public AdminAccessFilter(){
    	logger.info("--AdminAccessFilter对象创建");
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
    	logger.info("--AdminAccessFilter.run(),进入网关");
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        final String requestUri = request.getRequestURI().substring(zuulPrefix.length());
        final String method = request.getMethod();
        BaseContextHandler.setToken(null);
        // 不进行拦截的地址
        if (isStartWith(requestUri)) {
            return null;
        }
        IJWTInfo user = null;
        try {
            user = getJWTUser(request,ctx);
        } catch (Exception e) {
            setFailedRequest(JSON.toJSONString(new TokenErrorResponse(e.getMessage())),200);
            return null;
        }
        List<PermissionInfo> permissionInfos = userService.getAllPermissionInfo();
        // 判断资源是否启用权限约束
        Collection<PermissionInfo> result = getPermissionInfos(requestUri, method, permissionInfos);
        if(result.size()>0){
            checkAllow(requestUri, method, ctx, user.getUniqueName());
        }
        // 申请客户端密钥头
        ctx.addZuulRequestHeader(serviceAuthConfig.getTokenHeader(),serviceAuthUtil.getClientToken());
        ctx.addZuulRequestHeader(USER_HEAD_ID,user.getId());
        ctx.addZuulRequestHeader(USER_HEAD_NAME,user.getUniqueName());
        BaseContextHandler.remove();
        return null;
    }

    /**
     * 获取目标权限资源
     * @param requestUri
     * @param method
     * @param serviceInfo
     * @return
     */
    private Collection<PermissionInfo> getPermissionInfos(final String requestUri, final String method, List<PermissionInfo> serviceInfo) {
        return Collections2.filter(serviceInfo, new Predicate<PermissionInfo>() {
                @Override
                public boolean apply(PermissionInfo permissionInfo) {
                    String url = permissionInfo.getUri();
                    String uri = url.replaceAll("\\{\\*\\}", "[a-zA-Z\\\\d]+");
                    String regEx = "^" + uri + "$";
                    return (Pattern.compile(regEx).matcher(requestUri).find() || requestUri.startsWith(url + "/"))
                            && method.equals(permissionInfo.getMethod());
                }
            });
    }

    private void setCurrentUserInfoAndLog(RequestContext ctx, String username, PermissionInfo pm) {
        UserInfo info = userService.getUserByUsername(username);
        String host =  ClientUtil.getClientIp(ctx.getRequest());
        ctx.addZuulRequestHeader("userId", info.getId());
        ctx.addZuulRequestHeader("userName", URLEncoder.encode(info.getName()));
        ctx.addZuulRequestHeader("userHost", ClientUtil.getClientIp(ctx.getRequest()));
        LogInfo logInfo = new LogInfo(pm.getMenu(),pm.getName(),pm.getUri(),new Date(),info.getId(),info.getName(),host);
        DBLog.getInstance().setLogService(logService).offerQueue(logInfo);
    }

    /**
     * 返回session中的用户信息
     * @param request
     * @param ctx
     * @return
     */
    private IJWTInfo getJWTUser(HttpServletRequest request,RequestContext ctx) throws Exception {
        String authToken = request.getHeader(userAuthConfig.getTokenHeader());
        if(StringUtils.isBlank(authToken)){
            authToken = request.getParameter("token");
        }
        ctx.addZuulRequestHeader(userAuthConfig.getTokenHeader(),authToken);
        BaseContextHandler.setToken(authToken);
        return userAuthUtil.getInfoFromToken(authToken);
    }

    /**
     * 读取权限
     * @param request
     * @param username
     * @return
     */
    private List<PermissionInfo> getPermissionInfos(HttpServletRequest request, String username) {
        List<PermissionInfo> permissionInfos;
        if (request.getSession().getAttribute("permission") == null) {
            permissionInfos = userService.getPermissionByUsername(username);
            request.getSession().setAttribute("permission", permissionInfos);
        } else {
            permissionInfos = (List<PermissionInfo>) request.getSession().getAttribute("permission");
        }
        return permissionInfos;
    }

    /**
     * 权限校验
     * @param requestUri
     * @param method
     */
    private void checkAllow(final String requestUri, final String method ,RequestContext ctx,String username) {
        log.debug("uri：" + requestUri + "----method：" + method);
        List<PermissionInfo> permissionInfos = getPermissionInfos(ctx.getRequest(), username) ;
        Collection<PermissionInfo> result = getPermissionInfos(requestUri, method, permissionInfos);
        if (result.size() <= 0) {
            setFailedRequest(JSON.toJSONString(new TokenForbiddenResponse("Token Forbidden!")), 200);
        } else{
            PermissionInfo[] pms =  result.toArray(new PermissionInfo[]{});
            PermissionInfo pm = pms[0];
            if(!"GET".equals(method)){
                setCurrentUserInfoAndLog(ctx, username, pm);
            }
        }
    }


    /**
     * URI是否以什么打头
     * @param requestUri
     * @return
     */
    private boolean isStartWith(String requestUri) {
        boolean flag = false;
        for (String s : startWith.split(",")) {
            if (requestUri.startsWith(s)) {
                return true;
            }
        }
        return flag;
    }

    /**
     * Reports an error message given a response body and code.
     *
     * @param body
     * @param code
     */
    private void setFailedRequest(String body, int code) {
        log.debug("Reporting error ({}): {}", code, body);
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(code);
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(body);
            ctx.setSendZuulResponse(false);
//            throw new RuntimeException("Code: " + code + ", " + body); //optional
        }
    }
}
