/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.List;

import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.services.graphs.GraphStepFactory;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 */
public class ChgrpStepFactory implements GraphStepFactory {

    private final OmeroContext ctx;

    private final ExtendedMetadata em;

    private final Roles roles;

    private long grp;

    public ChgrpStepFactory(OmeroContext ctx, ExtendedMetadata em, Roles roles) {
        this.ctx = ctx;
        this.em = em;
        this.roles = roles;
    }

    public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
            GraphEntry entry, long[] ids) throws GraphException {
        return new ChgrpStep(ctx, em, roles, idx, stack, spec, entry, ids, grp);
    }

    public List<GraphStep> postProcess(List<GraphStep> steps) {
        int originalSize = steps.size();
        for (int i = 0; i < originalSize; i++) {
            GraphStep step = steps.get(i);
            steps.add(new ChgrpValidation(ctx, em, roles, step.idx, step.stack,
                    step.spec, step.entry, step.getIds(), grp));
        }
        return steps;
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
