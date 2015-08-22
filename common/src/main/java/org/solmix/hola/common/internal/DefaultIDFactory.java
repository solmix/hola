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

package org.solmix.hola.common.internal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.solmix.runtime.identity.ID;
import org.solmix.runtime.identity.IDCreateException;
import org.solmix.runtime.identity.IIDFactory;
import org.solmix.runtime.identity.Namespace;
import org.solmix.runtime.identity.support.GUID;
import org.solmix.runtime.identity.support.GUIDNamespace;
import org.solmix.runtime.identity.support.LongNamespace;
import org.solmix.runtime.identity.support.StringNamespace;
import org.solmix.runtime.identity.support.URINamespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月4日
 */

public class DefaultIDFactory implements IIDFactory
{

    protected static IIDFactory instance = null;

    private static Hashtable<String, Namespace> namespaces = new Hashtable<String, Namespace>();
    static {
        instance = new DefaultIDFactory();
        addNamespace0(new StringNamespace());
        addNamespace0(new GUIDNamespace());
        addNamespace0(new LongNamespace());
        addNamespace0(new URINamespace());
    }

    public synchronized static IIDFactory getDefault() {
        return instance;
    }

    public DefaultIDFactory()
    {
       instance=this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#addNamespace(org.solmix.hola.common.identity.Namespace)
     */
    @Override
    public Namespace addNamespace(Namespace namespace) throws SecurityException {
        if (namespace == null)
            return null;
        return addNamespace0(namespace);
    }

    public final static Namespace addNamespace0(Namespace namespace) {
        if (namespace == null)
            return null;
        return namespaces.put(namespace.getName(), namespace);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#containsNamespace(org.solmix.hola.common.identity.Namespace)
     */
    @Override
    public boolean containsNamespace(Namespace namespace)
        throws SecurityException {
        if (namespace == null)
            return false;
        return namespaces.containsKey(namespace.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#getNamespaces()
     */
    @Override
    public List<Namespace> getNamespaces() throws SecurityException {
        return new ArrayList<Namespace>(namespaces.values());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#getNamespace(org.solmix.hola.common.identity.Namespace)
     */
    @Override
    public Namespace getNamespace(Namespace namespace) throws SecurityException {
        if (namespace == null)
            return null;
        return namespaces.get(namespace.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#getNamespaceByName(java.lang.String)
     */
    @Override
    public Namespace getNamespaceByName(String name) throws SecurityException {
        return namespaces.get(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createGUID()
     */
    @Override
    public ID createGUID() throws IDCreateException {
        return createGUID(GUID.DEFAULT_BYTE_LENGTH);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createGUID(int)
     */
    @Override
    public ID createGUID(int length) throws IDCreateException {
        return createID(new GUIDNamespace(),
            new Integer[] { new Integer(length) });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createID(org.solmix.hola.common.identity.Namespace,
     *      java.lang.Object[])
     */
    @Override
    public ID createID(Namespace n, Object[] args) throws IDCreateException {
        Namespace ns = getNamespace(n);
        if (ns == null)
            throw new IDCreateException("Namespace " + n.getName()
                + " not found", null);
        return ns.createID(args);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createID(java.lang.String,
     *      java.lang.Object[])
     */
    @Override
    public ID createID(String namespaceName, Object[] args)
        throws IDCreateException {
        Namespace n = getNamespaceByName(namespaceName);
        if (n == null)
            throw new IDCreateException(
                "Namespace " + namespaceName + " not found");
        return createID(n, args);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createID(org.solmix.hola.common.identity.Namespace,
     *      java.lang.String)
     */
    @Override
    public ID createID(Namespace namespace, String uri)
        throws IDCreateException {
        return createID(namespace, new Object[] { uri });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createID(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public ID createID(String namespace, String uri) throws IDCreateException {
        return createID(namespace, new Object[] { uri });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createStringID(java.lang.String)
     */
    @Override
    public ID createStringID(String idString) throws IDCreateException {
        if (idString == null)
            throw new IDCreateException("StringID cannot be null");
        return createID(new StringNamespace(), new String[] { idString });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#createLongID(long)
     */
    @Override
    public ID createLongID(long l) throws IDCreateException {
        return createID(new LongNamespace(), new Long[] { new Long(l) });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.identity.IDFactory#removeNamespace(org.solmix.hola.common.identity.Namespace)
     */
    @Override
    public Namespace removeNamespace(Namespace namespace)
        throws SecurityException {
        if (namespace == null)
            return null;
        return namespaces.remove(namespace.getName());
    }

}
