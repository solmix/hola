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
package org.solmix.hola.core.serialize.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月12日
 */

public class JavaObjectInput implements ObjectInput
{
    private final ObjectInputStream inputStream;
    /**
     * @param input
     * @throws IOException 
     */
    public JavaObjectInput(InputStream input) throws IOException
    {
        inputStream= new ObjectInputStream(input);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFully(byte[])
     */
    @Override
    public void readFully(byte[] b) throws IOException {
        inputStream.readFully(b);

    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        inputStream.readFully(b, off, len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#skipBytes(int)
     */
    @Override
    public int skipBytes(int n) throws IOException {
        return inputStream.skipBytes(n);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readBoolean()
     */
    @Override
    public boolean readBoolean() throws IOException {
        return inputStream.readBoolean();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readByte()
     */
    @Override
    public byte readByte() throws IOException {
        return inputStream.readByte();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUnsignedByte()
     */
    @Override
    public int readUnsignedByte() throws IOException {
        return inputStream.readUnsignedByte();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readShort()
     */
    @Override
    public short readShort() throws IOException {
        return inputStream.readShort();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUnsignedShort()
     */
    @Override
    public int readUnsignedShort() throws IOException {
        return inputStream.readUnsignedShort();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readChar()
     */
    @Override
    public char readChar() throws IOException {
        return inputStream.readChar();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readInt()
     */
    @Override
    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readLong()
     */
    @Override
    public long readLong() throws IOException {
        return inputStream.readLong();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFloat()
     */
    @Override
    public float readFloat() throws IOException {
        return inputStream.readFloat();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readDouble()
     */
    @Override
    public double readDouble() throws IOException {
        return inputStream.readDouble();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readLine()
     */
    @Override
    public String readLine() throws IOException {
        return inputStream.readLine();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUTF()
     */
    @Override
    public String readUTF() throws IOException {
        return inputStream.readUTF();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#readObject()
     */
    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        return inputStream.readObject();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#read()
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#skip(long)
     */
    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#available()
     */
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#close()
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}
