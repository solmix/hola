package org.solmix.hola.builder.spring;

import org.solmix.hola.builder.ServiceDefinition;
import org.solmix.runtime.Container;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ServiceBean<T> extends ServiceDefinition<T> implements InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener, BeanNameAware  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4814350457867017065L;

	private transient ApplicationContext applicationContext;

    private transient String beanName;
    
    public ServiceBean(Container c,T t){
    	super(c,t);
    }
	@Override
	public void setBeanName(String name) {
		 this.beanName = name;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent){
			if (isPublish()) {
                if (logger.isInfoEnabled()) {
                    logger.info("The service ready on spring started. service: " + getInterface());
                }
                register();
            }
		}
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;
	}

	@Override
	public void destroy() throws Exception {
		unregister();
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
