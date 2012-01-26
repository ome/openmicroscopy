/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.PersonalManagementAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.GroupData;

/** 
 * Brings up the <code>Personal Management</code> menu.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PersonalManagementAction 
	extends TreeViewerAction
	implements MouseListener
{

	/** The description of the action. */
    private static final String DESCRIPTION = "Select your current group.";
    
    /** The location of the mouse pressed. */
    private Point point;

    /** Reference to the icons manager. */
    private IconManager icons;
    
    /** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onDataImport()
     */
    protected void onDataImport()
    {
    	onBrowserStateChange(model.getSelectedBrowser());
    }
    
    /** Sets the name, description and the icon depending on the permissions. */
    private void setPermissions()
    {
    	GroupData group = null;//model.getSelectedGroup();
    	String name = "";
    	String desc = DESCRIPTION;
    	Icon icon = icons.getIcon(IconManager.UP_DOWN_9_12);
    	if (group != null) {
    		name = group.getName();
    		int level = 
            TreeViewerAgent.getRegistry().getAdminService().getPermissionLevel(
            			group);
            switch (level) {
    			case AdminObject.PERMISSIONS_PRIVATE:
    				desc = AdminObject.PERMISSIONS_PRIVATE_TEXT;
    				icon = icons.getIcon(IconManager.PRIVATE_GROUP_DD_12);
    				break;
    			case AdminObject.PERMISSIONS_GROUP_READ:
    				desc = AdminObject.PERMISSIONS_GROUP_READ_TEXT;
    				icon = icons.getIcon(IconManager.READ_GROUP_DD_12);
    				break;
    			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
    				desc = AdminObject.PERMISSIONS_GROUP_READ_LINK_TEXT;
    				icon = icons.getIcon(IconManager.READ_LINK_GROUP_DD_12);
    				break;
    			case AdminObject.PERMISSIONS_PUBLIC_READ:
    				desc = AdminObject.PERMISSIONS_PUBLIC_READ_TEXT;
    				icon = icons.getIcon(IconManager.PUBLIC_GROUP_DD_12);
    				break;
    			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
    				desc = AdminObject.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
    				icon = icons.getIcon(IconManager.PUBLIC_GROUP_DD_12);
    		}

    	}
    	putValue(Action.NAME, name);
    	putValue(Action.SMALL_ICON, icon);
    	putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
    }
    
    /** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
    	if (browser == null) return;
    	if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
    		setEnabled(false);
    	} else {
    		if (browser.getState() == Browser.READY) {
    			UserNotifier un = 
    				TreeViewerAgent.getRegistry().getUserNotifier();
    			if (un.hasRunningActivities()) {
    				setEnabled(false);
    			} else {
    				if (!model.isImporting())
        				setEnabled(
        				TreeViewerAgent.getAvailableUserGroups().size() > 1);
        			else setEnabled(false);
    			}
        	} else setEnabled(false);
    	}
    }
    
    /** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onBrowserSelection(Browser)
     */
    protected void onBrowserSelection(Browser browser)
    {
    	onBrowserStateChange(browser);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public PersonalManagementAction(TreeViewer model)
    {
        super(model);
        icons = IconManager.getInstance();
        setPermissions();
        model.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (TreeViewer.GROUP_CHANGED_PROPERTY.equals(name)) {
					setPermissions();
				}
			}
		});
    }
    
    /** 
     * Sets the location of the point where the <code>mousePressed</code>
     * event occurred. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { point = me.getPoint(); }
    
    /** 
     * Brings up the menu. 
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
        Object source = me.getSource();
        if (point == null) point = me.getPoint();
        if (source instanceof Component && isEnabled())
            model.showMenu(TreeViewer.PERSONAL_MENU, (Component) source, point);
    }
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */   
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseExited(MouseEvent)
     */   
    public void mouseExited(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */   
    public void mouseClicked(MouseEvent e) {}
    
}
