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

import ome.api.local.LocalAdmin;
import ome.model.IObject;
import ome.services.chown.ChownStepFactory;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.tools.hibernate.HibernateUtils;
import ome.util.SqlAction;
import omero.cmd.Chown;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.Status;
import omero.cmd.Unknown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ChownI extends Chown implements IRequest {

    private static final long serialVersionUID = -3653063048111039L;

    private static final Log log = LogFactory.getLog(ChownI.class);

    private final ChownStepFactory factory;

    private final ApplicationContext specs;

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private/* final */GraphSpec spec;

    private/* final */Status status;

    private/* final */GraphState state;

    public ChownI(ChownStepFactory factory, ApplicationContext specs) {
        this.factory = factory;
        this.specs = specs;
    }

    public void init(Status status, SqlAction sql, Session session, ServiceFactory sf) {
        synchronized (status) {
            if (status.flags == null) {
                status.flags = new ArrayList<State>();
            }
        }
        this.status = status;

        //
        // initial security restrictions.
        //

        // security restrictions (#6620):
        // (1) prevent certain coarse-grained actions from happening, like dropping
        // data in someone else's group like a Cuckoo

        final EventContext ec = ((LocalAdmin)sf.getAdminService()).getEventContextQuiet();
        final Long userId = ec.getCurrentUserId();
        final boolean admin = ec.isCurrentUserAdmin();
        final boolean member = ec.getMemberOfGroupsList().contains(user);

        if (!admin && !member) {
            fail("non-member", "grp", ""+user, "usr", ""+userId);
            return; // EARLY EXIT!
        }

        try {
            this.factory.setGroup(user);
            this.spec = specs.getBean(type, GraphSpec.class);

            this.spec.initialize(id, "", options);

            StopWatch sw = new CommonsLogStopWatch();
            state = new GraphState(factory, sql, session, spec);
            status.steps = state.getTotalFoundCount();
            sw.stop("omero.chown.ids." + status.steps);

            if (status.steps == 0) {
                rsp.set(new OK()); // TODO: Subclass?
            } else {

                // security restrictions (#6620)
                // (2) now that we have the id for the top-level object, we
                // can check ownership, etc.

                if (!admin) {
                    final IObject obj = this.spec.load(session);
                    obj.getDetails().getOwner();
                    Long owner = HibernateUtils.nullSafeOwnerId(obj);
                    if (owner != null && !owner.equals(userId)) {
                        fail("non-owner", "owner", ""+owner);
                    } else {
                        // SUCCESS
                        log.info(String.format(
                                "type=%s, id=%s options=%s [steps=%s]",
                                type, id, options, status.steps));
                    }
                }
            }
        } catch (NoSuchBeanDefinitionException nsbde) {
            status.steps = 0;
            status.flags.add(State.FAILURE);
            Map<String, String> params = new HashMap<String, String>();
            params.put("message", "Unknown type: " + type);
            rsp.set(new Unknown(ice_id(), "notype", params));
        } catch (Throwable t) {
            status.steps = 0;
            status.flags.add(State.FAILURE);
            Map<String, String> params = new HashMap<String, String>();
            params.put("message", t.getMessage());
            rsp.set(new ERR(ice_id(), "INIT ERR", params));
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

    protected void fail(final String msg, final String...paramList) {
        status.steps = 0;
        status.flags.add(State.FAILURE);
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < paramList.length / 2; i++) {
            params.put(paramList[i], paramList[i+1]);
        }
        rsp.set(new ERR(ice_id(), msg, params));
    }

}
