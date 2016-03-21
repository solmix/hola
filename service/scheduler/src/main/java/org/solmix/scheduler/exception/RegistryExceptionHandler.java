
package org.solmix.scheduler.exception;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryExceptionHandler
{

    private static final Logger LOG = LoggerFactory.getLogger(RegistryExceptionHandler.class);

    public static void handleException(final Exception cause) {
        if (isIgnoredException(cause) || isIgnoredException(cause.getCause())) {
            LOG.debug("Elastic job: ignored exception for: {}", cause.getMessage());
        } else if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new RegistryException(cause);
        }
    }

    private static boolean isIgnoredException(final Throwable cause) {
        return null != cause
            && (cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException);
    }
}
