package org.solmix.scheduler;

import java.util.List;

public interface SchedulerRegistry
{
    
    /**
     * 初始化注册中心.
     */
    void init();
    
    /**
     * 关闭注册中心.
     */
    void close();
    
    /**
     * 获取注册数据.
     * 
     * @param key 键
     * @return 值
     */
    String get(String key);
    
    /**
     * 获取数据是否存在.
     * 
     * @param key 键
     * @return 数据是否存在
     */
    boolean isExisted(String key);
    
    /**
     * 持久化注册数据.
     * 
     * @param key 键
     * @param value 值
     */
    void persist(String key, String value);
    
    /**
     * 更新注册数据.
     * 
     * @param key 键
     * @param value 值
     */
    void update(String key, String value);
    
    /**
     * 删除注册数据.
     * 
     * @param key 键
     */
    void remove(String key);
    
    /**
     * 获取注册中心当前时间.
     * 
     * @param key 用于获取时间的键
     * @return 注册中心当前时间
     */
    long getRegistryCenterTime(String key);
    
    /**
     * 直接获取操作注册中心的原生客户端.
     * 如：Zookeeper或Redis等原生客户端.
     * 
     * @return 注册中心的原生客户端
     */
    Object getRawClient();
    
    /**
     * 直接从注册中心而非本地缓存获取数据.
     * 
     * @param key 键
     * @return 值
     */
    String getDirectly(String key);
    
    /**
     * 获取子节点名称集合.
     * 
     * @param key 键
     * @return 子节点名称集合
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * 持久化临时注册数据.
     * 
     * @param key 键
     * @param value 值
     */
    void persistEphemeral(String key, String value);
    
    /**
     * 持久化临时顺序注册数据.
     * 
     * @param key 键
     */
    void persistEphemeralSequential(String key);
    
    /**
     * 添加本地缓存.
     * 
     * @param cachePath 需加入缓存的路径
     */
    void addCacheData(String cachePath);
    
    /**
     * 获取注册中心数据缓存对象.
     * 
     * @param cachePath 缓存的节点路径
     * @return 注册中心数据缓存对象
     */
    Object getRawCache(String cachePath);
}
