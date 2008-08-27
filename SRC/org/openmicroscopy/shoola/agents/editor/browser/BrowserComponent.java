 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserComponent 
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

import javax.swing.JComponent;
import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/**  
 * Implements the {@link Browser} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It delegates actual functionality to the
 * MVC sub-components.
 *
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BrowserComponent 
	extends AbstractComponent
	implements Browser{
	
	
	/** The Model sub-component. */
    private BrowserModel    	model;
    
    /** The View sub-component. */
    private BrowserUI       	view;
    
    /** The Controller sub-component. */
    private BrowserControl  	controller;
    
    BrowserComponent(BrowserModel model, String viewingMode) {
    	this.model = model;
    	controller = new BrowserControl(this);
    	
        view = new BrowserUI(viewingMode);
    }
    
    /** 
	 * Links up the MVC triad. 
	 * Called by BrowserFactory.
	 * 
	 * @param exp The logged in experimenter.
	 */
	void initialize()
	{
	    model.initialize(this);
	    controller.initialize(view);
	    view.initialize(controller, model);
	}

	public void setTreeModel(TreeModel treeModel) 
    {
    	model.setTreeModel(treeModel);
    	view.displayTree();
    }

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
	public JComponent getUI() 
	{ 
		return view;
	}

}
