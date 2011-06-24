/*
 * org.openmicroscopy.shoola.util.image.io.EncoderException
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.image.io;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The exception thrown during the encoding process if an error occurred. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class EncoderException
    extends Exception
{

    
    /**
     * Creates a new instance.
     * 
     * @param message The error message.
     */
    public EncoderException(String message)
    {
        super(message);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param cause The exception that occurred during execution.
     */
    public EncoderException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param message The error message.
     * @param cause The exception that occurred during execution.
     */
    public EncoderException(String message, Throwable cause)
    {
      super(message, cause);
    }
    
}
