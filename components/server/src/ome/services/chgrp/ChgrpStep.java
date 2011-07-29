/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.chgrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.services.messages.EventLogMessage;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Single action produced by {@link ChgrpStepFactory}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.2
 */
public class ChgrpStep extends GraphStep {

    final private static Log log = LogFactory.getLog(ChgrpStep.class);

    final private OmeroContext ctx;

    public ChgrpStep(OmeroContext ctx, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "CHGRP", k,
                new ArrayList<Long>(ids));

        try {
            ctx.publishMessage(elm);
        } catch (Throwable t) {
            GraphException de = new GraphException("EventLogMessage failed.");
            de.initCause(t);
            throw de;
        }

    }
}
