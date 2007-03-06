/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.icy.config;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that creates an {@lik Ice.ObjectPrx} instance (or a decorator
 * that implements that interface).
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public class IceObjectPrxFactoryBean implements FactoryBean {

    private static final Log logger = LogFactory
            .getLog(IceObjectPrxFactoryBean.class);

    private Ice.Communicator communicator = null;

    private boolean useDefaultRouter = false;

    private Ice.Router router = null;

    private String identity = null;

    private String connection = null;

    private Ice.ObjectPrx obj;
    
    public void setCommunicator(Ice.Communicator ic) {
        this.communicator = ic;
    }

    public void setUseDefaultRouter(boolean use) {
        this.useDefaultRouter = use;
    }

    public void setRouter(Ice.Router iceRouter) {
        this.router = iceRouter;
    }

    public void setIdentity(String id) {
        this.identity = id;
    }

    public void setConnection(String conn) {
        this.connection = conn;
    }

    public void afterPropertiesSet() throws Exception {

        if (this.communicator == null || this.identity == null
                || this.connection == null) {
            throw new IllegalStateException("ObjectPrx creation requires an"
                    + "identity, a connection and an active communicator.");
        }

        if (this.router != null || this.useDefaultRouter) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using routing.");
            }
        }

        String proxyStr = getProxyString();
        Ice.ObjectPrx base = communicator.stringToProxy(proxyStr);
        if (base == null) {
            throw new RuntimeException("Cannot create proxy for " + proxyStr);
        }
        this.obj = base;
        // FIXME unsure if we need a cast here.
    }

    public Class getObjectType() {
        return (this.obj != null ? this.obj.getClass() : Ice.ObjectPrx.class);
    }

    public Object getObject() throws Exception {
        return this.obj;
    }

    public boolean isSingleton() {
        return true;
    }
    
    public String getProxyString() {
        String proxyStr = identity + ":" + connection;
        return proxyStr;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getProxyString());
        sb.append("(");
        sb.append(this.getClass());
        sb.append(")");
        return sb.toString();
    }

}
