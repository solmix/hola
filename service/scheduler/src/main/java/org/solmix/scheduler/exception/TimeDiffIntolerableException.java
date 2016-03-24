
package org.solmix.scheduler.exception;

/**
 * 本机与注册中心的时间误差超过容忍范围抛出的异常.
 * 
 */
public final class TimeDiffIntolerableException extends JobException {
    
    
    private static final long serialVersionUID = -1340611920706386257L;
    private static final String ERROR_MSG = "Time different between job server and register center exceed [%s] seconds, max time different is [%s] seconds.";
    
    public TimeDiffIntolerableException(final int timeDiffSeconds, final int maxTimeDiffSeconds) {
        super(ERROR_MSG, timeDiffSeconds, maxTimeDiffSeconds);
    }
}
