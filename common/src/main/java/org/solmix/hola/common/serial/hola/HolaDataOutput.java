package org.solmix.hola.common.serial.hola;

import java.io.IOException;
import java.io.OutputStream;

import org.solmix.hola.common.serial.DataOutput;


public class HolaDataOutput implements DataOutput, DataFlags
{
    private static final int CHAR_BUF_SIZE = 256;

    private final byte[] buffer, temp = new byte[9];

    private final char[] charBuf = new char[CHAR_BUF_SIZE];

    private final OutputStream output;

    private final int mLimit;

    private int position = 0;
    
    public HolaDataOutput(OutputStream out)
    {
          this(out, 1024);
    }

    public HolaDataOutput(OutputStream out, int buffSize)
    {
          output = out;
          mLimit = buffSize;
          buffer = new byte[buffSize];
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        write0( v ? VARINT_1 : VARINT_0 );
    }

    @Override
    public void writeByte(byte v) throws IOException {
        switch( v )
        {
              case 0: write0(VARINT_0); break; case 1: write0(VARINT_1); break; case 2: write0(VARINT_2); break; case 3: write0(VARINT_3); break;
              case 4: write0(VARINT_4); break; case 5: write0(VARINT_5); break; case 6: write0(VARINT_6); break; case 7: write0(VARINT_7); break;
              case 8: write0(VARINT_8); break; case 9: write0(VARINT_9); break; case 10: write0(VARINT_A); break; case 11: write0(VARINT_B); break;
              case 12: write0(VARINT_C); break; case 13: write0(VARINT_D); break; case 14: write0(VARINT_E); break; case 15: write0(VARINT_F); break;
              case 16: write0(VARINT_10); break; case 17: write0(VARINT_11); break; case 18: write0(VARINT_12); break; case 19: write0(VARINT_13); break;
              case 20: write0(VARINT_14); break; case 21: write0(VARINT_15); break; case 22: write0(VARINT_16); break; case 23: write0(VARINT_17); break;
              case 24: write0(VARINT_18); break; case 25: write0(VARINT_19); break; case 26: write0(VARINT_1A); break; case 27: write0(VARINT_1B); break;
              case 28: write0(VARINT_1C); break; case 29: write0(VARINT_1D); break; case 30: write0(VARINT_1E); break; case 31: write0(VARINT_1F); break;
              default:
                    write0(VARINT8);
                    write0(v);
        }
    }

