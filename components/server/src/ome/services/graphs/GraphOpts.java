/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.LinkedList;

import ome.system.EventContext;

/**
 * Manager for option instances for an entire action graph. As method calls are
 * made, this instance gets passed around and the appropriate {@link Op ops}
 * are {@link #push(Op) pushed} or {@link #pop() popped} changing the current
 * state of affairs.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 */
public class GraphOpts {

    public enum Op {

        /**
         * Default operation. If an action is not possible, i.e. it fails with a
         * {@link org.hibernate.exception.ConstraintViolationException} or
         * similar, then the failure will cause the entire command to fail as an
         * error.
         */
        HARD(false),

        /**
         * Graph is attempted, but the exceptions which would make a
         * {@link #HARD} operation fail lead only to warnings.
         */
        SOFT(false),

        /**
         * Prevents the action from being carried out. If an entry has a subspec,
         * then the entire subgraph will not be processed. In some cases,
         * specifically {@link AnnotationGraphSpec} this value may be
         * vetoed by {@link GraphSpec#overrideKeep()}.
         */
        KEEP(false),

        /**
         * Permits the use of force to remove objects even against the
         * permission system. (This option cannot override low-level
         * DB constraints)
         */
        FORCE(true),

        REAP(false),

        ORPHAN(false),

        /**
         * Nulls a particular field of the target rather than performing the
         * standard action on it.
         *
         * This is useful for situations where one user has generated data
         * from another user, as with projections.
         *
         * <em>WARNING:</em>Currently, NULL can only be used for the
         * Pixels.relatedTo relationship.
         */
        NULL(true);

        private final boolean restricted;

        Op(boolean restricted) {
            this.restricted = restricted;
        }

    }

    private final LinkedList<Op> list = new LinkedList<Op>();

    /**
     * Adds the given operation to the current list <em>if</em> the user
     * has permissions to do so.
     *
     * @param op Current {@link Op} to add to the stack
     * @param modified Whether or not the value was changed by the user
     * @param details Active user login
     */
    public void push(Op op, boolean modified, EventContext ec) throws GraphException {
        if (op.restricted && modified && ! ec.isCurrentUserAdmin()) {
            throw new GraphException("User " + ec.getCurrentUserId() +
                    " is not an admin and cannot set the operation to " +
                    op.toString());
        }
        list.add(op);
    }

    public void pop() {
        list.removeLast();
    }

    public boolean isForce() {
        return list.contains(Op.FORCE);
    }

}
