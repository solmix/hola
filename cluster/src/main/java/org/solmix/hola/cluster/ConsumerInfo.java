
package org.solmix.hola.cluster;

import java.util.Dictionary;
import java.util.Hashtable;

import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.DefaultServiceType;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceType;

public class ConsumerInfo
{

    private ServiceType type;

    private Dictionary<String, ?> properties;

    public ConsumerInfo(Dictionary<String, ?> properties)
    {
        String service = PropertiesUtils.getServiceInterface(properties);
        String group = PropertiesUtils.getString(properties, HOLA.GROUP_KEY);
        String category = PropertiesUtils.getString(properties, HOLA.CATEGORY_KEY, HOLA.DEFAULT_CATEGORY);
        type = new DefaultServiceType(service, group, category);
        this.properties = properties;
    }

    public ConsumerInfo(ServiceType type, Dictionary<String, ?> properties)
    {
        this.type = type;
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        PropertiesUtils.filterCopy((Dictionary<String, Object>)properties, dic, HOLA.CATEGORY_KEY);
        dic.put(HOLA.CATEGORY_KEY,type.getCategory());
        this.properties = properties;
    }

    public ServiceType geServiceType() {
        return type;
    }

    public Dictionary<String, ?> getServiceProperties() {
        return properties;
    }
}
