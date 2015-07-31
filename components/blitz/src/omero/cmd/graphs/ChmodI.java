/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.util.Map;

import ome.api.IAdmin;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.security.ChmodStrategy;
import omero.cmd.Chmod;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.OK;
import omero.cmd.Response;

/**
 * Chmod implementation like that in
 * {@link IAdmin#changePermissions(IObject, ome.model.internal.Permissions)} but
 * which can be run asynchronously.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChmodI extends Chmod implements IGraphModifyRequest {

    private static final long serialVersionUID = -33294230498203989L;

    private final ChmodStrategy strategy;

    private Helper helper;

    private IObject target;

    private Object[] checks;

    private final Ice.Communicator ic;

    public ChmodI(Ice.Communicator ic, ChmodStrategy strategy) {
        this.ic = ic;
        this.strategy = strategy;
    }

    //
    // IGraphModifyRequest
    //

    @Override
    @Deprecated
    public IGraphModifyRequest copy() {
        ChmodI copy = (ChmodI) ic.findObjectFactory(ice_id()).create(ChmodI.ice_staticId());
        copy.type = type;
        copy.id = id;
        copy.permissions = permissions;
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

        // Handle exceptions internally.
        target = load();

        try {
            checks = strategy.getChecks(target, permissions);
            // Here checks cannot be null.
        }
        catch (SecurityViolation sv) {
            throw helper.cancel(new ERR(), sv, "not permitted");
        }

        helper.setSteps(1 + checks.length);

    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);

        if (step == 0) {
            strategy.chmod(target, permissions);
        }
        else {
            try {
                strategy.check(target, checks[step - 1]);
            } catch (SecurityViolation sv) {
                throw helper.cancel(new ERR(), sv, "check failed");
            }
        }
        return null; // Nothing to return
    }

    @Override
    @Deprecated
    public void finish() throws Cancel {
        // no-op
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

    // Helpers
    // =========================================================================

    protected Map<String, String> params() {
        return helper.params("type", type, "id", ""+id);
    }

    protected IObject load() {
        if ("ExperimenterGroup".equals(type)
                || "/ExperimenterGroup".equals(type)
                || omero.model.ExperimenterGroup.ice_staticId().equals(type)
                || "omero.model.ExperimenterGroup".equals(type)
                || "omero.model.ExperimenterGroupI".equals(type)) {

            try {
                IObject obj = (IObject)
                    helper.getSession().get(ExperimenterGroup.class, id);
                if (obj == null) {
                    helper.cancel(new ERR(), null, "unknown target", params());
                }
                return obj;
            }
            catch (Exception e) {
                helper.cancel(new ERR(), e, "bad target", params());
            }
        }
        else {
            helper.cancel(new ERR(), null, "bad type", params());
        }
        return null; // Never reached.
    }

}
