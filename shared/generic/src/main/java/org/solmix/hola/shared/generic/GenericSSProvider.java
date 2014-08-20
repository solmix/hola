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

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.hola.core.AbstractConnectContext;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.identity.support.StringID;
import org.solmix.hola.shared.SharedConnetor;
import org.solmix.hola.shared.SharedMessage;
import org.solmix.hola.shared.SharedMessageSerializer;
import org.solmix.hola.shared.SharedService;
import org.solmix.hola.shared.SharedServiceAddException;
import org.solmix.hola.shared.SharedServiceConnectException;
import org.solmix.hola.shared.SharedServiceDisconnectException;
import org.solmix.hola.shared.SharedServiceProvider;
import org.solmix.hola.shared.SharedServiceProviderConfig;
import org.solmix.hola.shared.SharedTransaction;
import org.solmix.hola.shared.event.SharedMessageSendingEvent;
import org.solmix.hola.shared.generic.member.Member;
import org.solmix.hola.shared.generic.member.MemberObserver;
import org.solmix.hola.shared.generic.serialize.GenericMessageSerializer;
import org.solmix.hola.shared.transport.Channel;
import org.solmix.runtime.event.Event;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月17日
 */

public abstract class GenericSSProvider extends AbstractConnectContext
    implements SharedServiceProvider
{

    protected final SharedServiceProviderConfig config;

    protected final MemberObserver observer;

    protected ThreadGroup providerThreadGroup = null;

    private long sequenceNumber = 0L;

    private SharedMessageSerializer messageSerializer;

    private final Vector<SharedConnetor> connectors = null;

    protected boolean isClosing=false;
    
    protected Logger log= LoggerFactory.getLogger(this.getClass().getName());
    
    public GenericSSProvider(SharedServiceProviderConfig config)
    {
        this.config = config;
        Assert.isNotNull(config);
        observer = new MemberObserver(this, new Member(config.getID()));
        providerThreadGroup = new ThreadGroup(getID() + "::ThreadGroup");
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getRemoteNamespace()
     */
    @Override
    public Namespace getRemoteNamespace() {
        return IDFactory.getDefault().getNamespaceByName(
            StringID.class.getName());
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.identity.Identifiable#getID()
     */
    @Override
    public ID getID() {
        return config.getID();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#addService(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.shared.SharedService, java.util.Map)
     */
    @Override
    public ID addSharedService(ID sharedServiceID, SharedService sharedService,
        Map<String, Object> properties) throws SharedServiceAddException {
        if (sharedService == null || sharedServiceID == null)
            throw new SharedServiceAddException("Id or service is null");
        addServiceAndWait(sharedServiceID, sharedService, properties);

        // XXX FIRE SharedServiceAddEvent.
        return sharedServiceID;
    }

    /**
     * 
     */
    protected void addServiceAndWait(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties)
        throws SharedServiceAddException {
        SharedTransaction transaction = addService0(sharedServiceID,
            sharedService, properties);
        if (transaction != null) {
            transaction.waitToCommit();
        }

    }

    protected SharedTransaction addService0(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties)
        throws SharedServiceAddException {
        GenericSSHolder holer = createHolder(sharedServiceID, sharedService,
            properties);
        SharedTransaction transaction = null;
        synchronized (getOberserLock()) {
            final GenericSSHolder obj = observer.getFromAny(sharedServiceID);
            if (obj != null) {
                throw new SharedServiceAddException("service " + obj
                    + "already in");
            }
            try {
                holer.init();
            } catch (Exception e) {
                throw new SharedServiceAddException(e);
            }
            transaction = sharedService.adaptTo(SharedTransaction.class);
            observer.addServiceToActive(holer);
        }
        return transaction;

    }

    protected GenericSSHolder createHolder(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties) {
        return new GenericSSHolder(createServiceConfig(sharedServiceID,
            sharedService, properties), sharedService, this);
    }

    private GenericSSConfig createServiceConfig(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties) {
        return new GenericSSConfig(sharedServiceID, getID(), this, properties);
    }

    protected Object getOberserLock() {
        return observer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#connectService(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.identity.ID)
     */
    @Override
    public SharedConnetor connectSharedService(ID sendId, ID[] receiveIds)
        throws SharedServiceConnectException {
        if (sendId == null)
            throw new SharedServiceConnectException("Sender Id is not null");
        if (receiveIds == null)
            throw new SharedServiceConnectException("Recieve  Ids is not null");
        Hashtable<ID, Queue<Event>> queues = new Hashtable<ID, Queue<Event>>();
        SharedConnetor __return = null;
        synchronized (getOberserLock()) {
            GenericSSHolder holder = getSharedServiceHolder(sendId);
            if (holder == null)
                throw new SharedServiceConnectException("Sender Id is not null");
            for (ID id : receiveIds) {
                if (holder == null) {
                    throw new SharedServiceConnectException("Reciever :"
                        + id.getName() + " not found");
                }
                queues.put(id, holder.getQueue());
            }
            __return = new GenericSharedConnetor(sendId, queues);
            addConnector(__return);
            // XXX fire SharedServiceConnectEvent();
        }
        return __return;
    }

    protected void addConnector(SharedConnetor conn) {
        connectors.add(conn);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#disconnectService(org.solmix.hola.shared.SharedConnetor)
     */
    @Override
    public void disconnectSharedService(SharedConnetor connetor)
        throws SharedServiceDisconnectException {
        if (connetor == null)
            throw new SharedServiceDisconnectException(
                "SharedService connector can't be null");

        if (connectors.remove(connetor)) {
            throw new SharedServiceDisconnectException(" connector " + connetor
                + " can't found");
        }
        connetor.dispose();
        // XXX fire SharedServiceDisconnectEvent.
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#getService(org.solmix.hola.core.identity.ID)
     */
    @Override
    public SharedService getSharedService(ID sharedServiceID) {
        GenericSSHolder holder = getSharedServiceHolder(sharedServiceID);
        return holder == null ? null : holder.sharedService;
    }

    /**
     * @param sharedServiceID
     * @return
     */
    private GenericSSHolder getSharedServiceHolder(ID sharedServiceID) {
        return observer.getFromActive(sharedServiceID);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#removeService(org.solmix.hola.core.identity.ID)
     */
    @Override
    public SharedService removeSharedService(ID sharedServiceID) {
        // XXX fire SharedServiceRemoveEvent.
        synchronized (getOberserLock()) {
            GenericSSHolder holder = observer.getFromActive(sharedServiceID);
            if (holder == null) {
                return null;
            }
            observer.removeSharedService(sharedServiceID);
            return holder.sharedService;
        }
    }

    /**
     * @param serviceID
     */
    public void notifyServiceActivated(ID serviceID) {
        synchronized (getOberserLock()) {
            observer.notigyOthresActivated(serviceID);
            // XXX fire SharedServiceActivateEvent
        }

    }

    /**
     * @param serviceID
     */
    public void notifyServiceDeactivated(ID serviceID) {
        synchronized (getOberserLock()) {
            observer.notigyOthresDeactivated(serviceID);
            // XXX fire SharedServiceDeactivateEvent
        }
    }

    /**
     * @param serviceID
     * @param runnable
     */
    public Thread getNewServiceThread(ID serviceID, Runnable runnable) {
        return new Thread(providerThreadGroup, runnable, serviceID.getName()
            + "::Thread");

    }

    /**
     * @param genericServiceConfig
     * @param queue
     * @return
     */
    public GenericSSContext createSharedContext(GenericSSConfig config,
        Queue<Event> queue) {

        return new GenericSSContext(config.getSharedServiceID(),
            config.getHomeProviderID(), this, config.getProperties(), queue);
    }

    /**
     * @param target
     * @param serviceID
     * @param message
     */
    public void sendMessage(ID target, ID fromServiceID, SharedMessage message)
        throws IOException {
        fireConnectEvent(new SharedMessageSendingEvent(this, getID(), target,
            fromServiceID, message));
        final byte[] data = serializeSharedMessage(fromServiceID, message);
        sendSharedMessage(target, fromServiceID, data);

    }

    /**
     * @param target
     * @param serviceID
     * @param message
     */
    private void sendSharedMessage(ID target, ID fromServiceID,
        Serializable message) throws IOException {
        sendPacket(SharedDataPacket.createPacket(getID(), target,
            getNextSequenceNumber(), fromServiceID, message));
    }

    /**
     * @param createPacket
     */
    private void sendPacket(SharedDataPacket createPacket) throws IOException {
        synchronized (getOberserLock()) {
            final ID ourId = getID();
            if (!ourId.equals(createPacket.getToID()))
                queueDataPacket(createPacket);
        }

    }

    /**
     * @param createPacket
     */
    protected abstract void queueDataPacket(SharedDataPacket createPacket)
        throws IOException;

    protected long getNextSequenceNumber() {
        if (sequenceNumber == Long.MAX_VALUE) {
            sequenceNumber = 0;
            return sequenceNumber;
        }
        return sequenceNumber++;
    }

    /**
     * @param serviceID
     * @param message
     * @return
     * @throws IOException
     */
    protected byte[] serializeSharedMessage(ID serviceID, SharedMessage message)
        throws IOException {
        return getMessageSerializer().serialize(serviceID, message);
    }

    /**
     * @return the messageSerialier
     */
    public SharedMessageSerializer getMessageSerializer() {
        if (messageSerializer == null)
            messageSerializer = new GenericMessageSerializer(this);
        return messageSerializer;
    }

    /**
     * @param messageSerialier the messageSerialier to set
     */
    @Override
    public void setMessageSerializer(SharedMessageSerializer messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    /**
     * @return
     */
    public ID[] getServiceIDs() {
        return observer.getServiceIDs();
    }

    /**
     * @param removeID
     */
    public void destroySharedService(ID removeID) {
        observer.removeSharedService(removeID);
    }
    
    @Override
    public void destroy(){
        isClosing = true;
        if(observer!=null){
            observer.removeAllMembers();
        }
    }
    protected void disconnect(Channel conn) {
        if (conn != null && conn.isConnected())
            conn.disconnect();
    }

    protected void handleLeave(ID leaveID, Channel conn) {
        if (leaveID == null)
            return;
      if (observer.removeMember(leaveID)) {
            try {
                  forwardExcluding(getID(), leaveID, SharedDataPacket.createViewChangePacket(getID(), null, getNextSequenceNumber(), new ID[] {leaveID}, false, null));
            } catch (final IOException e) {
            }
      }
      if (conn != null)
            disconnect(conn);
        
    }
    protected boolean addNewRemoteMember(ID memberID, Object data) {
        return observer.addMember(new Member(memberID, data));
  }
    abstract protected void forwardExcluding(ID from, ID excluding, SharedDataPacket data) throws IOException;

}
