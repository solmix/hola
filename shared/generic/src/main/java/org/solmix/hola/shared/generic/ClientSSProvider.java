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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.ConnectException;
import org.solmix.hola.core.HolaException;
import org.solmix.hola.core.event.ConnectedEvent;
import org.solmix.hola.core.event.ConnectingEvent;
import org.solmix.hola.core.event.DisconnectedEvent;
import org.solmix.hola.core.event.DisconnectingEvent;
import org.solmix.hola.core.event.EjectedConnectEvent;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.core.security.Callback;
import org.solmix.hola.core.security.CallbackHandler;
import org.solmix.hola.core.security.ConnectSecurityContext;
import org.solmix.hola.core.security.UnsupportedCallbackException;
import org.solmix.hola.shared.SharedServiceProviderConfig;
import org.solmix.hola.shared.generic.support.Channel;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年6月23日
 */

public abstract class ClientSSProvider extends GenericSSProvider
{

    public static final int DEFAULT_CONNECT_TIMEOUT = 30000;

    protected byte connectionState;

    protected ID remoteServerID;

    public static final byte DISCONNECTED = 0;

    public static final byte CONNECTING = 1;

    public static final byte CONNECTED = 2;

    protected Object connectLock = new Object();

    protected Channel connection;

    /**
     * @param config
     */
    public ClientSSProvider(SharedServiceProviderConfig config)
    {
        super(config);
        connectionState = DISCONNECTED;
    }

    @Override
    public void destroy() {
        synchronized (connectLock) {
            isClosing = true;
            // 如果还是连接状态,关闭它
            if (isConnected()) {
                disconnect();
            } else {
                makeStateDisconnected(connection);
            }
        }
        super.destroy();
    }

    /**
     * 
     */
    private void makeStateDisconnected(Channel conn) {
        disconnect(conn);
        connection = null;
        connectionState = DISCONNECTED;
        remoteServerID = null;

    }

