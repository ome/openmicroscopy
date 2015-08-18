/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package omero.gateway.exception;

/**
 * Reports an error occurred while trying to access the OMERO service.
 * Such an error can posted in the following case:
 * <i>broken connection</i>, <i>expired session</i> or <i>not logged in</i>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class DSOutOfServiceException
    extends Exception
{

    /** More information about what nature the problem is */
    private ConnectionStatus connectionStatus;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Short explanation of the problem.
     */
    public DSOutOfServiceException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Short explanation of the problem.
     * @param connectionStatus The status of the connection to the server
     */
    public DSOutOfServiceException(String message,
            ConnectionStatus connectionStatus)
    {
        super(message);
        this.connectionStatus = connectionStatus;
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Short explanation of the problem.
     * @param cause The exception that caused this one to be risen.
     */
    public DSOutOfServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message Short explanation of the problem.
     * @param cause The exception that caused this one to be risen.
     * @param connectionStatus The status of the connection to the server.
     */
    public DSOutOfServiceException(String message, Throwable cause,
            ConnectionStatus connectionStatus)
    {
        super(message, cause);
        this.connectionStatus = connectionStatus;
    }

    /**
     * Gets the {@link ConnectionStatus}
     * @return See above
     */
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

}
