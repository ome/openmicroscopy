/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;



//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link Browser} objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowserFactory
{

    /**
     * Creates a new {@link Browser}.
     * 
     * @param browserType   The browser's type to create.
     * @param parent        Reference to the parent. 
     *                      Mustn't be <code>null</code>.  
     * @return See above.
     */
    public static Browser createBrowser(int browserType, TreeViewer parent)
    {
        if (parent == null)
            throw new IllegalArgumentException("No parent.");
        BrowserModel model = new BrowserModel(browserType, parent);
        BrowserComponent component = new BrowserComponent(model);
        model.initialize(component);
        component.initialize();
        return component;
    }
    
}
