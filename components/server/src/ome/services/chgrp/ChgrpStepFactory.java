/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.List;

import ome.services.graphs.AbstractStepFactory;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChgrpStepFactory extends AbstractStepFactory {

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
