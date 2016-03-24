
package org.solmix.scheduler.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.solmix.commons.util.NetUtils;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ElectionService;
import org.solmix.scheduler.services.JobServerNode;
import org.solmix.scheduler.services.JobServerService;
import org.solmix.scheduler.services.StorageService;

public class DefaultJobServerService implements JobServerService
{

    private final StorageService storage;

    private final ElectionService leader;

    public DefaultJobServerService(final SchedulerRegistry registry, JobInfo info)
    {
        storage = new DefaultStorageService(registry, info);
        leader = new DefaultElectionService(registry, info);
    }

    @Override
    public void clearPreviousServerStatus() {
        String ip = NetUtils.getLocalIp();
        storage.removeJobNodeIfExisted(JobServerNode.getStatusNode(ip));
        storage.removeJobNodeIfExisted(JobServerNode.getShutdownNode(ip));
    }

    @Override
    public void persistServerOnline() {
       if(!leader.isLeader()){
           leader.leaderElection();
       }
       storage.fillJobNodeIfNullOrOverwrite(JobServerNode.getHostNameNode(NetUtils.getLocalIp()), NetUtils.getLocalAddress().getHostName());
       persistDisabled();

    }

    private void persistDisabled() {
        if(!storage.getJobInfo().isOverwrite()){
            return;
        }
        if(storage.getJobInfo().isDisabled()){
            storage.fillJobNodeIfNullOrOverwrite(JobServerNode.getDisabledNode(NetUtils.getLocalIp()), "");
        }else{
            storage.removeJobNodeIfExisted(JobServerNode.getDisabledNode(NetUtils.getLocalIp()));
        }
        
    }

    @Override
    public void clearJobStoppedStatus() {
        storage.removeJobNodeIfExisted(JobServerNode.getShutdownNode(NetUtils.getLocalIp()));
    }

    @Override
    public boolean isJobStoppedManually() {
        return  storage.isJobNodeExisted(JobServerNode.getShutdownNode(NetUtils.getLocalIp()));
    }

    @Override
    public void processServerShutdown() {
        storage.removeJobNodeIfExisted(JobServerNode.getStatusNode(NetUtils.getLocalIp()));
    }

    @Override
    public void updateServerStatus(Status status) {
        storage.updateJobNode(JobServerNode.getStatusNode(NetUtils.getLocalIp()),status);
    }

    @Override
    public List<String> getAllNodes() {
        List<String> result = storage.getJobNodeChildrenKeys(JobServerNode.ROOT);
        Collections.sort(result);
        return result;
    }

    @Override
    public List<String> getAvailableNodes() {
        List<String> servers = getAllNodes();
        List<String> result = new ArrayList<String>(servers.size());
        for (String each : servers) {
            if (isAvailableServer(each)) {
                result.add(each);
            }
        }
        return result;
    }
    private Boolean isAvailableServer(final String ip) {
        return storage.isJobNodeExisted(JobServerNode.getStatusNode(ip)) && !storage.isJobNodeExisted(JobServerNode.getDisabledNode(ip));
    }

    @Override
    public boolean isAvailable() {
        if (storage.isJobNodeExisted(JobServerNode.getDisabledNode(NetUtils.getLocalIp()))) {
            return false;
        }
        if (storage.isJobNodeExisted(JobServerNode.getStoppedNode(NetUtils.getLocalIp()))) {
            return false;
        }
        String statusNode = JobServerNode.getStatusNode(NetUtils.getLocalIp());
        return storage.isJobNodeExisted(statusNode) && Status.READY.name().equals(storage.getJobNodeData(statusNode));
    }

    @Override
    public void persistSuccessCount(int successCount) {
        storage.replaceJobNode(JobServerNode.getProcessSuccessCountNode(NetUtils.getLocalIp()), successCount);
    }
    

    @Override
    public void persistFailureCount(int failureCount) {
        storage.replaceJobNode(JobServerNode.getProcessFailureCountNode(NetUtils.getLocalIp()), failureCount);
    }

}
