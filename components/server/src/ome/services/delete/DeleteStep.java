/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.LinkedList;
import java.util.List;

import ome.api.IDelete;
import ome.model.IObject;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 * @see ticket:3031
 */
public class DeleteStep {

    final private static Log log = LogFactory.getLog(DeleteStep.class);

    /**
     * Location of this step in {@link DeleteState#steps}.
     */
    final int idx;

    /**
     * Stack of other {@link DeleteStep} instances which show where this step is
     * in the entire graph.
     */
    final LinkedList<DeleteStep> stack;

    /**
     * Final member of {@link #stack} which is the direct ancestor of this
     * step.
     */
    final DeleteStep parent;

    /**
     * {@link DeleteSpec} instance which is active for this step.
     */
    final DeleteSpec spec;

    /**
     * {@link DeleteEntry} instance which is active for this step.
     */
    final DeleteEntry entry;

    /**
     * Ids of each element in the path to this node. For example, if we are
     * querying /Dataset/DatasetImageLink/Image then this contains: [4, 2, 1]
     * where 4 is the id of the dataset, and 1 is the id of the image.
     */
    final List<Long> ids;

    /**
     * The actual id to be deleted as opposed to {@link DeleteEntry#getId()}
     * which is the id of the root object.
     *
     * @see #ids
     */
    final long id;

    /**
     * Parsed table name used for the SQL/HQL statements.
     */
    final String table;

    /**
     * Type of object which is being deleted, using during
     * {@link DeleteState#release(String)} to send an {@link EventLogMessage}.
     */
    final Class<IObject> iObjectType;

    /**
     * String representation of the path to this {@link DeleteEntry} used for
     * logging.
     */
    final String pathMsg;

    /**
     * Information as to the current login.
     */
    final EventContext ec;

    /**
     * Not final. Set during {@link DeleteState#execute(int)}. If anything goes
     * wrong, it and possibly other instances from {@link #stack} will have
     * their savepoints rolled back.
     */
    String savepoint = null;

    /**
     * Not final. Incremented each time a child is created for whom the current
     * instance is their {@link #parent}.
     */
    int activeChildren = 0;

    DeleteStep(int idx, List<DeleteStep> stack, DeleteSpec spec, DeleteEntry entry,
            List<Long> ids) {
        this.idx = idx;
        this.stack = new LinkedList<DeleteStep>(stack);
        if (this.stack.size() > 0) {
            this.parent = this.stack.getLast();
            this.parent.activeChildren++;
        } else {
            this.parent = null;
        }
        this.spec = spec;
        this.entry = entry;
        this.ids = ids;
        this.id = ids == null ? -1L : ids.get(ids.size() - 1);
        this.ec = spec.getCurrentDetails().getCurrentEventContext();

        if (entry != null) {
            final String[] path = entry.path(entry.getSuperSpec());
            table = path[path.length - 1];
            pathMsg = StringUtils.join(path, "/");
            iObjectType = spec.getHibernateClass(table);
        } else {
            table = null;
            pathMsg = null;
            iObjectType = null;
        }
    }

    public void push(DeleteOpts opts) throws DeleteException {
        for (DeleteStep parent : stack) {
            parent.entry.push(opts, parent.ec);
        }
        entry.push(opts, ec);
    }

    public void pop(DeleteOpts opts) {
        for (DeleteStep parent : stack) {
            parent.entry.pop(opts);
        }
        entry.pop(opts);
    }

}
