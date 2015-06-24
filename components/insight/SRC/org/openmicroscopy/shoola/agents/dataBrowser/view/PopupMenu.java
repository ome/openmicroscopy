/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.PopupMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
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
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ActivatedUserAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.MoveToAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewOtherAction;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.LookupNames;

import pojos.ExperimenterData;

/** 
 * Pop-up menu for nodes in the browser display.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
class PopupMenu
    extends JPopupMenu
{

    /** Button to browse a container or bring up the Viewer for an image. */
    private JMenuItem view;

    /** Button to cut the selected elements. */
    private JMenuItem cutElement;

    /** Button to copy the selected elements. */
    private JMenuItem copyElement;

    /** Button to paste the selected elements. */
    private JMenuItem pasteElement;

    /** Button to remove the selected elements. */
    private JMenuItem removeElement;

    /** Button to paste the rendering settings. */
    private JMenuItem pasteRndSettings;

    /** Button to reset the rendering settings. */
    private JMenuItem resetRndSettings;

    /** Button to copy the rendering settings. */
    private JMenuItem copyRndSettings;

    /** Button to set the original rendering settings. */
    private JMenuItem setOwnerRndSettings;

    /** Button to tag the element. */
    private JMenuItem tagElement;

    /** Button to open a document with an external application. */
    private JMenu openWithMenu;

    /** Button to reset the password. */
    private JMenuItem resetPassword;

    /** Button to download the files.*/
    private JMenuItem download;

    /** Button to activate or not user. */
    private JCheckBoxMenuItem activatedUser;

    /** Reference to the control. */
    private DataBrowserControl controller;

    /** Reference to the model. */
    private DataBrowserModel model;

    /**
     * Creates a menu if the various groups the data can be moved to.
     * 
     * @return See above.
     */
    private JMenu createMoveToMenu()
    {
        List<MoveToAction> actions = controller.getMoveAction();
        if (CollectionUtils.isEmpty(actions)) return null;
        JMenu menu = new JMenu(MoveToAction.NAME);
        Iterator<MoveToAction> i = actions.iterator();
        while (i.hasNext()) {
            menu.add(new JMenuItem(i.next()));
        }
        return menu;
    }

    /**
     * Initializes the menu items with the given actions.
     */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        resetPassword = new JMenuItem(
                controller.getAction(DataBrowserControl.RESET_PASSWORD));
        ActivatedUserAction a = (ActivatedUserAction)
                controller.getAction(DataBrowserControl.USER_ACTIVATED);
        activatedUser = new JCheckBoxMenuItem();
        ImageDisplay node = model.getBrowser().getLastSelectedDisplay();
        boolean value = false;
        if (node != null) {
            Object o = node.getHierarchyObject();
            if (o instanceof ExperimenterData) {
                ExperimenterData exp = (ExperimenterData) o;
                ExperimenterData loggedIn = model.getCurrentUser();
                value = exp.getId() == loggedIn.getId();
                activatedUser.setSelected(exp.isActive());
                if (exp.isActive()) {
                    activatedUser.setIcon(ActivatedUserAction.ACTIVATED_ICON);
                } else {
                    activatedUser.setIcon(
                            ActivatedUserAction.NOT_ACTIVATED_ICON);
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
        tagElement = new JMenuItem(controller.getAction(
                DataBrowserControl.TAG));
        view = new JMenuItem(controller.getAction(DataBrowserControl.VIEW));
        download = new JMenuItem(controller.getAction(
                DataBrowserControl.DOWNLOAD));
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
        setOwnerRndSettings = new JMenuItem(
                controller.getAction(
                        DataBrowserControl.SET_OWNER_RND_SETTINGS));
        openWithMenu = new JMenu("Open with");
        openWithMenu.setIcon(icons.getIcon(IconManager.VIEWER));
        if (model.getType() == DataBrowserModel.SEARCH) {
            copyElement.setEnabled(false);
            pasteElement.setEnabled(false);
            cutElement.setEnabled(false);
        }
    }

    /**
     * Creates the menu to edit the object.
     * 
     * @return See above.
     */
    private JMenu buildEditMenu()
    {
        JMenu menu = new JMenu("Edit");
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
        JMenu menu = new JMenu("Rendering Settings");
        menu.add(copyRndSettings);
        menu.add(pasteRndSettings);
        menu.add(resetRndSettings);
        menu.add(setOwnerRndSettings);
        return menu;
    }

    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        if (model.getType() == DataBrowserModel.GROUP) {
            add(resetPassword);
            add(activatedUser);
            add(buildEditMenu());
            add(removeElement);
        } else {
            JMenu menu;
            String text = "View";
            switch (DataBrowserAgent.runAsPlugin()) {
            case LookupNames.IMAGE_J:
            case LookupNames.IMAGE_J_IMPORT:
                menu = new JMenu(text);
                menu.setIcon(view.getIcon());
                menu.add(view);
                menu.add(controller.getAction(DataBrowserControl.VIEW_IN_IJ));
                add(menu);
                break;
            case LookupNames.KNIME:
                menu = new JMenu(text);
                menu.setIcon(view.getIcon());
                menu.add(view);
                menu.add(controller.getAction(
                        DataBrowserControl.VIEW_IN_KNIME));
                add(menu);
                break;
            default:
                add(view);
            };
            add(openWithMenu);
            add(download);
            add(new JSeparator(JSeparator.HORIZONTAL));
            add(buildEditMenu());
            add(removeElement);
            JMenu m = createMoveToMenu();
            if (m != null) add(m);
            add(new JSeparator(JSeparator.HORIZONTAL));
            add(tagElement);
            add(new JSeparator(JSeparator.HORIZONTAL));
            add(buildRenderingSettingsMenu());
        }
    }

    /** 
     * Creates a new instance.
     *
     * @param controller The Controller. Mustn't be <code>null</code>.
     * @param model The Model. Mustn't be <code>null</code>.
     */
    PopupMenu(DataBrowserControl controller, DataBrowserModel model)
    {
        if (controller == null)
            throw new IllegalArgumentException("No control.");
        if (model == null) 
            throw new IllegalArgumentException("No model.");
        this.controller = controller;
        this.model = model;
        initComponents();
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
        if (!CollectionUtils.isEmpty(actions)) {
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
