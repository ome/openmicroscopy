/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.graphs;

import omero.cmd.Chgrp;
import omero.cmd.Err;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ChgrpI extends Chgrp implements IRequest {

    private final Log log = LogFactory.getLog(ChgrpI.class);

    private static final long serialVersionUID = -3653081139095111039L;


    public void init(Status status) {
        status.steps = 1;
    }

    public void step(int i) {
        return;
    }

    public Response finish() {
        return new Err();
    }
}
