/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.IconsVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Visits the nodes to set the icon associated to the hierarchy object.
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
public class IconsVisitor
    implements ImageDisplayVisitor
{

	/** Helper reference. */
	private IconManager icons;
	
    /** Creates a new instance. */
    public IconsVisitor()
    {
    	icons = IconManager.getInstance();
    }
    
    /**
     * Sets a suitable icon for this node.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
    	Object ho = node.getHierarchyObject();
    	if (ho instanceof ProjectData)
    		node.setFrameIcon(icons.getIcon(IconManager.PROJECT));
    	else if (ho instanceof DatasetData) {
    		if (node.isAnnotated())
    			node.setFrameIcon(icons.getIcon(IconManager.ANNOTATED_DATASET));
    		else 
    			node.setFrameIcon(icons.getIcon(IconManager.DATASET));
    	} else if (ho instanceof CategoryGroupData)
    		node.setFrameIcon(icons.getIcon(IconManager.CATEGORY_GROUP));
    	else if (ho instanceof CategoryData)
    		node.setFrameIcon(icons.getIcon(IconManager.CATEGORY));
    	if (node.getParentDisplay() == null)  //Root node.
    	node.setFrameIcon(icons.getIcon(IconManager.ROOT));
    }
    
    /**
     * Does nothing, as {@link ImageNode}s have no icon.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) {}

}
