/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.PopupMenu
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;


//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CreateTopContainerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.MoveToAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ViewOtherAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.LookupNames;

import pojos.ExperimenterData;


/** 
 * Pop-up menu for nodes in the browser display.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class PopupMenu
  	extends JPopupMenu
{

	/** Button to browse a container. */
	private JMenuItem browse;

	/** Button to add existing element to the specified container. */
	private JMenuItem existingElement;

	/** Button to import files to the specified container. */
	private JMenuItem importElement;
	
	/** Button to add element to the specified container. */
	private JMenuItem newElement;

	/** Button to cut the selected elements. */
	private JMenuItem cutElement;

	/** Button to copy the selected elements. */
	private JMenuItem copyElement;

	/** Button to paste the selected elements. */
	private JMenuItem pasteElement;

	/** Button to delete the selected elements. */
	private JMenuItem deleteElement;

	/** Button to download the selected elements. */
	private JMenuItem downloadElement;

	/** Button to remove experimenter node from the display. */
	private JMenuItem removeExperimenterElement;

	/** Button to refresh the experimenter data. */
	private JMenuItem refreshExperimenterElement;

	/** Button to add experimenter node from the display. */
	private JMenuItem addExperimenterElement;
	
	/** Button to refresh the tree data. */
	private JMenuItem refreshTreeElement;

	/** Button to create a top container. */
	private JMenuItem createTopElement;

	/** Button to switch user. */
	private JMenuItem switchUserElement;

	/** Button to paste Rnd settings. */
	private JMenuItem pasteRndElement;
	
	/** Button to copy Rnd settings. */
	private JMenuItem copyRndElement;
	
	/** Button to reset default Rnd settings. */
	private JMenuItem resetRndElement;
	
	/** Button to reset default Rnd settings. */
	private JMenuItem setOwnerRndElement;
	
	/** Button to add existing elements. */
	private JMenuItem addExistingElement;

	/** Button to quit the application. */
	private JMenuItem quitElement;
	
	/** Button to create a new project. */
	private JMenuItem createProject;
	
	/** Button to create a new dataset. */
	private JMenuItem createDataset;
	
	/** Button to create a new tag. */
	private JMenuItem createTag;
	
	/** Button to create a new screen. */
	private JMenuItem createScreen;
	
	/** Button to create a new Tag Set. */
	private JMenuItem createTagSet;
	
	/** Button to create a new group. */
	private JMenuItem createGroup;
	
	/** Button to create a experimenter. */
	private JMenuItem createExperimenter;
	
	/** Button to reset the password. */
	private JMenuItem resetPassword;
	
	/** Button to view an Image. */
	private JMenuItem view;
	
	/** Button to open the Tag wizard. */
	private JMenuItem tagElement;
	
	/** Button to view an Image using plug-in. */
	private JMenuItem viewInPlugin;
	
	/** Reference to the Control. */
	private TreeViewerControl controller;

	/** Reference to the Control. */
    private TreeViewerModel model;

	/** Font label. */
	private Font fontLabel;

	/** The index of the menu .*/
	private int index;

	/** The menu to open the file with third party. */
	private JMenu openWithMenu;
	
	/** Button to activate or not user. */
    private JCheckBoxMenuItem activatedUser;
    
    /** Button to remove group from the display. */
	private JMenuItem removeGroupElement;
    
	/**
	 * Sets the defaults of the specified menu item.
	 * 
	 * @param item The menu item.
	 * @param name The name of the item.
	 */
	private void initMenuItem(JMenuItem item, String name)
	{
		if (name != null) item.setText(name);
		item.setBorder(null);
		item.setFont(fontLabel);
	}

	/** Helper method to create the menu items with the given actions. */
	private void createMenuItems()
	{
		openWithMenu = new JMenu();
		initMenuItem(openWithMenu, "Open with");
		IconManager icons = IconManager.getInstance();
		openWithMenu.setIcon(icons.getIcon(IconManager.VIEWER));
		populateMenu();
		TreeViewerAction a;
		switch (index) {
			case TreeViewer.VIEW_MENU:
				switch (TreeViewerAgent.runAsPlugin()) {
				    case LookupNames.IMAGE_J:
				    case LookupNames.IMAGE_J_IMPORT:
						a = controller.getAction(TreeViewerControl.VIEW);
						view = new JMenuItem(a);
						initMenuItem(view, a.getActionName());
						a = controller.getAction(TreeViewerControl.VIEW_IN_IJ);
						viewInPlugin = new JMenuItem(a);
						initMenuItem(viewInPlugin, a.getActionName());
						break;
					case LookupNames.KNIME:
						a = controller.getAction(TreeViewerControl.VIEW);
						view = new JMenuItem(a);
						initMenuItem(view, a.getActionName());
						a = controller.getAction(
								TreeViewerControl.VIEW_IN_KNIME);
						viewInPlugin = new JMenuItem(a);
						initMenuItem(viewInPlugin, a.getActionName());
				}
				break;
			case TreeViewer.FULL_POP_UP_MENU:
				a = controller.getAction(TreeViewerControl.BROWSE);
				browse = new JMenuItem(a);
				initMenuItem(browse, a.getActionName());
				a = controller.getAction(TreeViewerControl.VIEW);
				view = new JMenuItem(a);
				initMenuItem(view, a.getActionName());
				a = null;
				switch (TreeViewerAgent.runAsPlugin()) {
					case LookupNames.IMAGE_J:
					case LookupNames.IMAGE_J_IMPORT:
						a = controller.getAction(TreeViewerControl.VIEW_IN_IJ);
						break;
					case LookupNames.KNIME:
						a = controller.getAction(
								TreeViewerControl.VIEW_IN_KNIME);
				}
				if (a != null) {
					viewInPlugin = new JMenuItem(a);
					initMenuItem(viewInPlugin, a.getActionName());
				}
				a = controller.getAction(TreeViewerControl.DOWNLOAD);
				downloadElement = new JMenuItem(a);
				initMenuItem(downloadElement, a.getActionName());

				a = controller.getAction(TreeViewerControl.TAGGING);
				tagElement = new JMenuItem(a);
				initMenuItem(tagElement, a.getActionName());
				
				a = controller.getAction(TreeViewerControl.IMPORT);
				importElement = new JMenuItem(a);
				initMenuItem(importElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.CREATE_OBJECT);
				newElement = new JMenuItem(a);
				initMenuItem(newElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.CUT_OBJECT);
				cutElement = new JMenuItem(a); 
				initMenuItem(cutElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.COPY_OBJECT);
				copyElement = new JMenuItem(a); 
				initMenuItem(copyElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.PASTE_OBJECT);
				pasteElement = new JMenuItem(a); 
				initMenuItem(pasteElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.DELETE_OBJECT);
				deleteElement = new JMenuItem(a); 
				initMenuItem(deleteElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.ADD_OBJECT);
				existingElement = new JMenuItem(a);
				initMenuItem(existingElement, a.getActionName());
				
				a = controller.getAction(TreeViewerControl.REMOVE_FROM_DISPLAY);
				removeExperimenterElement = new JMenuItem(a);
				initMenuItem(removeExperimenterElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.REMOVE_GROUP);
				removeGroupElement = new JMenuItem(a);
				initMenuItem(removeGroupElement, a.getActionName());
				
				a = controller.getAction(TreeViewerControl.SWITCH_USER);
				addExperimenterElement = new JMenuItem(a);
				addExperimenterElement.addMouseListener((SwitchUserAction) a);
				initMenuItem(addExperimenterElement, a.getActionName());
				
				a = controller.getAction(
						TreeViewerControl.REFRESH_EXPERIMENTER);
				refreshExperimenterElement = new JMenuItem(a);
				initMenuItem(refreshExperimenterElement, null);
				a = controller.getAction(TreeViewerControl.PASTE_RND_SETTINGS);
				pasteRndElement = new JMenuItem(a);
				initMenuItem(pasteRndElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.COPY_RND_SETTINGS);
				copyRndElement = new JMenuItem(a);
				initMenuItem(copyRndElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.RESET_RND_SETTINGS);
				resetRndElement = new JMenuItem(a);
				initMenuItem(resetRndElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.SET_RND_SETTINGS);
				a = controller.getAction(
						TreeViewerControl.SET_OWNER_RND_SETTINGS);
				setOwnerRndElement = new JMenuItem(a);
				initMenuItem(setOwnerRndElement, a.getActionName());

				a = controller.getAction(TreeViewerControl.CREATE_TOP_PROJECT);
				createProject = new JMenuItem(a);
				initMenuItem(createProject, a.getActionName());
				if (controller.isOrphanedImagesSelected()) {
					a = controller.getAction(
							TreeViewerControl.CREATE_DATASET_FROM_SELECTION);
				} else {
					a = controller.getAction(
							TreeViewerControl.CREATE_TOP_DATASET);
					((CreateTopContainerAction) a).setFromTopMenu(true);
				}
				createDataset = new JMenuItem(a);
				initMenuItem(createDataset, a.getActionName());
				a = controller.getAction(
						TreeViewerControl.CREATE_TOP_SCREEN);
				createScreen = new JMenuItem(a);
				initMenuItem(createScreen, a.getActionName());
				a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG_SET);
				createTagSet = new JMenuItem(a);
                initMenuItem(createTagSet, a.getActionName());
                a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG);
                createTag = new JMenuItem(a);
                initMenuItem(createTag, a.getActionName());
                a = controller.getAction(TreeViewerControl.CREATE_TOP_GROUP);
				break;
			case TreeViewer.PARTIAL_POP_UP_MENU:
				a = controller.getAction(TreeViewerControl.REFRESH_TREE);
				refreshTreeElement = new JMenuItem(a);
				initMenuItem(refreshTreeElement, a.getActionName());
				a = controller.getAction(
						TreeViewerControl.CREATE_TOP_PROJECT);
				createTopElement = new JMenuItem(a);
				initMenuItem(createTopElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.SWITCH_USER);
				switchUserElement = new JMenuItem(a);
				initMenuItem(switchUserElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.EXIT);
				quitElement = new JMenuItem(a);
				initMenuItem(quitElement, a.getActionName());
				break;
			case TreeViewer.CREATE_MENU_CONTAINERS:
				a = controller.getAction(TreeViewerControl.CREATE_TOP_PROJECT);
				createProject = new JMenuItem(a);
				initMenuItem(createProject, a.getActionName());
				if (controller.isOrphanedImagesSelected()) {
					a = controller.getAction(
							TreeViewerControl.CREATE_DATASET_FROM_SELECTION);
				} else {
					a = controller.getAction(
							TreeViewerControl.CREATE_TOP_DATASET);
					((CreateTopContainerAction) a).setFromTopMenu(true);
				}
				createDataset = new JMenuItem(a);
				initMenuItem(createDataset, a.getActionName());
				break;
			case TreeViewer.CREATE_MENU_SCREENS:
				a = controller.getAction(
						TreeViewerControl.CREATE_TOP_SCREEN);
				createScreen = new JMenuItem(a);
				initMenuItem(createScreen, a.getActionName());
				break;
			case TreeViewer.CREATE_MENU_TAGS:
				a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG);
				((CreateTopContainerAction) a).setFromTopMenu(true);
				createTag = new JMenuItem(a);
				initMenuItem(createTag, a.getActionName());
				a = controller.getAction(TreeViewerControl.CREATE_TOP_TAG_SET);
				createTagSet = new JMenuItem(a);
				initMenuItem(createTagSet, a.getActionName());
				break;
			case TreeViewer.CREATE_MENU_ADMIN:
				a = controller.getAction(TreeViewerControl.CREATE_TOP_GROUP);
				createGroup = new JMenuItem(a);
				initMenuItem(createGroup, a.getActionName());
				a = controller.getAction(
						TreeViewerControl.CREATE_TOP_EXPERIMENTER);
				createExperimenter = new JMenuItem(a);
				initMenuItem(createExperimenter, a.getActionName());
				break;
			case TreeViewer.ADMIN_MENU:
				a = controller.getAction(
						TreeViewerControl.CREATE_TOP_EXPERIMENTER);
				createExperimenter = new JMenuItem(a);
				initMenuItem(createExperimenter, a.getActionName());
				a = controller.getAction(TreeViewerControl.ADD_OBJECT);
				addExistingElement = new JMenuItem(a);
				initMenuItem(addExistingElement, a.getActionName());
				
				a = controller.getAction(TreeViewerControl.CUT_OBJECT);
				cutElement = new JMenuItem(a); 
				initMenuItem(cutElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.COPY_OBJECT);
				copyElement = new JMenuItem(a); 
				initMenuItem(copyElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.PASTE_OBJECT);
				pasteElement = new JMenuItem(a); 
				initMenuItem(pasteElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.DELETE_OBJECT);
				deleteElement = new JMenuItem(a); 
				initMenuItem(deleteElement, a.getActionName());
				a = controller.getAction(TreeViewerControl.RESET_PASSWORD);
				resetPassword = new JMenuItem(a);
				initMenuItem(resetPassword, a.getActionName());
				a = controller.getAction(TreeViewerControl.USER_ACTIVATED);
				activatedUser = new JCheckBoxMenuItem();
				TreeImageDisplay node = controller.getLastSelectedDisplay();
				boolean value = false;
				if (node != null) {
					Object o = node.getUserObject();
					if (o instanceof ExperimenterData) {
						ExperimenterData exp = (ExperimenterData) o;
						ExperimenterData loggedIn =
							TreeViewerAgent.getUserDetails();
						value = exp.getId() == loggedIn.getId();
						activatedUser.setSelected(exp.isActive());
						if (exp.isActive()) {
							activatedUser.setIcon(
									icons.getIcon(IconManager.OWNER));
						} else {
							activatedUser.setIcon(
								icons.getIcon(IconManager.OWNER_NOT_ACTIVE));
						}
						activatedUser.setEnabled(!value &&
						        !model.isSystemUser(exp.getId()));
					}
					if (!value)
						activatedUser.addItemListener(new ItemListener() {
							
							public void itemStateChanged(ItemEvent e) {
								controller.activateUser();
							}
						});
				} else activatedUser.setEnabled(false);
				activatedUser.setAction(a);
				initMenuItem(activatedUser, a.getActionName());
		}
	}

	/**
	 * Creates a menu if the various groups the data can be moved to.
	 * 
	 * @return See above.
	 */
	private JMenu createMoveToMenu()
	{
		List<MoveToAction> actions = controller.getMoveAction();
		if (actions == null || actions.size() == 0) return null;
		JMenu menu = new JMenu(MoveToAction.NAME);
		Iterator<MoveToAction> i = actions.iterator();
		while (i.hasNext()) {
			menu.add(new JMenuItem(i.next()));
		}
		return menu;
	}
	
	/**
	 * Creates the menu to create new object.
	 * 
	 * @return See above.
	 */
	private JMenu buildCreateNewMenu()
	{
	    JMenu menu = new JMenu();
	    initMenuItem(menu, TreeViewerWin.CREATE_NEW_MENU);
	    //Check the context
	    int type = controller.getSelectedBrowserType();
	    if (type == Browser.TAGS_EXPLORER) {
	        menu.add(createTagSet);
	        menu.add(createTag);
	    } else if (type == Browser.SCREENS_EXPLORER) {
	        menu.add(createScreen);
	    } else {
	        menu.add(createProject);
	        menu.add(createDataset);
	    }
		return menu;
	}
	
	/**
	 * Creates the menu to edit the object.
	 * 
	 * @return See above.
	 */
	private JMenu buildEditMenu()
	{
		JMenu menu = new JMenu();
		initMenuItem(menu, TreeViewerWin.EDIT_MENU);
		menu.add(cutElement);
		menu.add(copyElement);
		menu.add(pasteElement);
		return menu;
	}
	
	/**
	 * Creates the menu to manipulate the rendering settings.
	 * 
	 * @return See above.
	 */
	private JMenu buildRenderingSettingsMenu()
	{
		JMenu menu = new JMenu();
		initMenuItem(menu, TreeViewerWin.RENDERING_SETTINGS_MENU);
		menu.add(copyRndElement);
		menu.add(pasteRndElement);
		menu.add(resetRndElement);
		menu.add(setOwnerRndElement);
		return menu;
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		switch (index) {
			case TreeViewer.FULL_POP_UP_MENU:
				add(browse);
				if (viewInPlugin != null) {
					JMenu menu = new JMenu();
					initMenuItem(menu, TreeViewerWin.VIEW_MENU);
					menu.setIcon(view.getIcon());
					menu.add(view);
					menu.add(viewInPlugin);
					add(menu);
				} else add(view);
				add(openWithMenu);
				add(downloadElement);
				add(new JSeparator(JSeparator.HORIZONTAL));
				add(importElement);
				add(buildCreateNewMenu());
				add(buildEditMenu());
				add(deleteElement);
				JMenu m = createMoveToMenu();
				if (m != null) add(m);
				add(new JSeparator(JSeparator.HORIZONTAL));
				add(tagElement);
				add(new JSeparator(JSeparator.HORIZONTAL));
				add(buildRenderingSettingsMenu());
				add(removeGroupElement);
				add(refreshExperimenterElement);
				add(removeExperimenterElement);
				break;
			case TreeViewer.PARTIAL_POP_UP_MENU:
				add(refreshTreeElement);
				add(createTopElement);
				add(switchUserElement);
				add(quitElement);
				break;
			case TreeViewer.CREATE_MENU_CONTAINERS:
				add(createProject);
				add(createDataset);
				break;
			case TreeViewer.CREATE_MENU_SCREENS:
				add(createScreen);
				break;
			case TreeViewer.CREATE_MENU_TAGS:
				add(createTagSet);
				add(createTag);
				break;
			case TreeViewer.CREATE_MENU_ADMIN:
				add(createGroup);
				add(createExperimenter);
				break;
			case TreeViewer.ADMIN_MENU:
				add(createExperimenter);
				add(addExistingElement);
				add(new JSeparator(JSeparator.HORIZONTAL));
				add(resetPassword);
				add(activatedUser);
				add(buildEditMenu());
				add(deleteElement);
				break;
			case TreeViewer.VIEW_MENU:
				if (viewInPlugin != null) {
					add(view);
					add(viewInPlugin);
				}
		}
	}

	/** Populates the menu to view with other applications. */
	private void populateMenu()
	{
		List<ViewOtherAction> l = controller.getApplicationActions();
		JMenuItem item;
		TreeViewerAction a;
		if (l.size() > 0) {
			Iterator<ViewOtherAction> i = l.iterator();
			while (i.hasNext()) {
				a = i.next();
				item = new JMenuItem(a);
				initMenuItem(item, a.getActionName());
				openWithMenu.add(item);
			}
			openWithMenu.add(new JSeparator());
		}
		a = controller.getAction(TreeViewerControl.VIEWER_WITH_OTHER);
		item = new JMenuItem(a);
		initMenuItem(item, a.getActionName());
		openWithMenu.add(item);
	}
	
	/** 
	 * Creates a new instance.
	 *
	 * @param controller The Controller. Mustn't be <code>null</code>.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param index The index of the menu. One of the following
	 *              {@link TreeViewer#FULL_POP_UP_MENU} or
	 *              {@link TreeViewer#PARTIAL_POP_UP_MENU}
	 */
	PopupMenu(TreeViewerControl controller, TreeViewerModel model, int index)
	{
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.index = index;
		this.controller = controller;
		this.model = model;
		fontLabel = (Font) TreeViewerAgent.getRegistry().lookup(
				"/resources/fonts/Labels");
		createMenuItems();
		buildGUI() ;
	}
	
}
