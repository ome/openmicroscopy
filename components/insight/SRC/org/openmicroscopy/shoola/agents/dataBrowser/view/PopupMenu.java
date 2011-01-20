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
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

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
	
	/** Button to set the original rendering settings. */
	private JMenuItem			setOriginalRndSettings;

	/** Button to tag the element. */
	private JMenuItem			tagElement;
	
	/** Button to launch the editor with a new experiment. */
	private JMenuItem			newExperimentElement;

	/**
	 * Initializes the menu items with the given actions.
	 * 
	 * @param controller 	The Controller.
	 * @param model			The model.
	 */
	private void initComponents(DataBrowserControl controller, 
								DataBrowserModel model)
	{
		tagElement = new JMenuItem(controller.getAction(
				DataBrowserControl.TAG));
		newExperimentElement = new JMenuItem(controller.getAction(
				DataBrowserControl.NEW_EXPERIMENT));
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
		setOriginalRndSettings = new JMenuItem(
				controller.getAction(
						DataBrowserControl.SET_ORIGINAL_RND_SETTINGS));
		if (model.getType() == DataBrowserModel.SEARCH) {
			copyElement.setEnabled(false);
			pasteElement.setEnabled(false);
			removeElement.setEnabled(false);
			cutElement.setEnabled(false);
		}
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI() 
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(view);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(cutElement);
		add(copyElement);
		add(pasteElement);
		add(removeElement);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(tagElement);
		add(newExperimentElement);
		add(new JSeparator(JSeparator.HORIZONTAL));
		add(copyRndSettings);
		add(pasteRndSettings);
		//add(resetRndSettings);
		add(setOriginalRndSettings);
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
		initComponents(controller, model);
		buildGUI() ;
	}
	
}
