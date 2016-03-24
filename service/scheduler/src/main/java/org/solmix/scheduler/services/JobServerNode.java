package org.solmix.scheduler.services;

import org.solmix.commons.util.NetUtils;

public final  class JobServerNode
{
   public static final String ROOT = "servers";
    
    static final String HOST_NAME = ROOT + "/%s/hostName";
    
    static final String STATUS_APPENDIX = "status";
    
    static final String STATUS = ROOT + "/%s/" + STATUS_APPENDIX;
    
    static final String DISABLED_APPENDIX = "disabled";
    
    static final String DISABLED = ROOT + "/%s/" + DISABLED_APPENDIX;
    
    static final String PROCESS_SUCCESS_COUNT = ROOT + "/%s/processSuccessCount";
    
    static final String PROCESS_FAILURE_COUNT = ROOT + "/%s/processFailureCount";
    
    static final String STOPPED = ROOT + "/%s/stoped";
    
    static final String SHUTDOWN = ROOT + "/%s/shutdown";
    
    
    private final JobNodePath jobNodePath;
    
    public JobServerNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    public static String getHostNameNode(final String ip) {
        return String.format(HOST_NAME, ip);
    }
    
    public static String getStatusNode(final String ip) {
        return String.format(STATUS, ip);
    }
    
    public static String getDisabledNode(final String ip) {
        return String.format(DISABLED, ip);
    }
    
    public  static String getProcessSuccessCountNode(final String ip) {
        return String.format(PROCESS_SUCCESS_COUNT, ip);
    }
    
    public  static String getProcessFailureCountNode(final String ip) {
        return String.format(PROCESS_FAILURE_COUNT, ip);
    }
    
    public  static String getStoppedNode(final String ip) {
        return String.format(STOPPED, ip);
    }
    
    public static String getShutdownNode(final String ip) {
        return String.format(SHUTDOWN, ip);
    }
    
    public boolean isJobStoppedPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(JobServerNode.STOPPED, NetUtils.getLocalIp())));
    }
    
    public boolean isJobShutdownPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(JobServerNode.SHUTDOWN, NetUtils.getLocalIp())));
    }
    
    /**
     * 判断给定路径是否为作业服务器状态路径.
     * 
     * @param path 待判断的路径
     * @return 是否为作业服务器状态路径
     */
    public boolean isServerStatusPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ROOT)) && path.endsWith(STATUS_APPENDIX);
    }
    
    /**
     * 判断给定路径是否为作业服务器禁用路径.
     * 
     * @param path 待判断的路径
     * @return 是否为作业服务器禁用路径
     */
    public boolean isServerDisabledPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ROOT)) && path.endsWith(DISABLED_APPENDIX);
    }
}
