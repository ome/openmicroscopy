/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway;

import java.util.UUID;

/**
 * Holds all necessary information needed for joining an active OMERO server
 * session
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class JoinSessionCredentials extends LoginCredentials {

    /**
     * Creates a new instance
     * 
     * @param sessionId
     *            The session ID
     * @param host
     *            The server hostname
     */
    public JoinSessionCredentials(String sessionId, String host) {
        // simply set the session ID as username, everything else is
        // handled in Gateway.createSession(LoginCredentials)
        super(sessionId, "", host);
        
        // Check that the sessionId is an UUID
        UUID.fromString(sessionId);
    }

    /**
     * Creates a new instance
     * 
     * @param sessionId
     *            The session ID
     * @param host
     *            The server hostname
     * @param port
     *            The server port
     */
    public JoinSessionCredentials(String sessionId, String host, int port) {
        // simply set the session ID as username, everything else is
        // handled in Gateway.createSession(LoginCredentials)
        super(sessionId, "", host, port);
        
        // Check that the sessionId is an UUID
        UUID.fromString(sessionId);
    }
}
