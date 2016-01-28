/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ImageData;
import omero.gateway.model.WellSampleData;

/** 
 * Displays the available analysis routines.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AnalysisDialog 
	extends JPopupMenu
{
	
	/** The text associated to the FLIM action. */
	private static final String FLIM_TEXT = "FLIM";
	
	/** The text associated to the FLIM action. */
	private static final String FLIM_TOOLTIP = "";
	
	/** The text associated to the FRAP action. */
	private static final String FRAP_TEXT = "FRAP";
	
	/** The text associated to the FRAP action. */
	private static final String FRAP_TOOLTIP = "FRAP Analysis";
	
	/** Reference to the control. */
	private EditorControl controller;
	
	/** Reference to the Model. */
	private EditorModel   model;
	
	/** Component to do a FRAP analysis. */
	private JMenuItem 		FRAPItem;
	
	/** Component to do a FLIM analysis. */
	private JMenuItem 		FLIMItem;
	/**
	 * Creates a menu item.
	 * 
	 * @param icon 	  The icon associated to the item.
	 * @param tooltip The text displayed in the tool tip.
	 * @param text    The text associated to the item.
	 * @param id   The id of the action.
	 * @return See above.
	 */
	private JMenuItem createMenuItem(Icon icon, String tooltip, String text, 
			int id)
	{
		JMenuItem b = new JMenuItem(icon);
		b.setText(text);
		b.setToolTipText(tooltip);
		b.addActionListener(controller);
		b.setActionCommand(""+id);
		b.setEnabled(false);
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		FRAPItem = createMenuItem(icons.getIcon(IconManager.ANALYSE_FRAP), 
				FRAP_TOOLTIP, FRAP_TEXT, EditorControl.ANALYSE_FRAP);
		FLIMItem = createMenuItem(icons.getIcon(IconManager.ANALYSE), 
				FLIM_TOOLTIP, FLIM_TEXT, EditorControl.ANALYSE_FLIM);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(FRAPItem);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the controller.
	 */
	AnalysisDialog(EditorControl controller, EditorModel model)
	{
		this.controller = controller;
		this.model = model;
		initComponents();
		setRootObject();
		buildGUI();
	}
	
	/** Sets the root object. */
	void setRootObject()
	{
		Object refObject = model.getRefObject();
    	ImageData img = null;
    	
    	FRAPItem.setEnabled(false);
    	FLIMItem.setEnabled(false);
    	if (refObject instanceof ImageData) {
    		img = (ImageData) refObject;
    	} else if (refObject instanceof WellSampleData) {
    		img = ((WellSampleData) refObject).getImage();
    	}
    	if (img != null) {
    		try {
    			img.getDefaultPixels();
    			FRAPItem.setEnabled(true);
    			FLIMItem.setEnabled(true);
			} catch (Exception e) {}
    	}
	}

}
