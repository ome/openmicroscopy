/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import ome.services.chgrp.ChgrpStepFactory;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.util.SqlAction;
import omero.cmd.Chgrp;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.context.ApplicationContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ChgrpI extends Chgrp implements IRequest {

    private static final long serialVersionUID = -3653081139095111039L;

    private static final Log log = LogFactory.getLog(ChgrpI.class);

    private final ChgrpStepFactory factory;

    private final ApplicationContext specs;

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private/* final */GraphSpec spec;

    private/* final */Status status;

    private/* final */GraphState state;

    public ChgrpI(ChgrpStepFactory factory, ApplicationContext specs) {
        this.factory = factory;
        this.specs = specs;
    }

    public void init(Status status, Session session, SqlAction sql) {
        this.factory.setGroup(grp);
        this.spec = specs.getBean(type, GraphSpec.class);
        this.status = status;
        synchronized (status) {
            if (status.flags == null) {
                status.flags = new ArrayList<State>();
            }
        }

        if (this.spec == null) {
            status.steps = 0;
            status.flags.add(State.FAILURE);
            rsp.set(new ERR(ice_id(), "NO SPEC", null));
        } else {
            try {
                this.spec.initialize(id, "", options);

                StopWatch sw = new CommonsLogStopWatch();
                state = new GraphState(factory, sql, session, spec);
                status.steps = state.getTotalFoundCount();
                if (status.steps == 0) {
                    rsp.set(new OK()); // TODO: Subclass?
                } else {
                    sw.stop("omero.chgrp.ids." + status.steps);
                }

                // SUCCESS
                log.info(String.format("type=%s, id=%s options=%s [steps=%s]",
                        type, id, options, status.steps));

            } catch (Throwable t) {
                status.steps = 0;
                status.flags.add(State.FAILURE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("message", t.getMessage());
                rsp.set(new ERR(ice_id(), "INIT ERR", params));
            }
        }

    }

    public void step(int i) throws Cancel {
        if (i < 0 || i >= status.steps) {
            return;
        }
        try {
            state.execute(i);
        } catch (Throwable t) {
            rsp.set(new ERR());
            status.flags.add(State.FAILURE);
            Cancel cancel = new Cancel(t.getMessage());
            cancel.initCause(t);
            throw cancel;
        }
    }

    public void finish() {
        rsp.compareAndSet(null, new OK());
    }

    public Response getResponse() {
        return rsp.get();
    }

}
