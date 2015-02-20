/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.services.graphs.GraphStep;
import ome.services.graphs.GraphStepFactory;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

/**
 * State maintained for each element type in the {@link ExporterStepFactory}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ExporterIndex {

    final int indicesNeeded;

    final private List<GraphStep> steps = new ArrayList<GraphStep>();

    final private List<long[]> ids = new ArrayList<long[]>();

    public ExporterIndex(int indicesNeeded) {
        this.indicesNeeded = indicesNeeded;
    }

    public int size() {
        return this.steps.size();
    }

    public void add(GraphStep step, long[] ids) throws GraphException {

        if (ids == null) {
            throw new GraphException("Null ids");
        }

        this.steps.add(step);
        this.ids.add(ids);
    }

    /**
     * Returns the id of an object based on the order in which it was added.
     *
     * @param order
     *            order in which the looked for object was added via
     *            {@link #add(GraphStep, long[]). This value should be less than
     *            {@link #size()}
     * @return
     */
    public long getIdByOrder(int order) throws GraphException {

        if (order > size()) {
            throw new GraphException("Out of bounds.");
        }

        long[] ids = this.ids.get(order);
        return ids[ids.length - 1];
    }

}
