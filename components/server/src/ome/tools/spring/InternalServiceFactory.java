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
 * subclass of ome.system.ServiceFactory which retrieves unmanaged ("internal")
 * {@link ome.api.ServiceInterface service-}instances. These have fewer (or no)
 * layers of AOP interceptors wrapping them.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public class InternalServiceFactory extends ServiceFactory implements
        ApplicationContextAware {

    @Override
    protected String getPrefix() {
        return "internal-";
    }

    /**
     * returns null to prevent the lookup of any context, but rather wait on
     * injection as a {@link ApplicationContextAware}
     */
    @Override
    protected String getDefaultContext() {
        return null;
    }

    /** default construtor */
    public InternalServiceFactory() {
        // use setApplicationContext to fill this instance.
    }

    /** @see ServiceFactory#ServiceFactory(OmeroContext) */
    public InternalServiceFactory(OmeroContext omeroContext) {
        super(omeroContext);
    }

    /**
     * simple injector for the {@link ApplicationContext}
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.ctx = (OmeroContext) applicationContext;
    }

    @Override
    public String toString() {
        return "InternalSF@"+hashCode();
    }
}
