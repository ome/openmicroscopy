/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.PopupMenu 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewOtherAction;

//Third-party libraries

//Application-internal dependencies

/** 
 * Pop-up menu for nodes in the browser display.
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
class PopupMenu 
	extends JPopupMenu
{
	
	/** Button to browse a container or bring up the Viewer for an image. */
	private JMenuItem   		view;

	/** Button to cut the selected elements. */
	private JMenuItem			cutElement;

	/** Button to copy the selected elements. */
	private JMenuItem			copyElement;

	/** Button to paste the selected elements. */
	private JMenuItem			pasteElement;

	/** Button to remove the selected elements. */
	private JMenuItem			removeElement;

	/** Button to paste the rendering settings. */
	private JMenuItem			pasteRndSettings;

	/** Button to reset the rendering settings. */
	private JMenuItem			resetRndSettings;

	/** Button to copy the rendering settings. */
	private JMenuItem			copyRndSettings;
	
	/** Button to set the min/max for each channel. */
	private JMenuItem			setMinMaxSettings;

	/** Button to set the original rendering settings. */
	private JMenuItem			setOwnerRndSettings;
	
	/** Button to tag the element. */
	private JMenuItem			tagElement;
	
	/** Button to launch the editor with a new experiment. */
	private JMenuItem			newExperimentElement;

	/** Button to send feedback about an image. */
	private JMenuItem			sendFeedbackElement;
	
	/** Button to open a document with an external application. */
	private JMenu				openWithMenu;
	
	/** Reference to the control. */
	private DataBrowserControl controller;
	
	/**
	 * Initializes the menu items with the given actions.
	 * 
	 * @param model The model.
	 */
	private void initComponents(DataBrowserModel model)
	{
		tagElement = new JMenuItem(controller.getAction(
				DataBrowserControl.TAG));
		newExperimentElement = new JMenuItem(controller.getAction(
				DataBrowserControl.NEW_EXPERIMENT));
		sendFeedbackElement = new JMenuItem(controller.getAction(
				DataBrowserControl.SEND_FEEDBACK));
		
		view = new JMenuItem(controller.getAction(DataBrowserControl.VIEW));
		copyElement = new JMenuItem(
					controller.getAction(DataBrowserControl.COPY_OBJECT));
		cutElement = new JMenuItem(
				controller.getAction(DataBrowserControl.CUT_OBJECT));
		pasteElement = new JMenuItem(
						controller.getAction(DataBrowserControl.PASTE_OBJECT));
		removeElement = new JMenuItem(
				controller.getAction(DataBrowserControl.REMOVE_OBJECT));
		pasteRndSettings = new JMenuItem(
				controller.getAction(DataBrowserControl.PASTE_RND_SETTINGS));
		resetRndSettings = new JMenuItem(
				controller.getAction(DataBrowserControl.RESET_RND_SETTINGS));
		copyRndSettings = new JMenuItem(
				controller.getAction(DataBrowserControl.COPY_RND_SETTINGS));
		setMinMaxSettings = new JMenuItem(
				controller.getAction(
						DataBrowserControl.SET_MIN_MAX_SETTINGS));
		setOwnerRndSettings = new JMenuItem(
				controller.getAction(
						DataBrowserControl.SET_OWNER_RND_SETTINGS));
		openWithMenu = new JMenu("Open with");
		IconManager icons = IconManager.getInstance();
		openWithMenu.setIcon(icons.getIcon(IconManager.VIEWER));
		if (model.getType() == DataBrowserModel.SEARCH) {
			copyElement.setEnabled(false);
			pasteElement.setEnabled(false);
			//removeElement.setEnabled(false);
			cutElement.setEnabled(false);
		}
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI() 
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(view);
		add(openWithMenu);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(cutElement);
		add(copyElement);
		add(pasteElement);
		add(removeElement);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(tagElement);
		add(newExperimentElement);
		add(sendFeedbackElement);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(copyRndSettings);
		add(pasteRndSettings);
		add(resetRndSettings);
		add(setMinMaxSettings);
		add(setOwnerRndSettings);
	}
	
	/** 
	 * Creates a new instance.
	 *
	 * @param controller 	The Controller. Mustn't be <code>null</code>.
	 * @param model 		The Model. Mustn't be <code>null</code>.
	 */
	PopupMenu(DataBrowserControl controller, DataBrowserModel model)
	{
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		this.controller = controller;
		initComponents(model);
		buildGUI() ;
	}
	
	/**
	 * Populates the menu with the passed actions.
	 * 
	 * @param actions The list of actions.
	 */
	void populateOpenWith()
	{
		openWithMenu.removeAll();
		List<ViewOtherAction> actions = controller.getApplicationActions();
		if (actions != null && actions.size() > 0) {
			Iterator<ViewOtherAction> i = actions.iterator();
			while (i.hasNext()) {
				openWithMenu.add(new JMenuItem(i.next()));
			}
			openWithMenu.add(new JSeparator());
		}
		openWithMenu.add(new JMenuItem(
				controller.getAction(DataBrowserControl.OPEN_WITH)));
	}
	
}
