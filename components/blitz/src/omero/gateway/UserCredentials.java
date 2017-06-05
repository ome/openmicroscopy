/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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
 * Holds username and password of a user
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class UserCredentials {

    /** The username (or session ID) */
    private String username;

    /** The password */
    private String password;

    /**
     * Creates an empty instance
     */
    public UserCredentials() {

    }

    /**
     * Creates a new instance
     * 
     * @param username
     *            The username or alternatively a session ID (in which case
     *            the password will be ignored)
     * @param password
     *            The password
     */
    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the user name (or session ID, if it was set before instead of an
     * username)
     * 
     * @return The username (or session ID)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username (or session ID)
     * 
     * @param username
     *            The username or alternatively a session ID (in which case the
     *            password will be ignored)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password.
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     * 
     * @param password
     *            See above
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Formats user name and password. Each character of the password is
     * replaced by a star.
     * 
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("User Name: ");
        buf.append(username);
        buf.append(" -- Password: ");
        if (password != null)
            for (int i = 0; i < password.length(); ++i)
                buf.append('*');
        return buf.toString();
    }
}
