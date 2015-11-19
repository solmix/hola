package org.solmix.hola.rs.generic.exchange;

import java.util.Dictionary;
import java.util.Enumeration;

import org.solmix.exchange.Client;
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
    protected Client doGetClient() {
            
            Dictionary<String, ?>  properties = HolaRemoteServiceFactory.toDictionary(getRemoteReference());
            if(clientFactory.getProperties()==null){
                clientFactory.setProperties(properties);
            }else if(properties!=null){
                Enumeration<String> keys=properties.keys();
                
                Dictionary dic= clientFactory.getProperties();
                while(keys.hasMoreElements()){
                    String key = keys.nextElement();
                    Object value = properties.get(key);
                    dic.put(key, value);
                }
            }
            Client  client = clientFactory.create();
            return client;
    }

}
