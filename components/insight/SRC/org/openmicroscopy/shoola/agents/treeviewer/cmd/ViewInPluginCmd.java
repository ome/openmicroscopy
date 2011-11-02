/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewInPluginCmd 
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

import java.util.Set;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ViewInPluginCmd 
	implements ActionCmd
{

	/** Reference to the model. */
	private TreeViewer model;

    /** Indicate the plugin to open.*/
    private int plugin;

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
	 * @param plugin Indicate the plug-in to use.
	 */
	public ViewInPluginCmd(TreeViewer model, int plugin)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		this.model = model;
		this.plugin = plugin;
	}

	/** Implemented as specified by {@link ActionCmd}. */
	public void execute()
	{
		Browser browser = model.getSelectedBrowser();
		if (browser == null) return;
		//browser.browse(browser.getLastSelectedDisplay(), null, withThumbnails);
	}

}
