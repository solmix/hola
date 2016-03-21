
package org.solmix.scheduler.exception;

public class FileNotFoundException extends RegistryException
{

    /**
     * 
     */
    private static final long serialVersionUID = -4813848017107970388L;

    public FileNotFoundException(Exception cause)
    {
        super(cause);
    }

    public FileNotFoundException(final String path)
    {
        super(path);
    }
}
