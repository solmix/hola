
package org.solmix.scheduler.services;

public class JobNodePath
{

    private final String jobName;

    public JobNodePath(final String jobName)
    {
        this.jobName = jobName;
    }

    /**
     * 获取节点全路径.
     * 
     * @param node 节点名称
     * @return 节点全路径
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", jobName, node);
    }
}
