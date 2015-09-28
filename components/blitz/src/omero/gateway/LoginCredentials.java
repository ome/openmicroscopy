/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

/**
 * Holds all necessary information needed for connecting to an OMERO server
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class LoginCredentials {

    /** The user */
    private UserCredentials user;

    /** The server information */
    private ServerInformation server;

    /** Name of the application which wants to connect to the server */
    private String applicationName;

    /** Flag to enable encryption */
    private boolean encryption = true;

    /** Flag to enable network checks */
    private boolean checkNetwork = true;

    /** Data compression level */
    private float compression = 0.85f;

    /** ID of the group if not the default group of the user should be used */
    private long groupID = -1;

    /**
     * Creates a new instance
     */
    public LoginCredentials() {
        user = new UserCredentials();
        server = new ServerInformation();
    }

    /**
     * @return If encryption is enabled
     */
    public boolean isEncryption() {
        return encryption;
    }

    /**
     * Enable/Disable encryption
     * 
     * @param encryption
     *            See above
     */
    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    /**
     * @return If network checks should be performed
     */
    public boolean isCheckNetwork() {
        return checkNetwork;
    }

    /**
     * Enable/Disable network checks
     * 
     * @param checkNetwork
     *            See above
     */
    public void setCheckNetwork(boolean checkNetwork) {
        this.checkNetwork = checkNetwork;
    }

    /**
     * Returns the compression level.
     * @return The compression level
     */
    public float getCompression() {
        return compression;
    }

    /**
     * Sets the compression level
     * 
     * @param compression
     *            See above
     */
    public void setCompression(float compression) {
        this.compression = compression;
    }

    /**
     * @return The application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Set the application name
     * 
     * @param applicationName
     *            See above
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Returns the credentials.
     * @return The {@link UserCredentials}
     */
    public UserCredentials getUser() {
        return user;
    }

    /**
     * Returns the server information.
     * @return The {@link ServerInformation}
     */
    public ServerInformation getServer() {
        return server;
    }

    /**
     * Returns the OMERO group identifier.
     * @return The groupID to use for the connection
     */
    public long getGroupID() {
        return groupID;
    }

    /**
     * Sets the groupID to use for the connection
     * 
     * @param groupID
     */
    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

}
