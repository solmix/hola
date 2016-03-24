
package org.solmix.scheduler.services;

import java.util.List;

public interface JobServerService
{

    public enum Status
    {
        READY , RUNNING
    }

    void clearPreviousServerStatus();

    void persistServerOnline();

    void clearJobStoppedStatus();

    boolean isJobStoppedManually();

    void processServerShutdown();

    void updateServerStatus(final Status status);

    List<String> getAllNodes();

    List<String> getAvailableNodes();

    boolean isAvailable();

    void persistSuccessCount(final int successCount);

    void persistFailureCount(final int failureCount);


}
