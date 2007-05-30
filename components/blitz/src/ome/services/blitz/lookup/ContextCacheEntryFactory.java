/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.lookup;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 12:28:54 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1175 $")
public class ContextCacheEntryFactory implements CacheEntryFactory,
        ApplicationContextAware {

    private final static Log log = LogFactory
            .getLog(ContextCacheEntryFactory.class);

    private OmeroContext context;

    public Object createEntry(Object key) throws Exception {
        return context.getBean("ice:"+key.toString());
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
    }

}
