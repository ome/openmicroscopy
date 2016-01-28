/*
 * org.openmicroscopy.shoola.env.ui.TaskBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.ui;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/** 
 * Defines the functionality of the task bar UI.
 * <p>The task bar is a top level window that contains a menu bar and a series
 * of toolbars.  The container brings this window up after initialization for
 * the user to control some of the container's tasks &#151; like the connection
 * to remote services or quitting the application.</p>
 * <p>Agents that have a UI typically add an entry to the {@link #WINDOW_MENU}
 * (during the linking phase) for top
 * level windows that the user can bring up.<br>
 * The {@link TopWindow} class has built-in functionality to provide this 
 * linkage as well as functionality to manage the display state of the window.
 * So agents with a single top level window may want to have their window
 * inherit from {@link TopWindow}.  If an agent allows multiple simultaneous
 * instances of the same top level window, then it can use the 
 * {@link TopWindowGroup} to group all those instances together in the task bar
 * under a common {@link #WINDOW_MENU} entry. Like {@link TopWindow}, 
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
 * @since OME2.2
 */
public interface TaskBar 
{
	
	//NOTE: The TaskBarView uses these constants to do direct indexing.
	//So changing these values requires a review of TaskBarView as well.  
	 
    /** 
     * Identifies the tasks menu within the menu bar.
     * Entries in this menu trigger actions related to the application
     * workflow.
     */
    //public static final int     TASKS_MENU = 0;
    
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
	public static final int		WINDOW_MENU = 0;
	
	/** Identifies the help menu within the menu bar. */
	public static final int		HELP_MENU = 1;
	
	/** Identifies the file menu within the menu bar. */
	public static final int		FILE_MENU = 2;
	
	/** Identifies the <code>Send Comment</code> menu item. */
	public static final int		COMMENT = 100;
	
	/** Identifies the <code>Help content</code> menu item. */
	public static final int		HELP_CONTENTS = 101;
    
	/** Identifies the <code>Agents</code> tool bar. */
	public static final int		AGENTS = 201;
	
	/** Identifies the <code>Agents</code> tool bar. */
	public static final int		ANALYSIS = 202;
	
	/**
	 * Adds the component to the specified toolbar, This is the way to register
	 * to agents.
	 * 
	 * @param toolBarID The identifier of the tool bar.
	 * @param entry The item to add.
	 */
	public void addToToolBar(int toolBarID, JComponent entry);
	
	/** 
	 * Returns the collection of the entries registered for that tool bar.
	 * 
	 * @param toolBarID The identifier of the tool bar.
	 * @return See above.
	 */
	public List<JComponent> getToolBarEntries(int toolBarID);
	
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
    public JMenuBar getTaskBarMenuBar();
    
    /**
     * Builds and returns a copy of the menu item specified by the passed index.
     * If the passed value is not supported, a <code>null</code> value
     * is returned.
     * 
     * @param index	The index of the item to copy.
     * @return See above.
     */
    public JMenuItem getCopyMenuItem(int index);
    
    /**
     * Returns <code>true</code> if already connected,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean login();

    /** 
     * Opens the passed URL if possible.
     * 
     * @param path The path to handle.
     */
    public void openURL(String path);
    
    /**
     * Invokes when an error has occurred and the connection is lost.
     * 
     * @param index One of the constants defined by this class.
     */
    public void sessionExpired(int index);
    
    /**
     * Returns the menu corresponding to the specified value or 
     * <code>null</code>.
     * 
     * @param menuID The identifier of the menu.
     * @return See above.
     */
    public JMenu getMenu(int menuID);
    
    /**
     * Returns the relative path for file in the libs folder.
     * 
     * @param file The file to handle.
     * @return See above.
     */
    public String getLibFileRelative(String file);

}
