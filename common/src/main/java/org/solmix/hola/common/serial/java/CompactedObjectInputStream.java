
package org.solmix.hola.common.serial.java;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import org.solmix.commons.util.ClassLoaderUtils;

/**
 * Compacted java object input stream.
 * 
 */

public class CompactedObjectInputStream extends ObjectInputStream
{
	private ClassLoader mClassLoader;

	public CompactedObjectInputStream(InputStream in) throws IOException
	{
		this(in, Thread.currentThread().getContextClassLoader());
	}

	public CompactedObjectInputStream(InputStream in, ClassLoader cl) throws IOException
	{
		super(in);
		mClassLoader = cl == null ? ClassLoaderUtils.getDefaultClassLoader() : cl;
	}

	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException,ClassNotFoundException
	{
		int type = read();
		if( type < 0 )
			throw new EOFException();
		switch( type )
		{
			case 0:
				return super.readClassDescriptor();
			case 1:
				Class<?> clazz = loadClass(readUTF());
				return ObjectStreamClass.lookup(clazz);
			default:
				throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
		}
	}

	private Class<?> loadClass(String className) throws ClassNotFoundException
	{
		return mClassLoader.loadClass(className);
	}
}