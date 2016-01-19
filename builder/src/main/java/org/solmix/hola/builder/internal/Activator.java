package org.solmix.hola.builder.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.solmix.hola.builder.blueprint.HolaNamespaceHandler;
import org.solmix.runtime.support.blueprint.BPNamespaceFactory;
import org.solmix.runtime.support.blueprint.BPNamespaceRegisterer;


public class Activator implements BundleActivator
{

    @Override
    public void start(BundleContext context) throws Exception {

        BPNamespaceFactory factory = new BPNamespaceFactory() {
            
            @Override
            public Object createHandler() {
                return new HolaNamespaceHandler();
            }
        };
        BPNamespaceRegisterer.register(context, factory, "http://www.solmix.org/schema/hola/v1.0.0");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        
    }

}
