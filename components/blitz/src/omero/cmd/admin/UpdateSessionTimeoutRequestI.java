/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.admin;

import java.util.Map;

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.system.ServiceFactory;
import omero.RLong;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.UpdateSessionTimeoutRequest;

@SuppressWarnings("serial")
public class UpdateSessionTimeoutRequestI extends UpdateSessionTimeoutRequest
    implements IRequest {

    protected Helper helper;

    protected LocalQuery query;

    protected LocalUpdate update;

    protected final CurrentDetails current;

    protected final SessionManager manager;

    protected final SecuritySystem security;

    protected boolean updated = false;

    public UpdateSessionTimeoutRequestI(CurrentDetails current,
            SessionManager manager, SecuritySystem security) {
        this.current = current;
        this.manager = manager;
        this.security = security;
    }

    //
    // CMD API
    //

    @Override
    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        this.helper.setSteps(1);
        ServiceFactory sf = this.helper.getServiceFactory();
        query = (LocalQuery) sf.getQueryService();
        update = (LocalUpdate) sf.getUpdateService();
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        return updateSession();
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            manager.reload(session);
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // IMPLEMENTATION
    //

    protected Session updateSession() {
        Session s = helper.getServiceFactory().getQueryService()
                .findByQuery("select s from Session s where s.uuid = :uuid",
                new Parameters().addString("uuid", session));

        if (s == null) {
            // we assume that if the session is visible, then
            // the current user should be able to edit it.
            throw helper.cancel(new ERR(), null, "no-session");
        }

        boolean isAdmin = current.getCurrentEventContext().isCurrentUserAdmin();
        updated |= updateField(s, Session.TIMETOLIVE, timeToLive, isAdmin);
        updated |= updateField(s, Session.TIMETOIDLE, timeToIdle, isAdmin);
        if (updated) {
            security.runAsAdmin(new AdminAction(){
                @Override
                public void runAsAdmin() {
                    update.flush();
                }});
            return s;
        } else {
            throw helper.cancel(new ERR(), null, "no-update-performed",
                    "session", session);
        }
    }

    protected boolean updateField(Session s, String field, RLong value,
            boolean isAdmin) {

        if (value == null) {
            return false;
        }

        long target = value.getValue();
        long current = ((Long) s.retrieve(field)).longValue();
        long diff = target - current;
        if (!isAdmin && diff > 0) {
            throw helper.cancel(new ERR(), null, "non-admin-increase",
                    "target", ""+target,
                    "current", ""+current);
        }

        helper.info("Modifying %s from %s to %s for %s",
                field, current, target, session);
        s.putAt(field, target);
        return true;
    }
}
