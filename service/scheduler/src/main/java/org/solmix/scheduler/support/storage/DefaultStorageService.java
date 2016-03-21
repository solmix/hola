
package org.solmix.scheduler.support.storage;

import java.util.List;

import org.solmix.scheduler.SchedulerRegistry;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getJobNodeChildrenKeys(String node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createJobNodeIfNeeded(String node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeJobNodeIfExisted(String node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fillJobNodeIfNullOrOverwrite(String node, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fillEphemeralJobNode(String node, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateJobNode(String node, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void replaceJobNode(String node, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void executeInTransaction(TransactionExecutionCallback callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void executeInLeader(String latchNode, LeaderExecutionCallback callback) {
        // TODO Auto-generated method stub

    }

}
