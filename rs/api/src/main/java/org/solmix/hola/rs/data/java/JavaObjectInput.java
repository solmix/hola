/**
 * Copyright (c) 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.hola.rs.data.java;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;

import org.solmix.exchange.data.ObjectInput;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月19日
 */

public class JavaObjectInput implements ObjectInput {

    public final static int MAX_BYTE_ARRAY_LENGTH = 8 * 1024 * 1024;

    private final ObjectInputStream inputStream;

    public JavaObjectInput(InputStream in, ClassLoader loader)
        throws IOException {
        this(new DescObjectInputStream(in, loader));
    }

    public JavaObjectInput(ObjectInputStream in) {
        inputStream = in;
    }

    @Override
    public boolean readBool() throws IOException {
        return inputStream.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return inputStream.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return inputStream.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return inputStream.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return inputStream.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return inputStream.readDouble();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.data.DataInput#readUTF()
     */
    @Override
    public String readUTF() throws IOException {
        int len = inputStream.readInt();
        if (len < 0)
            return null;

        return inputStream.readUTF();
    }

   
    @Override
    public byte[] readBytes() throws IOException {
        int len = inputStream.readInt();
        if (len < 0)
            return null;
        if (len == 0)
            return new byte[0];
        if (len > MAX_BYTE_ARRAY_LENGTH)
            throw new IOException("Byte array length too large. " + len);

        byte[] b = new byte[len];
        inputStream.readFully(b);
        return b;
    }

 
    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        byte b = inputStream.readByte();
        if (b == 0)
            return null;

        return inputStream.readObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readObject(Class<T> cls) throws IOException,
        ClassNotFoundException {
        return (T) readObject();
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException,
        ClassNotFoundException {
        return (T) readObject();
    }

    static class DescObjectInputStream extends java.io.ObjectInputStream {

        private final ClassLoader classLoader;

        public DescObjectInputStream(InputStream in, ClassLoader classLoader)
            throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {
            int type = read();
            if (type < 0)
                throw new EOFException();
            switch (type) {
            case 0:
                return super.readClassDescriptor();
            case 1:
                Class<?> clazz = classLoader.loadClass(readUTF());
                return ObjectStreamClass.lookup(clazz);
            default:
                throw new StreamCorruptedException(
                    "Unexpected class descriptor type: " + type);
            }
        }

    }
}
