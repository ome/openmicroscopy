/*
 * org.openmicroscopy.shoola.env.init.ContainerConfigInit
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

package org.openmicroscopy.shoola.env.init;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
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
	
	/**
	 * Constructor required by superclass.
	 */
	public ContainerConfigInit() {}
	
	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName() 
	{
		return "Loading Container configuration";
	}

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
		