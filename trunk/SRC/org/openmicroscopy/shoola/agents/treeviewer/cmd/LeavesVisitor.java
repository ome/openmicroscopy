/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.LeavesVisitor
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
import java.util.HashSet;
import java.util.Set;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import pojos.ImageData;

/** 
 * Retrieves the nodes hosting {@link ImageData} objects. 
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
public class LeavesVisitor
    extends BrowserVisitor
{

    /** Set of nodes */
    private Set nodes;
    
    /** Set of corresponding <code>DataObject</code>s IDs*/
    private Set objects;
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the {@link Browser}.
     *                      Mustn't be <code>null</code>.
     */
    public LeavesVisitor(Browser model)
    {
        super(model);
        nodes = new HashSet();
        objects = new HashSet();
    }

    /**
     * Retrieves the node hosting an {@link ImageData} object.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node)
    { 
        Object uo = node.getUserObject();
        if (uo instanceof ImageData) {
            nodes.add(node);
            objects.add(new Long(((ImageData) uo).getId()));
        }
    }
    
    /**
     * Retrieves the node hosting an {@link ImageData} object.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    { 
        Object uo = node.getUserObject();
        if (uo instanceof ImageData) {
            nodes.add(node);
            objects.add(new Long(((ImageData) uo).getId()));
        }
    }
    
    /**
     * Returns the collection of images' id.
     * 
     * @return See above.
     */
    public Set getNodeIDs() { return objects; }
    
    /**
     * Returns the collection of {@link TreeImageNode}s.
     * 
     * @return See above.
     */
    public Set getNodes() { return nodes; }
    
    
}
