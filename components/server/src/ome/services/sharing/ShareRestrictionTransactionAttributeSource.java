/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import ome.api.IAdmin;
import ome.system.EventContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Prevents methods from setting the transaction to readOnly = false when a
 * share is activated.
 */
public class ShareRestrictionTransactionAttributeSource implements
        TransactionAttributeSource {

    final private static Log log = LogFactory
            .getLog(ShareRestrictionTransactionAttributeSource.class);

    final private IAdmin admin;

    public ShareRestrictionTransactionAttributeSource(IAdmin admin) {
        this.admin = admin;
    }

    public TransactionAttribute getTransactionAttribute(Method method,
            Class targetClass) {
        try {
            EventContext ec = admin.getEventContext();
            Long shareId = ec.getCurrentShareId();
            if (ec.getCurrentShareId() != null) {
                log.debug("Returning readOnly tx for shared " + shareId);
                DefaultTransactionAttribute ta = new DefaultTransactionAttribute();
                ta.setReadOnly(false);
                return ta;
            }
        } catch (NoSuchElementException nse) {
            // No one is logged in so there can't be a share active!
            return null;
        }
        return null;
    }
}
