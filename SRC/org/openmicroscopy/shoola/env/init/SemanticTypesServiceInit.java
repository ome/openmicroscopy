/*
 * org.openmicroscopy.shoola.env.init.SemanticTypeServiceInit
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
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;

/** 
 * Creates the {@link SemanticTypesService} and links it to the container's
 * {@link Registry}.
 *
 * @see	InitializationTask
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
final class SemanticTypesServiceInit
	extends InitializationTask
{

	/**
	 * Constructor required by superclass.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 */
	SemanticTypesServiceInit(Container c)
	{
		super(c);
	}
	
	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName()
	{
		return "Starting Semantic Types Service";
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
		Registry reg = container.getRegistry();
		SemanticTypesService sts = DataServicesFactory.createSTS(reg);
		RegistryFactory.linkSTS(sts, reg);
	}
	
	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}

}
