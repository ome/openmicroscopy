/*
 * org.openmicroscopy.shoola.env.data.OmeroSessionServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data;

import static java.util.Objects.requireNonNull;

import omero.api.ServiceFactoryPrx;
import omero.client;
import omero.model.Session;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the {@link OmeroSessionService}.
 */
public class OmeroSessionServiceImpl implements OmeroSessionService {

    /**
     * Idle timeout, in seconds, to use for an import session.
     * Currently set to one week.
     */
    public static int ImportSessionTimeout = 86400 * 7;

    private static Map<String, String> buildRequestContext(String host,
        int port, String username, String password, boolean secure) {
        Map<String, String> props = new HashMap<>();

        props.put("omero.host", host);
        props.put("omero.port", String.valueOf(port));
        if (!secure) {
            props.put(
                "Ice.Default.Router",
                "OMERO.Glacier2/router:tcp -p @omero.port@ -h @omero.host@");
        }
        props.put("omero.user", String.valueOf(username));
        props.put("omero.pass", String.valueOf(password));

        return props;
    }

    private static String group(Session existingSession) {
        return existingSession.getDetails().getGroup().getName().getValue();
    }

    private static long timeToIdle(int timeoutInSeconds) {
        return timeoutInSeconds * 1000L;
    }

    private final Registry context;
    private String host;
    private int port;
    private String username;
    private String password;

    public OmeroSessionServiceImpl(Registry context) {
        requireNonNull(context, "context");
        this.context = context;
        init();
    }

    private void init() {
        AdminService svc = context.getAdminService();
        host = svc.getServerName();
        port = svc.getPort();

        UserCredentials uc = (UserCredentials)
                context.lookup(LookupNames.USER_CREDENTIALS);
        username = uc.getUserName();
        password = uc.getPassword();
    }

    private client newClient() {
        Map<String, String> props =
                buildRequestContext(host, port, username, password, true);
        return new client(props);
    }

    @Override
    public String create(int timeout) throws Exception {
        client c = newClient();
        ServiceFactoryPrx serviceFactory = c.createSession();
        serviceFactory.setSecurityPassword(password);

        Session initialSession = serviceFactory.getSessionService()
                .getSession(c.getSessionId());
        Session newSession = serviceFactory.getSessionService()
                .createUserSession(0,
                        timeToIdle(timeout),
                        group(initialSession));
        c.killSession();  // close initial session.

        return newSession.getUuid().getValue();
    }

    @Override
    public String createOfflineImportSession() throws Exception {
        return create(ImportSessionTimeout);
    }

}
