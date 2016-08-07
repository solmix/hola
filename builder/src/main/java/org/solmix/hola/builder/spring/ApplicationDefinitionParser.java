package org.solmix.hola.builder.spring;

import java.util.UUID;

import org.solmix.commons.util.StringUtils;
import org.solmix.hola.builder.ApplicationDefinition;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ApplicationDefinitionParser extends AbstractBeanDefinitionParser {

	public ApplicationDefinitionParser() {
		super();
		setBeanClass(ApplicationDefinition.class);
	}

	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext ctx) {
		String name = getIdOrName(element);
		if (StringUtils.isEmpty(name)) {
			name = "application-" + UUID.randomUUID().toString();
		}
		return name;
	}

	@Override
	protected void attributeToProperty(BeanDefinitionBuilder bean,
			String propertyName, String val, ParserContext ctx) {
		if (!StringUtils.isEmpty(val)) {
			if ("monitor".equals(propertyName)) {
				bean.addPropertyReference(propertyName, val);
			} else if ("discovery".equals(propertyName)) {
				parseMultiRef("discoveries", val, bean, ctx);
			} else {
				bean.addPropertyValue(propertyName, val);
			}
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
