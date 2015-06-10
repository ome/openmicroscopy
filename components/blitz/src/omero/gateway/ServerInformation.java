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
 * Holds hostname and port of an OMERO server
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ServerInformation {

    /** The hostname */
    private String hostname;

    /** The port */
    private int port;

    /**
     * Creates an empty instance
     */
    public ServerInformation() {

    }

    /**
     * Creates a new instance
     * 
     * @param hostname
     *            The hostname
     * @param port
     *            The port
     */
    public ServerInformation(String hostname, int port) {
        super();
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * @return The hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the hostname
     * 
     * @param hostname
     *            See above
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port
     * 
     * @param port
     *            See above
     */
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerInformation other = (ServerInformation) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        if (port != other.port)
            return false;
        return true;
    }

}
