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
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * A collection of factory methods to create a {@link Registry} and helper
 * methods to manipulate one.
 * <p>Helper methods are needed so that we may link container's services 
 * to a registry without having to know about the actual {@link Registry}'s
 * implementation class &#151; this is required by some classes that perform 
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
	public static Registry makeNew() { return new RegistryImpl(); }
	
	/**
	 * Creates a new {@link Registry} and fills it up with the entries
	 * in the specified cofiguration file.
	 * 
	 * @param file	Path to a configuration file.
	 * @return A new {@link Registry} built from the specified file.
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
	 * Adds the {@link EventBus} instance to the specified {@link Registry}.
	 * 
	 * @param eb	The {@link EventBus} instance.
	 * @param reg	The {@link Registry}.
	 */
	public static void linkEventBus(EventBus eb, Registry reg)
	{
		((RegistryImpl) reg).setEventBus(eb);
	}
    
    /**
     * Adds the {@link OmeroImageService} instance to the specified
     * {@link Registry}.
     * 
     * @param rds   The {@link OmeroImageService} instance.
     * @param reg   The {@link Registry}.
     */
    public static void linkRDS(OmeroImageService rds, Registry reg)
    {
        ((RegistryImpl) reg).setRenderingService(rds);
    }
	
	/**
	 * Adds the {@link TaskBar} instance to the specified {@link Registry}.
	 * 
	 * @param tb	The {@link TaskBar} instance.
	 * @param reg	The {@link Registry}.
	 */
	public static void linkTaskBar(TaskBar tb, Registry reg)
	{
		((RegistryImpl) reg).setTaskBar(tb);
	}
	
	/**
	 * Adds the {@link Logger} instance to the specified {@link Registry}.
	 * 
	 * @param logger	The {@link Logger} instance.
	 * @param reg		The {@link Registry}.
	 */
	public static void linkLogger(Logger logger, Registry reg)
	{
		((RegistryImpl) reg).setLogger(logger);
	}
	
	/**
	 * Adds the {@link UserNotifier} instance to the specified {@link Registry}.
	 * 
	 * @param un	The {@link UserNotifier} instance.
	 * @param reg	The {@link Registry}.
	 */
	public static void linkUserNotifier(UserNotifier un, Registry reg)
	{
		((RegistryImpl) reg).setUserNotifier(un);
	}
    
    /**
     * Adds the {@link OmeroDataService} instance to
     * the specified {@link Registry}.
     * 
     * @param os    The {@link OmeroDataService} instance.
     * @param reg   The {@link Registry}.
     */
    public static void linkOS(OmeroDataService os, Registry reg)
    {
        ((RegistryImpl) reg).setOS(os);
    }
	
}
