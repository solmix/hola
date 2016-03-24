
package org.solmix.scheduler.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.scheduler.SchedulerRegistry;
import org.solmix.scheduler.model.JobInfo;
import org.solmix.scheduler.services.OffsetNode;
import org.solmix.scheduler.services.OffsetService;
import org.solmix.scheduler.services.StorageService;

import com.google.common.base.Strings;

public class DefaultOffsetService implements OffsetService
{

    public DefaultOffsetService(final SchedulerRegistry registry, JobInfo info)
    {
        storage = new DefaultStorageService(registry, info);
    }
    
    private final StorageService storage;
    
    /**
     * 更新数据处理位置.
     * 
     * @param item 分片项
     * @param offset 数据处理位置
     */
    @Override
    public void updateOffset(final int item, final String offset) {
        String node = OffsetNode.getItemNode(item);
        storage.createJobNodeIfNeeded(node);
        storage.updateJobNode(node, offset);
    }
    
    /**
     * 获取数据分片项和数据处理位置Map.
     * 
     * @param items 分片项集合
     * @return 数据分片项和数据处理位置Map
     */
    public Map<Integer, String> getOffsets(final List<Integer> items) {
        Map<Integer, String> result = new HashMap<Integer, String>(items.size());
        for (int each : items) {
            String offset = storage.getJobNodeDataDirectly(OffsetNode.getItemNode(each));
            if (!Strings.isNullOrEmpty(offset)) {
                result.put(each, offset);
            }
        }
        return result;
    }
}
