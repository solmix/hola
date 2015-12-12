package org.solmix.hola.common.serial.hola;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.solmix.hola.common.serial.ObjectInput;
import org.solmix.hola.common.serial.ObjectOutput;
import org.solmix.hola.common.serial.SerialConfiguration;
import org.solmix.hola.common.serial.Serialization;
import org.solmix.runtime.Extension;

@Extension(name="hola")
public class HolaSerialization implements Serialization
{

    @Override
    public byte getContentTypeId() {
        return 1;
    }

    @Override
    public String getContentType() {
        return "x-application/hola";
    }

    @Override
    public ObjectOutput createObjectOutput(SerialConfiguration info, OutputStream output) throws IOException {
        return new HolaObjectOutput(output);
    }

    @Override
    public ObjectInput createObjectInput(SerialConfiguration info, InputStream input) throws IOException {
        return new HolaObjectInput(input);
    }

}
