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
package org.solmix.hola.core.serialize.hola;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.UTFDataFormatException;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月24日
 */

public class HolaObjectInput extends InputStream implements ObjectInput,DataFlags
{
    private static final String EMPTY_STRING = "";

    private static final byte[] EMPTY_BYTES = {};
    private final ClassDescriptorMapper mapper;
    private final InputStream input;
    private final byte[] buffer;

    private int position;

    private int read;
    
    public HolaObjectInput(InputStream is, ClassDescriptorMapper mapper){
       this(is,1024,mapper);
    }
    
    public HolaObjectInput(InputStream is, int buffSize, ClassDescriptorMapper mapper)
    {
        this.mapper=mapper;
        this.input=is;
        this.buffer=new byte[buffSize];
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#skipBytes(int)
     */
    @Override
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;
        while ((total<n) && ((cur = (int) input.skip(n-total)) > 0)) {
            total += cur;
        }
        return total;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readBoolean()
     */
    @Override
    public boolean readBoolean() throws IOException {
        byte b = read0();

        switch( b )
        {
              case VARINT_0: return false;
              case VARINT_1: return true;
              default:
                    throw new IOException("Tag error, expect BYTE_TRUE|BYTE_FALSE, but get " + b);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readByte()
     */
    @Override
    public byte readByte() throws IOException {
        byte b = read0();

        switch( b )
        {
              case VARINT8:
                    return read0();
              case VARINT_0: return 0; case VARINT_1: return 1; case VARINT_2: return 2; case VARINT_3: return 3;
              case VARINT_4: return 4; case VARINT_5: return 5; case VARINT_6: return 6; case VARINT_7: return 7;
              case VARINT_8: return 8; case VARINT_9: return 9; case VARINT_A: return 10; case VARINT_B: return 11;
              case VARINT_C: return 12; case VARINT_D: return 13; case VARINT_E: return 14; case VARINT_F: return 15;
              case VARINT_10: return 16; case VARINT_11: return 17; case VARINT_12: return 18; case VARINT_13: return 19;
              case VARINT_14: return 20; case VARINT_15: return 21; case VARINT_16: return 22; case VARINT_17: return 23;
              case VARINT_18: return 24; case VARINT_19: return 25; case VARINT_1A: return 26; case VARINT_1B: return 27;
              case VARINT_1C: return 28; case VARINT_1D: return 29; case VARINT_1E: return 30; case VARINT_1F: return 31;
              default:
                    throw new IOException("Tag error, expect VARINT, but get " + b);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUnsignedByte()
     */
    @Override
    public int readUnsignedByte() throws IOException {
       throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readShort()
     */
    @Override
    public short readShort() throws IOException {
        return (short)readVarint32();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readInt()
     */
    @Override
    public int readInt() throws IOException {
        return readVarint32();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readLong()
     */
    @Override
    public long readLong() throws IOException {
        return readVarint64();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFloat()
     */
    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readVarint32());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readDouble()
     */
    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readVarint64());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUTF()
     */
    @Override
    public String readUTF() throws IOException {
        byte b = read0();

        switch( b )
        {
              case OBJECT_BYTES:
                    int len = readUInt();
                    StringBuilder sb = new StringBuilder();

                    for(int i=0;i<len;i++)
                    {
                          byte b1 = read0();
                          if( (b1 & 0x80) == 0 )
                          {
                                sb.append((char)b1);
                          }
                          else if( (b1 & 0xE0) == 0xC0 )
                          {
                                byte b2 = read0();
                                sb.append((char)(((b1 & 0x1F) << 6) | (b2 & 0x3F)));
                          }
                          else if( (b1 & 0xF0) == 0xE0 )
                          {
                                byte b2 = read0(), b3 = read0();
                                sb.append((char)(((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)));
                          }
                          else
                                throw new UTFDataFormatException("Bad utf-8 encoding at " + b1);
                    }
                    return sb.toString();
              case OBJECT_NULL: return null;
              case OBJECT_DUMMY: return EMPTY_STRING;
              default:
                    throw new IOException("Tag error, expect BYTES|BYTES_NULL|BYTES_EMPTY, but get " + b);
        }
    }
    public int readUInt() throws IOException
    {
          byte tmp = read0();
          if( tmp < 0 )
                return tmp & 0x7f;

          int ret = tmp & 0x7f;
          if( ( tmp = read0() ) < 0 )
          {
                ret |= ( tmp & 0x7f ) << 7;
          }
          else
          {
                ret |= tmp << 7;
                if( ( tmp = read0() ) < 0 )
                {
                      ret |= ( tmp & 0x7f ) << 14;
                }
                else
                {
                      ret |= tmp << 14;
                      if( ( tmp = read0() ) < 0 )
                      {
                        ret |= ( tmp & 0x7f ) << 21;
                      }
                      else
                      {
                            ret |= tmp << 21;
                            ret |= ( read0() & 0x7f ) << 28;
                      }
                }
          }
          return ret;
    }
    /**
     * {@inheritDoc}
     * 
     * @see java.io.ObjectInput#readObject()
     */
    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        String desc;
        byte b = read0();
        switch( b )
        {
              case OBJECT_NULL:
                    return null;
              case OBJECT_DUMMY:
                    return new Object();
              case OBJECT_DESC:
              {
                    desc = readUTF();
                    break;
              }
              case OBJECT_DESC_ID:
              {
                    int index = readUInt();
                    desc = mapper.getDescriptor(index);
                    if( desc == null )
                          throw new IOException("Can not find desc id: " + index );
                    break;
              }
              default:
                    throw new IOException("Flag error, expect OBJECT_NULL|OBJECT_DUMMY|OBJECT_DESC|OBJECT_DESC_ID, get " + b);
        }
       /* try
        {
              Class<?> c = ReflectUtils.desc2class(desc);
              return DescriptorBuilder.register(c).parseFrom(this);
        }
        catch(ClassNotFoundException e)
        {
              throw new IOException("Read object failed, class not found. " + StringUtils.toString(e));
        }*/
        return null;
    }


    private int readVarint32() throws IOException
    {
          byte b = read0();

          switch( b )
          {
                case VARINT8:
                      return read0();
                case VARINT16:
                {
                      byte b1 = read0(), b2 = read0();
                      return (short)( ( b1 & 0xff ) | ( ( b2 & 0xff ) << 8 ) );
                }
                case VARINT24:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0();
                      int ret = ( b1 & 0xff ) | ( ( b2 & 0xff ) << 8 ) | ( ( b3 & 0xff ) << 16 );
                      if( b3 < 0 )
                            return ret | 0xff000000;
                      return ret;
                }
                case VARINT32:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0();
                      return ( ( b1 & 0xff ) |
                            ( ( b2 & 0xff ) << 8 ) |
                            ( ( b3 & 0xff ) << 16 ) |
                            ( ( b4 & 0xff ) << 24 ) );
                }
                case VARINT_NF: return -15; case VARINT_NE: return -14; case VARINT_ND: return -13;
                case VARINT_NC: return -12; case VARINT_NB: return -11; case VARINT_NA: return -10; case VARINT_N9: return -9;
                case VARINT_N8: return -8; case VARINT_N7: return -7; case VARINT_N6: return -6; case VARINT_N5: return -5;
                case VARINT_N4: return -4; case VARINT_N3: return -3; case VARINT_N2: return -2; case VARINT_N1: return -1;
                case VARINT_0: return 0; case VARINT_1: return 1; case VARINT_2: return 2; case VARINT_3: return 3;
                case VARINT_4: return 4; case VARINT_5: return 5; case VARINT_6: return 6; case VARINT_7: return 7;
                case VARINT_8: return 8; case VARINT_9: return 9; case VARINT_A: return 10; case VARINT_B: return 11;
                case VARINT_C: return 12; case VARINT_D: return 13; case VARINT_E: return 14; case VARINT_F: return 15;
                case VARINT_10: return 16; case VARINT_11: return 17; case VARINT_12: return 18; case VARINT_13: return 19;
                case VARINT_14: return 20; case VARINT_15: return 21; case VARINT_16: return 22; case VARINT_17: return 23;
                case VARINT_18: return 24; case VARINT_19: return 25; case VARINT_1A: return 26; case VARINT_1B: return 27;
                case VARINT_1C: return 28; case VARINT_1D: return 29; case VARINT_1E: return 30; case VARINT_1F: return 31;
                default:
                      throw new IOException("Tag error, expect VARINT, but get " + b);
          }
    }

    private long readVarint64() throws IOException
    {
          byte b = read0();

          switch( b )
          {
                case VARINT8:
                      return read0();
                case VARINT16:
                {
                      byte b1 = read0(), b2 = read0();
                      return (short)( ( b1 & 0xff ) | ( ( b2 & 0xff ) << 8 ) );
                }
                case VARINT24:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0();
                      int ret = ( b1 & 0xff ) | ( ( b2 & 0xff ) << 8 ) | ( ( b3 & 0xff ) << 16 );
                      if( b3 < 0 )
                            return ret | 0xff000000;
                      return ret;
                }
                case VARINT32:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0();
                      return ( ( b1 & 0xff ) |
                            ( ( b2 & 0xff ) << 8 ) |
                            ( ( b3 & 0xff ) << 16 ) |
                            ( ( b4 & 0xff ) << 24 ) );
                }
                case VARINT40:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0(), b5 = read0();
                      long ret = ( (long)b1 & 0xff ) |
                            ( ( (long)b2 & 0xff ) << 8 ) |
                            ( ( (long)b3 & 0xff ) << 16 ) |
                            ( ( (long)b4 & 0xff ) << 24 ) |
                            ( ( (long)b5 & 0xff ) << 32 );
                      if( b5 < 0 )
                            return ret | 0xffffff0000000000l;
                      return ret;
                }
                case VARINT48:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0(), b5 = read0(), b6 = read0();
                      long ret = ( (long)b1 & 0xff ) |
                            ( ( (long)b2 & 0xff ) << 8 ) |
                            ( ( (long)b3 & 0xff ) << 16 ) |
                            ( ( (long)b4 & 0xff ) << 24 ) |
                            ( ( (long)b5 & 0xff ) << 32 ) |
                            ( ( (long)b6 & 0xff ) << 40 );
                      if( b6 < 0 )
                            return ret | 0xffff000000000000l;
                      return ret;
                }
                case VARINT56:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0(), b5 = read0(), b6 = read0(), b7 = read0();
                      long ret = ( (long)b1 & 0xff ) |
                            ( ( (long)b2 & 0xff ) << 8 ) |
                            ( ( (long)b3 & 0xff ) << 16 ) |
                            ( ( (long)b4 & 0xff ) << 24 ) |
                            ( ( (long)b5 & 0xff ) << 32 ) |
                            ( ( (long)b6 & 0xff ) << 40 ) |
                            ( ( (long)b7 & 0xff ) << 48 );
                      if( b7 < 0 )
                            return ret | 0xff00000000000000l;
                      return ret;
                }
                case VARINT64:
                {
                      byte b1 = read0(), b2 = read0(), b3 = read0(), b4 = read0();
                      byte b5 = read0(), b6 = read0(), b7 = read0(), b8 = read0();
                      return ( ( (long)b1 & 0xff ) |
                            ( ( (long)b2 & 0xff ) << 8 ) |
                            ( ( (long)b3 & 0xff ) << 16 ) |
                            ( ( (long)b4 & 0xff ) << 24 ) |
                            ( ( (long)b5 & 0xff ) << 32 ) |
                            ( ( (long)b6 & 0xff ) << 40 ) |
                            ( ( (long)b7 & 0xff ) << 48 ) |
                            ( ( (long)b8 & 0xff ) << 56 ) );
                }
                case VARINT_NF: return -15; case VARINT_NE: return -14; case VARINT_ND: return -13;
                case VARINT_NC: return -12; case VARINT_NB: return -11; case VARINT_NA: return -10; case VARINT_N9: return -9;
                case VARINT_N8: return -8; case VARINT_N7: return -7; case VARINT_N6: return -6; case VARINT_N5: return -5;
                case VARINT_N4: return -4; case VARINT_N3: return -3; case VARINT_N2: return -2; case VARINT_N1: return -1;
                case VARINT_0: return 0; case VARINT_1: return 1; case VARINT_2: return 2; case VARINT_3: return 3;
                case VARINT_4: return 4; case VARINT_5: return 5; case VARINT_6: return 6; case VARINT_7: return 7;
                case VARINT_8: return 8; case VARINT_9: return 9; case VARINT_A: return 10; case VARINT_B: return 11;
                case VARINT_C: return 12; case VARINT_D: return 13; case VARINT_E: return 14; case VARINT_F: return 15;
                case VARINT_10: return 16; case VARINT_11: return 17; case VARINT_12: return 18; case VARINT_13: return 19;
                case VARINT_14: return 20; case VARINT_15: return 21; case VARINT_16: return 22; case VARINT_17: return 23;
                case VARINT_18: return 24; case VARINT_19: return 25; case VARINT_1A: return 26; case VARINT_1B: return 27;
                case VARINT_1C: return 28; case VARINT_1D: return 29; case VARINT_1E: return 30; case VARINT_1F: return 31;
                default:
                      throw new IOException("Tag error, expect VARINT, but get " + b);
          }
    }
    protected byte read0() throws IOException
    {
          if( position == read )
                fillBuffer();
          return buffer[position++];
    }
    private void fillBuffer() throws IOException
    {
        position = 0;
        read = input.read(buffer);
          if( read == -1 )
          {
              read = 0;
                throw new EOFException();
          }
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFully(byte[])
     */
    @Override
    public void readFully(byte[] b) throws IOException {      
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        // TODO Auto-generated method stub
        
    }
  
    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readUnsignedShort()
     */
    @Override
    public int readUnsignedShort() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readChar()
     */
    @Override
    public char readChar() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.DataInput#readLine()
     */
    @Override
    public String readLine() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }
}
