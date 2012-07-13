/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.cmd;

import java.util.Map;

import ome.services.util.Executor;
import omero.util.CloseableServant;

/**
 * SPIOrthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy.
 *
 * @since Beta4.3.2
 */
public interface IHandle extends Runnable, CloseableServant {

    /**
     * Must be called on all instances exactly once before processing.
     * @param id
     *            Ice identity of this instance. Cannot be null.
     * @param req
     *            Request to be processed. Must also be an
     *            {@link omero.cmd.Request} instance.
     * @param ctx
     *            Possibly null call context which will be passed to
     *            {@link Executor#execute(Map, ome.system.Principal, ome.services.util.Executor.Work)}
     */
    void initialize(Ice.Identity id, IRequest req, Map<String, String> ctx);

}
