/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.rs.generic.exchange;

import java.io.IOException;
import java.io.InputStream;

import org.solmix.commons.io.Bytes;
import org.solmix.exchange.Message;
import org.solmix.exchange.data.ObjectInput;
import org.solmix.exchange.data.Serialization;
import org.solmix.exchange.model.NamedID;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.RemoteMessage;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月29日
 */

public class HolaMessage extends RemoteMessage
{

    private static final long serialVersionUID = 3996983377716646392L;

    public HolaMessage()
    {
        super();
    }

    @Override
    protected void decodeBody(InputStream inStream, byte[] header) throws IOException {
        byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
        Serialization serialization = serializationManager.getSerializationById(proto);
        ObjectInput input = serialization.createObjectInput(info, inStream);

        long id = Bytes.bytes2long(header, 4);
        setId(id);
        // response
        if ((flag & FLAG_REQUEST) == 0) {
            // TODO
        } else {// request
            put(HOLA.HOLA_VERSION_KEY, HOLA.VERSION);
            put(Message.ONEWAY, (flag & FLAG_ONEWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                put(Message.EVENT_MESSAGE, true);
            }
            try {
                decodeRequestHeader(input);

            } catch (Throwable e) {
                setContent(Exception.class, e);
            }
        }
    }

  
    protected void decodeRequestHeader(ObjectInput input) throws IOException {
        put(HOLA.HOLA_VERSION_KEY, input.readUTF());
        put(Message.PATH_INFO, input.readUTF());
        put(HOLA.VERSION_KEY,input.readUTF());
        String operationId = input.readUTF();
        NamedID operationName = NamedID.formIdentityString(operationId);
        put(Message.OPERATION,operationName);
    }

}
