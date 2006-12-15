/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;



//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;


/** 
 * Retrieves the nodes containing images and whose children are loaded.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class RefreshVisitor
    extends BrowserVisitor
{

    /** 
     * Collection of expanded {@link TreeImageSet}s corresponding
     * to a container whose children are images.
     */
    private List 	foundNodes;
    
    
    /** Contains the expanded top container nodes ID. */
    private List     expandedTopNodes;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     */
    public RefreshVisitor(Browser model)
    {
        super(model);
        foundNodes = new ArrayList();
        expandedTopNodes = new ArrayList();
    }

    /**
     * Returns the list of nodes found.
     * 
     * @return See above.
     */
    public List getFoundNodes() { return foundNodes; }

    /**
     * Returns the list of expanded top nodes IDs.
     * 
     * @return See above.
     */
    public List getExpandedTopNodes() { return expandedTopNodes; }
    
    /**
     * Retrieves the expanded nodes. Only the nodes containing images
     * are taken into account.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    {
        Object userObject = node.getUserObject();
        if ((userObject instanceof DatasetData) || 
                (userObject instanceof CategoryData)) {
        	if (node.isChildrenLoaded() && node.isExpanded() &&
        			node.getParentDisplay().isExpanded()) 
                foundNodes.add(userObject);
        } else if ((userObject instanceof ProjectData) || 
                    (userObject instanceof CategoryGroupData)){
            if (node.isExpanded()) {
                long id = ((DataObject) userObject).getId();
                expandedTopNodes.add(new Long(id));
            }
        }     
    }
    
}
