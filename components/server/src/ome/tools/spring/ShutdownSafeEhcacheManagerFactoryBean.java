/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

/**
 * Workaround for the Spring/EHCache shutdown sequence. Catches the {@link IllegalStateException}
 * which gets thrown on {@link #destroy()} and simply logs a message.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ShutdownSafeEhcacheManagerFactoryBean extends EhCacheManagerFactoryBean {

    private static final Logger log = LoggerFactory
            .getLogger(ShutdownSafeEhcacheManagerFactoryBean.class);

    @Override
    public void destroy() {
        try {
            super.destroy();
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Shutdown in progress")) {
                // ignore. It's because we're closing the application context
                // during shutdown.
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring \"Shutdown in progress\" error.");
                }
            } else {
                throw e;
            }
        }
    }
    
}
