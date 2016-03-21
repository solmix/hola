package org.solmix.scheduler.services;

import java.util.List;

import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;

public interface StorageService
{
    
    boolean isJobNodeExisted(final String node);
    
    String getJobNodeData(final String node);
    
    String getJobNodeDataDirectly(final String node);
    
    
    List<String> getJobNodeChildrenKeys(final String node) ;
    
    void createJobNodeIfNeeded(final String node);
    
    void removeJobNodeIfExisted(final String node) ;
    
    void fillJobNodeIfNullOrOverwrite(final String node, final Object value);
    
    void fillEphemeralJobNode(final String node, final Object value);
    
    void updateJobNode(final String node, final Object value) ;
    
    void replaceJobNode(final String node, final Object value) ;
    
    void executeInTransaction(final TransactionExecutionCallback callback) ;
    
    void executeInLeader(final String latchNode, final LeaderExecutionCallback callback);

    public interface LeaderExecutionCallback{
        void execute();
    }
    
    public interface TransactionExecutionCallback {
        
        /**
         * 事务执行的回调方法.
         * 
         * @param curatorTransactionFinal 执行事务的上下文
         * @throws Exception 处理中异常
         */
        void execute(CuratorTransactionFinal curatorTransactionFinal) throws Exception;
    }

}
