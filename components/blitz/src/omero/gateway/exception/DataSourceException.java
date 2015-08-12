/*
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omero.gateway.exception;

/** 
 * Reports an error occurred while trying to pull out data from the pixels data
 * repository.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *  <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class DataSourceException
    extends Exception
{

    /** Creates a new exception. */
    public DataSourceException() { super(); }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message Short explanation of the problem.
     */
    public DataSourceException(String message) { super(message); }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause The exception that caused this one to be risen.
     */
    public DataSourceException(Throwable cause) { super(cause); }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Short explanation of the problem.
     * @param cause The exception that caused this one to be risen.
     */
    public DataSourceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
