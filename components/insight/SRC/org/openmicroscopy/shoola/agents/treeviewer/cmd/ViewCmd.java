/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

/** 
* Views the selected image or browses the selected container.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
public class ViewCmd
	implements ActionCmd
{

	/** Reference to the model. */
	private TreeViewer model;

    /** Flag indicating to browse the object and retrieve the thumbnails. */
    private boolean withThumbnails;
    
	/**
	 * Returns the images' id contained in the passed node.
	 * 
	 * @param node 		The node to handle.
	 * @param browser 	The selected browser.
	 * @return See above.
	 */
	static Set getImageNodeIDs(TreeImageDisplay node, Browser browser) 
	{
		LeavesVisitor visitor = new LeavesVisitor(browser);
		node.accept(visitor);
		return visitor.getNodeIDs();
	}

	/**
	 * Returns the images contained in the passed node.
	 * 
	 * @param node 		The node to handle.
	 * @param browser 	The selected browser.
	 * @return See above.
	 */
	static Set getImageNodes(TreeImageDisplay node, Browser browser) 
	{
		LeavesVisitor visitor = new LeavesVisitor(browser);
		node.accept(visitor);
		return visitor.getNodes();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	public ViewCmd(TreeViewer model, boolean withThumbnails)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		this.model = model;
		this.withThumbnails = withThumbnails;
	}

	/** Implemented as specified by {@link ActionCmd}. */
	public void execute()
	{
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		browser.browse(browser.getLastSelectedDisplay(), withThumbnails);
	}
  
}
