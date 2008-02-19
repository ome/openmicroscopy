/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerUI 
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MetadataViewerUI 
	extends TopWindow
{

	/** Reference to the Control. */
	private MetadataViewerControl 		controller;

	/** Reference to the Model. */
	private MetadataViewerModel   		model;
	
	/** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        //c.add(model.getBrowser().getUI());
    }
    
	/**
	 * Creates a new instance.
	 * The 
	 * {@link #initialize(MetadataViewerControl, MetadataViewerModel) initialize} 
	 * method should be called straight after to link this View 
	 * to the Controller.
	 * 
	 * @param title The window title.
	 */
	MetadataViewerUI()
	{
		super("");
	}
	
	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(MetadataViewerControl controller, MetadataViewerModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		buildGUI();
	}
	
	/**
	 * Brings up the popup menu on top of the specified component at the
     * specified location.
     * 
	 * @param invoker 	The component that requested the popup menu.
	 * @param location 	The point at which to display the menu, relative to the
     *            		<code>component</code>'s coordinates.
	 */
	void showMenu(Component invoker, Point location)
	{
		PopupMenu menu = new PopupMenu(controller);
		menu.show(invoker, location.x, location.y);
	}
	
	/** Overrides the {@link #setOnScreen() setOnScreen} method. */
    public void setOnScreen()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(8*(screenSize.width/10), 8*(screenSize.height/10));
        UIUtilities.centerAndShow(this);
    }
    
}
