package org.solmix.hola.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;


public class ServiceUtilsTest extends Assert
{
    @Test
    public void testNoprotocol(){
        String ad="/context/path?version=1.0.0&params=param";
        Dictionary<String, ?> properties = PropertiesUtils.toProperties(ad);
        assertNull(properties.get(HOLA.HOST_KEY));
        assertNull(properties.get(HOLA.PROTOCOL_KEY));
        assertNull(properties.get(HOLA.PORT_KEY));
        assertEquals(properties.get("version"), "1.0.0");
        assertEquals(properties.get("params"), "param");
        assertEquals(properties.get(HOLA.PATH_KEY), "context/path");
//        String address = PropertiesUtils.toAddress(properties);
//        assertEquals(address, ad);
        
        ad="username:passwaord@1.2.3.4:8080/context/path?version=1.0.0&params=param";
        properties = PropertiesUtils.toProperties(ad);
        assertNull(properties.get(HOLA.PROTOCOL_KEY));
        assertEquals(properties.get(HOLA.HOST_KEY), "1.2.3.4");
        assertEquals(properties.get(HOLA.USER_KEY), "username");
        assertEquals(properties.get(HOLA.PASSWORD_KEY), "passwaord");
        assertEquals(PropertiesUtils.getInt(properties, HOLA.PORT_KEY), (Integer)8080);
        assertEquals(properties.get(HOLA.PATH_KEY), "context/path");
//        address = PropertiesUtils.toAddress(properties,true,true,false,false);
//        assertEquals(address, ad);
    }
    
    @Test
    public void testIdentity(){
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        dic.put(HOLA.PROTOCOL_KEY, "hola");
        dic.put(HOLA.HOST_KEY, "12.1.2.1");
        dic.put(HOLA.PORT_KEY, "1314");
        dic.put(HOLA.PATH_KEY, "/path/to/context");
        dic.put("cluster", "failover");
        dic.put("dicovery.connect", "5000");
        dic.put("timeout", "2333");
        
        
        Dictionary<String, Object> dic2 = new Hashtable<String, Object>();
        dic2.put(HOLA.PROTOCOL_KEY, "hola");
        dic2.put(HOLA.HOST_KEY, "12.1.2.1");
        dic2.put(HOLA.PORT_KEY, "1314");
        dic2.put(HOLA.PATH_KEY, "/path/to/context");
        dic2.put("dicovery.connect", "5000");
        dic2.put("timeout", "2333");
        dic2.put("cluster", "failover");
      
        assertTrue(PropertiesUtils.toAddress(dic).equals(PropertiesUtils.toAddress(dic2)));

        assertTrue(PropertiesUtils.toIndentityAddress(dic).equals(PropertiesUtils.toIndentityAddress(dic2)));
    }
    
    @Test
    public void test_putIfExitAsArray(){
        Hashtable<String, Object> dic = new Hashtable<String, Object>();
        dic.put(HOLA.FILTER_KEY, "test-filter");
        PropertiesUtils.putIfExitAsArray(dic, HOLA.FILTER_KEY, "monitor");
        assertEquals("test-filter,monitor", PropertiesUtils.getString(dic, HOLA.FILTER_KEY));
        PropertiesUtils.putIfExitAsArray(dic, HOLA.PORT_KEY, 124);
        assertEquals((Integer)124, PropertiesUtils.getInt(dic, HOLA.PORT_KEY));
        
        dic.clear();
        
        dic.put(HOLA.FILTER_KEY,  new ArrayList<String>(Arrays.asList("test-filter")));
        PropertiesUtils.putIfExitAsArray(dic, HOLA.FILTER_KEY, "monitor");
        assertEquals(2, PropertiesUtils.getList(dic, HOLA.FILTER_KEY).size());
    }

}
