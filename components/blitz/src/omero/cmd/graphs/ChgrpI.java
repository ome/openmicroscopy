/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.graphs;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import ome.api.local.LocalAdmin;
import ome.model.IObject;
import ome.services.chgrp.ChgrpStepFactory;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.tools.hibernate.HibernateUtils;

import omero.cmd.Chgrp;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.Unknown;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ChgrpI extends Chgrp implements IRequest {

    private static final long serialVersionUID = -3653081139095111039L;

    private final ChgrpStepFactory factory;

    private final ApplicationContext specs;

    private/* final */GraphSpec spec;

    private/* final */GraphState state;
    
    private/* final */Helper helper;

    public ChgrpI(ChgrpStepFactory factory, ApplicationContext specs) {
        this.factory = factory;
        this.specs = specs;
    }

    public Map<String, String> getCallContext() {
        Map<String, String> negOne = new HashMap<String, String>();
        negOne.put("omero.group", "-1");
        return negOne;
    }

    public void init(Helper helper) {
        this.helper = helper;

        //
        // initial security restrictions.
        //

        // security restrictions (#6620):
        // (1) prevent certain coarse-grained actions from happening, like dropping
        // data in someone else's group like a Cuckoo

        final ServiceFactory sf = helper.getServiceFactory();
        final EventContext ec = ((LocalAdmin)sf.getAdminService()).getEventContextQuiet();
        final Long userId = ec.getCurrentUserId();
        final boolean admin = ec.isCurrentUserAdmin();
        final boolean member = ec.getMemberOfGroupsList().contains(grp);

        if (!admin && !member) {
            helper.cancel(new ERR(), null, "non-member",
                    "grp", ""+grp, "usr", ""+userId);
            return; // EARLY EXIT!
        }

        try {
            this.factory.setGroup(grp);
            this.spec = specs.getBean(type, GraphSpec.class);

            this.spec.initialize(id, "", options);

            StopWatch sw = new Slf4JStopWatch();
            state = new GraphState(ec, factory, helper.getSql(),
                helper.getSession(), spec);

            // Throws on no steps
            this.helper.setSteps(state.getTotalFoundCount()+1); // +1 refresh;
            sw.stop("omero.chgrp.ids." + helper.getSteps());


            // security restrictions (#6620)
            // (2) now that we have the id for the top-level object, we
            // can check ownership, etc.

            final IObject obj = this.spec.load(helper.getSession());
            if (obj == null) {
                throw helper.cancel(new ERR(), null, "no-object");
            }

            helper.info("chgrp of %s to %s", obj, grp);

            if (!admin) {
                obj.getDetails().getOwner();
                Long owner = HibernateUtils.nullSafeOwnerId(obj);
                if (owner != null && !owner.equals(userId)) {
                    throw helper.cancel(new ERR(), null, "non-owner",
                            "owner", ""+owner);
                } else {
                    // SUCCESS
                    helper.info(
                            "type=%s, id=%s options=%s [steps=%s]",
                            type, id, options, helper.getSteps());
                }
            }
        } catch (Cancel c) {
            throw c;
        } catch (NoSuchBeanDefinitionException nsbde) {
            throw helper.cancel(new Unknown(), nsbde, "notype",
                    "message", "Unknown type:" + type);
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "INIT ERR");
        } finally {
            // helper.getSession().refresh(obj);
        }

    }

    public Object step(int i) throws Cancel {
        helper.assertStep(i);

        try {
            if ((i+1) == helper.getSteps()) {
                // The dataset was loaded in order to check its permissions.
                // Since these have been changed "in the background" (via SQL)
                // it's important that we refresh that object for later cmds.
                Session s = helper.getSession();
                IObject obj = spec.load(s);
                s.refresh(obj);
                return null;
            } else {
                return state.execute(i);
            }
        } catch (GraphException ge) {
            throw helper.cancel(new ERR(), ge, "STEP ERR", "step", ""+i, "id", ""+id);
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

}
