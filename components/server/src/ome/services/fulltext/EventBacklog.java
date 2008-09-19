/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.meta.EventLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread-safe java.util-like Container for storing {@link EventLog} instances
 * for later parsing. This container, however, will not add more than two
 * {@link EventLog logs} with the same (id, eventType, action) tuple.
 * 
 * Further, the container can only either be in the adding state or the removing
 * state. A newly created {@link EventBacklog} is in the adding state. The
 * popping state is entered the first time that {@link #remove()} is called. And
 * the adding state will only be re-entered, once {@link #remove()} has returned
 * null.
 * 
 * All calls to {@link #add(EventLog)} while in the popping state will return
 * false.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3.1
 */
public class EventBacklog {

    final private static Log logger = LogFactory.getLog(EventBacklog.class);

    final Map<Long, Map<String, Set<String>>> contained = new HashMap<Long, Map<String, Set<String>>>();

    final List<EventLog> logs = new ArrayList<EventLog>();

    /**
     * Switch between the adding and the removing states.
     */
    protected boolean adding = true;

    /**
     * Adds the given {@link EventLog} instance to the end of a queue for later
     * {@link #remove() popping} if no equivalent {@link EventLog} is present.
     * Equivalence is based on the entityType, entityId, and action fields.
     * Records tracking information to prevent the same {@link EventLog} from
     * being re-added before the last instance is removed.
     */
    public synchronized boolean add(EventLog log) {

        if (!adding) {
            if (logger.isInfoEnabled()) {
                logger.info("Backlog locked:" + log.getEntityType() + ":Id_"
                        + log.getEntityId());
            }
            return false;
        }

        if (log == null || log.getEntityType() == null
                || log.getEntityId() == null || log.getAction() == null) {
            throw new IllegalArgumentException(
                    "EventLog must contain entityType, entityId, and action");
        }

        Map<String, Set<String>> class2action = contained
                .get(log.getEntityId());

        if (class2action == null) {
            class2action = new HashMap<String, Set<String>>();
            contained.put(log.getEntityId(), class2action);
        }

        Set<String> actions = class2action.get(log.getEntityType());
        if (actions == null) {
            actions = new HashSet<String>();
            class2action.put(log.getEntityType(), actions);
        }

        boolean contained = actions.contains(log.getAction());
        if (contained) {
            if (logger.isInfoEnabled()) {
                logger.info("Already in backlog:" + log.getEntityType()
                        + ":Id_" + log.getEntityId());
            }
            return false;
        } else {
            actions.add(log.getAction());
            logs.add(log);
            if (logger.isInfoEnabled()) {
                logger.info("Added to backlog:" + log.getEntityType() + ":Id_"
                        + log.getEntityId());
            }
            return true;
        }
    }

    /**
     * Removes and returns the next {@link EventLog} instance or null if none is
     * present. Also cleans up any tracking information for the given
     * {@link EventLog}.
     * 
     * @return
     */
    public synchronized EventLog remove() {

        if (logs.size() == 0) {
            contained.clear();
            adding = true;
            return null; // EARLY EXIT
        }

        adding = false;
        EventLog log = logs.remove(0);

        // None of these can be null as long as the log is still contained
        Map<String, Set<String>> class2action = contained
                .get(log.getEntityId());
        Set<String> actions = class2action.get(log.getEntityType());
        assert actions.remove(log.getAction());
        if (actions.size() == 0) {
            class2action.remove(log.getEntityType());
            if (class2action.size() == 0) {
                contained.remove(log.getEntityId());
            }
        }

        return log;
    }

}