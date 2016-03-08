package org.solmix.hola.builder.blueprint;

import java.util.Arrays;
import java.util.UUID;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ReferenceDefinition;
import org.solmix.runtime.Container;
import org.solmix.runtime.support.blueprint.AbstractBPBeanDefinitionParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


public class ReferenceDefinitionParser extends AbstractBPBeanDefinitionParser
{


    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata meta = context.createMetadata(MutableBeanMetadata.class);
       
        meta.setRuntimeClass(ReferenceDefinition.class);
        String id = getIdOrName(element);
        parseAttributes(element, context, meta);
        parseChildElements(element, context, meta);
        
        
        String  containerId =element.getAttribute("container");
        if(StringUtils.isEmpty(containerId)){
            containerId="solmix";
        }
//        meta.addProperty("container", getContainerRef(context, containerId));
        meta.addArgument(this.getContainerRef(context, containerId), Container.class.getName(), 0);
        
        String interfaceName  = element.getAttribute("interface");
        MutableBeanMetadata bean = context.createMetadata(MutableBeanMetadata.class);
        
        if (StringUtils.isEmpty(id)) {
            bean.setId("reference-" + UUID.randomUUID().toString());
        } else {
            bean.setId(id);
        }
        bean.setFactoryComponent(meta);
        bean.setFactoryMethod("refer");
        bean.setDependsOn(Arrays.asList(containerId));
        bean.setClassName(interfaceName);
        return bean;
    }
    @Override
    protected boolean parseAttributes(Element element, ParserContext ctx, MutableBeanMetadata bean) {
        NamedNodeMap atts = element.getAttributes();
        boolean setContainer = false;
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
        return setContainer;
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
    
   static class ReferenceFactory extends ReferenceDefinition{
        
    }

}
