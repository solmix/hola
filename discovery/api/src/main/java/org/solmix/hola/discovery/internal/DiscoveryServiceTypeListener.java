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

package org.solmix.hola.discovery.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.discovery.DiscoveryServiceProvider;

/**
 * 监听OSGI环境中注册的{@link org.solmix.hola.discovery.ServiceListener},并自动添加listener.
 * 只Spring如需添加需手动代码实现
 */

public class DiscoveryServiceTypeListener  implements ServiceListener
{

    private final DiscoveryServiceProvider service;


    private final BundleContext context;


    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServiceTypeListener.class);

    public DiscoveryServiceTypeListener(DiscoveryServiceProvider protocol)
    {
        this.service = protocol;
        context = Activator.getDefault().getBundleContext();
        // OSGI下查找org.solmix.hola.discovery.ServiceListener
        if (context != null) {
            try {
                final ServiceReference<?>[] references = context.getServiceReferences(
                    org.solmix.hola.discovery.ServiceTypeListener.class.getName(),
                    null);
                addServiceListener(references);
                context.addServiceListener(this, getFilter());
            } catch (InvalidSyntaxException e) {
                LOG.error("Cannot create filter", e);
            }
        } else {// 不做任何处理

        }
    }

    /**
     * @param references
     */
    private void addServiceListener(ServiceReference<?>[] references) {
        if (references == null) {
            return;
        }
        for (ServiceReference<?> refer : references) {
            final org.solmix.hola.discovery.ServiceTypeListener listener = (org.solmix.hola.discovery.ServiceTypeListener) context.getService(refer);
            service.addServiceTypeListener(listener);
        }

    }

    

    /**
     * @return
     */
    protected String getFilter() {
        return "(" + Constants.OBJECTCLASS + "="
            + org.solmix.hola.discovery.ServiceTypeListener.class.getName() + ")";
    }

    private void removeServiceListener(ServiceReference<?>[] references) {
        if (references == null) {
            return;
        }
        for (ServiceReference<?> refer : references) {
            final org.solmix.hola.discovery.ServiceTypeListener listener = (org.solmix.hola.discovery.ServiceTypeListener) context.getService(refer);
            service.removeServiceTypeListener(listener);
        }

    }

    private void removeServiceListener(ServiceReference<?> reference) {
        removeServiceListener(new ServiceReference[] { reference });
    }

    private void addServiceListener(ServiceReference<?> reference) {
        addServiceListener(new ServiceReference[] { reference });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        Object service = context.getService(event.getServiceReference());
        if (service instanceof org.solmix.hola.discovery.ServiceListener) {
            switch (event.getType()) {
                case ServiceEvent.REGISTERED:
                    addServiceListener(event.getServiceReference());
                    break;
                case ServiceEvent.UNREGISTERING:
                    removeServiceListener(event.getServiceReference());
                    break;
                default:
                    break;
            }
        }

    }
    public void destroy() {
        if(context!=null)
              context.removeServiceListener(this);
  }

}
