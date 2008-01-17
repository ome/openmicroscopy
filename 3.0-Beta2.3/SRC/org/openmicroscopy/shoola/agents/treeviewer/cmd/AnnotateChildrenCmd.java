/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.AnnotateChildrenCmd 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;

import pojos.CategoryData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;

/** 
 * Command to annotate all the images within a given container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AnnotateChildrenCmd 
	implements ActionCmd
{

	/** Reference to the model. */
    private TreeViewer model;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public AnnotateChildrenCmd(TreeViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	Browser browser = model.getSelectedBrowser();
    	if (browser == null) return;
    	TreeImageDisplay node = browser.getLastSelectedDisplay();
    	Set<DataObject> s;
    	if (node instanceof TreeImageTimeSet) {
    		TreeImageTimeSet time = (TreeImageTimeSet) node;
    		ExperimenterData exp = model.getUserDetails();
    		TimeRefObject ref = new TimeRefObject(exp.getId(), 
    				time.getStartTime(), time.getEndTime());
    		model.annotate(ref);
    	} else {
    		Object ho = node.getUserObject();
    		Class klass = null;
    		s = new HashSet<DataObject>(1);
    		if (ho instanceof DatasetData) {
    			klass = DatasetData.class;
    			s.add((DatasetData) ho);
    		} else if (ho instanceof CategoryData) {
    			klass = CategoryData.class;
    			s.add((CategoryData) ho);
    		}
    		if (klass != null)
    			model.annotateChildren(klass, s);
    	}
    }
    
}
