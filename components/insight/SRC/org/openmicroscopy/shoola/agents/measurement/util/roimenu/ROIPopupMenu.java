/*
 * org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.roimenu;

//Java imports
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.util.actions.ROIAction;
import org.openmicroscopy.shoola.agents.measurement.util.actions.ROIStatsAction;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIPopupMenu
{	
	/** Text for the popup Menu -- not shown. */
	final static String POPUP_MENU_DESCRIPTION = "Manager Options";

	/** 
	 * Text for the ROI options -- parent of Split, merge, delete, propagate, 
	 * duplicate. 
	 * */
	final static String ROI_CREATION_OPTIONS = "ROI Management Options";
	
	/** 
	 * Text for the ROI stats options, the ability to call the roi stats:
	 * show intensity over time, project, etc.
	 * */
	final static String ROI_STATS_OPTIONS = "ROI Stats Options";
	
	/** The menubar which holds the menu items. */
	private JPopupMenu				popupMenu;
	
	/** The link to the controller for the pop up menu. */
	private ROIActionController		controller; 	
	
	/**
	 * Instantiate the popup menu
	 * @param controller class which has interface ROIActionController that 
	 * determines which action to perform depending on menu item selected.
	 */
	public ROIPopupMenu(ROIActionController controller)
	{
		this.controller = controller;
		createPopupMenu();
	}
			
	/**
	 * Creates the menu which will allow the user to adjust the ROI properties.
	 * 
	 * @return The ROI control menu.
	 */
	private JMenu createROICreationOptions()
	{
		JMenu roiOptionsParent = new JMenu(ROI_CREATION_OPTIONS);
		JMenuItem roiOption;
		
		for (int indexCnt = 0 ; indexCnt < ROIActionController.CreationActionType.values().length ; indexCnt++)
		{
			roiOption = new JMenuItem(new ROIAction(controller, 
					ROIActionController.CreationActionType.values()[indexCnt]));
			roiOptionsParent.add(roiOption);
		}
		return roiOptionsParent;
	}
	
	/**
	 * Creates the menu which will allow the user to calculate the ROIstats.
	 * 
	 * @return The ROI control menu.
	 */
	private JMenu createROIStatsOptions()
	{
		JMenu roiOptionsParent = new JMenu(ROI_STATS_OPTIONS);
		JMenuItem roiOption;
		
		for (int indexCnt = 0 ; indexCnt < ROIActionController.StatsActionType.values().length ; indexCnt++)
		{
			roiOption = new JMenuItem(new ROIStatsAction(controller, 
					ROIActionController.StatsActionType.values()[indexCnt]));
			roiOptionsParent.add(roiOption);
		}
		return roiOptionsParent;
	}
	
	/** Creates the popup menu. */
	private void createPopupMenu()
	{
		JMenuItem 	topOption;

		popupMenu = new JPopupMenu(POPUP_MENU_DESCRIPTION);
		
		topOption = new JMenuItem(POPUP_MENU_DESCRIPTION);
		popupMenu.add(topOption);
		popupMenu.addSeparator();
		popupMenu.add(createROICreationOptions());
	//	popupMenu.add(createROIStatsOptions());
	}

	/**
	 * Get the popup menu.
	 * @return see above.
	 */
	public JPopupMenu getPopupMenu()
	{
		return popupMenu;
	}
}