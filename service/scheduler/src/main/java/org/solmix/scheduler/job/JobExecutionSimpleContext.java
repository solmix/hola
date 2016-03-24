package org.solmix.scheduler.job;


public class JobExecutionSimpleContext extends AbstractJobContext
{
    /**
     * 运行在本作业服务器的分片序列号.
     */
    private int shardingItem;
    
    /**
     * 运行在本作业项的分片序列号和个性化参数.
     */
    private String shardingItemParameter;
    
    /**
     * 数据处理位置.
     */
    private String offset;

    
    public int getShardingItem() {
        return shardingItem;
    }

    
    public void setShardingItem(int shardingItem) {
        this.shardingItem = shardingItem;
    }

    
    public String getShardingItemParameter() {
        return shardingItemParameter;
    }

    
    public void setShardingItemParameter(String shardingItemParameter) {
        this.shardingItemParameter = shardingItemParameter;
    }

    
    public String getOffset() {
        return offset;
    }

    
    public void setOffset(String offset) {
        this.offset = offset;
    }
}
