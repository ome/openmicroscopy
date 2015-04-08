/*
 * org.openmicroscopy.shoola.env.init.DataServicesTestsInit
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
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.ConfigException;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import omero.gateway.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.Env;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.SyncMonitorFactory;

/** 
 * Initializes the Data Services so that can be used in
 * <code>DataServicesTestCase</code>s.
 * This task initializes the Data Services so that they connect to the server
 * address found in <code>Env</code> and use a <code>SyncBatchCallMonitor</code>
 * in asynchronous calls.  (This way a call outcome notification is always 
 * dispatched in the current thread instead of the <i>Swing</i> dispatcher.)
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
public class DataServicesTestsInit
    extends InitializationTask
{   
    
    /**
     * Constructor required by the superclass.
     */
    public DataServicesTestsInit() {}

    /**
     * Returns the name of this task.
     * @see InitializationTask#getName()
     */
    String getName()
    {
        return "Starting Test Data Services";
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
        try {
            //Rebind OMERO config entries with test entries
            OMEROInfo omeroAddr = new OMEROInfo(Env.getOmeroPort(),
            		Env.getOmeroPortSSL(), Env.getOmeroHost(), "false");
            reg.bind(LookupNames.OMERODS, omeroAddr);
            //Create services.
            DataServicesFactory factory = 
                                     DataServicesFactory.getInstance(container);
            
            //Link them to the container's registry.
            RegistryFactory.linkIS(factory.getIS(), reg);
            RegistryFactory.linkOS(factory.getOS(), reg);
            RegistryFactory.linkMS(factory.getMS(), reg);
            RegistryFactory.linkAdmin(factory.getAdmin(), reg);
            
            //Finally create and bind the factory used by the async data views
            //to create exec monitors.
            SyncMonitorFactory smf = new SyncMonitorFactory();
            reg.bind(LookupNames.MONITOR_FACTORY, smf);
        } catch (IllegalArgumentException iae) {
            throw new StartupException("No server URL.", iae);
        } catch (ConfigException ce) {
            throw new StartupException("Malformed server URL.", ce);
        } catch (DSOutOfServiceException dsose) {
            throw new StartupException("Can't initialize OMEDS proxies.", 
                                       dsose);
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
            factory.shutdown(null); 
        } catch (DSOutOfServiceException e) {}  
    }
    
    
}
