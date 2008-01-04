/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.io.Serializable;

import ome.api.Search;
import ome.services.SearchBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class QueryTemplate implements Serializable {

    private final static Log log = LogFactory.getLog(QueryTemplate.class);

    boolean caseSensitive = Search.DEFAULT_CASE_SENSITIVTY;
    int batchSize = Search.DEFAULT_BATCH_SIZE;
    boolean mergedBatches = Search.DEFAULT_MERGED_BATCHES;

    public QueryTemplate() {
    }

}
