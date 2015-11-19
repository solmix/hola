
package org.solmix.hola.rs.call;

import java.util.Map;

public class DefaultRemoteRequest implements RemoteRequest
{

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameter;

    private Map<String, Object> attachments;

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
    public String getMethod() {
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
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

}
