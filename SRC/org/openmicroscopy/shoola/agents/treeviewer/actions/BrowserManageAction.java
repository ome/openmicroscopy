/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserManageAction
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.actions;



//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Brings up the <code>Manager</code> menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BrowserManageAction 
	extends BrowserAction
	 implements MouseListener
{

	/** Indicates to show the menu for tags. */
	public static final int		NEW_TAGS = 0;
	
	/** Indicates to show the menu for tags. */
	public static final int		NEW_CONTAINERS = 1;
	
    /** The description of the action. */
    private static final String DESCRIPTION_CONTAINERS = 
    	"Create new Project, Dataset or Screen.";

    /** The description of the action. */
    private static final String DESCRIPTION_TAGS = "Create new Tag Set or Tag";

    /** The location of the mouse pressed. */
    private Point 	point;
    
    /** One of the constants defined by this class. */
    private int		index;
    
    /**
     * Controls if the passed index is valid or not.
     * 
     * @param index
     */
    private void checkIndex(int index)
    {
    	IconManager im = IconManager.getInstance();
    	switch (index) {
			case NEW_CONTAINERS:
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
				putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION_CONTAINERS));
				break;
			case NEW_TAGS:
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.TAG));
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_TAGS));
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
	    	 case Browser.LOADING_DATA:
	         case Browser.LOADING_LEAVES:
	         case Browser.COUNTING_ITEMS:  
	             setEnabled(false);
	             break;
	         default:
	         	setEnabled(true);
         }
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
	public BrowserManageAction(Browser model, int index)
	{
		super(model);
		checkIndex(index);
		this.index = index;
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
        if (source instanceof Component && isEnabled()) {
        	switch (index) {
			case NEW_TAGS:
				model.showMenu(TreeViewer.CREATE_MENU_TAGS, 
	            		(Component) source, point);
				break;
			case NEW_CONTAINERS:
				model.showMenu(TreeViewer.CREATE_MENU_CONTAINERS, 
	            		(Component) source, point);
        	}
        }
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
