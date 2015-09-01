/*
 * ome.tools.spring.InternalServiceFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ome.system.OmeroContext;
import ome.system.ServiceFactory;

/**
 * subclass of ome.system.ServiceFactory which retrieves managed
 * {@link ome.api.ServiceInterface service-}instances. These have all the
 * necessary layers of AOP interceptors for proper functioning. In fact, the
 * returned services behave almost exactly as if they were in an application
 * server ("container").
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public class ManagedServiceFactory extends ServiceFactory implements
        ApplicationContextAware {

    @Override
    protected String getPrefix() {
        return "managed-";
    }

    /**
     * returns null to prevent the lookup of any context, but rather wait on
     * injection as a {@link ApplicationContextAware}
     */
    @Override
    protected String getDefaultContext() {
        return null;
    }

    /**
     * simple injector for the {@link ApplicationContext}
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.ctx = (OmeroContext) applicationContext;
    }

}
