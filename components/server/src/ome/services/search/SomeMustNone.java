/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.services.SearchBean;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SomeMustNone extends QueryTemplate implements QueryBuilder {

    private static final long serialVersionUID = 1L;

    private final String[] some;
    private final String[] must;
    private final String[] none;

    public SomeMustNone(String[] some, String[] must, String[] none) {
        this.some = some;
        this.must = must;
        this.none = none;
        throw new IllegalArgumentException("Must check these");
    }

}
