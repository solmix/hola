
package org.solmix.scheduler.support;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.exception.JobException;
import org.solmix.scheduler.exception.RegistryExceptionHandler;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.JobNodePath;
import org.solmix.scheduler.services.StorageService;

public class DefaultStorageService implements StorageService
{

    private final SchedulerRegistry registry;

    private JobNodePath path;

    private final JobInfo info;

    public DefaultStorageService(final SchedulerRegistry registry, final JobInfo info)
    {
        this.registry = registry;
        this.info = info;
        this.path = new JobNodePath(info.getJobName());
    }

    @Override
    public boolean isJobNodeExisted(String node) {
        return registry.isExisted(path.getFullPath(node));
    }

    @Override
    public String getJobNodeData(String node) {
        return registry.get(path.getFullPath(node));
    }

    @Override
    public String getJobNodeDataDirectly(String node) {
        return registry.getDirectly(path.getFullPath(node));
    }

    @Override
    public List<String> getJobNodeChildrenKeys(String node) {
        return registry.getChildrenKeys(path.getFullPath(node));
    }

    @Override
    public void createJobNodeIfNeeded(String node) {
        if (!isJobNodeExisted(node)) {
            registry.persist(path.getFullPath(node), "");
        }

    }

    @Override
    public void removeJobNodeIfExisted(String node) {
        if (isJobNodeExisted(path.getFullPath(node))) {
            registry.remove(path.getFullPath(node));
        }

    }

    @Override
    public void fillJobNodeIfNullOrOverwrite(String node, Object value) {
        if (!isJobNodeExisted(node) || (info.isOverwrite() && !value.toString().equals(getJobNodeDataDirectly(node)))) {
            registry.persist(path.getFullPath(node), value.toString());
        }

    }

    @Override
    public void fillEphemeralJobNode(String node, Object value) {
        registry.persistEphemeral(path.getFullPath(node), value.toString());

    }

    @Override
    public void updateJobNode(String node, Object value) {
        registry.update(path.getFullPath(node), value.toString());

    }

    @Override
    public void replaceJobNode(String node, Object value) {
        registry.persist(path.getFullPath(node), value.toString());

    }

    @Override
    public void executeInTransaction(TransactionExecutionCallback callback) {
        try {
            CuratorTransactionFinal curatorTransactionFinal = getClient().inTransaction().check().forPath("/").and();
            callback.execute(curatorTransactionFinal);
            curatorTransactionFinal.commit();
        } catch (final Exception ex) {
            RegistryExceptionHandler.handleException(ex);
        }

    }

    private CuratorFramework getClient() {
        return (CuratorFramework) registry.getRawClient();
    }

    @Override
    public void executeInLeader(String latchNode, LeaderExecutionCallback callback) {
        LeaderLatch latch = null;
        try {
            latch = new LeaderLatch(getClient(), path.getFullPath(latchNode));
            latch.start();
            latch.await();
            callback.execute();
        } catch (final Exception ex) {
            handleException(ex);
        }

    }

    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new JobException(ex);
        }
    }

    @Override
    public JobInfo getJobInfo() {
        return info;
    }

    @Override
    public long getRegistryCenterTime() {
        return registry.getRegistryCenterTime(path.getFullPath("systemTime/current"));
    }
}
