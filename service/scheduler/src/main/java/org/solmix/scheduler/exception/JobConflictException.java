
package org.solmix.scheduler.exception;

public class JobConflictException extends JobException
{

    private static final long serialVersionUID = -9220913941833841617L;

    private static final String ERROR_MSG = "Job conflict with register center. The job [%s] in register center's class is [%s], your job class is [%s]";

    public JobConflictException(final String jobName, final String registeredJobClassName, final String toBeRegisteredJobClassName)
    {
        super(ERROR_MSG, jobName, registeredJobClassName, toBeRegisteredJobClassName);
    }
}
