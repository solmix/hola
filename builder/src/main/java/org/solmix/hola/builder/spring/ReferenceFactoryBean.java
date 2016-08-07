package org.solmix.hola.builder.spring;

import org.solmix.hola.builder.ReferenceDefinition;
import org.solmix.runtime.Container;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceFactoryBean<T> extends ReferenceDefinition<T> implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean{

	private static final long serialVersionUID = 3298068050925141509L;
	private transient ApplicationContext applicationContext;
	
	public ReferenceFactoryBean(Container container){
		super(container);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
	}

	@Override
	public Object getObject() throws Exception {
		return refer();
	}

	@Override
	public Class getObjectType() {
		return getInterfaceClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
