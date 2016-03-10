
package org.solmix.hola.monitor;

import java.io.Serializable;

public class MonitorState implements Serializable
{

    private static final long serialVersionUID = -5404530236150388604L;

    private long timestamp;

    private long success;

    private long failure;

    private long elapsed;

    private long concurrent;

    private String application;

    private String group;

    private String operation;

    private String version;
    
    private long maxElapsed;
    
    private long maxConcurrent;

    
    public long getTimestamp() {
        return timestamp;
    }

    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    
    public long getSuccess() {
        return success;
    }

    
    public void setSuccess(long success) {
        this.success = success;
    }

    
    public long getFailure() {
        return failure;
    }

    
    public void setFailure(long failure) {
        this.failure = failure;
    }

    
    public long getElapsed() {
        return elapsed;
    }

    
    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    
    public long getConcurrent() {
        return concurrent;
    }

    
    public void setConcurrent(long concurrent) {
        this.concurrent = concurrent;
    }

    
    public String getApplication() {
        return application;
    }

    
    public void setApplication(String application) {
        this.application = application;
    }

    
    public String getGroup() {
        return group;
    }

    
    public void setGroup(String group) {
        this.group = group;
    }

    
    public String getOperation() {
        return operation;
    }

    
    public void setOperation(String operation) {
        this.operation = operation;
    }

    
    public String getVersion() {
        return version;
    }

    
    public void setVersion(String version) {
        this.version = version;
    }

    
    public long getMaxElapsed() {
        return maxElapsed;
    }

    
    public void setMaxElapsed(long maxElapsed) {
        this.maxElapsed = maxElapsed;
    }

    
    public long getMaxConcurrent() {
        return maxConcurrent;
    }

    
    public void setMaxConcurrent(long maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    
    

}
