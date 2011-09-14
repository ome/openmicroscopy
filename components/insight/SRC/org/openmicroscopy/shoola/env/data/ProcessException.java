/*
 * org.openmicroscopy.shoola.env.data.ProcessException 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.ResourceError;

/** 
 * Reports an error occurred while trying to run a script.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ProcessException 
	extends Exception
{

	/** Indicates that no processor available to run the script.*/
	public static final int NO_PROCESSOR = 0;
	
	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message	Short explanation of the problem.
	 */
	public ProcessException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message	Short explanation of the problem.
	 * @param cause		The exception that caused this one to be risen.
	 */
	public ProcessException(String message, Throwable cause) 
	{
		super(message, cause);
	}

	/**
	 * Returns one of the constant defined by this class.
	 * 
	 * @return See above.
	 */
	public int getStatus()
	{
		Throwable cause = getCause();
		if (cause instanceof ResourceError) {
			ResourceError error = (ResourceError) cause;
			String message = error.message;
			if (message != null && message.toLowerCase().contains(
					"no processor available"))
				return NO_PROCESSOR;
		}
		return -1;
	}
	
}
