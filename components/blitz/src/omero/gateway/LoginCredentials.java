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

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class LoginCredentials {

    private UserCredentials user;

    private ServerInformation server;
    
    private String applicationName;

    private boolean encryption = true;

    private boolean checkNetwork = true;

    private float compression = 0.85f;

    private long groupID = -1;
    
    public LoginCredentials() {
        user = new UserCredentials();
        server = new ServerInformation();
    }

    public boolean isEncryption() {
        return encryption;
    }

    public void setEncrypyion(boolean encryption) {
        this.encryption = encryption;
    }

    public boolean isCheckNetwork() {
        return checkNetwork;
    }

    public void setCheckNetwork(boolean checkNetwork) {
        this.checkNetwork = checkNetwork;
    }

    public float getCompression() {
        return compression;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public UserCredentials getUser() {
        return user;
    }

    public ServerInformation getServer() {
        return server;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    
}
