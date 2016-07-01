package org.solmix.scheduler.model;

import com.google.common.base.Strings;

public class TestingZkInfo extends ZkInfo {

	 /**
     * 内嵌Zookeeper的端口号.
     * -1表示不开启内嵌Zookeeper.
     */
    private int nestedPort = -1;
    
    /**
     * 内嵌Zookeeper的数据存储路径.
     * 为空表示不开启内嵌Zookeeper.
     */
    private String nestedDataDir;
	public TestingZkInfo(String address,String namespace) {
		super(address, namespace);
	}
	/**
     * 判断是否需要开启内嵌Zookeeper.
     * 
     * @return 是否需要开启内嵌Zookeeper
     */
    public boolean isUseNestedZookeeper() {
        return -1 != nestedPort && !Strings.isNullOrEmpty(nestedDataDir);
    }
    

    
    public int getNestedPort() {
        return nestedPort;
    }

    
    public void setNestedPort(int nestedPort) {
        this.nestedPort = nestedPort;
    }

    
    public String getNestedDataDir() {
        return nestedDataDir;
    }

    
    public void setNestedDataDir(String nestedDataDir) {
        this.nestedDataDir = nestedDataDir;
    }

}
