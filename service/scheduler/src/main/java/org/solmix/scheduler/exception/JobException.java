
package org.solmix.scheduler.exception;

public class JobException extends RuntimeException
{

    private static final long serialVersionUID = -5360613368415980452L;

    public JobException(final String errorMessage, final Object... args)
    {
        super(String.format(errorMessage, args));
    }

    public JobException(final Exception cause)
    {
        super(cause);
    }
}
