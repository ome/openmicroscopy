/*
 * org.openmicroscopy.shoola.env.init.DataServicesInit
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */


package org.openmicroscopy.shoola.env.init;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;

/** 
 * Creates the {@link DataManagementService} and {@link SemanticTypesService}
 * and links them to the container's {@link Registry}.
 *
 * @author Douglas Creager (dcreager@alum.mit.edu)
 * @version 2.2 <i>(Internal: $Revision$ $Date$)</i>
 * @since OME2.2
 */

final class DataServicesInit
	extends InitializationTask
{

	/**
	 * Constructor required by superclass.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 */
	DataServicesInit(Container c)
	{
		super(c);
	}

	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName()
	{
		return "Starting data management services";
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
		try {
			//Create services.
			DataServicesFactory 
				factory = DataServicesFactory.getInstance(container);
			//Retrieve them.
			DataManagementService dms = factory.getDMS();
			SemanticTypesService sts = factory.getSTS();
			PixelsService ps = factory.getPS();
			//Link them to the container's registry.
			Registry reg = container.getRegistry();
			RegistryFactory.linkDMS(dms, reg);
			RegistryFactory.linkSTS(sts, reg);
			RegistryFactory.linkPS(ps, reg);
		} catch (DSOutOfServiceException e) {
			throw new StartupException("Can't connect to OMEDS", e);
		} 
	}
	
	/** 
	 * Shuts services down.
	 * @see InitializationTask#rollback()
	 */
	void rollback()
	{
		try {
			DataServicesFactory factory = 
								DataServicesFactory.getInstance(container);
			factory.shutdown();	
		} catch (DSOutOfServiceException e) {}	
	}
	
}
