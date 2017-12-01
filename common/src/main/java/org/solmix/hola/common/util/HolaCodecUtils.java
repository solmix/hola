package org.solmix.hola.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.solmix.commons.util.Base64Exception;
import org.solmix.commons.util.Base64Utils;
import org.solmix.commons.util.ClassLoaderUtils;
import org.solmix.commons.util.ClassLoaderUtils.ClassLoaderHolder;
import org.solmix.hola.common.serial.hola.HolaObjectInput;
import org.solmix.hola.common.serial.hola.HolaObjectOutput;

public final class HolaCodecUtils {

	public static String encode(Serializable object) throws IOException {
		return Base64Utils.encode(serialize(object));
	}

	public static byte[] serialize(Serializable object) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		 HolaObjectOutput ot = new HolaObjectOutput(out);
		 ot.writeObject(object);
		 ot.flushBuffer();
			out.close();
	
		return out.toByteArray();
	}

	public static <T> T deserialize(byte[] value, Class<T> resClass)throws IOException, ClassNotFoundException {
	    ClassLoaderHolder holder = ClassLoaderUtils.setThreadContextClassloader(resClass.getClassLoader());
	    try{
		ByteArrayInputStream bi = new ByteArrayInputStream(value);
		HolaObjectInput oi = new HolaObjectInput(bi);
		return oi.readObject(resClass);
	    }finally{
	        holder.reset();
	    }

	}

	public static <T> T decode(String value, Class<T> resClass)
			throws IOException, ClassNotFoundException {
		try {
			return deserialize(Base64Utils.decode(value), resClass);
		} catch (Base64Exception e) {
			throw new IOException(e);
		}
	}
}
