/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import ome.model.meta.EventLog;
import ome.services.eventlogs.EventLogLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Reseting that saved value would restart indexing.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class PersistentEventLogLoader extends ome.services.eventlogs.PersistentEventLogLoader {

    private final static Log log = LogFactory
            .getLog(PersistentEventLogLoader.class);

    protected final String repo;
    
    public PersistentEventLogLoader(String repo) {
        this.repo = repo;
    }
    
    @Override
    public void initialize() {
        // no-op
    }
    
    @Override
    protected EventLog query() {
        final long current_id = getCurrentId();
        final Long next_id = sql.nextPixelsDataLogForRepo(repo, current_id);

        if (next_id == null) {
            return null;
        }

        final EventLog el = queryService.find(EventLog.class, next_id);
        if (el != null) {
            setCurrentId(el.getId());
        }
        return el;

    }

}
