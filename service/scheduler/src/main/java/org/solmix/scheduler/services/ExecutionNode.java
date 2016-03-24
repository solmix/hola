package org.solmix.scheduler.services;


public class ExecutionNode
{

    /**
     * 执行状态根节点.
     */
    public static final String ROOT = "execution";
    
    public static final String RUNNING_APPENDIX = "running";
    
    public static final String RUNNING = ROOT + "/%s/" + RUNNING_APPENDIX;
    
    public  static final String COMPLETED = ROOT + "/%s/completed";
    
    public  static final String LAST_BEGIN_TIME = ROOT + "/%s/lastBeginTime";
    
    public  static final String NEXT_FIRE_TIME = ROOT + "/%s/nextFireTime";
    
    public static final String LAST_COMPLETE_TIME = ROOT + "/%s/lastCompleteTime";
    
    public  static final String MISFIRE = ROOT + "/%s/misfire";
    
    public static final String LEADER_ROOT = ElectionNode.ROOT + "/" + ROOT;
    
    public static final String NECESSARY = LEADER_ROOT + "/necessary";
    
    public static final String CLEANING = LEADER_ROOT + "/cleaning";
    
    private final JobNodePath jobNodePath;
    
    public ExecutionNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * 获取作业运行状态节点路径.
     * 
     * @param item 作业项
     * @return 作业运行状态节点路径
     */
    public static String getRunningNode(final int item) {
        return String.format(RUNNING, item);
    }
    
    public  static String getCompletedNode(final int item) {
        return String.format(COMPLETED, item);
    }
    
    public static String getLastBeginTimeNode(final int item) {
        return String.format(LAST_BEGIN_TIME, item);
    }
    
    public   static String getNextFireTimeNode(final int item) {
        return String.format(NEXT_FIRE_TIME, item);
    }
    
    public static String getLastCompleteTimeNode(final int item) {
        return String.format(LAST_COMPLETE_TIME, item);
    }
    
    public  static String getMisfireNode(final int item) {
        return String.format(MISFIRE, item);
    }
    
    /**
     * 根据运行中的分片路径获取分片项.
     * 
     * @param path 运行中的分片路径
     * @return 分片项, 不是运行中的分片路径获则返回null
     */
    public Integer getItemByRunningItemPath(final String path) {
        if (!isRunningItemPath(path)) {
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ROOT).length() + 1, path.lastIndexOf(RUNNING_APPENDIX) - 1));
    }
    
    private boolean isRunningItemPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ROOT)) && path.endsWith(RUNNING_APPENDIX);
    }
}
