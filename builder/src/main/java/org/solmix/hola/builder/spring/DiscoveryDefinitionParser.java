package org.solmix.hola.builder.spring;

import java.util.Map;
import java.util.UUID;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.DiscoveryDefinition;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class DiscoveryDefinitionParser extends AbstractBeanDefinitionParser {
	public DiscoveryDefinitionParser() {
		super();
		setBeanClass(DiscoveryDefinition.class);
	}

	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext ctx) {
		String name = getIdOrName(element);
		if (StringUtils.isEmpty(name)) {
			name = "module-" + UUID.randomUUID().toString();
		}
		return name;
	}

	@Override
	protected void parseElement(ParserContext ctx, BeanDefinitionBuilder bean,
			Element e, String name) {
		if ("properties".equals(name)) {
			Map<?, ?> map = ctx.getDelegate().parseMapElement(e,
					bean.getBeanDefinition());
			bean.addPropertyValue("properties", map);
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
