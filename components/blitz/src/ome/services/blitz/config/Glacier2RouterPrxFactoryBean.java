/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.config;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import Ice.RouterPrx;

/**
 * FactoryBean that creates an {@lik Ice.ObjectPrx} instance (or a decorator
 * that implements that interface).
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public class Glacier2RouterPrxFactoryBean implements FactoryBean {

    private static final Log logger = LogFactory
            .getLog(Glacier2RouterPrxFactoryBean.class);

    private Ice.Communicator communicator = null;

    private boolean useDefaultRouter = false;

    private Glacier2.RouterPrx obj;
    
    public void setCommunicator(Ice.Communicator ic) {
        this.communicator = ic;
    }

    public void setUseDefaultRouter(boolean use) {
        this.useDefaultRouter = use;
    }

    public void afterPropertiesSet() throws Exception {

        if (this.communicator == null) {
            throw new IllegalStateException("RouterPrx creation requires an"
                    + "an active communicator.");
        }

//        if (this.router != null || this.useDefaultRouter) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Using routing.");
//            }
//        }

        RouterPrx defaultRouter = communicator.getDefaultRouter();
        Glacier2.RouterPrx router = 
            Glacier2.RouterPrxHelper.checkedCast(defaultRouter);
        this.obj = router;
    }

    public Class getObjectType() {
        return (this.obj != null ? this.obj.getClass() : Glacier2.RouterPrx.class);
    }

    public Object getObject() throws Exception {
        return this.obj;
    }

    public boolean isSingleton() {
        return true;
    }

}
