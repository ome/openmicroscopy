/*
 * org.openmicroscopy.shoola.env.init.FakeCacheInit 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

import omero.gateway.cache.CacheService;
import org.openmicroscopy.shoola.env.cache.NullCacheService;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.log.NullLogger;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class FakeCacheInit    
	extends InitializationTask
{

    /**
     * Default Null service.
     * Change it to whatever implementation is required by your tests.
     */
    public static final CacheService  service = new NullCacheService();

    /**
     * Constructor required by superclass.
     */
    public FakeCacheInit() {}

    /**
     * Returns the name of this task.
     * @see InitializationTask#getName()
     */
    String getName() 
    {
        return "Creating Task Bar";
    }

    /** 
     * Does nothing, as this task requires no set up.
     * @see InitializationTask#configure()
     */
    void configure() {}

    /** 
     * Carries out this task.
     * 
     * @see InitializationTask#execute()
     */
    void execute() 
        throws StartupException
    {
        Registry reg = container.getRegistry();
        RegistryFactory.linkCacheService(service, reg);
    }
    
    /** 
     * Does nothing.
     * @see InitializationTask#rollback()
     */
    void rollback() {}

}
