/*
 * Copyright 2014 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.hola.rt.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.hola.common.Constants;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月14日
 */
@ThreadSafe
public class CopyOfDiscoveryInfo extends AbstractURIInfo<CopyOfDiscoveryInfo>
{
    private static final long serialVersionUID = -2090513507409471371L;
    private static final Logger LOG = LoggerFactory.getLogger(CopyOfDiscoveryInfo.class);
    public static final String ADDRESS_KEY="discovery.address";
   
    public CopyOfDiscoveryInfo(String protocol,  String host, int port)
    {
       this(protocol,null,null,host,port,null,null);
    }
  
    public CopyOfDiscoveryInfo(String protocol, String username, String password, String host, int port, String path, Map<String, Object> properties) {
        super(protocol,username,password,host,port,path,properties);
        
    }
    public static CopyOfDiscoveryInfo valueOf(URI uri){
        if(uri==null)
            return null;
        Builder b= newBuilder();
        b.setProtocol(uri.getScheme());
       String userInfo=uri.getRawUserInfo();
       String username=null;
       String password=null;
       if(userInfo!=null&&userInfo.length()>0){
           int j = userInfo.indexOf(":");
           if (j >= 0) {
               password = userInfo.substring(j + 1);
               username = userInfo.substring(0, j);
           }else{
               username=userInfo;
           }
       }
       b.setUserName(username);
       b.setPassword(password);
       b.setHost(uri.getHost());
       b.setPort(uri.getPort());
       b.setPath(uri.getRawPath());
       Map<String,Object> param= parseQuery(uri.getRawQuery());
       if(param!=null&&param.size()>0)
       b.setProperties(param);
        
        return b.build();
    }
    
    public static CopyOfDiscoveryInfo valueOf(String uri){
        try {
            URI u = new URI(uri);
            return valueOf(u);
        } catch (URISyntaxException e) {
            throw  new IllegalArgumentException(e);
        }
    }
  
    @Override
    public String getAddress(){
        return getString("address");
    }

    /**
     * @return
     */
    public String getBackupAddress() {
       StringBuilder sb = new StringBuilder();
       sb.append(getAddress());
       String backup= getString("backup", null);
       if(backup!=null&&backup.length()>0){
         String[] backs=  Constants.SPLIT_COMMA_PATTERN.split(backup);
         for(String back:backs){
             sb.append(",");
             sb.append(back);
         }
       }
        return sb.toString();
    }
    
    @Override
    public String getAuthority() {
        String username=getString("username");
        String password=getString("password");
        if ((username == null || username.length() == 0)
                && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) 
                + ":" + (password == null ? "" : password);
    }
    
    public String getGroup(String df){
        return getString("group", df);
    }

    /**
     * @param retryPeriod
     * @return
     */
    public int getRetyPeriod(int retryPeriod) {
        return getInt("retry.peroid", retryPeriod);
    }

    /**
     * 启动时是否检查错误
     * 
     * @param b
     */
    public boolean getCheck(boolean b) {
        return getBoolean("check", b);
        
    }
    @Override
    public String getProtocol(){
        return getString("protocol");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rt.config.ExtensionInfo#getSelf()
     */
    @Override
    protected CopyOfDiscoveryInfo getSelf() {
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rt.config.ExtensionInfo#makeSelf(java.util.Map)
     */
    @Override
    protected CopyOfDiscoveryInfo makeSelf(Map<String, Object> map) {
        return new CopyOfDiscoveryInfo(protocol,username,password,host,port,path,map);

    }
    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(AbstractURIInfo<?> info) {
        return new Builder(info);
    }

    public static class Builder
    {

        private   Map<String, Object> properties = new HashMap<String, Object>();
        protected  String protocol;
        protected  String username;
        protected  String password;
        protected  String host;
        protected  int port;
        protected  String path;
        private Builder()
        {
        }

        /**
         * @param info
         */
        public Builder(AbstractURIInfo<?> info)
        {
            properties.putAll(info.getProperties());
            protocol=info.getProtocol();
            username=info.getUsername();
            password=info.getPassword();
            host=info.getHost();
            port=info.getPort();
            path=info.getPath();
        }
        
        public Builder setProperties(Map<String,Object> properties) {
            this.properties=new HashMap<String, Object>(properties);
            return this;
        }

        public Builder setPropertyIfAbsent(String key, Object value){
            if (key == null || key.length() == 0 || value == null)
                return this;
            if (hasProperty(key))
                return this;
            properties.put(key, value);
            return this;
        }

        public boolean hasProperty(String key){
            Object value = properties.get(key);
            return value != null;
        }
        
        public Builder setPort(int  port) {
            this.port=port;
             return this;
         }
        public Builder setPath(String  path) {
           this.path=path;
            return this;
        }
        public Builder setUserName(String  username) {
            this.username=username;
             return this;
         }
        public Builder setPassword(String  password) {
            this.password=password;
             return this;
         }
        
        public Builder setProtocol(String  protocol) {
            this.protocol=protocol;
             return this;
         }
       
        /**
         * @param host the host to set
         */
        public Builder setHost(String host) {
           this.host=host;
            return this;
        }

      
     

        public CopyOfDiscoveryInfo build() {

            return new CopyOfDiscoveryInfo(protocol,username,password,host,port,path,properties);
        }

    }
}
