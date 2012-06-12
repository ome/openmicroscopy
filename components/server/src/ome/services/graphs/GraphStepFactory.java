/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.List;

/**
 * strategy interface which can be passed to {@link GraphState} to create the {@link GraphStep} instances needed.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public interface GraphStepFactory {

    /**
     *
     * @param idx
     *            Index which is being assigned to this step.
     * @param stack
     *            Non-null stack of other steps which precede this one in the
     *            graph.
     * @param spec
     *            Non-null {@link GraphSpec} which was used to define this step.
     * @param entry
     *            Non-null {@link GraphEntry} which was used to define this
     *            step.
     * @param ids
     *            Non-null, non-empty array of ids which were found for this
     *            {@link GraphStep}
     * @return some specific {@link GraphStep} implementation which will permit
     *         arbitrary operations on all the nodes of the graph.
     *
     * @throws GraphException
     */
    GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException;

    /**
     * Gives the {@link GraphStepFactory} a chance to add pre or post-steps for
     * validation and similar activities.
     *
     * @param steps
     *            Non-null list of steps which are to be post-processed.
     * @return Either the original unmodified list, possibly with additions or
     *         deletions, or a new list entirely.
     */
    List<GraphStep> postProcess(List<GraphStep> steps);
}
