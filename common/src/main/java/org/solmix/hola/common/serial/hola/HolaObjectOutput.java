
package org.solmix.hola.common.serial.hola;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.commons.util.ClassDescUtils;
import org.solmix.hola.common.serial.ObjectOutput;

public class HolaObjectOutput extends HolaDataOutput implements ObjectOutput
{

    private ClassDescriptorMapper mapper;

    private Map<Object, Integer> refs = new ConcurrentHashMap<Object, Integer>();

    public HolaObjectOutput(OutputStream os)
    {
        this(os, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    public HolaObjectOutput(OutputStream os, ClassDescriptorMapper mapper)
    {
        super(os);
        this.mapper = mapper;
    }

    public HolaObjectOutput(OutputStream os, int buffersize)
    {
        this(os, buffersize, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    public HolaObjectOutput(OutputStream os, int buffersize, ClassDescriptorMapper mapper)
    {
        super(os, buffersize);
        this.mapper = mapper;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if( obj == null )
        {
              write0(OBJECT_NULL);
              return;
        }
        
        Class<?> c = obj.getClass();
        if( c == Object.class )
        {
              write0(OBJECT_DUMMY);
        }
        else
        {
              String desc = ClassDescUtils.getTypeDesc(c);
              int index = mapper.getDescriptorIndex(desc);
              if( index < 0 )
              {
                    write0(OBJECT_DESC);
                    writeUTF(desc);
              }
              else
              {
                    write0(OBJECT_DESC_ID);
                    writeUInt(index);
              }
              Builder b = Builder.register(c, false);
              b.writeTo(obj, this);
        }
    }
    public void addRef(Object obj)
    {
          refs.put(obj, refs.size());
    }

    public int getRef(Object obj)
    {
          Integer ref = refs.get(obj);
          if( ref == null )
                return -1;
          return ref.intValue();
    }
}
