/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.List;

import ome.api.IDelete;
import ome.services.graphs.AbstractStepFactory;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;

/**
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @see IDelete
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class DeleteStepFactory extends AbstractStepFactory {

    private final OmeroContext ctx;

    private final ExtendedMetadata em;

    public DeleteStepFactory(OmeroContext ctx, ExtendedMetadata em) {
        this.ctx = ctx;
        this.em = em;
    }

    public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException {
        return new DeleteStep(em, ctx, idx, stack, spec, entry, ids);
    }

}
