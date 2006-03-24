/*
 * ome.system.OmeroContext
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.system;

//Java imports

//Third-party libraries
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//Application-internal dependencies

/**
* global application context. 
* 
* @author <br>
*         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
*         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
* @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
*          </small>
* @since OME3.0
*/
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
        ContextSingletonBeanFactoryLocator.getInstance()
            .useBeanFactory(beanFactoryName).getFactory();
        return ctx;
    }
    
    public void applyBeanPropertyValues(Object target, String beanName)
    {
        this.getAutowireCapableBeanFactory().
            applyBeanPropertyValues(target,beanName);
    }
    
}
