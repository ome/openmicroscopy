/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.SelectedNodeVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

/** 
 * Selects the node hosting the {@link #selectedDisplay}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class SelectedNodeVisitor
	implements TreeViewNodeVisitor
{

    /** Reference to the Model. */
    private TreeView 		model;
    
    /** The selected node in the <code>Browser</code>. */
    private ImageDisplay	selectedDisplay;
    
    /**
     * Sets the selected node in the tree. 
     * 
     * @param node The visited node.
     */
    private void selectNode(TreeViewNode node)
    {
        if (selectedDisplay.equals(node.getUserObject()))
            model.selectNode(node);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model A reference to the Model. Mustn't be <code>null</code>.
     * @param selectedDisplay 	The selected node in the <code>Browser</code>.
     * 							Mustn't be <code>null</code>.
     */
    public SelectedNodeVisitor(TreeView model, ImageDisplay selectedDisplay)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (selectedDisplay == null) 
            throw new IllegalArgumentException("No selected node.");
        this.model = model;
        this.selectedDisplay = selectedDisplay;
    }
    
    /**
     * If the visited {@link TreeViewNode node} hosts the currently selected
     * {@link ImageDisplay} then the node is selected in the <code>Tree</code>.
     * @see TreeViewNodeVisitor#visit(TreeViewImageNode)
     */
    public void visit(TreeViewImageNode node) { selectNode(node) ; }

    /**
     * If the visited {@link TreeViewNode node} hosts the currently selected
     * {@link ImageDisplay} then the node is selected in the <code>Tree</code>.
     * @see TreeViewNodeVisitor#visit(TreeViewImageSet)
     */
    public void visit(TreeViewImageSet node) { selectNode(node) ; }

}
