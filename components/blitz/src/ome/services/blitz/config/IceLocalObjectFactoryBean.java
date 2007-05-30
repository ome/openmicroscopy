/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.config;

import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * FactoryBean that creates an ICE {@lik Ice.Communicator} instance (or a
 * decorator that implements that interface).
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public abstract class IceLocalObjectFactoryBean implements FactoryBean,
        BeanNameAware, DisposableBean, InitializingBean,
        ApplicationContextAware {

    protected final Log logger = LogFactory.getLog(getClass());

    protected Ice.LocalObject obj;

    protected String beanName;

    protected OmeroContext context;

    /** Describes which communicator instance this is */
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public final Object getObject() {
        return this.obj;
    }

    public Class getObjectType() {
        return (this.obj != null ? this.obj.getClass() : Ice.LocalObject.class);
    }

    public final boolean isSingleton() {
        return true;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
    }

    public void destroy() throws Exception {
        boolean log = logger.isDebugEnabled();
        if (this.obj != null) {
            if (log) {
                this.logger.debug(String.format("Destroying %s (%s)", beanName,
                        this.obj));
            }
            doDestroy();
            this.obj = null;
        } else {
            if (log) {
                this.logger.debug(beanName + " is null. Cannot destroy.");
            }
        }
    }

    public abstract void doDestroy() throws Exception;

}
