package org.solmix.hola.builder.blueprint;

import java.util.UUID;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.solmix.commons.util.Assert;
import org.solmix.commons.util.DOMUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ServiceDefinition;
import org.solmix.runtime.Container;
import org.solmix.runtime.support.blueprint.AbstractBPBeanDefinitionParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


public class ServiceDefinitionParser extends AbstractBPBeanDefinitionParser
{

    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata meta = context.createMetadata(MutableBeanMetadata.class);
        String id = getIdOrName(element);
        if (StringUtils.isEmpty(id)) {
            meta.setId("service-" + UUID.randomUUID().toString());
        } else {
            meta.setId(id);
        }
        meta.setRuntimeClass(ServiceDefinition.class);
        parseAttributes(element, context, meta);
        parseChildElements(element, context, meta);
        meta.addProperty("id", createValue(context, id));
        
        Metadata impl = null;
        String ref=  element.getAttribute("ref");
        if(!StringUtils.isEmpty(ref)){
            impl=createRef(context, ref);
        }else{
            Element el = DOMUtils.getFirstElement(element);
            while (el != null) {
                String name = el.getLocalName();
                if("ref".equals(name)){
                    impl = context.parseElement(Metadata.class, meta, el);
                    break;
                }
                el = DOMUtils.getNextElement(el);
            }
        }
        
        Assert.assertNotNull(impl,"ref instance is null,please checkout <hola:service ref=\"\" ..>");
        
        meta.addProperty("ref", impl);
        
        String  containerId =element.getAttribute("container");
        if(StringUtils.isEmpty(containerId)){
            containerId="solmix";
        }
        meta.addArgument(this.getContainerRef(context, containerId), Container.class.getName(), 0);
        meta.addArgument(impl, Object.class.getName(), 1);
        meta.setInitMethod("register");
        meta.setDestroyMethod("unregister");
        return meta;
    }
    @Override
    protected boolean parseAttributes(Element element, ParserContext ctx, MutableBeanMetadata bean) {
        NamedNodeMap atts = element.getAttributes();
        boolean setContainers = false;
        for (int i = 0; i < atts.getLength(); i++) {
            Attr node = (Attr) atts.item(i);
            String val = node.getValue();
            String pre = node.getPrefix();
            String name = node.getLocalName();
            String prefix = node.getPrefix();
            if (isNamespace(name, prefix)) {
                continue;
            }
            if ("createdFromAPI".equals(name) || "abstract".equals(name)) {
                bean.setScope(BeanMetadata.SCOPE_PROTOTYPE);
            } else {
                if ("depends-on".equals(name)) {
                    bean.addDependsOn(val);
                } else if (!"id".equals(name) && isAttribute(pre, name)) {
                    mapAttribute(bean, element, name, val, ctx);
                }
            }
        }
        return setContainers;
    }
    
    @Override
    protected boolean isAttribute(String pre, String name) {
        return super.isAttribute(pre, name)
            && !"ref".equals(name)
            && !"container".equals(name);
    }
    
    @Override
    protected void mapToProperty(MutableBeanMetadata bean, String propertyName, String val, ParserContext context) {
        if (!StringUtils.isEmpty(val)) {
            if ("provider".equals(propertyName)||"module".equals(propertyName)||"monitor".equals(propertyName)||"application".equals(propertyName)) {
                bean.addProperty(propertyName, createRef(context, val));
            } else if ("discovery".equals(propertyName)) {
                bean.addProperty("discoveries", mapMultiProperty(bean,propertyName,val,context));
            } else {
                bean.addProperty(propertyName, createValue(context, val));
            }
        }
    }
    
    @Override
    protected void parseElement(ParserContext ctx, MutableBeanMetadata bean, Element el, String name) {
    
        if ("properties".equals(name)) {
            bean.addProperty(name, parseMapData(ctx, bean, el));
        } else  {
          super.parseElement(ctx, bean, el, name);
        }
    }

}
