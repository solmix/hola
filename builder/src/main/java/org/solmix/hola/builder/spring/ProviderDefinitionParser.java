package org.solmix.hola.builder.spring;

import java.util.Map;
import java.util.UUID;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ProviderDefinition;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ProviderDefinitionParser   extends AbstractBeanDefinitionParser{
	public ProviderDefinitionParser() {
		super();
		setBeanClass(ProviderDefinition.class);
	}

	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext ctx) {
		String name = getIdOrName(element);
		if (StringUtils.isEmpty(name)) {
			name = "consumer-" + UUID.randomUUID().toString();
		}
		return name;
	}

	@Override
	protected void attributeToProperty(BeanDefinitionBuilder bean,
			String propertyName, String val, ParserContext ctx) {
		if (!StringUtils.isEmpty(val)) {
			if ("module".equals(propertyName)||"monitor".equals(propertyName)||"application".equals(propertyName)) {
				bean.addPropertyReference(propertyName, val);
			} else if ("discovery".equals(propertyName)) {
				parseMultiRef("discoveries", val, bean, ctx);
			} else {
				bean.addPropertyValue(propertyName, val);
			}
		}
	}
	@Override
	protected void parseElement(ParserContext ctx, BeanDefinitionBuilder bean,
			Element e, String name) {
		if ("properties".equals(name)) {
			Map<?, ?> map = ctx.getDelegate().parseMapElement(e,
					bean.getBeanDefinition());
			bean.addPropertyValue("properties", map);
		}else if ("service".equals(name)) {
			BeanDefinition bd = new ServiceDefinitionParser().parse(e, ctx);
			ctx.registerBeanComponent(new BeanComponentDefinition(bd,null));
			
		} else {
			super.parseElement(ctx, bean, e, name);
		}
	}

	@Override
	protected void parseIdAttribute(BeanDefinitionBuilder bean,
			Element element, String name, String val, ParserContext ctx) {
		bean.addPropertyValue(BeanDefinitionParserDelegate.ID_ATTRIBUTE, val);
	}

	@Override
	protected void parseNameAttribute(Element element, ParserContext ctx,
			BeanDefinitionBuilder bean, String val) {
		bean.addPropertyValue(BeanDefinitionParserDelegate.NAME_ATTRIBUTE, val);
	}
}
