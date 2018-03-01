package org.solmix.hola.common.model;

import java.util.Dictionary;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.common.HOLA;



public class PropertiesUtilsTest
{

    @Test
    public void test() {
        Dictionary<String, ?>  table =PropertiesBuilder.newBuilder().setProtocol("zk")
            .setProperty("sdfsd", "sdfsd33 sdfd")
            .setHost("127.0.0.1")
            .setPort(444)
            .build();
        List<Dictionary<String, ?>> urls=  PropertiesUtils.parseURLs("192.168.0.1:2181,192.168.0.3:2181,192.168.0.2:2181", table);
        Assert.assertEquals(1, urls.size());
        Dictionary<String, ?> dic = urls.get(0);
        Assert.assertEquals("192.168.0.1", dic.get(HOLA.HOST_KEY));
        Assert.assertEquals("192.168.0.3:2181,192.168.0.2:2181", dic.get(HOLA.BACKUP_KEY));
        String address = PropertiesUtils.toAddress(table);
      
        Dictionary<String, Object>  dd=   PropertiesUtils.toProperties(address);
        Assert.assertEquals("sdfsd33 sdfd", dd.get("sdfsd"));
    }

}
