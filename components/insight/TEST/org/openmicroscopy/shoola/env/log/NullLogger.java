/*
 * org.openmicroscopy.shoola.env.log.NullLogger
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

package org.openmicroscopy.shoola.env.log;

import omero.log.LogMessage;
import omero.log.Logger;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class NullLogger
    implements Logger
{

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#debug(java.lang.Object, java.lang.String)
     */
    public void debug(Object originator, String logMsg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#debug(java.lang.Object, org.openmicroscopy.shoola.env.log.LogMessage)
     */
    public void debug(Object originator, LogMessage msg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#info(java.lang.Object, java.lang.String)
     */
    public void info(Object originator, String logMsg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#info(java.lang.Object, org.openmicroscopy.shoola.env.log.LogMessage)
     */
    public void info(Object originator, LogMessage msg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#warn(java.lang.Object, java.lang.String)
     */
    public void warn(Object originator, String logMsg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#warn(java.lang.Object, org.openmicroscopy.shoola.env.log.LogMessage)
     */
    public void warn(Object originator, LogMessage msg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#error(java.lang.Object, java.lang.String)
     */
    public void error(Object originator, String logMsg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#error(java.lang.Object, org.openmicroscopy.shoola.env.log.LogMessage)
     */
    public void error(Object originator, LogMessage msg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#fatal(java.lang.Object, java.lang.String)
     */
    public void fatal(Object originator, String logMsg)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.log.Logger#fatal(java.lang.Object, org.openmicroscopy.shoola.env.log.LogMessage)
     */
    public void fatal(Object originator, LogMessage msg)
    {
        // TODO Auto-generated method stub
        
    }

	public String getLogFile() {
		// TODO Auto-generated method stub
		return null;
	}

}