    /**
     * @return
     */
    private boolean isConnected() {
        return (connectionState == CONNECTED);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#connect(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.security.ConnectSecurityContext)
     */
    @Override
    public void connect(ID remoteID, ConnectSecurityContext securityContext)
        throws ConnectException {
        try {
            if (isClosing) {
                throw new IllegalStateException(
                    "Connection is closed");
            }
            Assert.isNotNull(remoteID);
            Object response = null;
            synchronized (connectLock) {
                if (isConnected())
                    throw new IllegalStateException(
                        "Container already connected connectedID="
                            + getTargetID());
                if (isConnecting())
                    throw new IllegalStateException("Container is connecting");
                final Channel channel = createChannel(remoteID,securityContext);
                makeStateConnecting(channel);
                fireConnectEvent(new ConnectingEvent(this, getID(), remoteID,
                    securityContext));

                final Object connectData = getConnectData(remoteID,
                    securityContext);
                final int connectTimeout = getConnectTimeout();
                synchronized (channel) {
                    try {
                        response =  channel.connect(remoteID, connectData, connectTimeout);
                    } catch (HolaException e) {
                        if (this.connection != channel)
                            disconnect(channel);
                        else
                            makeStateDisconnected(channel);
                        throw e;
                    }
                    if (this.connection != channel) {
                        disconnect(channel);
                        throw new IllegalStateException(
                            "Container connect failed because not in correct state");
                    }
                    ID serverID = null;
                    try {
                        serverID = handleConnectResponse(remoteID, response);
                    } catch (final Exception e) {
                        makeStateDisconnected(channel);
                        throw e;
                    }
                    makeStateConnected(serverID, channel);
                    // notify listeners
                    fireConnectEvent(new ConnectedEvent(this, this.getID(),
                        remoteServerID));
                    channel.start();
                }
            }

        } catch (HolaException e) {
            throw new ConnectException("ClientSSProvider Hola exception",e);
        }catch(Exception ee){
            throw new ConnectException("ClientSSProvider exception",ee);
        }
    }

   
    /**
     * @param remoteID
     * @param response
     * @return
     * @throws Exception 
     */
    private ID handleConnectResponse(ID remoteID, Object response) throws Exception {
        final SharedDataPacket aPacket = (SharedDataPacket) response;
        final ID fromID = aPacket.getFromID();
        Assert.isNotNull(fromID, "fromID cannot be null"); 
        final SharedDataPacket.ViewChangePacket viewChangeMessage = ( SharedDataPacket.ViewChangePacket  ) aPacket.getData();
        // If it's not an add message then we've been refused. Get exception
        // info from viewChangeMessage and
        // throw if there
        if (!viewChangeMessage.isAdd()) {
              // We were refused by server...so we retrieve data and throw
              final Object data = viewChangeMessage.getData();
              if (data != null && data instanceof Exception)
                    throw (Exception) data;
              throw new InvalidObjectException("Invalid server response"); 
        }
        // Otherwise everything is OK to this point and we get the group member
        // IDs from server
        final ID[] ids = viewChangeMessage.getChangeIDs();
        Assert.isNotNull(ids, "view change ids cannot be null"); 
        for (int i = 0; i < ids.length; i++) {
              final ID id = ids[i];
              if (id != null && !id.equals(getID()))
                    addNewRemoteMember(id, null);
        }
        return fromID;
    }

    /**
     * @param remoteID
     * @param securityContext
     * @return
     * @throws UnsupportedCallbackException 
     * @throws IOException 
     */
    private Object getConnectData(ID remoteID,
        ConnectSecurityContext securityContext) throws IOException, UnsupportedCallbackException {
        final Callback[] callbacks = createAuthorizationCallbacks();
        if (securityContext != null) {
              final CallbackHandler handler = securityContext.getCallbackHandler();
              if (handler != null)
                    handler.onHandle(callbacks);
        }
        return SharedDataPacket.createJoinGroupPacket(getID(),remoteID,getNextSequenceNumber(),(Serializable)null);
    }

    /**
     * @return
     */
    protected Callback[] createAuthorizationCallbacks() {
        return null;
    }

    protected abstract Channel createChannel(ID remoteID,
        ConnectSecurityContext securityContext) ;

    /**
     * @param serverID
     * @param sconn
     */
    private void makeStateConnected(ID serverID, Channel sconn) {
        connectionState = CONNECTED;
        connection = sconn;
        remoteServerID = serverID;
    }

    /**
     * @return
     */
    protected int getConnectTimeout() {
        return DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * @param sconn
     */
    private void makeStateConnecting(Channel conn) {
        connectionState = CONNECTING;
        connection = conn;

    }

    protected boolean isConnecting() {
        return (connectionState == CONNECTING);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#getTargetID()
     */
    @Override
    public ID getTargetID() {
        return remoteServerID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.ConnectContext#disconnect()
     */
    @Override
    public void disconnect() {
        disconnect((Throwable) null);
    }

    protected void disconnect(Throwable exception) {
        synchronized (connectLock) {
            if (isConnected()) {
                final ID remoteId = getTargetID();
                if (exception == null) {
                    fireConnectEvent(new DisconnectingEvent(this, this.getID(),
                        remoteId));
                }
                synchronized (connection) {
                    try {
                        connection.post(
                            remoteId,
                            serialize(SharedDataPacket.createServiceLeavePacket(
                                getID(), remoteId, getNextSequenceNumber(),
                                getLeaveData(remoteId))));
                    } catch (Exception e) {
                        log.warn("Send disconnection exception:", e);
                    }
                    synchronized (getOberserLock()) {
                        handleLeave(remoteId, connection);
                    }
                }
                if (exception == null) {
                    fireConnectEvent(new DisconnectedEvent(this, this.getID(),
                        remoteId));
                } else {
                    fireConnectEvent(new EjectedConnectEvent(this,
                        this.getID(), remoteId, exception));
                }
            }
        }
    }

    @Override
    protected void handleLeave(ID fromID, Channel conn) {
        if (fromID.equals(remoteServerID)) {
            observer.removeNonLocalMembers();
            super.handleLeave(fromID, conn);
            makeStateDisconnected(null);
        } else if (fromID.equals(getID())) {
            super.handleLeave(fromID, conn);
        }

    }

    protected Serializable getLeaveData(ID target) {
        return null;
    }

    public static byte[] serialize(Serializable obj) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        return bos.toByteArray();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.GenericSSProvider#queueDataPacket(org.solmix.hola.shared.generic.SharedDataPacket)
     */
    @Override
    protected void queueDataPacket(SharedDataPacket createPacket)
        throws IOException {
        connection.post(createPacket.getToID(), serialize(createPacket));

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.generic.GenericSSProvider#forwardExcluding(org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.core.identity.ID,
     *      org.solmix.hola.shared.generic.SharedDataPacket)
     */
    @Override
    protected void forwardExcluding(ID from, ID excluding, SharedDataPacket data)
        throws IOException {
    }

}
