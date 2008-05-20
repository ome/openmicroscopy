/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.api.IQuery;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

/**
 * Delegate to {@link IQuery#findAllByQuery(String, Parameters)}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class HqlQuery extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String query;
    private final Parameters p;

    public HqlQuery(SearchValues values, String query, Parameters p) {
        super(values);
        if (query == null || query.length() < 1) {
            throw new IllegalArgumentException("Query string must be non-empty");
        }
        this.query = query;
        this.p = p;
    }

    public Object doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        return sf.getQueryService().findAllByQuery(query, p);
    }

}
