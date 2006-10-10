/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;



//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import pojos.CategoryData;
import pojos.DatasetData;


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
    private List foundNodes;
    
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
    }

    /**
     * Returns the list of nodes found.
     * 
     * @return See above.
     */
    public List getFoundNodes() { return foundNodes; }
    
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
            if (node.isChildrenLoaded()) foundNodes.add(userObject);
        }
    }
    
}
