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
package org.solmix.hola.transport.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年7月15日
 */

public class MultiMessage implements Iterable {

    public static MultiMessage createFromCollection(Collection collection) {
        MultiMessage result = new MultiMessage();
        result.addMessages(collection);
        return result;
    }

    public static MultiMessage createFromArray(Object... args) {
        return createFromCollection(Arrays.asList(args));
    }

    public static MultiMessage create() {
        return new MultiMessage();
    }

    private final List messages = new ArrayList();

    private MultiMessage() {}

    public void addMessage(Object msg) {
        messages.add(msg);
    }

    public void addMessages(Collection collection) {
        messages.addAll(collection);
    }

    public Collection getMessages() {
        return Collections.unmodifiableCollection(messages);
    }

    public int size() {
        return messages.size();
    }

    public Object get(int index) {
        return messages.get(index);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Collection removeMessages() {
        Collection result = Collections.unmodifiableCollection(messages);
        messages.clear();
        return result;
    }

    @Override
    public Iterator iterator() {
        return messages.iterator();
    }

}