/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;

import omero.gateway.model.ExperimenterData;

/** 
 * Factory to create {@link Browser} objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class BrowserFactory
{

    /**
     * Returns the node hosting the experimenter passing a child node.
     * 
     * @param node The child node.
     * @return See above.
     */
    public static TreeImageDisplay getDataOwner(TreeImageDisplay node)
    {
    	return EditorUtil.getDataOwner(node);
    }
    
    /**
     * Creates a new {@link Browser}.
     * 
     * @param browserType   The browser's type to create.
     * @param parent        Reference to the parent. 
     *                      Mustn't be <code>null</code>.  
     * @param experimenter  The experimenter this browser is for. 
     * @param display		Pass <code>true</code> to indicate that the 
     * 						browser will be displayed, <code>false</code>
     * 						otherwise.
     * @return See above.
     */
    public static Browser createBrowser(int browserType, TreeViewer parent, 
    								ExperimenterData experimenter, boolean
    								display)
    {
        if (parent == null)
            throw new IllegalArgumentException("No parent.");
        BrowserModel model = new BrowserModel(browserType, parent);
        model.setDisplayed(display);
        BrowserComponent component = new BrowserComponent(model);
        model.initialize(component);
        component.initialize(experimenter);
        return component;
    }
    
}
