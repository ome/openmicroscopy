/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.eventlogs;

import ome.api.ITypes;
import ome.conditions.InternalException;
import ome.model.meta.EventLog;
import ome.util.SqlAction;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * {@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Resetting that saved value would restart indexing.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class PersistentEventLogLoader extends EventLogLoader {

    /**
     * Key used to look configuration value; 'name'
     */
    protected String key;

    protected ITypes types;

    protected SqlAction sql;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setTypes(ITypes types) {
        this.types = types;
    }

    public void setSqlAction(SqlAction sql) {
        this.sql = sql;
    }

    @Override
    protected EventLog query() {

        long current_id = getCurrentId();

        EventLog el = nextEventLog(current_id);
        if (el != null) {
            setCurrentId(el.getId());
        }
        return el;

    }

    /**
     * Called when the configuration database does not contain a valid
     * current_id.
     */
    public abstract void initialize();

    /**
     * Get current {@link EventLog} id. If the lookup throws an exception,
     * either the configuration has been deleted or renamed, in which we need to
     * reinitialize, or the table is missing and something is wrong.
     */
    public long getCurrentId() {
        long current_id;
        try {
            current_id = sql.selectCurrentEventLog(key);
        } catch (EmptyResultDataAccessException erdae) {
            // This event log loader has never been run. Initialize
            current_id = -1;
            setCurrentId(-1);
            initialize();
        } catch (DataAccessException dae) {
            // Most likely there's no configuration table.
            throw new InternalException(
                    "The configuration table seems to be missing \n"
                            + "from your database. Please check your server installation instructions \n"
                            + "for possible reasons.");
        }
        return current_id;
    }

    public void setCurrentId(long id) {
        sql.setCurrentEventLog(id, key);
    }

    public void deleteCurrentId() {
        sql.delCurrentEventLog(key);
    }

    @Override
    public long more() {
        long diff = lastEventLog().getEntityId() - getCurrentId();
        return diff < 0 ? 0 : diff;
    }

}
