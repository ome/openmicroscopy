/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.services.SearchBean;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SomeMustNone extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String[] some;
    private final String[] must;
    private final String[] none;

    public SomeMustNone(SearchValues values, String[] some, String[] must,
            String[] none) {
        super(values);
        this.some = some;
        this.must = must;
        this.none = none;
        throw new IllegalArgumentException("Must check these");
    }

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        throw new UnsupportedOperationException();
    }
}
