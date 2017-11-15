/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yonyou.cloud.zuul.db.route;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import com.yonyou.cloud.zuul.db.store.ZuulRouteStore;
import com.yonyou.cloud.zuul.db.support.ZuulProxyStoreConfiguration;

/**
 * A simple {@link org.springframework.cloud.netflix.zuul.filters.RouteLocator} that is being populated from configured
 * {@link ZuulRouteStore}.
 *
 * @author Jakub Narloch
 */
public class StoreProxyRouteLocator extends DiscoveryClientRouteLocator {
	private Logger logger=Logger.getLogger(StoreProxyRouteLocator.class);

    private final ZuulRouteStore store;

    /**
     * Creates new instance of {@link StoreProxyRouteLocator}
     * @param servletPath the application servlet path
     * @param discovery the discovery service
     * @param properties the zuul properties
     * @param store the route store
     */
    public StoreProxyRouteLocator(String servletPath, DiscoveryClient discovery,
			ZuulProperties properties,
                                  ZuulRouteStore store) {
        super(servletPath, discovery, properties);
        this.store = store;
        addConfiguredRoutes(properties.getRoutes());
    }
    @Override
    protected LinkedHashMap<String, ZuulProperties.ZuulRoute> locateRoutes() {


        final LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<String, ZuulProperties.ZuulRoute>();
        routesMap.putAll((Map<String, ZuulProperties.ZuulRoute>)super.locateRoutes());
        return routesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addConfiguredRoutes(final Map<String, ZuulProperties.ZuulRoute> routes) {
    	logger.info("--StoreProxyRouteLocator.addConfiguredRoutes");
//    protected Map<String, ZuulRoute> locateRoutes() {
        
//        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
//        Map<String, ZuulRoute> tmp=super.locateRoutes();
//        //从application.properties中加载路由信息
//        routesMap.putAll(tmp);
        //从db中加载路由信息
        for (ZuulProperties.ZuulRoute route : store.findAll()) {
        	routes.put(route.getPath(), route);
        }
//        return routesMap;
    }
}
