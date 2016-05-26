package org.solmix.hola.common.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.solmix.runtime.identity.ID;
import org.solmix.runtime.identity.IDFactory;
import org.solmix.runtime.identity.support.GUID;

public class HolaCodecUtilsTest {

	@Test
	public void test() throws IOException, ClassNotFoundException {
		HashMap<String,String> id =new HashMap<String,String>();
		id.put("aa", "bb");
		String sstr=HolaCodecUtils.encode(id);
		assertNotNull(sstr);
		@SuppressWarnings("unchecked")
		HashMap<String,String> newId= HolaCodecUtils.decode(sstr, HashMap.class);
		assertEquals("bb", newId.get("aa"));
	}

}
