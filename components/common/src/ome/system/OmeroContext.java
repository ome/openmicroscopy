package ome.system;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class OmeroContext extends ClassPathXmlApplicationContext
{
    public final static String CLIENT_CONTEXT = "ome.client";
    public final static String MANAGED_CONTEXT = "ome.server";
    public final static String INTERNAL_CONTEXT = "ome.internal";

    // ~ Super-Constructors
    // =========================================================================
    public OmeroContext(String configLocation) 
    throws BeansException {
        super(configLocation);
    }

    public OmeroContext(String[] configLocations) 
    throws BeansException {
        super(configLocations);
    }

    public OmeroContext(String[] configLocations, boolean refresh) 
    throws BeansException {
        super(configLocations,refresh);
    }
    
    public OmeroContext(String[] configLocations, ApplicationContext parent)
    throws BeansException {
        super(configLocations, parent);
    }

    public OmeroContext(String[] configLocations, boolean refresh, ApplicationContext parent)
    throws BeansException {
        super(configLocations,refresh,parent);
    }

    
    // ~ Creation
    // =========================================================================

    public static OmeroContext getClientContext()
    {
        return getInstance(CLIENT_CONTEXT);
    }
    
    public static OmeroContext getInternalServerContext()
    {
        return getInstance(INTERNAL_CONTEXT);
    }
    
    public static OmeroContext getManagedServerContext()
    {
        return getInstance(MANAGED_CONTEXT);
    }
    
    public static OmeroContext getInstance(String beanFactoryName)
    {
        OmeroContext ctx = (OmeroContext) 
        SingletonBeanFactoryLocator.getInstance()
            .useBeanFactory(beanFactoryName).getFactory();
        return ctx;
    }
    
    public void applyBeanPropertyValues(Object target, String beanName)
    {
        this.getAutowireCapableBeanFactory().
            applyBeanPropertyValues(target,beanName);
    }
    
}
