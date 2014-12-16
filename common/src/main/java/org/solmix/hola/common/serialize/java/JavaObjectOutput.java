/*
 * Copyright 2013 The Solmix Project
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
package org.solmix.hola.common.serialize.java;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月12日
 */

public class JavaObjectOutput implements ObjectOutput
{
    private final ObjectOutputStream outputStream;
    /**
     * @param output
     * @throws IOException 
     */
    public JavaObjectOutput(OutputStream output) throws IOException
    {
        outputStream=new ObjectOutputStream(output);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    @Override
    public void writeBoolean(boolean v) throws IOException {
        outputStream.writeBoolean(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeByte(int)
     */
    @Override
    public void writeByte(int v) throws IOException {
        outputStream.writeByte(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeShort(int)
     */
    @Override
    public void writeShort(int v) throws IOException {
        outputStream.writeShort(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeChar(int)
     */
    @Override
    public void writeChar(int v) throws IOException {
        outputStream.writeChar(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeInt(int)
     */
    @Override
    public void writeInt(int v) throws IOException {
        outputStream.writeInt(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeLong(long)
     */
    @Override
    public void writeLong(long v) throws IOException {
        outputStream.writeLong(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeFloat(float)
     */
    @Override
    public void writeFloat(float v) throws IOException {
        outputStream.writeFloat(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeDouble(double)
     */
    @Override
    public void writeDouble(double v) throws IOException {
        outputStream.writeDouble(v);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    @Override
    public void writeBytes(String s) throws IOException {
        outputStream.writeBytes(s);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeChars(java.lang.String)
     */
    @Override
    public void writeChars(String s) throws IOException {
        outputStream.writeChars(s);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    @Override
    public void writeUTF(String s) throws IOException {
        outputStream.writeUTF(s);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#writeObject(java.lang.Object)
     */
    @Override
    public void writeObject(Object obj) throws IOException {
        outputStream.writeObject(obj);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b,off,len);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#flush()
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectOutput#close()
     */
    @Override
    public void close() throws IOException {
        outputStream.close();

    }

}
