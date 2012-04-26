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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import ome.api.IAdmin;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.security.ChmodStrategy;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.cmd.Chmod;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.Status;

/**
 * Chmod implementation like that in
 * {@link IAdmin#changePermissions(IObject, ome.model.internal.Permissions)} but
 * which can be run asynchronously.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class ChmodI extends Chmod implements IRequest {

    private static final long serialVersionUID = -33294230498203989L;

    private static final Log log = LogFactory.getLog(ChmodI.class);

    private final ChmodStrategy strategy;

    private Helper helper;

    private IObject target;

    private Object[] checks;

    public ChmodI(ChmodStrategy strategy) {
        this.strategy = strategy;
    }

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Status status, SqlAction sql, Session session,
            ServiceFactory sf) {

        this.helper = new Helper(this, status, sql, session, sf);

        // Handle exceptions internally.
        target = load();

        try {
            checks = strategy.getChecks(target, permissions);
            // Here checks cannot be null.
        }
        catch (SecurityViolation sv) {
            throw helper.cancel(new ERR(), sv, "not permitted");
        }

        status.steps = 1 + checks.length;

    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);

        try {
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
        catch (Cancel c) {
            throw c;
        }
        catch (Throwable t) {
            Map<String, String> rv = params(); // default params
            rv.put("" + id, "" + step);
            throw helper.cancel(new ERR(), t, "STEP ERR", "");
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            helper.setResponse(new OK());
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
                    helper.cancel(new ERR(), null, "unkown target", params());
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
