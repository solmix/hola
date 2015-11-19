
package org.solmix.hola.cluster;

import org.solmix.hola.common.HolaRuntimeException;

public class ClusterException extends HolaRuntimeException
{

    private static final long serialVersionUID = -3497996039568641629L;

    public ClusterException(String msg)
    {
        super(msg);
    }

    public ClusterException(String msg, Throwable e)
    {
        super(msg, e);
    }

    public ClusterException(Throwable e)
    {
        super(e);
    }
}
