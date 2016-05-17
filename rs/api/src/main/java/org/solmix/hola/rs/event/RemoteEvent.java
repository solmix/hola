/**
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

package org.solmix.hola.rs.event;

import java.util.Dictionary;
import java.util.EventObject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.runtime.event.Event;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年4月29日
 */

public class RemoteEvent extends EventObject implements Event {

    private static final long serialVersionUID = 6307503964324620448L;

    private final RemoteReference<?> reference;

    /**
     * Type of service lifecycle change.
     */
    private final int type;

    /**
     * This service has been registered.
     * <p>
     * This event is synchronously delivered <strong>after</strong> the service
     * has been registered with the Framework.
     * 
     * @see BundleContext#registerService(String[],Object,Dictionary)
     */
    public final static int REGISTERED = 0x00000001;

    /**
     * The properties of a registered service have been modified.
     * <p>
     * This event is synchronously delivered <strong>after</strong> the service
     * properties have been modified.
     * 
     * @see ServiceRegistration#setProperties
     */
    public final static int MODIFIED = 0x00000002;

    /**
     * This service is in the process of being unregistered.
     * <p>
     * This event is synchronously delivered <strong>before</strong> the service
     * has completed unregistering.
     * 
     * <p>
     * If a bundle is using a service that is {@code UNREGISTERING}, the bundle
     * should release its use of the service when it receives this event. If the
     * bundle does not release its use of the service when it receives this
     * event, the Framework will automatically release the bundle's use of the
     * service while completing the service unregistration operation.
     * 
     * @see ServiceRegistration#unregister
     * @see BundleContext#ungetService
     */
    public final static int UNREGISTERED = 0x00000004;

    public RemoteEvent(int type, RemoteReference<?> reference) {
        super(reference);
        this.reference = reference;
        this.type = type;
    }

    /**
     * Returns a reference to the service that had a change occur in its
     * lifecycle.
     * <p>
     * This reference is the source of the event.
     * 
     * @return Reference to the service that had a lifecycle change.
     */
    public RemoteReference<?> getRemoteServiceReference() {
        return reference;
    }

    /**
     * Returns the type of event. The event type values are:
     * <ul>
     * <li>{@link #REGISTERED}</li>
     * <li>{@link #MODIFIED}</li>
     * <li>{@link #UNREGISTERING}</li>
     * </ul>
     * 
     * @return Type of service lifecycle change.
     */

    public int getType() {
        return type;
    }

    // ID getLocalNodeID();

    // ID getNodeID();

    // String[] getClazzes();
}
