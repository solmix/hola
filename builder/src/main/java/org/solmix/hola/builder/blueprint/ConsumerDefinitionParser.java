package org.solmix.hola.builder.blueprint;

import java.util.UUID;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ConsumerDefinition;
import org.solmix.runtime.support.blueprint.AbstractBPBeanDefinitionParser;
import org.w3c.dom.Element;


public class ConsumerDefinitionParser extends AbstractBPBeanDefinitionParser
{


    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata meta = context.createMetadata(MutableBeanMetadata.class);
        String id = getIdOrName(element);
        if (StringUtils.isEmpty(id)) {
            meta.setId("consumer-" + UUID.randomUUID().toString());
        } else {
            meta.setId(id);
        }
        meta.setRuntimeClass(ConsumerDefinition.class);
        parseAttributes(element, context, meta);
        parseChildElements(element, context, meta);
        meta.addProperty("id", createValue(context, id));
        return meta;
    }
    
    @Override
    protected void mapToProperty(MutableBeanMetadata bean, String propertyName, String val, ParserContext context) {
        if (!StringUtils.isEmpty(val)) {
            if ("module".equals(propertyName)||"monitor".equals(propertyName)||"application".equals(propertyName)) {
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
        }else if ("reference".equals(name)) {
            ctx.getComponentDefinitionRegistry()
            .registerComponentDefinition((ComponentMetadata)new ReferenceDefinitionParser().parse(el, ctx));
        } else  {
          super.parseElement(ctx, bean, el, name);
        }
    }
}
