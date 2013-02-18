/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import ome.conditions.SessionException;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.state.SessionCache;
import ome.system.EventContext;
import ome.system.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Prevents methods from setting the transaction to readOnly = false when a
 * share is activated.
 */
public class ShareRestrictionTransactionAttributeSource implements
        TransactionAttributeSource {

    final private static Logger log = LoggerFactory
            .getLogger(ShareRestrictionTransactionAttributeSource.class);

    final private CurrentDetails current;

    final private SessionCache cache;
    
    public ShareRestrictionTransactionAttributeSource(CurrentDetails details, SessionCache cache) {
        this.current = details;
        this.cache = cache;
    }

    public TransactionAttribute getTransactionAttribute(Method method,
            Class targetClass) {
        try {
            Principal principal = current.getLast();
            String uuid = principal.getName();
            EventContext ec = cache.getSessionContext(uuid);
            Long shareId = ec.getCurrentShareId();
            if (ec.getCurrentShareId() != null) {
                log.debug("Returning readOnly tx for shared " + shareId);
                DefaultTransactionAttribute ta = new DefaultTransactionAttribute();
                ta.setReadOnly(true);
                return ta;
            }
        } catch (SessionException se) {
            // No worries. It's not our job to enforce anything.
            return null;
        } catch (NoSuchElementException nse) {
            // No one is logged in so there can't be a share active!
            return null;
        }
        return null;
    }
}
