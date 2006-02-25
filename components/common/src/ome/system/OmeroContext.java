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

    private static OmeroContext _client;
    private static OmeroContext _internal;
    private static OmeroContext _managed;;
    
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

    private final static Object mutex = new Object();
    
    public static OmeroContext getClientContext()
    {
        synchronized (mutex)
        {
            if (_client == null) 
                _client = getInstance(CLIENT_CONTEXT); 
            
            return _client; 
        }    
    }
    
    public static OmeroContext getInternalServerContext()
    {
        synchronized (mutex)
        {
            if (_internal== null) 
                _internal = getInstance(INTERNAL_CONTEXT); 
            
            return _internal; 
        }
    }
    
    public static OmeroContext getManagedServerContext()
    {
        synchronized (mutex)
        {
            if (_managed == null) 
                _managed = getInstance(MANAGED_CONTEXT); 
            
            return _managed; 
        }
        
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
