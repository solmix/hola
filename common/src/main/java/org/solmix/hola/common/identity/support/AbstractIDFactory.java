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

package org.solmix.hola.common.identity.support;

import org.solmix.hola.common.identity.ID;
import org.solmix.hola.common.identity.IDCreateException;
import org.solmix.hola.common.identity.IDFactory;
import org.solmix.hola.common.identity.Namespace;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月4日
 */

public abstract class AbstractIDFactory implements IDFactory
{
  
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

}
