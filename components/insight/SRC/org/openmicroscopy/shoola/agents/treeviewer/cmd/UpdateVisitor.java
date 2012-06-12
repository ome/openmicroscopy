/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.UpdateVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Updates the object e.g. name.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class UpdateVisitor
	extends BrowserVisitor
{

	/** The update object.*/
	private DataObject object;
	
	/**
	 * Updates the specified node.
	 * 
	 * @param node The node to handle.
	 */
	private void updateNode(TreeImageDisplay node)
	{
		if (node == null) return;
		Object ho = node.getUserObject();
		if (!(ho instanceof DataObject)) return;
		DataObject data = (DataObject) ho;
		if (data.getClass().equals(object.getClass()) &&
			object.getId() == data.getId()) {
			node.setUserObject(object);
			TreeViewerTranslator.formatToolTipFor(node);
		}
	}
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     */
    public UpdateVisitor(Browser model, DataObject object)
    {
        super(model);
        if (object == null)
        	throw new IllegalArgumentException("No object to update.");
        this.object = object;
    }
    
    /**
     * Updates the nodes.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node) { updateNode(node); }
    
    /**
     * Updates the node.
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { updateNode(node); }

}
