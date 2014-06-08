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

package org.solmix.hola.rs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.ID;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月28日
 */

public class RSProviderManager implements RemoteServiceProviderManager
{

    protected static RSProviderManager instance ;
    private volatile static boolean init = false;
    static final Map<ID,ProviderEntry> providers= new HashMap<ID,ProviderEntry>();
    static final List<RemoteServiceProviderListener> managerListeners = new ArrayList<RemoteServiceProviderListener>();
    static final Map<String,RemoteServiceProviderDescription> providerDescriptions = new HashMap<String,RemoteServiceProviderDescription>();
    private RSProviderManager(){
        RSProviderManager.instance=this;
    }
    public synchronized static RSProviderManager getDefault() {
        if(!init){
            init();
            init=true;
        }
        return instance;
    }

    private static void init(){
        if(instance==null)
            instance= new RSProviderManager();
      
    }
    
    @Override
    public RemoteServiceProviderDescription addDescription(RemoteServiceProviderDescription description){
        if (description == null)
            return null;
      synchronized (providerDescriptions) {
            return providerDescriptions.put(description.getName(), description);
      }
    }
    @Override
    public RemoteServiceProviderDescription removeDescription(RemoteServiceProviderDescription description){
        if (description == null)
            return null;
      synchronized (providerDescriptions) {
            return providerDescriptions.remove(description.getName());
      }
    }
    public RemoteServiceProviderDescription getDescription(RemoteServiceProviderDescription description){
        if (description == null)
            return null;
      synchronized (providerDescriptions) {
            return providerDescriptions.get(description.getName());
      }
    }
    public List<RemoteServiceProviderDescription> getDescriptions(){
        synchronized (providerDescriptions) {
            return new ArrayList<RemoteServiceProviderDescription>(providerDescriptions.values());
        }
    }
    public RemoteServiceProvider createProvider(String descriptionName) throws ProviderCreateException{
        return createProvider(getDescriptionByName(descriptionName),(Object[]) null);
    }
   
