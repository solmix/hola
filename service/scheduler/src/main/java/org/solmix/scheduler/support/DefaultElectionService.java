package org.solmix.scheduler.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.NetUtils;
import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.ElectionNode;
import org.solmix.scheduler.services.ElectionService;
import org.solmix.scheduler.services.StorageService;
import org.solmix.scheduler.services.StorageService.LeaderExecutionCallback;


public class DefaultElectionService implements ElectionService
{
    private static final Logger LOG  = LoggerFactory.getLogger(DefaultElectionService.class);
    private final StorageService storage;

    public DefaultElectionService(final SchedulerRegistry registry,JobInfo info){
        storage = new DefaultStorageService(registry, info);
    }
    @Override
    public void leaderElection() {
        storage.executeInLeader(ElectionNode.LATCH, new LeaderElectionExecutionCallback() );
    }

    @Override
    public Boolean isLeader() {
        String localHostIp = NetUtils.getLocalIp();
        while (!hasLeader()) {
            LOG.info("Scheduler: leader node is electing, waiting for 100 ms at server '{}'", localHostIp);
           
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return localHostIp.equals(storage.getJobNodeData(ElectionNode.LEADER_HOST));
    }

    @Override
    public boolean hasLeader() {
        return storage.isJobNodeExisted(ElectionNode.LEADER_HOST);
    }
    
  class LeaderElectionExecutionCallback implements LeaderExecutionCallback {
        
        @Override
        public void execute() {
            if (!storage.isJobNodeExisted(ElectionNode.LEADER_HOST)) {
                storage.fillEphemeralJobNode(ElectionNode.LEADER_HOST, NetUtils.getLocalIp());
            }
        }
    }

}
