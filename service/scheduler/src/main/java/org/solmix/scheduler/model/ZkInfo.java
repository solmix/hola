package org.solmix.scheduler.model;


/**
 * zookeeper 配置信息
 * 
 * @author solmix
 *
 */
public class ZkInfo
{

    
    /**
     * 命名空间.
     */
    private String namespace;
    
    /**
     * 等待重试的间隔时间的初始值.
     * 单位毫秒.
     */
    private int baseSleepTimeMilliseconds = 1000;
    
    /**
     * 等待重试的间隔时间的最大值.
     * 单位毫秒.
     */
    private int maxSleepTimeMilliseconds = 3000;
    
    /**
     * 最大重试次数.
     */
    private int maxRetries = 3;
    
    /**
     * 会话超时时间.
     * 单位毫秒.
     */
    private int sessionTimeoutMilliseconds;
    /**
     * 本地属性文件路径.
     */
    private String localPropertiesPath;
    
    /**
     * 是否允许本地值覆盖注册中心.
     */
    private boolean overwrite;
    /**
     * 连接超时时间.
     * 单位毫秒.
     */
    private int connectionTimeoutMilliseconds;
    
    /**
     * 连接Zookeeper的权限令牌.
     * 缺省为不需要权限验证.
     */
    private String digest;
    
   

    private String address;
    
    public ZkInfo(final String address, final String namespace){
    	this.address = address;
        this.namespace = namespace;
    }
    /**
     * 包含了必需属性的构造器.
     * 
     * @param serverLists 连接Zookeeper服务器的列表
     * @param namespace 命名空间
     * @param baseSleepTimeMilliseconds 等待重试的间隔时间的初始值
     * @param maxSleepTimeMilliseconds 等待重试的间隔时间的最大值
     * @param maxRetries 最大重试次数
     */
    public ZkInfo(final String address, final String namespace, final int baseSleepTimeMilliseconds, final int maxSleepTimeMilliseconds, final int maxRetries) {
        this.address = address;
        this.namespace = namespace;
        this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
        this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
        this.maxRetries = maxRetries;
    }
    
    

    
    public String getNamespace() {
        return namespace;
    }

    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    
    public int getBaseSleepTimeMilliseconds() {
        return baseSleepTimeMilliseconds;
    }

    
    public void setBaseSleepTimeMilliseconds(int baseSleepTimeMilliseconds) {
        this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
    }

    
    public int getMaxSleepTimeMilliseconds() {
        return maxSleepTimeMilliseconds;
    }

    
    public void setMaxSleepTimeMilliseconds(int maxSleepTimeMilliseconds) {
        this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
    }

    
    public int getMaxRetries() {
        return maxRetries;
    }

    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    
    public int getSessionTimeoutMilliseconds() {
        return sessionTimeoutMilliseconds;
    }

    
    public void setSessionTimeoutMilliseconds(int sessionTimeoutMilliseconds) {
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
    }

    
    public int getConnectionTimeoutMilliseconds() {
        return connectionTimeoutMilliseconds;
    }

    
    public void setConnectionTimeoutMilliseconds(int connectionTimeoutMilliseconds) {
        this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
    }

    
    public String getDigest() {
        return digest;
    }

    
    public void setDigest(String digest) {
        this.digest = digest;
    }

    
    public String getAddress() {
        return address;
    }

    
    public void setAddress(String address) {
        this.address = address;
    }

    
    public String getLocalPropertiesPath() {
        return localPropertiesPath;
    }

    
    public void setLocalPropertiesPath(String localPropertiesPath) {
        this.localPropertiesPath = localPropertiesPath;
    }

    
    public boolean isOverwrite() {
        return overwrite;
    }

    
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    
}
