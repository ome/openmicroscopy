/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.GroupData;

/** 
 * Action to bring up the Switch user dialog.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SwitchUserAction 
	extends TreeViewerAction
	implements MouseListener
{

	/** The name of the action. */
	public static final String NAME = "Display User...";
	
	/** The name of the action. */
	public static final String NAME_TO = "Display User from";
	
	/** The description of the action. */
	public static final String DESCRIPTION = "Select users and view their" +
			" data.";
	
    /** The location of the mouse pressed. */
    private Point point;
    
    /** 
     * Handles the group.
     * 
     * @param group The group to handle.
     */
    private void handleGroupSelection(GroupData group)
    {
    	if (group == null) {
    		setEnabled(false);
    		return;
    	}
    	Collection l = group.getExperimenters();
    	if (l == null) {
    		setEnabled(true);
    		return;
    	}
    	int level = model.getGroupPermissions(group);
    	boolean b = false;
		if (level == GroupData.PERMISSIONS_PRIVATE) {
			if (model.isLeaderOfGroup(group) ||
					TreeViewerAgent.isAdministrator())
				b = l.size() > 1;
		} else {
			b = l.size() > 1;
		}
		setEnabled(b);
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
        	setEnabled(false);
            return;
        }
        handleGroupSelection(model.getSelectedGroup());
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
    		return;
    	}
    	if (browser.getState() == Browser.READY) {
    		onDisplayChange(browser.getLastSelectedDisplay());
    	} else setEnabled(false);
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
	public SwitchUserAction(TreeViewer model)
	{
		super(model);
		setEnabled(false);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.OWNER));
	}
	
    /**
     * Brings up the switch user dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	if (point != null) {
    		SwingUtilities.convertPointToScreen(point, 
    				((Component) e.getSource()));
    	}
    	//model.retrieveUserGroups(point, null);
    }
    
    /** 
     * Sets the location of the point where the <code>mousePressed</code>
     * event occurred. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { point = me.getPoint(); }
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in our
     * case, no-operation implementation.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {}
    
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
