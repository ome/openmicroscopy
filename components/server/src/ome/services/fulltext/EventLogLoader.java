/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.List;

import ome.api.local.LocalQuery;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;

/**
 * Data access object for the {@link FullTextIndexer} which provides some small
 * number of {@link EventLog} instances to be properly indexed. The default
 * implementation keeps tracks of the last {@link EventLog} instance, and only
 * provides new ones. Reseting that saved value would restart indexing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class EventLogLoader {

    private final static Parameters P = new Parameters();

    static {
        P.setFilter(new Filter().page(0, 10));
    }

    private LocalQuery queryService;

    public void setQueryService(LocalQuery queryService) {
        this.queryService = queryService;
    }

    public List<EventLog> nextBatch() {

        return this.queryService.findAllByQuery("select el from EventLog el "
                + "order by id desc", P);
    }

}
