
package org.solmix.scheduler.exception;

/**
 * 分片序列号和个性化参数格式不正确抛出的异常.
 * 
 */
public final class ShardingItemParametersException extends JobException {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 7345178670217006341L;

    public ShardingItemParametersException(final String errorMsg, final Object... args) {
        super(errorMsg, args);
    }
}
