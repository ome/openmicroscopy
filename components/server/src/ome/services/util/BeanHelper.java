/*
 *   $Id$
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import ome.conditions.InternalException;
import ome.system.OmeroContext;
import ome.system.SelfConfigurableService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextAware;

/**
 * Helper for all bean implementations. This allows us to (largely) not subclass
 * a single abstract bean.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-Beta2
 */
public class BeanHelper {

    private transient Class beanClass;
    
    private transient Logger logger;

    private transient OmeroContext applicationContext;

    public BeanHelper(Class implementationClass) {
        beanClass = implementationClass;
        logger = LoggerFactory.getLogger(beanClass);
    }
    
    // ~ Self-configuration
    // =========================================================================

    /**
     * Lazy loads the application context, which means that if we're not in
     * the application server, then the {@link OmeroContext#MANAGED_CONTEXT} 
     * should never get loaded.
     */
    public final void acquireContext() {
        if (this.applicationContext == null) {
            this.applicationContext = OmeroContext.getManagedServerContext();
        }
    }

    public final void configure(SelfConfigurableService bean) {
        this.acquireContext();
        // This will, in turn, call throwIfAlreadySet
        this.applicationContext.applyBeanPropertyValues(bean,
                bean.getServiceInterface());
        // FIXME setApplicationContext should be called properly (I think?)
        // However, we're going to do it here anyway.
        if (bean instanceof ApplicationContextAware) {
            ApplicationContextAware aca = (ApplicationContextAware) bean;
            aca.setApplicationContext(applicationContext);
        }
    }

    // ~ Helpers
    // =========================================================================

    public void throwIfAlreadySet(Object current, Object injected) {
        if (current != null) {
            throw new InternalException(String.format("%s already configured "
                    + "with %s cannot set inject %s.", this.getClass()
                    .getName(), current, injected));
        }
    }

    public void passivationNotAllowed() {
        throw new InternalException(
                String
                        .format(
                                "Passivation should have been disabled for this Stateful Session Beans (%s).\n"
                                        + "Please contact the Omero development team for how to ensure that passivation\n"
                                        + "is disabled on your application server.",
                                this.getClass().getName()));
    }

    public Exception translateException(Throwable t) {
        if (Exception.class.isAssignableFrom(t.getClass())) {
            return (Exception) t;
        } else {
            InternalException ie = new InternalException(t.getMessage());
            ie.setStackTrace(t.getStackTrace());
            return ie;
        }
    }
    
    public Logger getLogger() {
        return this.logger;
    }

    public String getLogString(SelfConfigurableService bean) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean ");
        sb.append(bean);
        sb.append("\n with Context ");
        sb.append(applicationContext);
        return sb.toString();
    }

}
