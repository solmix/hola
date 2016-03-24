
package org.solmix.scheduler.services;

public interface ElectionService
{

    void leaderElection();

    Boolean isLeader();

    boolean hasLeader();

}
