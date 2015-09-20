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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

import org.solmix.exchange.data.ObjectOutput;



/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月19日
 */

public class JavaObjectOutput implements ObjectOutput {

    private final ObjectOutputStream outputStream;
    
    public JavaObjectOutput(OutputStream out) throws IOException{
        this(new DescObjectOutputStream(out));
    }
    public JavaObjectOutput(ObjectOutputStream out){
        outputStream=out;
    }
    @Override
    public void writeBool(boolean v) throws IOException {
        outputStream.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        outputStream.writeByte(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        outputStream.writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        outputStream.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        outputStream.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        outputStream.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        outputStream.writeDouble(v);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.data.DataOutput#writeUTF(java.lang.String)
     */
    @Override
    public void writeUTF(String v) throws IOException {
        if (v == null) {
            outputStream.writeInt(-1);
        } else {
            outputStream.writeInt(v.length());
            outputStream.writeUTF(v);
        }
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            outputStream.writeInt(-1);
        } else {
            writeBytes(v, 0, v.length);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            outputStream.writeInt(-1);
        } else {
            outputStream.writeInt(len);
            outputStream.write(v, off, len);
        }

    }

    @Override
    public void flushBuffer() throws IOException {
        outputStream.flush();
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            outputStream.writeByte(0);
        } else {
            outputStream.writeByte(1);
            outputStream.writeObject(obj);
        }
    }
    
    static class DescObjectOutputStream extends java.io.ObjectOutputStream{

        public DescObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }
        @Override
        protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException
        {
              Class<?> clazz = desc.forClass();
              if( clazz.isPrimitive() || clazz.isArray() )
              {
                    write(0);
                    super.writeClassDescriptor(desc);
              }
              else
              {
                    write(1);
                    writeUTF(desc.getName());
              }
        }
    }

}
