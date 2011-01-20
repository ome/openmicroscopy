/*
 * org.openmicroscopy.shoola.agents.treeviewer.AdminCreator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.ExperimenterData;

/** 
 * Creates groups and/or experimenters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AdminCreator 
	extends DataTreeViewerLoader
{

	/** The object to handle. */
	private AdminObject object;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the Model. Mustn't be <code>null</code>.
     * @param object The object hosting details about object to create.
     */
	public AdminCreator(TreeViewer viewer, AdminObject object)
	{
		super(viewer);
		if (object == null)
			throw new IllegalArgumentException("No object");
		this.object = object;
	}
	
	/**
     * Creates the object.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	switch (object.getIndex()) {
			case AdminObject.CREATE_GROUP:
				handle = adminView.createGroup(object, this);
				break;
			case AdminObject.CREATE_EXPERIMENTER:
				handle = adminView.createExperimenters(object, this);
				break;
			case AdminObject.RESET_PASSWORD:
				handle = adminView.resetExperimentersPassword(object, this);
				break;
			case AdminObject.ADD_EXPERIMENTER_TO_GROUP:
				Map m = new HashMap();
				m.put(object.getGroup(), object.getExperimenters().keySet());
				handle = dmView.addExistingObjects(m, this);
		}
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        switch (object.getIndex()) {
        	case AdminObject.CREATE_GROUP:
        	case AdminObject.CREATE_EXPERIMENTER:
        	case AdminObject.ADD_EXPERIMENTER_TO_GROUP:
        		viewer.refreshTree();
				break;
        	case AdminObject.RESET_PASSWORD:
        		List l = (List) result;
        		if (l.size() != 0) {
        			UserNotifier un = 
        				TreeViewerAgent.getRegistry().getUserNotifier();
        			Iterator i = l.iterator();
        			ExperimenterData exp;
        			String s = "";
        			while (i.hasNext()) {
        				exp = (ExperimenterData) i.next();
						s += exp.getUserName()+"\n";
					}
        			StringBuffer buffer = new StringBuffer();
        			buffer.append("The password could not be reset for ");
        			buffer.append("the following experimenters:\n");
        			buffer.append(s);
        			un.notifyInfo("Reset password", buffer.toString());
        		}
		}
    }
    
}
