/*
 * org.openmicroscopy.shoola.agents.browser.BrowserLoader
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
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.env.config.Registry;

/**
 * A cleaner (and separate) implementation of the browser loader class previously
 * found in BrowserAgent.  Coordinates the retrieval of information from the
 * database and builds a browser window based on the contents of a dataset.
 * 
 * Part of a resolution to bug 183.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public final class BrowserLoader
{
    private List imageTypeList; // the list of image-granular STs.
    private BrowserEnvironment env; // the browser context.
    private Registry appContext; // the Shoola context.
    
    // TODO [BUG 184]: change this
    private int datasetID; // the dataset being loaded by this loader.
    private String datasetName;
    
    /**
     * Constructs a browser loader that will be taking care of loading the dataset
     * with the specified ID.
     * 
     * TODO [BUG 184] change datasetToLoad to generic list of images.
     * 
     * @param appContext The application context.
     * @param datasetToLoad The ID of the dataset to load.
     * @throws IllegalArgumentException If the context is null.
     */
    public BrowserLoader(Registry appContext, int datasetToLoad)
        throws IllegalArgumentException
    {
        if(appContext == null)
        {
            throw new IllegalArgumentException("Null context");
        }
        this.appContext = appContext;
        this.datasetID = datasetToLoad;
        env = BrowserEnvironment.getInstance();
        imageTypeList = new ArrayList();
    }
    
    /**
     * Begins loading the dataset and creating a browser window.  Assumes that
     * the application context has been fully specified; that is, image types have
     * been loaded into the browser environment, the
     */
    public void load() // TODO: throws exceptions (specify which ones)
    {
        
    }
    
    /**
     * Cancels loading this browser.
     */
    public void cancelLoad()
    {
    }
}
