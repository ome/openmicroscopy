/*
 * org.openmicroscopy.shoola.env.init.NullDataServicesInit
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.data.NullOmeroPojoService;
import org.openmicroscopy.shoola.env.data.NullRenderingService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;

/** 
 * Fake intialization task.
 * Binds the container's registry to the data management services specified by
 * its static fields.
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
public class FakeDataServicesInit
    extends InitializationTask
{
    
    /** 
     * Default Null service.
     * Change it to whatever implementation is required by your tests.
     */
    public static final OmeroImageService rds = new NullRenderingService();
    
    /** 
     * Default Null service.
     * Change it to whatever implementation is required by your tests.
     */
    public static final OmeroDataService os = new NullOmeroPojoService();

    /**
     * Constructor required by superclass.
     */
    public FakeDataServicesInit() {}

    /**
     * Returns the name of this task.
     * @see InitializationTask#getName()
     */
    String getName() { return "Starting null data management services"; }

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
        //Link services to the container's registry.
        Registry reg = container.getRegistry();
        RegistryFactory.linkIS(rds, reg); 
        RegistryFactory.linkOS(os, reg); 
    }
    
    /** 
     * Shuts services down.
     * @see InitializationTask#rollback()
     */
    void rollback() {}
    
}
