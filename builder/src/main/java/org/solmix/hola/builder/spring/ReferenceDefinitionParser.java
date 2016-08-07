package org.solmix.hola.builder.spring;

import java.util.Map;
import java.util.UUID;

import org.solmix.commons.util.StringUtils;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ReferenceDefinitionParser extends AbstractBeanDefinitionParser {
	public ReferenceDefinitionParser() {
		super();
		setBeanClass(ReferenceFactoryBean.class);
	}

	 @Override
	    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
		 super.doParse(element, ctx, bean);
		 String  containerId =element.getAttribute("container");
	        if(StringUtils.isEmpty(containerId)){
	            containerId="solmix";
	        }
		 bean.addConstructorArgReference(containerId);
		 bean.addDependsOn(containerId);
	 }
	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext ctx) {
		String name = getIdOrName(element);
		if (StringUtils.isEmpty(name)) {
			name = "reference-" + UUID.randomUUID().toString();
		}
		return name;
	}

	@Override
	protected boolean isAttribute(String pre, String name) {
		return super.isAttribute(pre, name) && !"ref".equals(name)
				&& !"container".equals(name);
	}

	@Override
	protected void attributeToProperty(BeanDefinitionBuilder bean,
			String propertyName, String val, ParserContext ctx) {
		if (!StringUtils.isEmpty(val)) {
			if ("consumer".equals(propertyName)
					|| "module".equals(propertyName)
					|| "monitor".equals(propertyName)
					|| "application".equals(propertyName)) {
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
