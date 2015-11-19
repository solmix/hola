
package org.solmix.hola.rs.call;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DefaultRemoteResponse implements RemoteResponse, Serializable
{

    private static final long serialVersionUID = 8905883218764988765L;

    private Object value;

    private Throwable exception;

    private Map<String, Object> attachments ;

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }

    @Override
    public Map<String, Object> getResponseContext() {
        return attachments;
    }

    @Override
    public Object getContextAttr(String key) {
        return attachments==null?null: attachments.get(key);
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
    public String toString() {
        return "RpcResult [value=" + value + ", exception=" + exception + "]";
    }

    
    public void setValue(Object value) {
        this.value = value;
    }

    
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    
    public void setAttachments(Map<String,Object> map) {
        if (map != null && map.size() > 0) {
            attachments = new HashMap<String, Object>();
            attachments.putAll(map);
        }
    }
    
}
