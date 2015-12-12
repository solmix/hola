package org.solmix.hola.common.serial.hola;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.util.ClassDescUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.serial.ObjectInput;


public class HolaObjectInput extends HolaDataInput implements ObjectInput
{
    private static Object SKIPPED_OBJECT = new Object();
    private List<Object> refs = new ArrayList<Object>();
    private ClassDescriptorMapper mapper;
    public HolaObjectInput(InputStream is)
    {
        this(is,Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }
    public HolaObjectInput(InputStream is, ClassDescriptorMapper mapper)
    {
        super(is);
        this.mapper= mapper;
    }
    
    public HolaObjectInput(InputStream is,int buffersize, ClassDescriptorMapper mapper)
    {
        super(is,buffersize);
        this.mapper= mapper;
    }
    public HolaObjectInput(InputStream is,int buffersize)
    {
        this(is,buffersize,Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    @Override
    public Object readObject() throws IOException {
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
        try
        {
              Class<?> c = ClassDescUtils.desc2class(desc);
              return Builder.register(c).parseFrom(this);
        }
        catch(ClassNotFoundException e)
        {
              throw new IOException("Read object failed, class not found. " + StringUtils.toString(e));
        }
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T)readObject();
    }
    public void addRef(Object obj)
    {
          refs.add(obj);
    }

    public Object getRef(int index) throws IOException
    {
          if( index < 0 || index >= refs.size() )
                return null;

          Object ret = refs.get(index);
          if( ret == SKIPPED_OBJECT )
                throw new IOException("Ref skipped-object.");
          return ret;
    }

    public void skipAny() throws IOException
    {
          byte b = read0();
          switch( b )
          {
                case VARINT_NF: case VARINT_NE: case VARINT_ND: case VARINT_NC: case VARINT_NB: case VARINT_NA: case VARINT_N9:
                case VARINT_N8: case VARINT_N7: case VARINT_N6: case VARINT_N5: case VARINT_N4: case VARINT_N3: case VARINT_N2: case VARINT_N1:
                case VARINT_0: case VARINT_1: case VARINT_2: case VARINT_3: case VARINT_4: case VARINT_5: case VARINT_6: case VARINT_7:
                case VARINT_8: case VARINT_9: case VARINT_A: case VARINT_B: case VARINT_C: case VARINT_D: case VARINT_E: case VARINT_F:
                case VARINT_10: case VARINT_11: case VARINT_12: case VARINT_13: case VARINT_14: case VARINT_15: case VARINT_16: case VARINT_17:
                case VARINT_18: case VARINT_19: case VARINT_1A: case VARINT_1B: case VARINT_1C: case VARINT_1D: case VARINT_1E: case VARINT_1F:
                case OBJECT_NULL: case OBJECT_DUMMY:
                      break;
                case VARINT8:
                {
                      read0();
                      break;
                }
                case VARINT16:
                {
                      read0(); read0();
                      break;
                }
                case VARINT24:
                {
                      read0(); read0(); read0();
                      break;
                }
                case VARINT32:
                {
                      read0(); read0(); read0(); read0();
                      break;
                }
                case VARINT40:
                {
                      read0(); read0(); read0(); read0(); read0();
                      break;
                }
                case VARINT48:
                {
                      read0(); read0(); read0(); read0(); read0(); read0();
                      break;
                }
                case VARINT56:
                {
                      read0(); read0(); read0(); read0(); read0(); read0(); read0();
                      break;
                }
                case VARINT64:
                {
                      read0(); read0(); read0(); read0(); read0(); read0(); read0(); read0();
                      break;
                }
                case OBJECT:
                {
                      addRef(SKIPPED_OBJECT);
                      int len = readUInt();
                      for(int i=0;i<len;i++)
                            skipAny();
                      break;
                }
                case OBJECT_REF:
                {
                      readUInt();
                      break;
                }
                case OBJECT_STREAM: case OBJECT_BYTES:
                {
                      read0(readUInt());
                      break;
                }
                case OBJECT_VALUE:
                {
                      skipAny();
                      break;
                }
                case OBJECT_VALUES:
                {
                      int len = readUInt();
                      for(int i=0;i<len;i++)
                            skipAny();
                      break;
                }
                case OBJECT_MAP:
                {
                      int len = readUInt();
                      for(int i=0;i<len;i++)
                      {
                            skipAny(); // skip key
                            skipAny(); // skip value
                      }
                      break;
                }
                case OBJECT_DESC:
                {
                      readUTF();
                      int len = readUInt();
                      for(int i=0;i<len;i++)
                            skipAny();
                      break;
                }
                case OBJECT_DESC_ID:
                {
                      readUInt();
                      int len = readUInt();
                      for(int i=0;i<len;i++)
                            skipAny();
                      break;
                }
                default:
                      throw new IOException("Flag error, get " + b);
          }
    }
}
