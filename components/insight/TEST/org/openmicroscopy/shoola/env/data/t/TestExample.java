/*
 * org.openmicroscopy.shoola.env.data.t.TestExample
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

package org.openmicroscopy.shoola.env.data.t;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DataServicesTestCase;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * 
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
public class TestExample
    extends DataServicesTestCase
{

    /** Basic example to show how to access a <code>DataServiceView</code>. */
    public void testADataServicesView()
    {
        //Get a reference to the Data Services View we're going to use.
        registry.getDataServicesView(HierarchyBrowsingView.class);
     
        //Create an observer so to be notified of the outcome of the call.
        //Because the Container is in test mode, this observer will be run
        //in this thread.
        new DSCallAdapter() {
            public void handleException(Throwable exc)
            {
                throw new RuntimeException("Unexpected exception.", exc);
            }
            public void handleNullResult()
            {
                fail("Shouldn't have returned null.");
            }

            public void handleResult(Object result)
            {
                //TODO: check result...
            }
        };
        
        //Measure how long this call takes and fail if it takes too long.
        //Note that because the Container is in test mode, this call is 
        //run in this thread.
        long start = System.currentTimeMillis(), end;
        //hbw.loadHierarchy(ProjectSummary.class, 1, observer);
        end = System.currentTimeMillis();
        assertTrue("The call took too long: "+(end-start)+"ms.", 
                   end-start < 300);
        //TODO: value depends on how much data will be retrieved, so
        //these kind of tests have to assume the DB has been loaded
        //in a way known a-priori.
    }
    
    /** Basic example to show how to access the <code>OMERO</code> service. */
    public void testOmeroService() 
        throws DSOutOfServiceException, DSAccessException
    {
        //Get a reference to the Data Management Service.
        OmeroDataService os = registry.getDataService();
        //Measure how long this call takes and fail if it takes too long.
        //Note that calls to the DMS are synchronous even when the Container
        //is not in test mode.
        long start = System.currentTimeMillis(), end;
        os.getExperimenterImages(new SecurityContext(0), 1, false);
        //TODO: check result.
        end = System.currentTimeMillis();
        assertTrue("The call took too long: "+(end-start)+"ms.", 
                   end-start < 300);
        //TODO: value depends on how much data will be retrieved, so
        //these kind of tests have to assume the DB has been loaded
        //in a way known a-priori.
    }
    
}
