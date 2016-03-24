
package org.solmix.scheduler.services;

public final class ElectionNode
{

    public static final String ROOT = "leader";

    public static final String ELECTION_ROOT = ROOT + "/election";

    public static final String LEADER_HOST = ELECTION_ROOT + "/host";

    public static final String LATCH = ELECTION_ROOT + "/latch";

    private final JobNodePath jobNodePath;

    ElectionNode(final String jobName)
    {
        jobNodePath = new JobNodePath(jobName);
    }

    boolean isLeaderHostPath(final String path) {
        return jobNodePath.getFullPath(LEADER_HOST).equals(path);
    }
}
