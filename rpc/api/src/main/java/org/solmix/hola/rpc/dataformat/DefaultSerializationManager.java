/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.rpc.dataformat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Resource;

import org.solmix.runtime.Container;
import org.solmix.runtime.bean.ConfiguredBeanProvider;
import org.solmix.runtime.bean.ConfiguredBeanProvider.BeanLoaderListener;
import org.solmix.runtime.exchange.dataformat.Serialization;
import org.solmix.runtime.exchange.dataformat.SerializationManager;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月14日
 */

public class DefaultSerializationManager implements SerializationManager {

    private Container container;

    private final Map<String, Serialization> nameSerials;

    private final Map<Byte, Serialization> idSerials;

    private final Set<String> failed = new CopyOnWriteArraySet<String>();

    private final Set<String> loaded = new CopyOnWriteArraySet<String>();

    public DefaultSerializationManager() {
        this(null);
    }

    public DefaultSerializationManager(Container container) {
        nameSerials = new ConcurrentHashMap<String, Serialization>(8, 0.75f, 4);
        idSerials = new ConcurrentHashMap<Byte, Serialization>(8, 0.75f, 4);
        setContainer(container);
    }

    @Override
    public Serialization getSerializationById(Byte id) {
        Serialization s = idSerials.get(id);
        if (s == null) {
            s = loadSerialization(id);
        }
        if (s == null) {
            throw new IllegalArgumentException(
                "No found Serialization   ContentTypeId is :" + id);
        }
        return s;
    }

    @Override
    public Serialization getSerializationByName(String extendsionName) {
        Serialization s = nameSerials.get(extendsionName);
        if (s == null && !failed.contains(extendsionName)) {
            s = loadSerialization(extendsionName);
        }
        if (s == null) {
            failed.add(extendsionName);
            throw new IllegalArgumentException(
                "No found Serialization   named :" + extendsionName);
        }
        return s;
    }

    /**
     * @param extendsionName
     * @return
     */
    private Serialization loadSerialization(String extendsionName) {
        loadAll();
        return nameSerials.get(extendsionName);
    }

    private Serialization loadSerialization(Byte id) {
        loadAll();
        return idSerials.get(id);
    }

    private void loadAll() {
        ConfiguredBeanProvider provider = container.getExtension(ConfiguredBeanProvider.class);
        provider.loadBeansOfType(Serialization.class,
            new BeanLoaderListener<Serialization>() {

                @Override
                public boolean loadBean(String name,
                    Class<? extends Serialization> type) {
                    loaded.add(name);
                    return nameSerials.containsKey(type);
                }

                @Override
                public boolean beanLoaded(String name, Serialization bean) {
                    if (!loaded.contains(name)) {
                        nameSerials.put(name, bean);
                        idSerials.put(bean.getContentTypeId(), bean);
                    }
                    return nameSerials.containsKey(name);
                }

            });
    }

    public Container getContainer() {
        return container;
    }

    @Resource
    public void setContainer(Container container) {
        this.container = container;
        if (container != null) {
            container.setExtension(this, SerializationManager.class);
        }
    }
}