    @Override
    public void writeShort(short v) throws IOException {
        writeVarint32(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        writeVarint32(v);

    }

    @Override
    public void writeLong(long v) throws IOException {
        writeVarint64(v);

    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeVarint32(Float.floatToRawIntBits(v));

    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeVarint64(Double.doubleToRawLongBits(v));

    }

    @Override
    public void writeUTF(String v) throws IOException {
        if( v == null )
        {
              write0(OBJECT_NULL);
        }
        else
        {
              int len = v.length();
              if( len == 0 )
              {
                    write0(OBJECT_DUMMY);
              }
              else
              {
                    write0(OBJECT_BYTES);
                    writeUInt(len);

                    int off = 0, limit = mLimit - 3, size;
                    char[] buf = charBuf;
                    do
                    {
                          size = Math.min(len-off, CHAR_BUF_SIZE);
                          v.getChars(off, off+size, buf, 0);

                          for(int i=0;i<size;i++)
                          {
                                char c = buf[i];
                                if( position > limit )
                                {
                                      if( c < 0x80 )
                                      {
                                            write0((byte)c);
                                      }
                                      else if( c < 0x800 )
                                      {
                                            write0((byte)(0xC0 | ((c >> 6) & 0x1F)));
                                            write0((byte)(0x80 | (c & 0x3F)));
                                      }
                                      else
                                      {
                                            write0((byte)(0xE0 | ((c >> 12) & 0x0F)));
                                            write0((byte)(0x80 | ((c >> 6) & 0x3F)));
                                            write0((byte)(0x80 | (c & 0x3F)));
                                      }
                                }
                                else
                                {
                                      if( c < 0x80 )
                                      {
                                            buffer[position++] = (byte)c;
                                      }
                                      else if( c < 0x800 )
                                      {
                                            buffer[position++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
                                            buffer[position++] = (byte)(0x80 | (c & 0x3F));
                                      }
                                      else
                                      {
                                            buffer[position++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
                                            buffer[position++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                                            buffer[position++] = (byte)(0x80 | (c & 0x3F));
                                      }
                                }
                          }
                          off += size;
                    }
                    while( off < len );
              }
        }
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if( v == null )
            write0(OBJECT_NULL);
      else
            writeBytes(v, 0, v.length);
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if( len == 0 )
        {
              write0(OBJECT_DUMMY);
        }
        else
        {
              write0(OBJECT_BYTES);
              writeUInt(len);
              write0(v, off, len);
        }

    }

    @Override
    public void flushBuffer() throws IOException
    {
          if( position > 0 )
          {
                output.write(buffer, 0, position);
                position = 0;
          }
    }
    public void writeUInt(int v) throws IOException
    {
          byte tmp;
          while( true )
          {
                tmp = (byte)( v & 0x7f );
                if( ( v >>>= 7 ) == 0 )
                {
                      write0( (byte)( tmp | 0x80 ) );
                      return;
                }
                else
                {
                      write0(tmp);
                }
          }
    }

    protected void write0(byte b) throws IOException
    {
          if( position == mLimit )
                flushBuffer();

          buffer[position++] = b;
    }

    protected void write0(byte[] b, int off, int len) throws IOException
    {
          int rem = mLimit - position;
          if( rem > len )
          {
                System.arraycopy(b, off, buffer, position, len);
                position += len;
          }
          else
          {
                System.arraycopy(b, off, buffer, position, rem);
                position = mLimit;
                flushBuffer();

                off += rem;
                len -= rem;

                if( mLimit > len )
                {
                      System.arraycopy(b, off, buffer, 0, len);
                      position = len;
                }
                else
                {
                      output.write(b, off, len);
                }
          }
    }

    private void writeVarint32(int v) throws IOException
    {
          switch( v )
          {
                case -15: write0(VARINT_NF); break; case -14: write0(VARINT_NE); break; case -13: write0(VARINT_ND); break;
                case -12: write0(VARINT_NC); break; case -11: write0(VARINT_NB); break; case -10: write0(VARINT_NA); break; case -9: write0(VARINT_N9); break;
                case -8: write0(VARINT_N8); break; case -7: write0(VARINT_N7); break; case -6: write0(VARINT_N6); break; case -5: write0(VARINT_N5); break;
                case -4: write0(VARINT_N4); break; case -3: write0(VARINT_N3); break; case -2: write0(VARINT_N2); break; case -1: write0(VARINT_N1); break;
                case 0: write0(VARINT_0); break; case 1: write0(VARINT_1); break; case 2: write0(VARINT_2); break; case 3: write0(VARINT_3); break;
                case 4: write0(VARINT_4); break; case 5: write0(VARINT_5); break; case 6: write0(VARINT_6); break; case 7: write0(VARINT_7); break;
                case 8: write0(VARINT_8); break; case 9: write0(VARINT_9); break; case 10: write0(VARINT_A); break; case 11: write0(VARINT_B); break;
                case 12: write0(VARINT_C); break; case 13: write0(VARINT_D); break; case 14: write0(VARINT_E); break; case 15: write0(VARINT_F); break;
                case 16: write0(VARINT_10); break; case 17: write0(VARINT_11); break; case 18: write0(VARINT_12); break; case 19: write0(VARINT_13); break;
                case 20: write0(VARINT_14); break; case 21: write0(VARINT_15); break; case 22: write0(VARINT_16); break; case 23: write0(VARINT_17); break;
                case 24: write0(VARINT_18); break; case 25: write0(VARINT_19); break; case 26: write0(VARINT_1A); break; case 27: write0(VARINT_1B); break;
                case 28: write0(VARINT_1C); break; case 29: write0(VARINT_1D); break; case 30: write0(VARINT_1E); break; case 31: write0(VARINT_1F); break;
                default:
                      int t = v, ix = 0;
                      byte[] b = temp;

                      while( true )
                      {
                            b[++ix] = (byte)( v & 0xff );
                            if( ( v >>>= 8 ) == 0 )
                                  break;
                      }

                      if( t > 0 )
                      {
                            // [ 0a e2 => 0a e2 00 ] [ 92 => 92 00 ]
                            if( b[ix] < 0 )
                                  b[++ix] = 0;
                      }
                      else
                      {
                            // [ 01 ff ff ff => 01 ff ] [ e0 ff ff ff => e0 ]
                            while( b[ix] == (byte)0xff && b[ix-1] < 0 )
                                  ix--;
                      }

                      b[0] = (byte)( VARINT + ix - 1 );
                      write0(b, 0, ix+1);
          }
    }

    private void writeVarint64(long v) throws IOException
    {
          int i = (int)v;
          if( v == i )
          {
                writeVarint32(i);
          }
          else
          {
                long t = v;
                int ix = 0;
                byte[] b = temp;

                while( true )
                {
                      b[++ix] = (byte)( v & 0xff );
                      if( ( v >>>= 8 ) == 0 )
                            break;
                }

                if( t > 0 )
                {
                      // [ 0a e2 => 0a e2 00 ] [ 92 => 92 00 ]
                      if( b[ix] < 0 )
                            b[++ix] = 0;
                }
                else
                {
                      // [ 01 ff ff ff => 01 ff ] [ e0 ff ff ff => e0 ]
                      while( b[ix] == (byte)0xff && b[ix-1] < 0 )
                            ix--;
                }

                b[0] = (byte)( VARINT + ix - 1 );
                write0(b, 0, ix+1);
          }
    }
}
