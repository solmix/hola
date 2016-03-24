package org.solmix.scheduler.services;


public class FailoverNode
{
    public  static final String FAILOVER = "failover";
    
    public static final String LEADER_ROOT = ElectionNode.ROOT + "/" + FAILOVER;
    
    public static final String ITEMS_ROOT = LEADER_ROOT + "/items";
    
    public static final String ITEMS = ITEMS_ROOT + "/%s";
    
    public static final String LATCH = LEADER_ROOT + "/latch";
    
    private  static final String EXECUTION_FAILOVER = ExecutionNode.ROOT + "/%s/" + FAILOVER;
    
    private final JobNodePath jobNodePath;
    
    public FailoverNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    public  static String getItemsNode(final int item) {
        return String.format(ITEMS, item);
    }
    
    public static String getExecutionFailoverNode(final int item) {
        return String.format(EXECUTION_FAILOVER, item);
    }
    
    /**
     * 根据失效转移执行路径获取分片项.
     * 
     * @param path 失效转移执行路径
     * @return 分片项, 不是失效转移执行路径获则返回null
     */
    public Integer getItemByExecutionFailoverPath(final String path) {
        if (!isFailoverPath(path)) {
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ExecutionNode.ROOT).length() + 1, path.lastIndexOf(FailoverNode.FAILOVER) - 1));
    }
    
    private boolean isFailoverPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ExecutionNode.ROOT)) && path.endsWith(FailoverNode.FAILOVER);
    }
}
