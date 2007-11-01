/*
 * org.openmicroscopy.shoola.env.Agent
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

package org.openmicroscopy.shoola.env;


// Java Imports;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

// App-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;


/** 
 * The <code>Agent</code> interface plays the role of Separated interface, 
 * decoupling the container from knowledge of concrete agents.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public interface Agent 
{

	/** Container tells agent to get ready for service. */
    public void activate();
    
	/** Container tells agent to release acquired resources and stop service. */
    public void terminate();
    
	/** 
	 * Container create a Registry from entries in agent's configuration 
	 * file and add entries for services that can be accessed by agent. 
	 * It then passes  this to setContext
     * 
     * @param ctx The Registry to set.
	 */
    public void setContext(Registry ctx);
    
	/**
     * Container tells agent to release acquired resources and stop service. 
     * 
     * @return See above.
     */
    public boolean canTerminate();
    
    /**
     * Returns the map with events and data to save.
     * 
     * @return See above.
     */
    public Map<String, Set> hasDataToSave();

}
