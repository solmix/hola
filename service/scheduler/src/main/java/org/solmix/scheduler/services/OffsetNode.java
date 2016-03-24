package org.solmix.scheduler.services;


public class OffsetNode
{
    
   public static final String ROOT = "offset";
    
    private static final String ITEM = ROOT + "/%s";
    
    /**
     * 获取分片数据处理位置节点路径.
     * 
     * @param item 作业项
     * @return 分片数据处理位置节点路径
     */
    public static String getItemNode(final int item) {
        return String.format(ITEM, item);
    }

}
