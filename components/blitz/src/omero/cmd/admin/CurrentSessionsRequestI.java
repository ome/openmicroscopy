/*
 * Copyright (C) 2015-2018 University of Dundee & Open Microscopy Environment.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.services.sessions.SessionProvider;
import ome.services.util.ReadOnlyStatus;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.system.SimpleEventContext;
import omero.RType;
import omero.cmd.CurrentSessionsRequest;
import omero.cmd.CurrentSessionsResponse;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.model.Session;
import omero.util.IceMapper;
import omero.util.ObjectFactoryRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Communicator;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("serial")
public class CurrentSessionsRequestI extends CurrentSessionsRequest
    implements IRequest, ReadOnlyStatus.IsAware {

    private final Logger log = LoggerFactory.getLogger(CurrentSessionsRequestI.class);

    public static class Factory extends ObjectFactoryRegistry {
        private final ObjectFactory factory;
        public Factory(final CurrentDetails current,
                final SessionManager sessionManager, final SessionProvider sessionProvider) {
            factory = new ObjectFactory(ice_staticId()) {
                @Override
                public Ice.Object create(String name) {
                    return new CurrentSessionsRequestI(
                            current, sessionManager, sessionProvider);
                }};
            }

        @Override
        public Map<String, ObjectFactory> createFactories(Communicator ic) {
            return new ImmutableMap.Builder<String, ObjectFactory>()
                    .put(ice_staticId(), factory).build();
        }
    }

    protected Helper helper;

    protected final CurrentDetails current;

    protected final SessionManager manager;

    protected final SessionProvider provider;

    protected Map<String, Map<String, Object>> contexts;

    public CurrentSessionsRequestI(CurrentDetails current,
            SessionManager manager, SessionProvider provider) {
        this.current = current;
        this.manager = manager;
        this.provider = provider;
    }

    private boolean isCurrentUserGuest = false;

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
        isCurrentUserGuest = current.isCurrentUserGuest();
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);

        contexts = manager.getSessionData();
        if (contexts.isEmpty()) {
            return Collections.emptyList();
        }
        final ServiceFactory sf = helper.getServiceFactory();
        final List<ome.model.meta.Session> sessions = new ArrayList<>(contexts.size());
        for (final String uuid : contexts.keySet()) {
            final Long sessionId = provider.findSessionIdByUuid(uuid, sf);
            if (sessionId != null) {
                final ome.model.meta.Session session = provider.findSessionById(sessionId, sf);
                if (session != null) {
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    @SuppressWarnings("unchecked")
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);

        List<ome.model.meta.Session> rv = (List<ome.model.meta.Session>) object;
        Map<String, Session> objects = new HashMap<String, Session>();
        IceMapper mapper = new IceMapper();
        for (ome.model.meta.Session obj : rv) {
            if (obj.isLoaded()) {
                objects.put(obj.getUuid(), (Session) mapper.map(obj));
            }
        }

        if (helper.isLast(step)) {
            final int size = contexts.size();
            CurrentSessionsResponse rsp = new CurrentSessionsResponse();
            rsp.sessions = new ArrayList<Session>(size);
            rsp.contexts = new ArrayList<omero.sys.EventContext>(size);
            final List<Map<String, RType>> dataMaps = new ArrayList<Map<String, RType>>(size);
            for (Map.Entry<String, Map<String, Object>> entry : contexts.entrySet()) {
                String uuid = entry.getKey();
                Map<String, Object> data = entry.getValue();
                EventContext orig = new SimpleEventContext(
                        (EventContext) data.get("sessionContext"));
                Session s = objects.get(uuid);
                if (s == null) {
                    // Non-admin
                    if (isCurrentUserGuest) {
                        continue;
                    }
                    rsp.sessions.add(s);
                    omero.sys.EventContext ec = new omero.sys.EventContext();
                    rsp.contexts.add(ec);
                    ec.userId = orig.getCurrentUserId();
                    ec.userName = orig.getCurrentUserName();
                    ec.groupId = orig.getCurrentGroupId();
                    ec.groupName = orig.getCurrentGroupName();
                    ec.isAdmin = orig.isCurrentUserAdmin();
                    dataMaps.add(Collections.<String, RType>emptyMap());
                } else {
                    rsp.sessions.add(s);
                    rsp.contexts.add(IceMapper.convert(orig));
                    dataMaps.add(parseData(rsp, data));
                }
            }
            rsp.data = dataMaps.toArray(new Map[dataMaps.size()]);
            helper.setResponseIfNull(rsp);
        }
    }

    private Map<String, RType> parseData(CurrentSessionsResponse rsp, Map<String, Object> data) {
        Map<String, RType> parsed = new HashMap<String, RType>();
        for (Map.Entry<String, Object> entry2 : data.entrySet()) {
            String key2 = entry2.getKey();
            Object obj2 = entry2.getValue();
            if ("class".equals(key2) || "sessionContext".equals(key2)) {
                continue;
            }
            RType wrapped = null;
            try {
                if (key2.endsWith("Time")) {
                    wrapped = omero.rtypes.rtime((Long)obj2);
                } else {
                    wrapped = omero.rtypes.wrap(obj2);
                }
            } catch (omero.ClientError ce) {
                log.warn("Failed to convert {}", obj2, ce);
                wrapped = omero.rtypes.rstring(obj2.toString());
            }
            parsed.put(key2, wrapped);
        }
        return parsed;
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    @Override
    public boolean isReadOnly(ReadOnlyStatus readOnly) {
        return true;
    }
}
