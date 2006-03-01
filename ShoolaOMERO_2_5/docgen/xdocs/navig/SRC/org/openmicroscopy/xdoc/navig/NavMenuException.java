/*
 * org.openmicroscopy.xdoc.navig.NavMenuException
 *
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

package org.openmicroscopy.xdoc.navig;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Signals errors occurred during the construction or operation of the 
 * navigation menu.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NavMenuException
    extends Exception
{

    /**
     * Creates a new exception.
     */
    public NavMenuException() { super(); }

    /**
     * Creates a new exception.
     * 
     * @param message Description of the error.
     */
    public NavMenuException(String message) { super(message); }

    /**
     * Creates a new exception.
     * 
     * @param cause The exception that caused this one to be raised.
     */
    public NavMenuException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception.
     * 
     * @param message Description of the error.
     * @param cause The exception that caused this one to be raised.
     */
    public NavMenuException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
