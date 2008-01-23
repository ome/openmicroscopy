/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.services.SearchBean;

/**
 * Serializable action used by {@link SearchBean} to generate results lazily.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class SearchAction implements ome.services.util.Executor.Work {

    protected final SearchValues values = new SearchValues();

    public SearchAction(SearchValues values) {
        if (values == null) {
            throw new IllegalArgumentException(
                    "SearchValues argument must not be null");
        }
        this.values.copy(values);
    }

}
