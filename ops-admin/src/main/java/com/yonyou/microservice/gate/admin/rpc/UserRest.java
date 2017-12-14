package com.yonyou.microservice.gate.admin.rpc;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yonyou.microservice.gate.admin.rpc.service.PermissionService;
import com.yonyou.microservice.gate.common.vo.authority.PermissionInfo;
import com.yonyou.microservice.gate.common.vo.user.UserInfo;

/**
 * ${DESCRIPTION}
 *
 * @author joy
 * @create 2017-06-21 8:15
 */
@RestController
@RequestMapping("api")
public class UserRest {
    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "/user/username",method = RequestMethod.POST, produces="application/json")
    public  @ResponseBody UserInfo getUserByUsername(@RequestBody Map<String,String> data) {
        return permissionService.getUserByUsername(data.get("username"));
    }

    @RequestMapping(value = "/user/username/{username}",method = RequestMethod.GET, produces="application/json")
    public  @ResponseBody UserInfo getUserByUsername(@PathVariable("username")String username) {
        return permissionService.getUserByUsername(username);
    }

    @RequestMapping(value = "/user/username/test",method = RequestMethod.GET, produces="application/json")
    public  @ResponseBody UserInfo getUserByTest(@RequestParam("openid")String openid) {
        return permissionService.getUserByUsername("test");
    }


    @RequestMapping(value = "/user/phone/{phone}",method = RequestMethod.GET, produces="application/json")
    public  @ResponseBody UserInfo getUserByPhone(@PathVariable("phone")String phone) {
        return permissionService.getUserByPhone(phone);
    }
    
    @RequestMapping(value = "/permissions", method = RequestMethod.GET)
    public @ResponseBody List<PermissionInfo> getAllPermission(){
        return permissionService.getAllPermission();
    }


    @RequestMapping(value = "/user/un/{username}/permissions", method = RequestMethod.GET)
    public @ResponseBody List<PermissionInfo> getPermissionByUsername(@PathVariable("username") String username){
        return permissionService.getPermissionByUsername(username);
    }
}
