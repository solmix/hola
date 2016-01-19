package org.solmix.hola.builder.blueprint;

import java.util.UUID;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.DiscoveryDefinition;
import org.solmix.runtime.support.blueprint.AbstractBPBeanDefinitionParser;
import org.w3c.dom.Element;


public class DiscoveryDefinitionParser extends AbstractBPBeanDefinitionParser
{

    public Metadata parse(Element element, ParserContext context) {
        MutableBeanMetadata meta = context.createMetadata(MutableBeanMetadata.class);
        String id = getIdOrName(element);
        if (StringUtils.isEmpty(id)) {
            meta.setId("discovery-" + UUID.randomUUID().toString());
        } else {
            meta.setId(id);
        }
        meta.setRuntimeClass(DiscoveryDefinition.class);
        parseAttributes(element, context, meta);
        parseChildElements(element, context, meta);
        meta.addProperty("id", createValue(context, id));
        return meta;
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
