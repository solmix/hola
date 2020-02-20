package org.solmix.hola.common.util;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;


public class PropertiesUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void test() {
		Hashtable<String, Object> p = new Hashtable<>();
		
		p.put(HOLA.PROTOCOL_KEY, "hola");
		p.put(HOLA.HOST_KEY, "111::1");
		p.put(HOLA.PORT_KEY, "1231");
		p.put(HOLA.USER_KEY, "user");
		p.put(HOLA.PASSWORD_KEY, "pass");
		String address =PropertiesUtils.toAddress(p,true,true,true,true);
		
		
		Dictionary<String, Object> dict =PropertiesUtils.toProperties(address);
		Assert.assertEquals("hola://user:pass@[111:0:0:0:0:0:0:1]:1231", address);
		p.put(HOLA.HOST_KEY, "127.0.0.1");
		String address2 =PropertiesUtils.toAddress(p,false,true,true,true);
		
		Assert.assertEquals("hola://127.0.0.1:1231", address2);
	}

}
