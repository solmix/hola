
package org.solmix.hola.rs.call;

import java.lang.reflect.Method;
import java.util.Map;

import org.solmix.hola.common.model.ServiceProperties;

public class DefaultRemoteRequest implements RemoteRequest
{

    private String methodName;
    
    private Method method;

    private Class<?>[] parameterTypes;

    private Object[] parameter;

    private Map<String, Object> attachments;
    
    private  ServiceProperties serviceProperties;
    
    private boolean async;

    public DefaultRemoteRequest(Method method, Object[] parameter, Map<String, Object> attachments){
        this.method=method;
        this.parameter = parameter;
        this.attachments = attachments;
    }
    public DefaultRemoteRequest(Method method, Object[] parameter){
        this.method=method;
        this.parameter = parameter;
    }
    public DefaultRemoteRequest(String methodName, Class<?>[] parameterTypes, Object[] parameter, Map<String, Object> attachments)
    {
        this.methodName = methodName;
        this.parameter = parameter;
        this.parameterTypes = parameterTypes;
        this.attachments = attachments;
    }
    public DefaultRemoteRequest(String methodName, Object... parameter){
       
        this(methodName,determineType(parameter),parameter,null);
    }
    public DefaultRemoteRequest(String methodName, Object[] parameter,Map<String, Object> attachments){
    	
    	this(methodName,determineType(parameter),parameter,attachments);
    }
     
    private static  Class<?>[] determineType(Object[] parameter){
        Class<?>[] types  = new Class<?>[parameter.length];
        for(int i=0;i<parameter.length;i++){
            if(parameter[i]==null){
                throw new IllegalArgumentException("Parameter ["+i+"] is null and can't determine type by self.");
            }
            types[i]=parameter[i].getClass();
        }
        return types;
    }

    @Override
    public String getMethodName() {
        if(method!=null){
            methodName=method.getName();
        }
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        if(method!=null){
            parameterTypes=method.getParameterTypes();
        }
        return parameterTypes;
    }

    @Override
    public Object[] getParameters() {
        return parameter;
    }

    @Override
    public Map<String, Object> getRequestContext() {
        return attachments;
    }

    @Override
    public Object getContextAttr(String key) {
        return attachments == null ? null : attachments.get(key);
    }

    @Override
    public Object getContextAttr(String key, Object defaultValue) {
        Object result = getContextAttr(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }
    @Override
    public Method getMethod() {
        return method;
    }

    public void setContextAttr(String key,Object value){
        attachments.put(key, value);
    }
    
    @Override
    public boolean isAsync() {
        return async;
    }
    
    public void setAsync(boolean async) {
        this.async = async;
    }
    

    
    
}