    public RemoteServiceProvider createProvider(
        RemoteServiceProviderDescription description, Object[] parameters)
        throws ProviderCreateException {
        RemoteServiceProviderDescription rd = getDescription(description);
        if (description == null)
            throw new ProviderCreateException(
                "RemoteServiceProviderDescription can't be null");
        if (rd == null) {
            throw new ProviderCreateException(
                "RemoteServiceProviderDescription no found");
        }
        RemoteServiceProviderFactory factory = rd.getFactory();
        RemoteServiceProvider provider = factory.create(rd, parameters);
        if (provider == null)
            throw new ProviderCreateException("factory create instance is null");
        ID id = provider.getID();
        if (id != null) {
            addProvider(provider, description);
        }
        return provider;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#getProvider(org.solmix.hola.core.identity.ID)
     */
    @Override
    public RemoteServiceProvider getProvider(ID providerID) {
        if (providerID == null)
            return null;
        synchronized (providers) {
            ProviderEntry entry = providers.get(providerID);
            if (entry != null)
                return entry.getRemoteServiceProvider();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#getProviderDescription(org.solmix.hola.core.identity.ID)
     */
    @Override
    public RemoteServiceProviderDescription getProviderDescription(ID providerID) {
        if (providerID == null)
            return null;
        synchronized (providers) {
            ProviderEntry entry = providers.get(providerID);
            if (entry != null)
                return entry.getRemoteServiceProviderDesc();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#getProviders()
     */
    @Override
    public RemoteServiceProvider[] getProviders() {
        List<RemoteServiceProvider> result = new ArrayList<RemoteServiceProvider>();
        synchronized (providers) {
              Collection<ProviderEntry> providerEntrys = providers.values();
              for (Iterator<ProviderEntry> i = providerEntrys.iterator(); i.hasNext();) {
                  ProviderEntry entry =  i.next();
                  result.add(entry.getRemoteServiceProvider());
              }
        }
        return result.toArray(new RemoteServiceProvider[] {});
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#hasProvider(org.solmix.hola.core.identity.ID)
     */
    @Override
    public boolean hasProvider(ID providerID) {
        Assert.isNotNull(providerID);
        synchronized (providers) {
            return providers.containsKey(providerID);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#addProvider(org.solmix.hola.rs.RemoteServiceProvider,
     *      org.solmix.hola.rs.RemoteServiceProviderDescription)
     */
    @Override
    public RemoteServiceProvider addProvider(RemoteServiceProvider provider,
        RemoteServiceProviderDescription desc) {
        Assert.isNotNull(provider);
        Assert.isNotNull(desc);
        ID id = provider.getID();
        Assert.isNotNull(id, "RemoteServiceProvider id must be not null");
        ProviderEntry entry=null;
        synchronized (providers) {
            entry=  providers.put(id, new ProviderEntry(provider, desc));
        }
        if(entry!=null)
            fireProviderAdd(provider);
        return provider;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#removeProvider(org.solmix.hola.rs.RemoteServiceProvider)
     */
    @Override
    public RemoteServiceProvider removeProvider(RemoteServiceProvider provider) {
      Assert.isNotNull(provider);
      ID id=provider.getID();
      if(id==null)
          return null;
        return removeProvider(id);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#removeProvider(org.solmix.hola.core.identity.ID)
     */
    @Override
    public RemoteServiceProvider removeProvider(ID providerID) {
        Assert.isNotNull(providerID);
        ProviderEntry entry = null;
        synchronized (providers) {
            entry = providers.remove(providerID);
        }
        RemoteServiceProvider removed = null;
        if (entry != null) {
            removed = entry.getRemoteServiceProvider();
            fireProviderRemove(removed);
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#addListener(org.solmix.hola.rs.RemoteServiceProviderListener)
     */
    @Override
    public boolean addListener(RemoteServiceProviderListener listener) {
        Assert.isNotNull(listener);
        synchronized (managerListeners) {
              return managerListeners.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#remoteListener(org.solmix.hola.rs.RemoteServiceProviderListener)
     */
    @Override
    public boolean remoteListener(RemoteServiceProviderListener listener) {
        Assert.isNotNull(listener);
        synchronized (managerListeners) {
              return managerListeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteServiceProviderManager#remoteAllProviders()
     */
    @Override
    public void remoteAllProviders() {
       synchronized (providers) {
        for(Iterator<ID> i= providers.keySet().iterator();i.hasNext();){
            ID key=i.next();
            ProviderEntry entry= providers.get(key);
            i.remove();
            fireProviderRemove(entry.getRemoteServiceProvider());
        }
    }

    }
    /**
     * @param remoteServiceProvider
     */
    protected void fireProviderRemove(RemoteServiceProvider rsProvider) {
        List<RemoteServiceProviderListener> toNotify = null;
        synchronized (managerListeners) {
              toNotify = new ArrayList<RemoteServiceProviderListener>(managerListeners);
        }
        for (Iterator<RemoteServiceProviderListener> i = toNotify.iterator(); i.hasNext();) {
            RemoteServiceProviderListener cml = i.next();
              cml.providerRemove(rsProvider);
        }
        
    }
    
    protected void fireProviderAdd(RemoteServiceProvider rsProvider) {
        List<RemoteServiceProviderListener> toNotify = null;
        synchronized (managerListeners) {
              toNotify = new ArrayList<RemoteServiceProviderListener>(managerListeners);
        }
        for (Iterator<RemoteServiceProviderListener> i = toNotify.iterator(); i.hasNext();) {
            RemoteServiceProviderListener cml = i.next();
              cml.providerAdd(rsProvider);
        }
        
    }
    class ProviderEntry{
        private final RemoteServiceProvider remoteServiceProvider;
        private final RemoteServiceProviderDescription remoteServiceProviderDescription;
        
        ProviderEntry(RemoteServiceProvider provider,RemoteServiceProviderDescription desc){
            this.remoteServiceProvider=provider;
            this.remoteServiceProviderDescription=desc;
        }
        
        /**
         * @return the remoteServiceProvider
         */
        public RemoteServiceProvider getRemoteServiceProvider() {
            return remoteServiceProvider;
        }
        
        /**
         * @return the remoteServiceProviderDescription
         */
        public RemoteServiceProviderDescription getRemoteServiceProviderDesc() {
            return remoteServiceProviderDescription;
        }
        
    }
    
    protected RemoteServiceProviderDescription getDescriptionByName(String descriptionName){
        if(descriptionName==null)
            return null;
        synchronized (providerDescriptions) {
            return providerDescriptions.get(descriptionName);
        }
    }
    public static void close(){
        synchronized(providers){
            for(Iterator<ID> i=providers.keySet().iterator();i.hasNext();){
                ProviderEntry entry=providers.get(i.next());
                if(entry!=null){
                    RemoteServiceProvider provider=  entry.getRemoteServiceProvider();
                    provider.destroy();
                }
                providers.clear();
            }
        }
        managerListeners.clear();
    }

}
