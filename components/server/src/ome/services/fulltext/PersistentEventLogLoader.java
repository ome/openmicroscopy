/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.api.ITypes;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.services.messages.ReindexMessage;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * {@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Reseting that saved value would restart indexing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class PersistentEventLogLoader extends EventLogLoader implements
        ApplicationListener {

    private final static Log log = LogFactory
            .getLog(PersistentEventLogLoader.class);

    /**
     * Key used to look configuration value; 'name'
     */
    protected String key;

    /**
     * Query used with parameter 'name' to lookup configuration value
     */
    protected String query;

    /**
     * String use with parameters 'name', and 'value' to insert a new current id
     */
    protected String insert;

    /**
     * String used with parameters 'value' and 'name' to change the current id
     */
    protected String update;

    /**
     * String used with parameter 'name' to drop the current id;
     */
    protected String delete;

    protected ITypes types;

    protected SimpleJdbcTemplate template;

    public void setKey(String key) {
        this.key = key;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setInsert(String insert) {
        this.insert = insert;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public void setTypes(ITypes types) {
        this.types = types;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }

    @Override
    protected EventLog query() {

        long current_id;
        try {
            current_id = getCurrentId();
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

        synchronized (backlog) {
            if (backlog.size() > 0) {
                return backlog.remove(0); // EARLY EXIT.
            }
        }

        EventLog el = nextEventLog(current_id);
        if (el != null) {
            setCurrentId(el.getId());
        }
        return el;

    }

    /**
     * Called when the configuration database does not contain a valid
     * current_id. Used to index all the data which does not have an EventLog.
     */
    public void initialize() {
        for (Class<IEnum> cls : types.getEnumerationTypes()) {
            for (IEnum e : queryService.findAll(cls, null)) {
                addEventLog(cls, e.getId());
            }
        }
    }

    public long getCurrentId() {
        return template.queryForLong(query, key);
    }

    public void setCurrentId(long id) {
        int count = template.update(update, id, key);
        if (count == 0) {
            template.update(insert, key, id);
        }
    }

    public void deleteCurrentId() {
        template.update(delete, key);
    }

    @SuppressWarnings("unchecked")
    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof ReindexMessage) {
            ReindexMessage<? extends IObject> rm = (ReindexMessage<? extends IObject>) arg0;
            for (IObject obj : rm.objects) {
                Class trueClass = Utils.trueClass(obj.getClass());
                addEventLog(trueClass, obj.getId());
                if (log.isInfoEnabled()) {
                    log.info("Added to backlog:" + obj);
                }
            }
        }
    }

    /**
     * Adds a fake {@link EventLog} for the given {@link Class} and id.
     */
    protected void addEventLog(Class<? extends IObject> cls, long id) {
        EventLog el = new EventLog();
        el.setEntityId(id);
        el.setEntityType(cls.getName());
        el.setAction("INSERT");
        backlog.add(el);
    }
}