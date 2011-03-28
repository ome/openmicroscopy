/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.api.ITypes;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.meta.EventLog;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

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

    /**
     * Called when the configuration database does not contain a valid
     * current_id. Used to index all the data which does not have an EventLog.
     */
    @Override
    public void initialize() {
        for (Class<IEnum> cls : types.getEnumerationTypes()) {
            for (IEnum e : queryService.findAll(cls, null)) {
                addEventLog(cls, e.getId());
            }
        }
    }

}
