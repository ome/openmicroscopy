/*
 * org.openmicroscopy.shoola.env.config.RegistryFactory
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

package org.openmicroscopy.shoola.env.config;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * A collection of factory methods to create a {@link Registry} and helper
 * methods to manipulate one.
 * <p>Helper methods are needed so that we may link container's services 
 * to a registry without having to know about the actual {@link Registry}'s
 * implementation class -- this is required by some classes that perform 
 * initialization tasks.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class RegistryFactory 
{
	/**
	 * Creates a new empty {@link Registry}.
	 * 
	 * @return	See above.
	 */
	public static Registry makeNew()
	{
		return new RegistryImpl();
	}
	
	/**
	 * Creates a new {@link Registry} and fills it up with the entries
	 * in the specified cofiguration file.
	 * 
	 * @param file	Path to a configuration file.
	 * @return	A new {@link Registry} built from the specified file.
	 * @throws ConfigException	If an error occurs while accessing the file
	 * 							or the file contents are not valid.
	 */
	public static Registry makeNew(String file)
		throws ConfigException
	{
		RegistryImpl reg = new RegistryImpl();
		Parser p = new Parser(file, reg);
		p.parse();
		return reg;
	}
	
	/**
	 * Fills up the specified {@link Registry} with the entries
	 * in the specified cofiguration file.
	 * 
	 * @param file	Path to a configuration file.
	 * @param reg	The {@link Registry} to fill.
	 * @throws ConfigException	If an error occurs while accessing the file
	 * 							or the file contents are not valid.
	 */
	public static void fillFromFile(String file, Registry reg)
		throws ConfigException
	{
		Parser p = new Parser(file, (RegistryImpl) reg);
		p.parse();
	}
	/**
	 * Links the {@link Registry} and the {@link EventBus}.
	 * 
	 * @param eb	Reference to the {@link EventBus}.
	 * @param reg	The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkEventBus(EventBus eb, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setEventBus(eb);
	}
	/**
	 * Links the {@link Registry} and the {@link DataManagementService}.
	 * 
	 * @param dms	Reference to the {@link DataManagementService}.
	 * @param reg	The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkDMS(DataManagementService dms, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setDMS(dms);
	}
	
	/**
	 * Links the {@link Registry} and the {@link SemanticTypeService}.
	 * 
	 * @param sts	Reference to the {@link SemanticTypeService}.
	 * @param reg	The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkSTS(SemanticTypesService sts, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setSTS(sts);
	}
	/**
	 * Links the {@link Registry} and the {@link TopFrame}.
	 * 
	 * @param tf	Reference to the {@link TopFrame}.
	 * @param reg	The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkTopFrame(TopFrame tf, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setTopFrame(tf);
	}
	/**
	 * Links the {@link Registry} and the {@link Logger}.
	 * 
	 * @param logger	Reference to the {@link Logger}.
	 * @param reg		The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkLogger(Logger logger, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setLogger(logger);
	}
	/**
	 * Links the {@link Registry} and the {@link UserNotifier}.
	 * 
	 * @param un		Reference to the {@link UserNotifier}.
	 * @param reg		The {@link Registry} to link.
	 * @throws ConfigException
	 */
	public static void linkUserNotifier(UserNotifier un, Registry reg)
		throws ConfigException
	{
		((RegistryImpl) reg).setUserNotifier(un);
	}
	
	
	/* TODO: helper methods to link a service to the registry
	 * linkSemTypesService(sts, reg)
	 * etc.
	 */
}
