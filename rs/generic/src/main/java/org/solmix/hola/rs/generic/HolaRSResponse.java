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

package org.solmix.hola.rs.generic;

import java.io.IOException;
import java.io.ObjectInput;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.rs.RSRequest;
import org.solmix.hola.rs.support.RSResponseImpl;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.codec.Decodeable;
import org.solmix.hola.transport.exchange.Response;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年8月25日
 */

public class HolaRSResponse extends RSResponseImpl implements Decodeable
{

    private static final long serialVersionUID = -3494785915968389559L;

    private final Channel channel;

    private final Response response;

    private final ObjectInput input;

    private final RSRequest request;
    private volatile boolean decoded;
    public HolaRSResponse(Channel channel, Response res, ObjectInput in,
        RSRequest requestData)
    {
        this.channel = channel;
        this.response = res;
        this.input = in;
        this.request = requestData;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Decodeable#decode()
     */
    @Override
    public void decode() throws Exception {
        if (!HolaRSResponse.this.decoded && channel != null && input != null) {
      
        try{
            byte flag = input.readByte();            
        switch (flag) {
            case HolaCodec.RESPONSE_NULL_VALUE:
                break;
            case HolaCodec.RESPONSE_VALUE:
                try{
                   setValue( input.readObject());
                }catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString( e)); 
                }
                break;
            case HolaCodec.RESPONSE_WITH_EXCEPTION:
                try {
                    Object obj = input.readObject();
                    if (obj instanceof Throwable == false)
                        throw new IOException("Response data error, expect Throwable, but get " + obj);
                    setException((Throwable) obj);
                } catch (ClassNotFoundException e) {
                    throw new IOException("Read response data failed.", e);
                }
                break;
            default:
                throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
        }
        }catch(Exception  e){
            response.setStatus(Response.CLIENT_ERROR);
            response.setErrorMessage(StringUtils.toString(e));
        }finally{
            HolaRSResponse.this.decoded=true;
        }
        }
    }

}
