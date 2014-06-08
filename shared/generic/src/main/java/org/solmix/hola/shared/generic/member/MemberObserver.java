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

package org.solmix.hola.shared.generic.member;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.ID;
import org.solmix.hola.shared.generic.GenericProvider;
import org.solmix.hola.shared.generic.GenericWrapper;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月18日
 */

public class MemberObserver implements java.util.Observer
{

    private final GenericProvider provider;

    private final GroupMemberManager groupManager;

    private int maxMember = -1;

    private static final Logger LOG = LoggerFactory.getLogger(MemberObserver.class.getName());

    TreeMap<ID, GenericWrapper> loading, active;

    Member localMember;

    public MemberObserver(GenericProvider provider, Member local)
    {
        this.provider = provider;
        groupManager = new GroupMemberManager();
        groupManager.addObserver(this);
        loading = new TreeMap<ID, GenericWrapper>();
        active = new TreeMap<ID, GenericWrapper>();
        localMember = local;
        addMember(local);

    }

    /**
     * @param local
     */
    public synchronized boolean addMember(Member local) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addMember :" + local.getID());
        }
        if (maxMember > 0 && getSize() > maxMember)
            return false;
        return groupManager.addMember(local);

    }

    synchronized int getSize() {
        return groupManager.getSize();
    }

    public synchronized boolean removeMember(Member m) {
        final boolean res = groupManager.removeMember(m);
        if (res) {
            removeSharedServices(m);
        }
        return res;
    }

    private void removeSharedServices(Member m) {
        removeSharedServices(m, true);
    }

    /**
     * @param m
     */
    private void removeSharedServices(Member m, boolean match) {
        final HashSet set = getRemoveIDs(m.getID(), match);
        final Iterator i = set.iterator();
        while (i.hasNext()) {
            final ID removeID = (ID) i.next();
            if (isLoading(removeID)) {
                removeSharedObjectFromLoading(removeID);
            } else {
                container.destroySharedObject(removeID);
            }
        }

    }

    HashSet<ID> getRemoveIDs(ID homeID, boolean match) {
        final HashSet<ID> aSet = new HashSet<ID>();
        for (final DestroyIterator i = new DestroyIterator(loading, homeID,
            match); i.hasNext();) {
            aSet.add(i.next());
        }
        for (final DestroyIterator i = new DestroyIterator(active, homeID,
            match); i.hasNext();) {
            aSet.add(i.next());
        }
        return aSet;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        final MemberChanged mc = (MemberChanged) arg;
        notifyAllOfMemberChange(mc.getMember(), active, mc.getAdded());

    }

    void notifyAllOfMemberChange(Member m, TreeMap<ID, GenericWrapper>  map, boolean add) {
        for (final Iterator<GenericWrapper> i = map.values().iterator(); i.hasNext();) {
            final GenericWrapper ro = i.next();
            ro.memberChanged(m, add);
        }
    }

    synchronized boolean isActive(ID id) {
        return active.containsKey(id);
    }

    synchronized boolean isLoading(ID id) {
        return loading.containsKey(id);
    }

    /**
     * @return the maxMember
     */
    public int getMaxMember() {
        return maxMember;
    }

    /**
     * @param maxMember the maxMember to set
     */
    public int setMaxMember(int max) {
        final int old = maxMember;
        maxMember = max;
        return old;
    }

    /**
     * 根据ID查找对象
     * 
     * @param id
     * @return
     */
    public GenericWrapper getFromAny(ID id) {
        GenericWrapper found = active.get(id);
        if (found != null)
            return found;
        found = loading.get(id);
        return found;
    }

    /**
     * @param wrapper
     */
    public void addServiceToActive(GenericWrapper wrapper) {
        if (wrapper == null)
            return;
        if (LOG.isDebugEnabled())
            LOG.debug("addServiceToActive " + wrapper.toString());
        active.put(wrapper.getServiceID(), wrapper);
        wrapper.activated();
    }

    /**
     * @return
     */
    public ID[] getServiceIDs() {
        return active.keySet().toArray(new ID[0]);
    }

}

class DestroyIterator implements Iterator<ID>
{

    ID next;

    ID homeID;

    Iterator<GenericWrapper> i;

    boolean match;

    public DestroyIterator(TreeMap<ID, GenericWrapper> map, ID hID, boolean m)
    {
        i = map.values().iterator();
        homeID = hID;
        next = null;
        match = m;
    }

    @Override
    public boolean hasNext() {
        if (next == null)
            next = getNext();
        return (next != null);
    }

    @Override
    public ID next() {
        if (hasNext()) {
            final ID value = next;
            next = null;
            return value;
        }
        throw new java.util.NoSuchElementException();
    }

    ID getNext() {
        while (i.hasNext()) {
            final GenericWrapper ro = i.next();
            if (homeID == null
                || (match ^ !ro.getLocalProviderID().equals(homeID))) {
                return ro.getServiceID();
            }
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}