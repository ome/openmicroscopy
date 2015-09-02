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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies

import org.jhotdraw.draw.Figure;
import org.openmicroscopy.shoola.agents.measurement.util.actions.ROIAction;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController.CreationActionType;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

/** 
 * Displays options to manipulate a ROI.
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
	
	/** Text for the pop-up Menu -- not shown. */
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
	
	/** The list of actions. */
	private List<ROIAction>			actions;
	
	/**
	 * Instantiate the popup menu
	 * @param controller class which has interface ROIActionController that 
	 * determines which action to perform depending on menu item selected.
	 */
	public ROIPopupMenu(ROIActionController controller)
	{
		this.controller = controller;
		actions = new ArrayList<ROIAction>();
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
		ROIAction action;
		CreationActionType[] values = 
			ROIActionController.CreationActionType.values();
		for (int indexCnt = 0 ; indexCnt < values.length ; indexCnt++)
		{
			action = new ROIAction(controller, values[indexCnt]);
			actions.add(action);
			popupMenu.add(new JMenuItem(action));
		}
		return roiOptionsParent;
	}

	/** Creates the popup menu. */
	private void createPopupMenu()
	{
		popupMenu = new JPopupMenu();
		createROICreationOptions();
	}

	/**
	 * Sets the flag to access the action to manage ROIs.
	 * 
	 * @param figures The selected figure.
	 */
	public void setActionsEnabled(Collection<Figure> figures)
	{
		Iterator<Figure> i = figures.iterator();
		Figure figure;
		int readable = 0;
		int delete = 0;
		int edit = 0;
		ROIFigure roi;
		while (i.hasNext()) {
			figure = i.next();
			if (figure instanceof ROIFigure) {
				roi = (ROIFigure) figure;
				if (!(roi.isReadOnly())) {
					readable++;
					if (roi.canEdit()) edit++;
					if (roi.canDelete()) delete++;
				}
			}
		}
		Iterator<ROIAction> j = actions.iterator();
		if (readable != figures.size()) { //some read-only
			while (j.hasNext()) {
				j.next().setEnabled(false);
			}
		} else {
			ROIAction action;
			boolean db = delete == figures.size();
			boolean eb = edit == figures.size();
			while (j.hasNext()) {
				action = j.next();
				switch (action.getCreationActionType()) {
					case DUPLICATE:
						action.setEnabled(true);
						break;
					case DELETE:
						action.setEnabled(db);
						break;
					default:
						action.setEnabled(eb);
				}
			}
		}
	}

	/**
	 * Returns the popup menu.
	 * @return see above.
	 */
	public JPopupMenu getPopupMenu() { return popupMenu; }
	
}