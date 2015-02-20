/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.export;

import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphOpts;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphStep;
import ome.tools.hibernate.ExtendedMetadata;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;

/**
 * Marker action used during export.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ExporterStep extends GraphStep {

    final private static Logger log = LoggerFactory.getLogger(ExporterStep.class);

    public ExporterStep(ExtendedMetadata em, int idx, List<GraphStep> stack,
            GraphSpec spec, GraphEntry entry, long[] ids) {
        super(em, idx, stack, spec, entry, ids);
    }

    @Override
    public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
            throws GraphException {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void onRelease(Class<IObject> k, Set<Long> ids)
            throws GraphException {

                // no-op

    }
}
