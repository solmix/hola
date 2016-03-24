package org.solmix.scheduler.job;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.DataUtils;
import org.solmix.scheduler.exception.JobException;

public class JobExecutionShardingContext extends AbstractJobContext
{
  private static int initCollectionSize = 64;
    
    /**
     * 运行在本作业服务器的分片序列号集合.
     */
    private List<Integer> shardingItems = new ArrayList<Integer>(initCollectionSize);
    
    /**
     * 运行在本作业项的分片序列号和个性化参数列表.
     */
    private Map<Integer, String> shardingItemParameters = new HashMap<Integer, String>(initCollectionSize);
    
    /**
     * 数据分片项和数据处理位置Map.
     */
    private Map<Integer, String> offsets = new HashMap<Integer, String>();

    public JobExecutionSimpleContext createJobExecutionSimpleContext(final int item) {
        JobExecutionSimpleContext result = new JobExecutionSimpleContext();
        try {
            DataUtils.copyProperties(result, this);
        } catch (final IllegalAccessException  ex){
            throw new JobException(ex);
        }catch( InvocationTargetException ex) {
            throw new JobException(ex);
        } catch (Exception e) {
            throw new JobException(e);
        }
        result.setShardingItem(item);
        result.setShardingItemParameter(shardingItemParameters.get(item));
        result.setOffset(offsets.get(item));
        return result;
    }

    
    public static int getInitCollectionSize() {
        return initCollectionSize;
    }

    
    public static void setInitCollectionSize(int initCollectionSize) {
        JobExecutionShardingContext.initCollectionSize = initCollectionSize;
    }

    
    public Map<Integer, String> getShardingItemParameters() {
        return shardingItemParameters;
    }

    
    public void setShardingItemParameters(Map<Integer, String> shardingItemParameters) {
        this.shardingItemParameters = shardingItemParameters;
    }

    
    public Map<Integer, String> getOffsets() {
        return offsets;
    }

    
    public void setOffsets(Map<Integer, String> offsets) {
        this.offsets = offsets;
    }

    
    public void setShardingItems(List<Integer> shardingItems) {
        this.shardingItems = shardingItems;
    }


    
    public List<Integer> getShardingItems() {
        return shardingItems;
    }

}
