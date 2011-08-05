/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.List;

import ome.api.IDelete;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.services.graphs.GraphStepFactory;
import ome.system.OmeroContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 */
public class ChgrpStepFactory implements GraphStepFactory {

    private final OmeroContext ctx;

    private long grp;

    public ChgrpStepFactory(OmeroContext ctx) {
        this.ctx = ctx;
    }

    public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException {
        return new ChgrpStep(ctx, idx, stack, spec, entry, ids, grp);
    }

    /**
     * Set the group id which will be passed to all new {@link ChgrpStep}
     * instances. Since a new {@link ChgrpStepFactory} is created for every
     * chgrp action, this is thread safe.
     *
     * @param grp
     */
    public void setGroup(long grp) {
        this.grp = grp;
    }
}
