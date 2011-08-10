/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.List;

import ome.api.IDelete;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.services.graphs.GraphStepFactory;
import ome.system.OmeroContext;

/**
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @see IDelete
 */
public class DeleteStepFactory implements GraphStepFactory {

    private final OmeroContext ctx;

    public DeleteStepFactory(OmeroContext ctx) {
        this.ctx = ctx;
    }

    public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException {
        return new DeleteStep(ctx, idx, stack, spec, entry, ids);
    }

    public List<GraphStep> postProcess(List<GraphStep> steps) {
        return steps;
    }
}
