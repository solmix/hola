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
import org.solmix.hola.shared.generic.GenericSSHolder;
import org.solmix.hola.shared.generic.GenericSSProvider;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年5月18日
 */

public class MemberObserver implements java.util.Observer
{

    private final GenericSSProvider provider;

    private final GroupMemberManager groupManager;

    private int maxMember = -1;

    private static final Logger LOG = LoggerFactory.getLogger(MemberObserver.class.getName());

    TreeMap<ID, GenericSSHolder> loading, active;

    Member localMember;

    public MemberObserver(GenericSSProvider provider, Member local)
    {
        this.provider = provider;
        groupManager = new GroupMemberManager();
        groupManager.addObserver(this);
        loading = new TreeMap<ID, GenericSSHolder>();
        active = new TreeMap<ID, GenericSSHolder>();
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

    public synchronized boolean removeMember(ID id) {
        final Member m = getMemberForID(id);
        if (m == null)
            return false;
        return removeMember(m);
    }

    synchronized Member getMemberForID(ID id) {
        final Member newMem = new Member(id);
        for (final Iterator<Member> i = groupManager.iterator(); i.hasNext();) {
            final Member oldMem = i.next();
            if (newMem.equals(oldMem))
                return oldMem;
        }
        return null;
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
        final HashSet<ID> set = getRemoveIDs(m.getID(), match);
        final Iterator<ID> i = set.iterator();
        while (i.hasNext()) {
            final ID removeID = i.next();
            if (isLoading(removeID)) {
                removeSharedServiceFromLoading(removeID);
            } else {
                provider.destroySharedService(removeID);
            }
        }

    }

    /**
     * @param removeID
     */
    private boolean removeSharedServiceFromLoading(ID removeID) {
        if (loading.remove(removeID) != null) {
            return true;
        }
        return false;

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

    void notifyAllOfMemberChange(Member m, TreeMap<ID, GenericSSHolder> map,
        boolean add) {
        for (final Iterator<GenericSSHolder> i = map.values().iterator(); i.hasNext();) {
            final GenericSSHolder ro = i.next();
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
    public GenericSSHolder getFromAny(ID id) {
        GenericSSHolder found = active.get(id);
        if (found != null)
            return found;
        found = loading.get(id);
        return found;
    }

    /**
     * @param holder
     */
    public void addServiceToActive(GenericSSHolder holder) {
        if (holder == null)
            return;
        if (LOG.isDebugEnabled())
            LOG.debug("addServiceToActive " + holder.toString());
        active.put(holder.getServiceID(), holder);
        holder.activated();
    }

    /**
     * @return
     */
    public ID[] getServiceIDs() {
        return active.keySet().toArray(new ID[0]);
    }

    /**
     * @param sharedServiceID
     * @return
     */
    public GenericSSHolder getFromActive(ID sharedServiceID) {
        return active.get(sharedServiceID);
    }

    /**
     * @param sharedServiceID
     */
    public boolean removeSharedService(ID sharedServiceID) {
        GenericSSHolder holder = active.remove(sharedServiceID);
        if (holder == null)
            return false;
        holder.deactivated();
        return true;

    }

    /**
     * @param serviceID
     */
    public void notigyOthresActivated(ID serviceID) {
        notifyOtherChanged(serviceID, active, true);

    }

    /**
     * @param serviceID
     */
    public void notigyOthresDeactivated(ID serviceID) {
        notifyOtherChanged(serviceID, active, false);

    }

    private void notifyOtherChanged(ID serviceID,
        TreeMap<ID, GenericSSHolder> active, boolean activated) {
        for (GenericSSHolder holder : active.values()) {
            if (serviceID.equals(holder.getServiceID())) {
                holder.otherChanged(serviceID, activated);
            }
        }

    }

    synchronized void removeAllMembers(Member exception) {

        final Object m[] = getMembers();
        for (int i = 0; i < m.length; i++) {
            final Member mem = (Member) m[i];
            if (exception == null || !exception.equals(mem))
                removeMember(mem);
        }
    }

    synchronized Object[] getMembers() {
        return groupManager.getMembers();
    }

    public void removeAllMembers() {
        removeAllMembers(null);
    }

    /**
     * 
     */
    public void removeNonLocalMembers() {
        removeAllMembers(localMember);
    }
}

class DestroyIterator implements Iterator<ID>
{

    ID next;

    ID homeID;

    Iterator<GenericSSHolder> i;

    boolean match;

    public DestroyIterator(TreeMap<ID, GenericSSHolder> map, ID hID, boolean m)
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
            final GenericSSHolder ro = i.next();
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