/*
 * org.openmicroscopy.shoola.env.rnd.RenderingServiceException
 *
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

import java.io.PrintWriter;
import java.io.StringWriter;

/** 
 * Reports an error occurred while trying to render a plane.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">
 *         donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class RenderingServiceException
    extends Exception
{

    /** Indicates that the error occurred due to a connection failure.*/
    public static final int CONNECTION = 1;

    /**
     * Indicates that the error occurred due to an operation not being
     * supported.
     */
    public static final int OPERATION_NOT_SUPPORTED = 2;

    /** The index of the exception.*/
    private int index = 0;

    /** Creates a new exception. */
    public RenderingServiceException() { super(); }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Short explanation of the problem.
     */
    public RenderingServiceException(String message) { super(message); }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause The exception that caused this one to be risen.
     */
    public RenderingServiceException(Throwable cause) { super(cause); }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Short explanation of the problem.
     * @param cause The exception that caused this one to be risen.
     */
    public RenderingServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Prints the stack trace and returns it as a string.
     *
     * @return See above.
     */
    public String getExtendedMessage()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Sets the index indicating if the error occurred due to a network 
     * failure.
     *
     * @param index The value to set.
     */
    public void setIndex(int index) { this.index = index; }

    /**
     * Returns the index.
     *
     * @return See above.
     */
    public int getIndex() { return index; }

}
