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
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
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
	 * Adds the {@link EventBus} instance to the container's {@link Registry}.
	 * 
	 * @param eb	The {@link EventBus} instance.
	 * @param reg	The container's {@link Registry}.
	 */
	public static void linkEventBus(EventBus eb, Registry reg)
	{
		((RegistryImpl) reg).setEventBus(eb);
	}
	
	/**
	 * Adds the {@link DataManagementService} instance to the container's 
	 * {@link Registry}.
	 * 
	 * @param dms	The {@link DataManagementService} instance.
	 * @param reg	The container's {@link Registry}.
	 */
	public static void linkDMS(DataManagementService dms, Registry reg)
	{
		((RegistryImpl) reg).setDMS(dms);
	}
	
	/**
	 * Adds the {@link SemanticTypesService} instance to the container's 
	 * {@link Registry}.
	 * 
	 * @param sts	The {@link SemanticTypesService} instance.
	 * @param reg	The container's {@link Registry}.
	 */
	public static void linkSTS(SemanticTypesService sts, Registry reg)
	{
		((RegistryImpl) reg).setSTS(sts);
	}
	
	/**
	 * Adds the {@link TopFrame} instance to the container's {@link Registry}.
	 * 
	 * @param tf	The {@link TopFrame} instance.
	 * @param reg	The container's {@link Registry}.
	 */
	public static void linkTopFrame(TopFrame tf, Registry reg)
	{
		((RegistryImpl) reg).setTopFrame(tf);
	}
	
	/**
	 * Adds the {@link Logger} instance to the container's {@link Registry}.
	 * 
	 * @param logger	The {@link Logger} instance.
	 * @param reg		The container's {@link Registry}.
	 */
	public static void linkLogger(Logger logger, Registry reg)
	{
		((RegistryImpl) reg).setLogger(logger);
	}
	
	/**
	 * Adds the {@link UserNotifier} instance to the container's 
	 * {@link Registry}.
	 * 
	 * @param un	The {@link UserNotifier} instance.
	 * @param reg	The container's {@link Registry}.
	 */
	public static void linkUserNotifier(UserNotifier un, Registry reg)
	{
		((RegistryImpl) reg).setUserNotifier(un);
	}
	
}
