/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import omero.gateway.model.DataObject;

/** 
 * Delete the selected object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class DeleteCmd
    implements ActionCmd
{

    /** Reference to the model. */
    private Browser model;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public DeleteCmd(Browser model)
    {
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	if (model == null) return;
        TreeImageDisplay[] nodes = model.getSelectedDisplays();
        if (nodes.length == 0) return;
        List<TreeImageDisplay> objects = new ArrayList<TreeImageDisplay>();
        TreeImageDisplay n;
        for (int i = 0; i < nodes.length; i++) {
            n = nodes[i];
            if (n != null && (n.getUserObject() instanceof DataObject)) {
            	objects.add(n);
            }
        }
        if (objects.size() > 0) model.deleteObjects(objects);
    }
    
}
