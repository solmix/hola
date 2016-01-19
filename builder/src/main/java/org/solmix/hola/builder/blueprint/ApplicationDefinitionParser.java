package org.solmix.hola.builder.blueprint;

import java.util.UUID;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ApplicationDefinition;
import org.solmix.runtime.support.blueprint.AbstractBPBeanDefinitionParser;
import org.w3c.dom.Element;


public class ApplicationDefinitionParser extends AbstractBPBeanDefinitionParser
{

    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata meta = context.createMetadata(MutableBeanMetadata.class);
        String id = getIdOrName(element);
        if (StringUtils.isEmpty(id)) {
            meta.setId("application-" + UUID.randomUUID().toString());
        } else {
            meta.setId(id);
        }
        meta.setRuntimeClass(ApplicationDefinition.class);
        parseAttributes(element, context, meta);
        meta.addProperty("id", createValue(context, id));
        return meta;
    }
    
    @Override
    protected void mapToProperty(MutableBeanMetadata bean, String propertyName, String val, ParserContext context) {
        if (!StringUtils.isEmpty(val)) {
            if ("monitor".equals(propertyName)) {
                bean.addProperty(propertyName, createRef(context, val));
            } else if ("discovery".equals(propertyName)) {
                bean.addProperty("discoveries", mapMultiProperty(bean,propertyName,val,context));
            } else {
                bean.addProperty(propertyName, createValue(context, val));
            }
        }
    }
   
}
