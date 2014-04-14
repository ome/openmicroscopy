/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package omero.gateway.exception;

/**
 * Indicates that there is a version mismatch between client and server
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class VersionMismatchException extends Exception {
    
    /** the client's version */
    private String clientVersion;

    /** the server's version */
    private String serverVersion;

    /**
     * See {@link Exception#Exception(String)}
     * @param message
     */
    public VersionMismatchException(String message) {
        super(message);
    }

    /**
     * Createa a new instance, providing client and server version number
     * @param clientVersion the client's version 
     * @param serverVersion the server's version
     */
    public VersionMismatchException(String clientVersion, String serverVersion) {
        super();
        this.clientVersion = clientVersion;
        this.serverVersion = serverVersion;
    }

    /**
     * Get the client's version number
     * @return See above
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * Get the server's version number
     * @return See above
     */
    public String getServerVersion() {
        return serverVersion;
    }
    
}
