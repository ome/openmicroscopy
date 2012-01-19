/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ParentVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ParentVisitor
	implements TreeImageDisplayVisitor
{
	
	/** Map hosting the data objects owned by users displayed.*/
	private Map<Long, List<TreeImageDisplay>> data;
	
	/** Creates a new instance.*/
	public ParentVisitor()
	{
		data = new HashMap<Long, List<TreeImageDisplay>>();
	}
	
    /**
     * Implemented as specified by {@link BrowserVisitor}.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) {}
    
    /**
     * Retrieves the node hosting an {@link ImageData} object.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    { 
        Object uo = node.getUserObject();
        if (uo instanceof ProjectData || uo instanceof ScreenData ||
        		uo instanceof DatasetData || uo instanceof PlateData) {
           TreeImageDisplay parent = node.getParentDisplay();
           Object ho = parent.getUserObject();
           if (ho instanceof ExperimenterData) {
        	   ExperimenterData exp = (ExperimenterData) ho;
        	   List<TreeImageDisplay> l = data.get(exp.getId());
        	   if (l == null) {
        		   l = new ArrayList<TreeImageDisplay>();
        		   data.put(exp.getId(), l);
        	   }
        	   l.add(node);
           }
        }
    }
    
	/**
	 * Returns the data.
	 * 
	 * @return See above.
	 */
	public Map<Long, List<TreeImageDisplay>> getData() { return data; }

}
