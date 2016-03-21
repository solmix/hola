package org.solmix.scheduler.exception;

public class RegistryException extends RuntimeException
{
    private static final long serialVersionUID = 2666687123428113285L;

    public RegistryException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    
    public RegistryException(final Exception cause) {
        super(cause);
    }
    
 
}
