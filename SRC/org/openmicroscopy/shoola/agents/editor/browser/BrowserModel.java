 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies

/** 
 *	The Browser model. 
 * The data is stored in a treeModel.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class BrowserModel
{
    
    /** Reference to the component that embeds this model. */
    protected Browser           component;

    /** The model is delegated to a treeModel. */
    private TreeModel 			treeModel;
    
    /**
     * Creates an instance. 
     */
    BrowserModel() {
    }
    
    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }
    
    /**
     * Returns a reference to the treeModel
     * 
     * @return	A reference to the treeModel.
     */
    TreeModel getTreeModel() { return treeModel; }
    
    /**
     * Sets the treeModel.
     * 
     * @param model		the new TreeModel.
     */
    void setTreeModel(TreeModel model) 
    {
    	this.treeModel = model;
    }
}
