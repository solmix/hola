package org.solmix.hola.builder.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.runtime.support.spring.AbstractBeanDefinitionParser;


public class SpringDefinitionParser extends AbstractBeanDefinitionParser
{
    private static final Logger LOG = LoggerFactory.getLogger(SpringDefinitionParser.class);
    
    private final Class<?> beanClass;
    
    private final boolean requiredId;
    public SpringDefinitionParser(Class<?> beanClass,boolean requiredId){
        super();
        this.beanClass=beanClass;
        this.requiredId=requiredId;
        
        setBeanClass(beanClass);
    }

}
