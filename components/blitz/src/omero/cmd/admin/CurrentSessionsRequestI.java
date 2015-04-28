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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.parameters.Parameters;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.system.EventContext;
import omero.cmd.CurrentSessionsRequest;
import omero.cmd.CurrentSessionsResponse;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.model.Session;
import omero.util.IceMapper;
import edu.emory.mathcs.backport.java.util.Collections;

@SuppressWarnings("serial")
public class CurrentSessionsRequestI extends CurrentSessionsRequest
    implements IRequest {

    protected Helper helper;

    protected final CurrentDetails current;

    protected final SessionManager manager;

    protected Map<String, EventContext> contexts;

    public CurrentSessionsRequestI(CurrentDetails current,
            SessionManager manager) {
        this.current = current;
        this.manager = manager;
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
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);

        contexts = manager.getAll();
        if (contexts.size() == 0) {
            return Collections.emptyList();
        }
        return helper.getServiceFactory().getQueryService().
                findAllByQuery("select s from Session s where s.uuid in (:uuid)",
                        new Parameters().addList("uuid", new ArrayList<String>(
                                contexts.keySet())));
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);

        @SuppressWarnings("unchecked")
        List<ome.model.meta.Session> rv = (List<ome.model.meta.Session>) object;
        Map<String, Session> objects = new HashMap<String, Session>();
        IceMapper mapper = new IceMapper();
        for (ome.model.meta.Session obj : rv) {
            objects.put(obj.getUuid(), (Session) mapper.map(obj));
        }

        if (helper.isLast(step)) {
            CurrentSessionsResponse rsp = new CurrentSessionsResponse();
            rsp.sessions = new ArrayList<Session>(contexts.size());
            rsp.contexts = new ArrayList<omero.sys.EventContext>(contexts.size());
            for (Map.Entry<String, EventContext> entry : contexts.entrySet()) {
                String uuid = entry.getKey();
                Session s = objects.get(uuid);
                rsp.sessions.add(s);
                if (s == null) {
                    // Non-admin
                    EventContext orig = entry.getValue();
                    omero.sys.EventContext ec = new omero.sys.EventContext();
                    rsp.contexts.add(ec);
                    ec.userId = orig.getCurrentUserId();
                    ec.userName = orig.getCurrentUserName();
                    ec.groupId = orig.getCurrentGroupId();
                    ec.groupName = orig.getCurrentGroupName();
                    ec.isAdmin = orig.isCurrentUserAdmin();
                } else {
                    rsp.contexts.add(IceMapper.convert(entry.getValue()));
                }
            }
            helper.setResponseIfNull(rsp);
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

}
