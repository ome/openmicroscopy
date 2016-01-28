/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.actions;


import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;


import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.GroupData;

/** 
 * Brings up the <code>Personal Management</code> menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PersonalManagementAction 
	extends ImporterAction
	implements MouseListener
{

	/** The description of the action. */
    private static final String DESCRIPTION = "Select the group " +
    		"you wish to import the data to.";
    
    /** The location of the mouse pressed. */
    private Point point;

    /** Reference to the icons manager. */
    private IconManager icons;
    
    /** Sets the name, description and the icon depending on the permissions. */
    private void setPermissions()
    {
    	GroupData group = model.getSelectedGroup();
    	String name = "";
    	String desc = DESCRIPTION;
    	Icon icon = icons.getIcon(IconManager.UP_DOWN_9_12);
    	if (group != null) {
    		name = group.getName();
            switch (group.getPermissions().getPermissionsLevel()) {
    			case GroupData.PERMISSIONS_PRIVATE:
    				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
    				icon = icons.getIcon(IconManager.PRIVATE_GROUP_DD_12);
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ:
    				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
    				icon = icons.getIcon(IconManager.READ_GROUP_DD_12);
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ_LINK:
    				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
    				icon = icons.getIcon(IconManager.READ_LINK_GROUP_DD_12);
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
    				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
    				icon = icons.getIcon(IconManager.READ_WRITE_GROUP_DD_12);
    				break;
    			case GroupData.PERMISSIONS_PUBLIC_READ:
    				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
    				icon = icons.getIcon(IconManager.PUBLIC_GROUP_DD_12);
    				break;
    			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
    				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
    				icon = icons.getIcon(IconManager.PUBLIC_GROUP_DD_12);
    		}

    	}
    	putValue(Action.NAME, name);
    	putValue(Action.SMALL_ICON, icon);
    	putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
    }

    /**
     * Sets the <code>enabled</code> flag depending on the state.
     * @see ImporterAction#onStateChange()
     */
    protected void onStateChange()
    {
    	if (model.getState() == Importer.IMPORTING) {
    		setEnabled(false);
    	} else {
    		setEnabled(ImporterAgent.getAvailableUserGroups().size() > 1);
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public PersonalManagementAction(Importer model)
    {
        super(model);
        icons = IconManager.getInstance();
        setPermissions();
        setEnabled(ImporterAgent.getAvailableUserGroups().size() > 1);
        model.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (Importer.CHANGED_GROUP_PROPERTY.equals(name)) {
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
            model.showMenu(Importer.PERSONAL_MENU, (Component) source, point);
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
