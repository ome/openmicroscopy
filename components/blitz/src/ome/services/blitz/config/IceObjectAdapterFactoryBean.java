/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.config;

import java.util.Map;

/**
 * FactoryBean that creates an {@lik Ice.ObjectAdapter} instance (or a decorator
 * that implements that interface).
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public class IceObjectAdapterFactoryBean extends IceLocalObjectFactoryBean {

    private Map<String, Ice.ServantLocator> locators;

    private Map<String, Ice.ObjectImpl> servants;

    public void setLocators(Map<String, Ice.ServantLocator> locs) {
        this.locators = locs;
    }

    public void setServants(Map<String, Ice.ObjectImpl> srvs) {
        this.servants = srvs;
    }

    public void afterPropertiesSet() throws Exception {
        // We can do nothing here. We have to wait for the 
        // Communicator to be created.
    }
    
    public void initialize(Ice.Communicator communicator) throws Exception {

        Ice.ObjectAdapter adapter = communicator.createObjectAdapter(beanName);

        if (locators == null && servants == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("No ServantLocators specified for "
                        + "ObjectAdapter %s (%s)", beanName, this));
            }
        }
        
        if (locators != null) {
            for (String category : locators.keySet()) {
                Ice.ServantLocator locator = locators.get(category);
                adapter.addServantLocator(locator, category);
            }
        }
   
        
        if (servants != null) {
            for (String s : servants.keySet()) {
                adapter.add(servants.get(s), Ice.Util.stringToIdentity(s));
            }
        }
        
        adapter.activate();
        obj = adapter;
    }

    public Class getObjectType() {
        return (this.obj != null ? this.obj.getClass()
                : Ice.ObjectAdapter.class);
    }

    @Override
    public void doDestroy() throws Exception {
        // Can do nothign here. See afterPropertiesSet for why.
    }

}
