/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.parameters.Parameters;
import ome.services.SearchBean;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class HqlQuery extends QueryTemplate implements QueryBuilder {

    private static final long serialVersionUID = 1L;

    private final String query;
    private final Parameters p;

    public HqlQuery(String query, Parameters p) {
        if (query == null || query.length() < 1) {
            throw new IllegalArgumentException("Query string must be non-empty");
        }
        this.query = query;
        this.p = p;
    }

}
