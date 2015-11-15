
package org.solmix.hola.common.model;

import java.util.Dictionary;
import java.util.Hashtable;

import org.solmix.hola.common.HOLA;

public final class PropertiesBuilder
{

    Dictionary<String, Object> dic;

    private PropertiesBuilder()
    {
        dic = new Hashtable<String, Object>();
    }

    private PropertiesBuilder(ServiceProperties prop)
    {
        if (prop.isReadOnly()) {
            dic = new Hashtable<String, Object>();
            String[] keys = prop.getPropertyKeys();
            for (String key : keys) {
                dic.put(key, prop.get(key));
            }
        }
    }

    public static PropertiesBuilder newBuilder() {
        return new PropertiesBuilder();
    }
    
    public static PropertiesBuilder newBuilder(ServiceProperties prop) {
        return new PropertiesBuilder(prop);
    }
    public PropertiesBuilder setAddress(String address){
        PropertiesUtils.toProperties(address, dic);
        return this;
    }
    public PropertiesBuilder setProtocol(String protocol){
        dic.put(HOLA.PROTOCOL_KEY, protocol);
        return this;
    }

    public PropertiesBuilder setUserName(String userName){
        dic.put(HOLA.USER_KEY, userName);
        return this;
    }
    
    public PropertiesBuilder setPassword(String password){
        dic.put(HOLA.PALYLOAD_KEY, password);
        return this;
    }
    
    public PropertiesBuilder setHost(String host){
        dic.put(HOLA.HOST_KEY, host);
        return this;
    }
    
    public PropertiesBuilder setPort(int port){
        dic.put(HOLA.PORT_KEY, port);
        return this;
    }
    
    public PropertiesBuilder setPath(String path){
        dic.put(HOLA.PATH_KEY, path);
        return this;
    }
    
    
    public PropertiesBuilder setVersion(String version){
        dic.put(HOLA.VERSION_KEY, version);
        return this;
    }
    
    public PropertiesBuilder setProperty(String key,String value){
        dic.put(key, value);
        return this;
    }

    /** 创建可以改变的Properties */
    public Dictionary<String, ?> build() {
        validate();
        return dic;
    }
    public Dictionary<String, ?> build(boolean readonly) {
        validate();
        return dic;
    }
    private void validate() {
        String password = PropertiesUtils.getString(dic, HOLA.PASSWORD_KEY);
        String username = PropertiesUtils.getString(dic, HOLA.USER_KEY);
        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
    }
    /**
     * 创建ServiceProperties
     * 
     * @param readOnly readOnly为真创建不可修改的Properties，否则可修改.
     * @return
     */
    public ServiceProperties buildServiceProperties(boolean readOnly) {
        validate();
        ServiceProperties sp = new ServiceProperties(dic);
        if (readOnly) {
            sp.setReadOnly();
        }
        return sp;
    }

}
