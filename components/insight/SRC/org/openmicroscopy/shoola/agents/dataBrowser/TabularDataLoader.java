/*
 * org.openmicroscopy.shoola.agents.metadata.TabularDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.PlateData;

/** 
 * Loads the tabular data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TabularDataLoader 
	extends DataBrowserLoader
{

	/** Parameters to load the table. */
	private TableParameters parameters;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ids The identifier of the files hosting the tabular data.
     */
    public TabularDataLoader(DataBrowser viewer, List<Long> ids)
    {
    	 super(viewer);
    	 if (ids == null || ids.size() <= 0)
    		 throw new IllegalArgumentException("No file to retrieve.");
    	 parameters = new TableParameters(ids);
    }
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param plate The plate to handle.
     */
    public TabularDataLoader(DataBrowser viewer, PlateData plate)
    {
    	 super(viewer);
    	 if (plate == null)
    		 throw new IllegalArgumentException("No file to retrieve.");
    	 parameters = new TableParameters(PlateData.class, plate.getId());
    }
    
    /** 
	 * Loads the tabular data. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		handle = mhView.loadTabularData(parameters, -1, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	//decide what to do with result
    	if (result == null) return;
    	viewer.setTabularData((List<TableResult>) result);
    } 

}
