
package org.solmix.hola.rs.generic.exchange;

import java.util.Dictionary;
import java.util.Enumeration;

import org.solmix.commons.util.Assert;
import org.solmix.exchange.Client;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemotePipelineSelector;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.generic.HolaRemoteServiceFactory;
import org.solmix.hola.rs.support.BaseRemoteService;
import org.solmix.hola.rs.support.RemoteReferenceImpl;
import org.solmix.runtime.Container;

public class HolaRemoteService<T> extends BaseRemoteService<T>
{

    public HolaRemoteService(Container container, RemoteReferenceImpl<T> refer)
    {
        super(new HolaClientFactory(), refer);
        getClientFactory().setContainer(container);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Client doGetClient(RemoteRequest request) {
        Assert.assertNotNull(clientFactory);
        clientFactory.setServiceClass(getServiceClass());
        Dictionary<String, ?> properties = HolaRemoteServiceFactory.toDictionary(getRemoteReference());
        if (clientFactory.getProperties() == null) {
            clientFactory.setProperties(properties);
        } else if (properties != null) {
            Enumeration<String> keys = properties.keys();

            Dictionary dic = clientFactory.getProperties();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                Object value = properties.get(key);
                dic.put(key, value);
            }
        }
        //处理pipeline共享
        int pipelines= PropertiesUtils.getInt(clientFactory.getProperties(), HOLA.PIPELINES_KEY,0);
        if(pipelines<=0){
            pipelines=1;
        }
        clientFactory.setPipelineSelector(new RemotePipelineSelector(clientFactory.getContainer(),true, pipelines));
        Client client = clientFactory.create();
        return client;
    }


}
