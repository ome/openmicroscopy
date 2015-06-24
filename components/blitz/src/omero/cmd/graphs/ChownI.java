/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.graphs;

import java.util.Map;

import ome.api.local.LocalAdmin;
import ome.model.IObject;
import ome.services.chown.ChownStep;
import ome.services.chown.ChownStepFactory;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.tools.hibernate.HibernateUtils;
import omero.cmd.Chown;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.Unknown;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChownI extends Chown implements IGraphModifyRequest {

    private static final long serialVersionUID = -3653063048111039L;

    private final ChownStepFactory factory;

    private final ApplicationContext specs;

    private/* final */GraphSpec spec;

    private/* final */GraphState state;

    private Helper helper;

    private final Ice.Communicator ic;

    public ChownI(Ice.Communicator ic, ChownStepFactory factory, ApplicationContext specs) {
        this.ic = ic;
        this.factory = factory;
        this.specs = specs;
    }

    //
    // IGraphModifyRequest
    //

    @Override
    public IGraphModifyRequest copy() {
        ChownI copy = (ChownI) ic.findObjectFactory(ice_id()).create(ChownI.ice_staticId());
        copy.type = type;
        copy.id = id;
        copy.user = user;
        return copy;
    }

    //
    // IRequest
    //

    public Map<String, String> getCallContext() {
        return null;
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
        final boolean member = ec.getMemberOfGroupsList().contains(user);

        if (!admin && !member) {
            throw helper.cancel(new ERR(), null, "non-member",
                    "grp", ""+user, "usr", ""+userId);
        }

        try {
            this.factory.setGroup(user);
            this.spec = specs.getBean(type, GraphSpec.class);

            this.spec.initialize(id, "", options);

            StopWatch sw = new Slf4JStopWatch();
            state = new GraphState(ec, factory, helper.getSql(),
                helper.getSession(), spec);
            // Throws if steps == 0
            helper.setSteps(state.getTotalFoundCount());
            sw.stop("omero.chown.ids." + helper.getSteps());

            // security restrictions (#6620)
            // (2) now that we have the id for the top-level object, we
            // can check ownership, etc.

            if (!admin) {
                final IObject obj = this.spec.load(helper.getSession());
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

        } catch (NoSuchBeanDefinitionException nsbde) {
            throw helper.cancel(new Unknown(), nsbde, "notype",
                    "Unknown type", type);
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "INIT ERR");
        }

    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            return state.execute(step);
        } catch (GraphException ge) {
            throw helper.graphException(ge, step, id);
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "step", ""+step);
        }
    }

    @Override
    public void finish() throws Cancel {
        // Replaces ChownValidation
        int steps = state.validation();
        for (int i = 0; i < steps; i++) {
            try {
                state.validate(i);
            } catch (GraphException ge) {
                throw helper.graphException(ge, i, id);
            }
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
