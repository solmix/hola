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
import java.util.Map;
import java.util.Queue;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.AbstractConnectContext;
import org.solmix.hola.core.identity.DefaultIDFactory;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.identity.Namespace;
import org.solmix.hola.core.identity.support.StringID;
import org.solmix.hola.shared.SharedConnetor;
import org.solmix.hola.shared.SharedMessage;
import org.solmix.hola.shared.SharedMessageSerializer;
import org.solmix.hola.shared.SharedService;
import org.solmix.hola.shared.SharedServiceAddException;
import org.solmix.hola.shared.SharedServiceConnectException;
import org.solmix.hola.shared.SharedServiceProvider;
import org.solmix.hola.shared.SharedServiceProviderConfig;
import org.solmix.hola.shared.SharedTransaction;
import org.solmix.hola.shared.event.SharedMessageSendingEvent;
import org.solmix.hola.shared.generic.member.Member;
import org.solmix.hola.shared.generic.member.MemberObserver;
import org.solmix.hola.shared.generic.serialize.GenericMessageSerializer;
import org.solmix.runtime.Event;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月17日
 */

public abstract class GenericProvider extends AbstractConnectContext implements
    SharedServiceProvider
{

    protected final SharedServiceProviderConfig config;

    private final MemberObserver observer;

    protected ThreadGroup providerThreadGroup = null;

    private long sequenceNumber = 0L;

    public GenericProvider(SharedServiceProviderConfig config)
    {
        this.config = config;
        Assert.isNotNull(config);
        observer = new MemberObserver(this, new Member(config.getID()));
        providerThreadGroup = new ThreadGroup(getID() + "::ThreadGroup");
    }

    private SharedMessageSerializer messageSerializer;

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getRemoteNamespace()
     */
    @Override
    public Namespace getRemoteNamespace() {
        return DefaultIDFactory.getDefault().getNamespaceByName(
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
    public ID addService(ID sharedServiceID, SharedService sharedService,
        Map<String, Object> properties) throws SharedServiceAddException {
        if (sharedService == null || sharedServiceID == null)
            throw new SharedServiceAddException("Id or service is null");
        addServiceAndWait(sharedServiceID, sharedService, properties);

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
        GenericWrapper wrapper = createWrapper(sharedServiceID, sharedService,
            properties);
        SharedTransaction transaction = null;
        synchronized (getOberserLock()) {
            final GenericWrapper obj = observer.getFromAny(sharedServiceID);
            if (obj != null) {
                throw new SharedServiceAddException("service " + obj
                    + "already in");
            }
            try {
                wrapper.init();
            } catch (Exception e) {
                throw new SharedServiceAddException(e);
            }
            transaction = sharedService.adaptTo(SharedTransaction.class);
            observer.addServiceToActive(wrapper);
        }
        return transaction;

    }

    protected GenericWrapper createWrapper(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties) {
        return new GenericWrapper(createServiceConfig(sharedServiceID,
            sharedService, properties), sharedService, this);
    }

    private GenericServiceConfig createServiceConfig(ID sharedServiceID,
        SharedService sharedService, Map<String, Object> properties) {
        return new GenericServiceConfig(sharedServiceID, getID(), this,
            properties);
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
    public SharedConnetor connectService(ID fromID, ID toID)
        throws SharedServiceConnectException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#disconnectService(org.solmix.hola.shared.SharedConnetor)
     */
    @Override
    public void disconnectService(SharedConnetor channel) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#getService(org.solmix.hola.core.identity.ID)
     */
    @Override
    public SharedService getService(ID sharedServiceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedServiceProvider#removeService(org.solmix.hola.core.identity.ID)
     */
    @Override
    public SharedService removeService(ID sharedServiceID) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param serviceID
     */
    public void notifyServiceActivated(ID serviceID) {
        // TODO Auto-generated method stub

    }

    /**
     * @param serviceID
     */
    public void notifyServiceDeactivated(ID serviceID) {
        // TODO Auto-generated method stub

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
    public GenericContext createSharedContext(GenericServiceConfig config,
        Queue<Event> queue) {

        return new GenericContext(config.getSharedServiceID(),
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
    public void setMessageSerializer(SharedMessageSerializer messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    /**
     * @return
     */
    public ID[] getServiceIDs() {
        return observer.getServiceIDs();
    }

}
