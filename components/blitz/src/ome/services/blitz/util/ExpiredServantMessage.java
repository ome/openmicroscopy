/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import net.sf.ehcache.event.CacheEventListener;
import ome.util.messages.InternalMessage;

import org.springframework.context.ApplicationEvent;

/**
 * {@link ApplicationEvent} which gets raised on
 * {@link CacheEventListener#notifyElementExpired(net.sf.ehcache.Ehcache, net.sf.ehcache.Element)}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class ExpiredServantMessage extends InternalMessage {

    private static final long serialVersionUID = 3409582093802L;

    private final String key;

    public ExpiredServantMessage(Object source, String serviceKey) {
        super(source);
        this.key = serviceKey;
    }

    public String getServiceKey() {
        return this.key;
    }

}