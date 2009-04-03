/*
 * org.openmicroscopy.shoola.env.init.ContainerConfigInit
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

package org.openmicroscopy.shoola.env.init;

//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.ConfigException;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;

/** 
 * Fills up the {@link Container}'s registry with the entries from its
 * configuration file.
 *
 * @see	InitializationTask
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

public final class ContainerConfigInit
	extends InitializationTask 
{
	
	/** Constructor required by superclass. */
	public ContainerConfigInit() {}
	
	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName() { return "Loading Container configuration"; }

	/** 
	 * Does nothing, as this task requires no set up.
	 * @see InitializationTask#configure()
	 */
	void configure() {}

	/** 
	 * Carries out this task.
	 * @see InitializationTask#execute()
	 */
	void execute() 
		throws StartupException
	{
		String file = container.getConfigFile();
		Registry reg = container.getRegistry();
		try {
			RegistryFactory.fillFromFile(file, reg);
            String name = (String) reg.lookup(LookupNames.OMERO_HOME);
    		String omeroDir = System.getProperty("user.home")
    							+File.separator+name;
            reg.bind(LookupNames.USER_HOME_OMERO, omeroDir);
		} catch (ConfigException ce) {
			throw new StartupException("Unable to load Container configuration",
										ce);
		}
	}
	
	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}
	
}
		