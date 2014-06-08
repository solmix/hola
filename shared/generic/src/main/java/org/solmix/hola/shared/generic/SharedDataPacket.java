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

package org.solmix.hola.shared.generic;

import java.io.Serializable;

import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月20日
 */

public class SharedDataPacket implements java.io.Serializable
{

    private static final long serialVersionUID = -5005293732385815925L;

    private ID fromID;

    private ID toID;

    private long sequence;

    private Serializable data;

    /**
     * @return the fromID
     */
    public ID getFromID() {
        return fromID;
    }

    /**
     * @param fromID the fromID to set
     */
    public void setFromID(ID fromID) {
        this.fromID = fromID;
    }

    /**
     * @return the toID
     */
    public ID getToID() {
        return toID;
    }

    /**
     * @param toID the toID to set
     */
    public void setToID(ID toID) {
        this.toID = toID;
    }

    /**
     * @return the sequence
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    /**
     * @return the data
     */
    public Serializable getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Serializable data) {
        this.data = data;
    }

    protected SharedDataPacket(ID fromId, ID toId, long seq, Serializable data)
    {
        this.fromID = fromId;
        this.toID = toId;
        this.sequence = seq;
        this.data = data;
    }

    /**
     * @return
     */
    public static SharedDataPacket createPacket(ID fromId, ID toId, long seq, ID serviceID,Serializable data) {
        return new SharedDataPacket(fromId,toId,seq,new MessagePacket(serviceID,data));
    }
    public static final class MessagePacket implements java.io.Serializable{
        private static final long serialVersionUID = -473853437731777672L;
        private final Serializable data;
        private final ID fromSharedObjectID;

        MessagePacket(ID fromSharedObject, Serializable data) {
              this.fromSharedObjectID = fromSharedObject;
              this.data = data;
        }

        @Override
        public String toString() {
              final StringBuffer sb = new StringBuffer("SharedObjectMessage["); 
              sb.append(fromSharedObjectID).append(";").append(data).append("]");
              return sb.toString();
        }

        /**
         * @return Returns the data.
         */
        public Serializable getData() {
              return data;
        }

        /**
         * @return Returns the fromSharedObjectID.
         */
        public ID getFromSharedObjectID() {
              return fromSharedObjectID;
        }
        
    }
}


