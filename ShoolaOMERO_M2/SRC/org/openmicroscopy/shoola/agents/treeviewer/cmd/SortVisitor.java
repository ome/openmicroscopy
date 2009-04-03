/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.SortVisitor
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
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;

/** 
 * Retrieves all displayed {@link TreeImageNode}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class SortVisitor
    extends BrowserVisitor
{

    /** Collection of retrieved {@link TreeImageNode}s. */
    private List nodes;
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the {@link Browser model}.
     *                  Mustn't be <code>null</code>
     */
    SortVisitor(Browser model)
    {
        super(model);
        nodes = new ArrayList();
    }
    
    /** 
     * Returns a read-only version of the collection of nodes.
     * 
     * @return See above.
     */
    List getNodes() { return nodes; };
    
    /** 
     * Retrieves all {@link TreeImageNode}s.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { nodes.add(node); }
    
}
