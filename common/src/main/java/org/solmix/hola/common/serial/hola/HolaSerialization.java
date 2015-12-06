package org.solmix.hola.common.serial.hola;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.solmix.hola.common.serial.ObjectInput;
import org.solmix.hola.common.serial.ObjectOutput;
import org.solmix.hola.common.serial.SerialConfiguration;
import org.solmix.hola.common.serial.Serialization;


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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectInput createObjectInput(SerialConfiguration info, InputStream input) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
