/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

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
 * Single action performed by {@link DeleteState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public class DeleteStep extends GraphStep {

    final private static Log log = LogFactory.getLog(DeleteStep.class);

    final private OmeroContext ctx;

    public DeleteStep(OmeroContext ctx, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        super(idx, stack, spec, entry, ids);
        this.ctx = ctx;
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {
        EventLogMessage elm = new EventLogMessage(this, "DELETE", k,
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
