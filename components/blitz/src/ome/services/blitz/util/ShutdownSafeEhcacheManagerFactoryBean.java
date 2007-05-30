/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

/**
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public class ShutdownSafeEhcacheManagerFactoryBean extends EhCacheManagerFactoryBean {

    private static final Log logger = LogFactory
            .getLog(ShutdownSafeEhcacheManagerFactoryBean.class);

    @Override
    public void destroy() {
        try {
            super.destroy();
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Shutdown in progress")) {
                // ignore. It's because we're closing the application context
                // during shutdown.
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring \"Shutdown in progress\" error.");
                }
            } else {
                throw e;
            }
        }
    }
    
}
