/*
 * org.openmicroscopy.shoola.env.ui.TaskBar
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.ui;

//Java imports
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the functionality of the task bar UI.
 * <p>The task bar is a top level window that contains a menu bar and a series
 * of toolbars.  The container brings this window up after initialization for
 * the user to control some of the container's tasks &#151; like the connection
 * to remote services or quitting the application.</p>
 * <p>Agents that have a UI typically add an entry to the {@link #WINDOW_MENU}
 * and to the {@link #QUICK_LAUNCH_TOOLBAR} (during the linking phase) for top
 * level windows that the user can bring up.<br>
 * The {@link TopWindow} class has built-in functionality to provide this 
 * linkage as well as functionality to manage the display state of the window.
 * So agents with a single top level window may want to have their window
 * inherit from {@link TopWindow}.  If an agent allows multiple simultaneous
 * instances of the same top level window, then it can use the 
 * {@link TopWindowGroup} to group all those instances together in the task bar
 * under a common {@link #WINDOW_MENU} entry and a drop-down button in the 
 * {@link #QUICK_LAUNCH_TOOLBAR}.  Like {@link TopWindow}, 
 * {@link TopWindowGroup} also takes care of managing the display state of its
 * windows; however, it doesn't require its managed windows to inherit from 
 * {@link TopWindow}.</p> 
 *
 * @see org.openmicroscopy.shoola.env.ui.TopWindow
 * @see org.openmicroscopy.shoola.env.ui.TopWindowGroup
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface TaskBar 
{
	
	//NOTE: The TaskBarView uses these constants to do direct indexing.
	//So changing these values requires a review of TaskBarView as well.  
	
	/** Identifies the file menu within the menu bar. */
	//public static final int		FILE_MENU = 0;
    
    /** 
     * Identifies the tasks menu within the menu bar.
     * Entries in this menu trigger actions related to the application
     * workflow.
     */
    public static final int     TASKS_MENU = 0;
    
	/** 
	 * Identifies the connect menu within the menu bar.
	 * Entries in this menu trigger actions to connect/disconnect to/from
	 * remote services.
	 */
	//public static final int		CONNECT_MENU = 1;
	
	/** 
	 * Identifies the window menu within the menu bar.
	 * Entries in this menu trigger actions to bring up top level windows.
	 */
	public static final int		WINDOW_MENU = 1;
	
	/** Identifies the help menu within the menu bar. */
	public static final int		HELP_MENU = 2;
	
	/**
	 * Identifies the tasks toolbar.
	 * Icon buttons in this toolbar trigger actions related to the application
	 * workflow and are usually a shortcut to the most commonly used entries
	 * in the {@link #TASKS_MENU}.
	 */
	public static final int		TASKS_TOOLBAR = 0; 
	
	/**
	 * Identifies the quick launch toolbar.
	 * Icon buttons in this toolbar trigger actions to bring up top level
	 * windows and are usually a shortcut to the most commonly used entries
	 * in the {@link #WINDOW_MENU}.
	 */
	public static final int		QUICK_LAUNCH_TOOLBAR = 1; 
	
	
	/**
	 * Adds <code>entry</code> to the specified menu.
	 * 
	 * @param menuID	ID of one of the menus supported by the task bar.  Must
	 * 					be one of the constants defined by this interface.
	 * @param entry		The item to add.
	 * @throws IllegalArgumentException	If <code>menuID</code> is not valid.
	 * @throws NullPointerException	If <code>entry</code> is <code>null</code>.
	 */
	public void addToMenu(int menuID, JMenuItem entry);
	
	/**
	 * Removes <code>entry</code> from the specified menu.  
	 * 
	 * @param menuID	ID of one of the menus supported by the task bar.  Must
	 * 					be one of the constants defined by this interface.
	 * @param entry		The item to remove.
	 * @throws IllegalArgumentException	If <code>menuID</code> is not valid.
	 */
	public void removeFromMenu(int menuID, JMenuItem entry);
	
	/**
	 * Adds <code>entry</code> to the specified toolbar.
	 * The <code>entry</code> is assumed to have a <code>16x16</code> icon and
	 * no text.
	 * 
	 * @param toolBarID	ID of one of the toolbars supported by the task bar. 
	 * 					Must be one of the constants defined by this interface.
	 * @param entry		The item to add.
	 * @throws IllegalArgumentException	If <code>toolBarID</code> is not valid.
	 * @throws NullPointerException	If <code>entry</code> is <code>null</code>.
	 */
	public void addToToolBar(int toolBarID, AbstractButton entry);
	
	/**
	 * Removes <code>entry</code> from the specified toolbar.  
	 * 
	 * @param toolBarID	ID of one of the toolbars supported by the task bar. 
	 * 					Must be one of the constants defined by this interface.
	 * @param entry		The item to remove.
	 * @throws IllegalArgumentException	If <code>toolBarID</code> is not valid.
	 */
	public void removeFromToolBar(int toolBarID, AbstractButton entry);
	
    /** 
     * Adds the specified menu to the menu bar, before the existing menus
     * if passed flag is <code>true</code>, after otherwise.
     * 
     * @param menus      The menus to add.
     * @param before    Pass <code>true</code> to add the menu before the 
     *                  existing menus, pass <code>false</code> to add if after
     *                  the existing ones.
     */
    public void addToMenuBar(JMenu[] menus, boolean before);
    
	/**
	 * Brings up the task bar window.
	 */
	//public void open();
    
    /** Iconifies the task bar window. */
    //public void iconify();
    
    /**
     * Returns a reference to the task bar window.
     * 
     * @return See above.
     */
    public JFrame getFrame();

    /**
     * Returns the <code>JMenuBar</code> of the task bar.
     * 
     * @return See above.
     */
    public JMenuBar getTaskBarMenu();

}
