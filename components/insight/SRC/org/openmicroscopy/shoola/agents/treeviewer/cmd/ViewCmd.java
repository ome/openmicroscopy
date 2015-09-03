/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

import java.util.Set;

import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
* Views the selected image or browses the selected container.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @version 2.2
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
	 * @param node The node to handle.
	 * @param browser The selected browser.
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
	 * @param node The node to handle.
	 * @param browser The selected browser.
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
	 *                      <code>false</code> otherwise.
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
		TreeImageDisplay d = browser.getLastSelectedDisplay();
		//add check for null node since we can browse specify null node
		if (d != null && d.getUserObject() instanceof ImageData) {
		    Object uo = d.getUserObject();
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			ViewImageObject vo = new ViewImageObject((ImageData) uo);
			TreeImageDisplay p = d.getParentDisplay();
			TreeImageDisplay gp = null;
			DataObject po = null;
			DataObject gpo = null;
			if (p != null) {
				uo = p.getUserObject();
				gp = p.getParentDisplay();
				if (uo instanceof DataObject)
					po = (DataObject) uo;
				if (gp != null) {
					uo = gp.getUserObject();
					if (uo instanceof DataObject)
						gpo = (DataObject) uo;
				}
			}
			vo.setContext(po, gpo);
			SecurityContext ctx = browser.getSecurityContext(d);
			ViewImage evt = new ViewImage(ctx, vo, model.getUI().getBounds());
			evt.setSeparateWindow(true);
			bus.post(evt);
		} else 
			browser.browse(d, null, withThumbnails);
	}

}
