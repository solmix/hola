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
package org.solmix.hola.discovery;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年4月5日
 */

public interface ServiceProperties extends Serializable
{
    /**
     * Get property names. This should return an Enumeration of String objects
     * that identify all of the names in this IServiceProperties instance
     * 
     * @return Enumeration of all service property names as Strings.  Will not be <code>null</code>.
     */
    public Enumeration<String> getPropertyNames();

    /**
     * Get property name as String. Returns a valid String if there is a
     * property of the given name. Returns null if there is no property by that
     * name, or if the property has some other type than String.
     * 
     * @param name
     *            the name of the property to return.  Must not be <code>null</code>.
     * @return the property as a String.  Will be <code>null</code> if property does
     * not exist.
     */
    public String getPropertyString(String name);

    /**
     * Get property name as byte[]. Returns a non-null byte[] if there is a
     * property of the given name. Returns null if there is no property by that
     * name, or if the property has some other type than byte[].
     * 
     * @param name
     *            the name of the property to return.  Must not be <code>null</code>.
     * @return the property as a byte[].  Will be <code>null</code> if property does
     * not exist.
     */
    public byte[] getPropertyBytes(String name);

    /**
     * Get property as an Object. Returns a non-null Object if there is a
     * property of the given name. Returns <code>null</code> if there is no property by that
     * name.
     * 
     * @param name
     *            the name of the property to return. Must not be <code>null</code>.
     * @return the property as an Object.  Returns <code>null</code> if there is no
     * property of given name.
     */
    public Object getProperty(String name);

    /**
     * Set property as String.
     * 
     * @param name
     *            the property name of the property. Must not be <code>null</code>.
     * @param value
     *            the property value to associated with the name. Must not be
     *            <code>null</code>.
     * @return Object that was previous value associated with given name. May be <code>null</code>
     *         if not previously in properties.
     */
    public Object setPropertyString(String name, String value);

    /**
     * Set property as byte [].
     * 
     * @param name
     *            the property name of the property. Must not be null.
     * @param value
     *            the property value to associated with the name. Must not be
     *            null.
     * @return Object that was previous value associated with given name. Null
     *         if not previously in properties
     */
    public Object setPropertyBytes(String name, byte[] value);

    /**
     * Set property as Object.
     * 
     * @param name
     *            the property name of the property. Must not be null.
     * @param value
     *            the property value to associated with the name. Must not be
     *            null.
     * @return Object that was previous value associated with given name. Null
     *         if not previously in properties
     */
    public Object setProperty(String name, Object value);

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj);

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode();

    /**
     * @return Answers the number of key/value pairs in this ServiceProperties
     * @see Dictionary#size()
     */
    public int size();
}
