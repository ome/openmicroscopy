/*
 * org.openmicroscopy.shoola.agents.editor.actions.PersonalManagementAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.editor.actions;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.GroupData;

/** 
 * Brings up the menu with available group.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class PersonalManagementAction 
	extends EditorAction
	implements MouseListener
{

	/** The description of the action. */
    private static final String DESCRIPTION = "Select your current group.";
    
    /** The location of the mouse pressed. */
    private Point point;

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public PersonalManagementAction(Editor model)
    {
        super(model);
        setEnabled(true);
        setPermissions();
    }
    
    /** Sets the name, description and the icon depending on the permissions. */
    public void setPermissions()
    {
    	GroupData group = model.getSelectedGroup();
    	String name = "";
    	String desc = DESCRIPTION;
    	int iconID = IconManager.UP_DOWN_9_12;
    	if (group != null) {
    		name = group.getName();
            switch (group.getPermissions().getPermissionsLevel()) {
    			case GroupData.PERMISSIONS_PRIVATE:
    				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
    				iconID = IconManager.PRIVATE_GROUP_DD_12;
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ:
    				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
    				iconID = IconManager.READ_GROUP_DD_12;
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ_LINK:
    				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
    				iconID = IconManager.READ_LINK_GROUP_DD_12;
    				break;
    			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
    				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
    				iconID = IconManager.READ_WRITE_GROUP_DD_12;
    				break;
    			case GroupData.PERMISSIONS_PUBLIC_READ:
    				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
    				iconID = IconManager.PUBLIC_GROUP_DD_12;
    				break;
    			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
    				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
    				iconID = IconManager.PUBLIC_GROUP_DD_12;
    		}
    	}
    	putValue(Action.NAME, name);
    	setIcon(iconID);
    	putValue(Action.SHORT_DESCRIPTION, UIUtilities.formatToolTipText(desc));
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
            model.showMenu(Editor.GROUP_MENU, (Component) source, point);
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
