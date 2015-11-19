package org.solmix.hola.discovery.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.discovery.DiscoveryException;
import org.solmix.hola.discovery.ServiceTypeListener;
import org.solmix.hola.discovery.event.DiscoveryTypeEvent;
import org.solmix.hola.discovery.model.DiscoveryInfo;
import org.solmix.hola.discovery.model.DiscoveryInfoImpl;
import org.solmix.hola.discovery.model.ServiceID;
import org.solmix.hola.discovery.model.ServiceType;
import org.solmix.hola.discovery.model.ServiceTypeImpl;
import org.solmix.hola.discovery.support.FailbackDiscovery;
import org.solmix.runtime.Container;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class RedisDiscovery extends FailbackDiscovery
{

    private static final int DEFAULT_REDIS_PORT = 6379;
    private static final Logger LOG = LoggerFactory.getLogger(RedisDiscovery.class);

    private final Map<String, JedisPool> jedisPools = new ConcurrentHashMap<String, JedisPool>();
    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("RedisRegistryExpireTimer", true));
    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<String, Notifier>();

    private final int reconnectPeriod;
    private final int expirePeriod;
    private final ScheduledFuture<?> expireFuture;
    private final String root;
    private volatile boolean admin = false;
    private boolean replicate;
    public RedisDiscovery(Dictionary<String, ?> properties, Container container)
    {
        super(properties, container);
        if(PropertiesUtils.getString(properties, HOLA.HOST_KEY)==null){
            throw new IllegalArgumentException("redis discovery address is null");
        }
        List<String> addresses = new ArrayList<String>();
        addresses.add(PropertiesUtils.toAddress(properties));
        String backs = PropertiesUtils.getString(properties, HOLA.BACKUP_KEY);
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        infos.add(new DiscoveryInfoImpl(properties));
        String[] backups =backs==null?null: HOLA.SPLIT_COMMA_PATTERN.split(backs);
        if (backups != null && backups.length > 0) {
            addresses.addAll(Arrays.asList(backups));
        }
        GenericObjectPoolConfig config = getObjectPoolConfig(properties);
        for (String address : addresses) {
            if(address.indexOf("://")>0){
                address=address.substring(address.indexOf("://")+3);
            }
            int i = address.indexOf(':');
            String host;
            int port;
            if (i > 0) {
                host = address.substring(0, i);
                port = Integer.parseInt(address.substring(i + 1));
            } else {
                host = address;
                port = DEFAULT_REDIS_PORT;
            }
            this.jedisPools.put(address, new JedisPool(config, host, port, PropertiesUtils.getInt(properties, HOLA.TIMEOUT_KEY, HOLA.DEFAULT_TIMEOUT)));
        }
        reconnectPeriod=PropertiesUtils.getInt(properties, HOLA.DISCOVERY_RECONNECT_PERIOD, HOLA.DEFAULT_DISCOVERY_RECONNECT_PERIOD);
       
        //redis集群方式
        String cluster = PropertiesUtils.getString(properties, "cluster", "failover");
        if (! "failover".equals(cluster) && ! "replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        replicate = "replicate".equals(cluster);
        
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY,HOLA.DEFAULT_ROOT);
        if (! group.startsWith(HOLA.PATH_SEPARATOR)) {
            group = HOLA.PATH_SEPARATOR + group;
        }
        if (! group.endsWith(HOLA.PATH_SEPARATOR)) {
            group = group + HOLA.PATH_SEPARATOR;
        }
        this.root=group;
        this.expirePeriod=PropertiesUtils.getInt(properties, HOLA.DISCOVERY_SESSION_TIMEOUT, HOLA.DEFAULT_SESSION_TIMEOUT);
        this.expireFuture = expireExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    deferExpired(); // 延长过期时间
                } catch (Throwable t) { // 防御性容错
                    LOG.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
                }
            }
        }, expirePeriod / 2, expirePeriod / 2, TimeUnit.MILLISECONDS);
    }
    
    private void deferExpired() {
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    for (DiscoveryInfo info : new HashSet<DiscoveryInfo>(getRegistered())) {
                        if (PropertiesUtils.getBoolean(info.getServiceProperties(), HOLA.DYNAMIC_KEY, true)) {
                            String key =  toRedisRoot(info.getServiceID().getServiceType().getIdentityName());
                            String address = PropertiesUtils.toAddress(info.getServiceProperties());
                            if (jedis.hset(key, address, String.valueOf(System.currentTimeMillis() + expirePeriod)) == 1) {
                                jedis.publish(key, HOLA.REGISTER);
                            }
                        }
                    }
                    if (admin) {
                        clean(jedis);
                    }
                    if (! replicate) {
                        break;//  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                LOG.warn("Failed to write provider heartbeat to redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }
    //删除脏数据
    private void clean(Jedis jedis) {
        Set<String> keys = jedis.keys(root + HOLA.ANY_VALUE);
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                Map<String, String> values = jedis.hgetAll(key);
                if (values != null && values.size() > 0) {
                    boolean delete = false;
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        Dictionary<String, ?> properties = PropertiesUtils.toProperties(entry.getKey());
                        if (PropertiesUtils.getBoolean(properties, HOLA.DYNAMIC_KEY, true)) {
                            long expire = Long.parseLong(entry.getValue());
                            if (expire < now) {
                                jedis.hdel(key, entry.getKey());
                                delete = true;
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn("Delete expired key: " + key + " -> value: " + entry.getKey() + ", expire: " + new Date(expire)
                                        + ", now: " + new Date(now));
                                }
                            }
                        }
                    }
                    if (delete) {
                        jedis.publish(key, HOLA.UNREGISTER);
                    }
                }
            }
        }
    }
    
    public boolean isAvailable() {
        for (JedisPool jedisPool : jedisPools.values()) {
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                  if (jedis.isConnected()) {
                        return true; // 至少需单台机器可用
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
            }
        }
        return false;
    }
    
    @Override
    public void destroy() throws IOException{
        super.destroy();
        try {
            expireFuture.cancel(true);
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
        try {
            for (Notifier notifier : notifiers.values()) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                jedisPool.destroy();
            } catch (Throwable t) {
                LOG.warn("Failed to destroy the redis registry client. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }
    
    private GenericObjectPoolConfig getObjectPoolConfig(Dictionary<String, ?> properties){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setTestOnBorrow(PropertiesUtils.getBoolean(properties, "test.on.borrow", true));
        config.setTestOnReturn(PropertiesUtils.getBoolean(properties, "test.on.return", false));
        config.setTestWhileIdle(PropertiesUtils.getBoolean(properties, "test.while.idle", false));
        
        if(PropertiesUtils.getInt(properties, "max.idle",0)>0){
            config.setMaxIdle(PropertiesUtils.getInt(properties, "max.idle",0));
        }
        
        if(PropertiesUtils.getInt(properties, "min.idle",0)>0){
            config.setMinIdle(PropertiesUtils.getInt(properties, "min.idle",0));
        }
        
        if(PropertiesUtils.getInt(properties, "max.total",0)>0){
            config.setMaxTotal(PropertiesUtils.getInt(properties, "max.total",0));
        }
        
        if(PropertiesUtils.getInt(properties, "max.wait",0)>0){
            config.setMaxWaitMillis(PropertiesUtils.getInt(properties, "max.wait",0));
        }
        
        if(PropertiesUtils.getInt(properties, "time.between.eviction.runs.millis",0)>0){
            config.setTimeBetweenEvictionRunsMillis(PropertiesUtils.getInt(properties, "time.between.eviction.runs.millis",0));
        }
        
        if(PropertiesUtils.getInt(properties, "num.tests.per.eviction.run",0)>0){
            config.setNumTestsPerEvictionRun(PropertiesUtils.getInt(properties, "num.tests.per.eviction.run",0));
        }
        
        if(PropertiesUtils.getInt(properties, "min.evictable.idle.time.millis",0)>0){
            config.setMinEvictableIdleTimeMillis(PropertiesUtils.getInt(properties, "min.evictable.idle.time.millis",0));
        }
        return config;
    }

    @Override
    public DiscoveryInfo getService(ServiceID id) {
        DiscoveryInfo[] infos  = getServices(id.getServiceType());
        for(DiscoveryInfo info:infos){
            if(StringUtils.isEquals(info.getServiceName(),id.getName())){
                return info;
            }
        }
        return null;
    }

    @Override
    public DiscoveryInfo[] getServices() {
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    Set<String> keys = jedis.keys(root + HOLA.ANY_VALUE);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            Map<String, String> values = jedis.hgetAll(key);
                            if (values != null && values.size() > 0) {
                                long now = System.currentTimeMillis();
                                for (String id : values.keySet()) {
                                    Dictionary<String, ?> properties = PropertiesUtils.toProperties(id);
                                    if (PropertiesUtils.getBoolean(properties, HOLA.DYNAMIC_KEY, true)) {
                                        long expire = Long.parseLong(values.get(id));
                                        if (expire > now) {
                                            // 过期公告不要，但也不注销，等管理线程处理
                                            infos.add(new DiscoveryInfoImpl(properties));
                                        }
                                    }
                                }

                            }
                        }
                    }
                    if (!replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Exception e) {
                LOG.warn("Failed to get discoveried advertise", e);
            }

        }
        return getServices(root + HOLA.ANY_VALUE);
    }
    
    public DiscoveryInfo[] getServices(String keyPattern) {
        List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    Set<String> keys = jedis.keys(keyPattern);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            Map<String, String> values = jedis.hgetAll(key);
                            if (values != null && values.size() > 0) {
                                long now = System.currentTimeMillis();
                                for (String id : values.keySet()) {
                                    Dictionary<String, ?> properties = PropertiesUtils.toProperties(id);
                                    if (PropertiesUtils.getBoolean(properties, HOLA.DYNAMIC_KEY, true)) {
                                        long expire = Long.parseLong(values.get(id));
                                        if (expire > now) {
                                            // 过期公告不要，但也不注销，等管理线程处理
                                            infos.add(new DiscoveryInfoImpl(properties));
                                        }
                                    }
                                }

                            }
                        }
                    }
                    if (!replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Exception e) {
                LOG.warn("Failed to get discoveried advertise", e);
            }

        }
        return infos.toArray(new DiscoveryInfo[] {});
    }

    @Override
    public DiscoveryInfo[] getServices(ServiceType type) {
        return getServices(toRedisRoot(type.getIdentityName()));
    }

    @Override
    public ServiceType[] getServiceTypes() {
        List<ServiceType> types = new ArrayList<ServiceType>();
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    Set<String> keys = jedis.keys(root + HOLA.ANY_VALUE);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            types.add(ServiceTypeImpl.fromAddress(key));
                        }
                    }
                    if (!replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Exception e) {
                LOG.warn("Failed to get discoveried advertise", e);
            }
        }
        return types.toArray(new ServiceType[]{});
    }

    @Override
    protected void doRegister(DiscoveryInfo meta) {
        //生命周期
       long ttl=meta.getTTL()<0?expirePeriod:meta.getTTL();
        String expire = String.valueOf(System.currentTimeMillis() + ttl);
        boolean success = false;
        String key = toRedisRoot(meta.getServiceID().getServiceType().getIdentityName());
        String value  = PropertiesUtils.toAddress(meta.getServiceProperties());
        
        DiscoveryException exception=null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hset(key, value, expire);
                    jedis.publish(key, HOLA.REGISTER);
                    success = true;
                    if (! replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                exception = new DiscoveryException("Failed to register service to redis discovery. advertise: " + entry.getKey() + ", service: " + meta + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOG.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
        
    }

    @Override
    protected void doUnregister(DiscoveryInfo meta) {
        boolean success = false;
        String key = toRedisRoot(meta.getServiceID().getServiceType().getIdentityName());
        String value  = PropertiesUtils.toAddress(meta.getServiceProperties());
        
        DiscoveryException exception=null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.hdel(key, value);
                    jedis.publish(key, HOLA.UNREGISTER);
                    success = true;
                    if (! replicate) {
                        break; //  如果服务器端已同步数据，只需写入单台机器
                    }
                } finally {
                    jedis.close();
                }
            } catch (Throwable t) {
                exception = new DiscoveryException("Failed to register service to redis registry. registry: " + entry.getKey() + ", service: " + meta + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOG.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
        
    }

    @Override
    protected void doSubscribe(ServiceType type, ServiceTypeListener listener) {
        String servicekey = toRedisRoot( type.getServiceName());
        Notifier notifier = notifiers.get(servicekey);
        if (notifier == null) {
            Notifier newNotifier = new Notifier(servicekey);
            notifiers.putIfAbsent(servicekey, newNotifier);
            notifier = notifiers.get(servicekey);
            if (notifier == newNotifier) {
                notifier.start();
            }
        }
        boolean success = false;
        DiscoveryException exception = null;
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    if (servicekey.endsWith(HOLA.ANY_VALUE)) {
                        admin = true;
                        Set<String> keys = jedis.keys(servicekey);
                        if (keys != null && keys.size() > 0) {
                            Map<String, Set<String>> serviceKeys = new HashMap<String, Set<String>>();
                            for (String key : keys) {
                                String serviceKey = toServicePath(key);
                                Set<String> sk = serviceKeys.get(serviceKey);
                                if (sk == null) {
                                    sk = new HashSet<String>();
                                    serviceKeys.put(serviceKey, sk);
                                }
                                sk.add(key);
                            }
                            for (Set<String> sk : serviceKeys.values()) {
                                doNotify(jedis, sk, type, Arrays.asList(listener),DiscoveryTypeEvent.REGISTER);
                            }
                        }
                    } else {
                        doNotify(jedis, jedis.keys(servicekey + HOLA.PATH_SEPARATOR + HOLA.ANY_VALUE), type, Arrays.asList(listener),DiscoveryTypeEvent.REGISTER);
                    }
                    success = true;
                    break; // 只需读一个服务器的数据
                } finally {
                    jedis.close();
                }
            } catch(Throwable t) { // 尝试下一个服务器
                exception = new DiscoveryException("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", service: " + type + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                LOG.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    protected void doUnsubscribe(ServiceType type, ServiceTypeListener listener) {
        
    }
    
    private void doNotify(Jedis jedis, String key,int type) {
        for (Map.Entry<ServiceType, Set<ServiceTypeListener>> entry : new HashMap<ServiceType, Set<ServiceTypeListener>>(getTypeListeners()).entrySet()) {
            doNotify(jedis, Arrays.asList(key), entry.getKey(), new HashSet<ServiceTypeListener>(entry.getValue()),type);
        }
    }
    
    private void doNotify(Jedis jedis, Collection<String> keys, ServiceType type, Collection<ServiceTypeListener> listeners,int etype) {
        if (keys == null || keys.size() == 0
                || listeners == null || listeners.size() == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        List<DiscoveryInfo> result = new ArrayList<DiscoveryInfo>();
        List<String> categories = Arrays.asList(type.getCategory());
        String consumerService = type.getServiceInterface();
        for (String key : keys) {
            if (! HOLA.ANY_VALUE.equals(consumerService)) {
                String prvoiderService = toServiceName(key);
                if (! prvoiderService.equals(consumerService)) {
                    continue;
                }
            }
            String category = toCategoryName(key);
            if (! categories.contains(HOLA.ANY_VALUE) && ! categories.contains(category)) {
                continue;
            }
            List<DiscoveryInfo> infos = new ArrayList<DiscoveryInfo>();
            Map<String, String> values = jedis.hgetAll(key);
            if (values != null && values.size() > 0) {
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    DiscoveryInfo info = new DiscoveryInfoImpl(PropertiesUtils.toProperties(entry.getKey()));
                    if (! PropertiesUtils.getBoolean(info.getServiceProperties(), HOLA.DYNAMIC_KEY, true)
                            || Long.parseLong(entry.getValue()) >= now) {
                        if (isMatch(type, info)) {
                            infos.add(info);
                        }
                    }
                }
            }
            if(infos.size()>0){
                result.addAll(infos);
                if (LOG.isInfoEnabled()) {
                    LOG.info("redis notify: " + key + " = " + infos);
                }
            }
        }
        if (result == null || result.size() == 0) {
            return;
        }
        for (ServiceTypeListener listener : listeners) {
            notify(type, listener, result,etype);
        }
    }
    private String toServiceName(String categoryPath) {
        String servicePath = toServicePath(categoryPath);
        return servicePath.startsWith(root) ? servicePath.substring(root.length()) : servicePath;
    } 
    
    private String toCategoryName(String categoryPath) {
        int i = categoryPath.lastIndexOf(HOLA.PATH_SEPARATOR);
        return i > 0 ? categoryPath.substring(i + 1) : categoryPath;
    }
    
    private String toRedisRoot(String path){
        return root+path;
    }
    
    private String toServicePath(String categoryPath) {
        int i;
        if (categoryPath.startsWith(root)) {
            i = categoryPath.indexOf(HOLA.PATH_SEPARATOR, root.length());
        } else {
            i = categoryPath.indexOf(HOLA.PATH_SEPARATOR);
        }
        return i > 0 ? categoryPath.substring(0, i) : categoryPath;
    }

    
    private class Notifier extends Thread {

        private final String service;

        private volatile Jedis jedis;

        private volatile boolean first = true;
        
        private volatile boolean running = true;
        
        private final AtomicInteger connectSkip = new AtomicInteger();

        private final AtomicInteger connectSkiped = new AtomicInteger();

        private final Random random = new Random();
        
        private volatile int connectRandom;

        private void resetSkip() {
            connectSkip.set(0);
            connectSkiped.set(0);
            connectRandom = 0;
        }
        
        private boolean isSkip() {
            int skip = connectSkip.get(); // 跳过次数增长
            if (skip >= 10) { // 如果跳过次数增长超过10，取随机数
                if (connectRandom == 0) {
                    connectRandom = random.nextInt(10);
                }
                skip = 10 + connectRandom;
            }
            if (connectSkiped.getAndIncrement() < skip) { // 检查跳过次数
                return true;
            }
            connectSkip.incrementAndGet();
            connectSkiped.set(0);
            connectRandom = 0;
            return false;
        }
        
        public Notifier(String service) {
            super.setDaemon(true);
            super.setName("HolaRedisSubscribe");
            this.service = service;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    if (! isSkip()) {
                        try {
                            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                                JedisPool jedisPool = entry.getValue();
                                try {
                                    jedis = jedisPool.getResource();
                                    try {
                                        if (service.endsWith(HOLA.ANY_VALUE)) {
                                            if (! first) {
                                                first = false;
                                                Set<String> keys = jedis.keys(service);
                                                if (keys != null && keys.size() > 0) {
                                                    for (String s : keys) {
                                                        doNotify(jedis, s,DiscoveryTypeEvent.REGISTER);
                                                    }
                                                }
                                                resetSkip();
                                            }
                                            jedis.psubscribe(new NotifySub(jedisPool), service); // 阻塞
                                        } else {
                                            if (! first) {
                                                first = false;
                                                doNotify(jedis, service,DiscoveryTypeEvent.REGISTER);
                                                resetSkip();
                                            }
                                            jedis.psubscribe(new NotifySub(jedisPool), service + HOLA.PATH_SEPARATOR + HOLA.ANY_VALUE); // 阻塞
                                        }
                                        break;
                                    } finally {
                                        jedis.close();
                                    }
                                } catch (Throwable t) { // 重试另一台
                                    LOG.warn("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
                                    // 如果在单台redis的情况下，需要休息一会，避免空转占用过多cpu资源
                                    sleep(reconnectPeriod);
                                }
                            }
                        } catch (Throwable t) {
                            LOG.error(t.getMessage(), t);
                            sleep(reconnectPeriod);
                        }
                    }
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }
        }
        
        public void shutdown() {
            try {
                running = false;
                jedis.disconnect();
            } catch (Throwable t) {
                LOG.warn(t.getMessage(), t);
            }
        }
        
    }
private class NotifySub extends JedisPubSub {
        
        private final JedisPool jedisPool;

        public NotifySub(JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }
        @Override
        public void onPMessage(String pattern, String channel, String message) {
            if (LOG.isInfoEnabled()) {
                LOG.info("redis onPMessage: "+pattern);
            }
            onMessage(channel,message);
        }

        @Override
        public void onMessage(String key, String msg) {
            if (LOG.isInfoEnabled()) {
                LOG.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals(HOLA.REGISTER) 
                    || msg.equals(HOLA.UNREGISTER)) {
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        if(msg.equals(HOLA.REGISTER) ){
                            doNotify(jedis, key,DiscoveryTypeEvent.REGISTER);
                        }else{
                            doNotify(jedis, key,DiscoveryTypeEvent.UNREGISTER);
                        }
                        
                    } finally {
                        jedis.close();
                    }
                } catch (Throwable t) { // TODO 通知失败没有恢复机制保障
                    LOG.error(t.getMessage(), t);
                }
            }
        }
}
}
