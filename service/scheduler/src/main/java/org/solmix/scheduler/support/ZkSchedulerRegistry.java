
package org.solmix.scheduler.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.exception.FileNotFoundException;
import org.solmix.scheduler.exception.RegistryExceptionHandler;
import org.solmix.scheduler.model.ZkInfo;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class ZkSchedulerRegistry implements SchedulerRegistry
{

    private static final Logger LOG = LoggerFactory.getLogger(ZkSchedulerRegistry.class);

    private CuratorFramework client;

    private ZkInfo zkInfo;

    private final Map<String, TreeCache> caches = new HashMap<String, TreeCache>();

    public ZkSchedulerRegistry(final ZkInfo zkInfo)
    {
        this.zkInfo = zkInfo;
    }

    @Override
    public void init() {
        if (zkInfo.isUseNestedZookeeper()) {
            ZkServers.getInstance().startServerIfNotStarted(zkInfo.getNestedPort(), zkInfo.getNestedDataDir());
        }
        LOG.debug("Init registry address :{}", zkInfo.getAddress());
        org.apache.curator.framework.CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(
            zkInfo.getAddress()).retryPolicy(
                new ExponentialBackoffRetry(zkInfo.getBaseSleepTimeMilliseconds(), zkInfo.getMaxRetries(),
                    zkInfo.getMaxSleepTimeMilliseconds())).namespace(zkInfo.getNamespace());
        if (0 != zkInfo.getSessionTimeoutMilliseconds()) {
            builder.sessionTimeoutMs(zkInfo.getSessionTimeoutMilliseconds());
        }
        if (0 != zkInfo.getConnectionTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(zkInfo.getConnectionTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(zkInfo.getDigest())) {
            builder.authorization("digest", zkInfo.getDigest().getBytes(Charset.forName("UTF-8"))).aclProvider(new ACLProvider() {

                @Override
                public List<ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(final String path) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });
        }
        client = builder.build();
        client.start();
        try {
            client.blockUntilConnected();
            if (!Strings.isNullOrEmpty(zkInfo.getLocalPropertiesPath())) {
                fillData();
            }
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }
    }

    private void fillData() throws Exception {
        for (Entry<Object, Object> entry : loadLocalProperties().entrySet()) {
            String key = entry.getKey().toString();
            byte[] value = entry.getValue().toString().getBytes(Charset.forName("UTF-8"));
            if (null == client.checkExists().forPath(key)) {
                client.create().creatingParentsIfNeeded().forPath(key, value);
            } else if (zkInfo.isOverwrite() || 0 == client.getData().forPath(key).length) {
                client.setData().forPath(key, value);
            }
        }
    }

    private Properties loadLocalProperties() {
        Properties result = new Properties();
        InputStream input = null;
        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(zkInfo.getLocalPropertiesPath());
            if (null == input) {
                throw new FileNotFoundException(zkInfo.getLocalPropertiesPath());
            }
            result.load(input);
        } catch (final IOException ex) {
            throw new FileNotFoundException(ex);
        } finally {
            if (input != null) {
                IOUtils.closeQuietly(input);
            }
        }
        return result;
    }

    @Override
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
        if (zkInfo.isUseNestedZookeeper()) {
            ZkServers.getInstance().closeServer(zkInfo.getNestedPort());
        }
    }

    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String get(String key) {
        TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charset.forName("UTF-8"));
        }
        return getDirectly(key);
    }

    private TreeCache findTreeCache(final String key) {
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean isExisted(String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persist(String key, String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes());
            } else {
                update(key, value);
            }
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    @Override
    public void update(String key, String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charset.forName("UTF-8"))).and().commit();
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    @Override
    public void remove(String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    @Override
    public long getRegistryCenterTime(String key) {
        long result = 0L;
        try {
            String path = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
            result = client.checkExists().forPath(path).getCtime();
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }
        Preconditions.checkState(0L != result, "Cannot get registry center time.");
        return result;
    }

    @Override
    public Object getRawClient() {
        return client;
    }

    @Override
    public String getDirectly(String key) {
        try {
            return new String(client.getData().forPath(key), Charset.forName("UTF-8"));
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
            return null;
        }
    }

    @Override
    public List<String> getChildrenKeys(String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {

                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public void persistEphemeral(String key, String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Charset.forName("UTF-8")));
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    @Override
    public void persistEphemeralSequential(String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    @Override
    public void addCacheData(String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);

    }

    @Override
    public Object getRawCache(String cachePath) {
        return caches.get(cachePath + "/");
    }

}
