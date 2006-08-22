/*
 * org.openmicroscopy.shoola.env.ui.TopWindowGroup
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ToolBarButtonMenu;


/** 
 * Links a group of windows to the {@link TaskBar} and manages their display
 * on screen.
 * <p>Rather than adding a quick-launch button in the 
 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and an entry in the
 * {@link TaskBar#WINDOW_MENU} for each window in the group, the constructor of
 * this class adds a drop-down {@link ToolBarButtonMenu button}
 * (a button that triggers the display of a popup menu) to the
 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and
 * a sub-menu to the {@link TaskBar#WINDOW_MENU}. These menus contain an entry
 * for each window in the group and are populated/depopulated via the
 * {@link #add(TopWindow, String, Icon) add}/{@link #remove(JFrame) remove}
 * methods.</p>
 * <p>All those menu entries are display-trigger buttons that cause the 
 * corresponding window to be shown on screen.  This class uses the
 * {@link TopWindowManager} to control mouse clicks on these buttons as well as
 * to manage the display state of the windows in the group.</p>
 * 
 * @see org.openmicroscopy.shoola.env.ui.TopWindowManager
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
public class TopWindowGroup 
{

	/**
	 * Just a record to hold the display buttons of a window and its manager.
	 */
	private static class WindowConfig
	{
		JMenuItem			winSubMenuEntry;
		JMenuItem			dropDownButtonEntry;
		TopWindowManager	manager;
	}


	/** Cached reference to the {@link TaskBar}. */
	private TaskBar                taskBar;
	
	/** The sub-menu in the {@link TaskBar#WINDOW_MENU}. */
	private JMenu		           winSubMenu;
	
	/** The drop-down button on  the {@link TaskBar#QUICK_LAUNCH_TOOLBAR}. */
	private ToolBarButtonMenu      dropDownButton;
	
	/** Maps each window in the group to its configuration object. */
	private Map			           windows;
	
	/** The close all item in the {@link #winSubMenu}.*/
	private JMenuItem              closeAllWinSubMenuEntry;
	
	/** The close all item in the {@link #dropDownButton}. */
	private JMenuItem              closeAllDropDownButtonEntry;
	
	/**
	 * Helper method to create the display buttons and the manager for the
	 * specified window.
	 * 
	 * @param window	The window.
	 * @return	A record-like class containing the display buttons and the
	 * 			manager for <code>window</code>.
	 */
	private WindowConfig makeConfigFor(TopWindow window)
	{
		WindowConfig cfg = new WindowConfig();
		cfg.winSubMenuEntry = new JMenuItem();
		cfg.dropDownButtonEntry = new JMenuItem();
		cfg.manager = new TopWindowManager(window, new AbstractButton[] 
								{cfg.winSubMenuEntry, cfg.dropDownButtonEntry});
		return cfg;
	}
	
	/**
	 * Creates the close all buttons and attaches the needed listeners.
	 */
	private void makeCloseAllButtons()
	{
		String closeAll = "Close All";
		closeAllWinSubMenuEntry = new JMenuItem(closeAll);
		closeAllDropDownButtonEntry = new JMenuItem(closeAll);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) { removeAll(true); }
		};
		closeAllWinSubMenuEntry.addActionListener(al);
		closeAllDropDownButtonEntry.addActionListener(al);
	}
	
	/**
	 * Helper method to add the close all items to the {@link #winSubMenu} and
	 * to the {@link #dropDownButton}.
	 */
	private void addCloseAllButtons()
	{
		winSubMenu.add(closeAllWinSubMenuEntry);
		winSubMenu.addSeparator();
		dropDownButton.addToMenu(closeAllDropDownButtonEntry);
		dropDownButton.addSeparator();
	}

	/**
	 * Helper method to remove all items from the {@link #winSubMenu}
	 * and from the {@link #dropDownButton}.
	 * This method is called after the last window has been removed from this
	 * group so as to get rid of the close all buttons and separators.
	 */	
	private void clearMenus()
	{
		winSubMenu.removeAll();
		winSubMenu.setEnabled(false);
		dropDownButton.clearMenu();
	}
	
	/**
	 * Creates a new window group.
	 * This constructor adds a drop-down button to the 
	 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and a sub-menu to the
	 * {@link TaskBar#WINDOW_MENU}.  These menus can then be populated/
	 * depopulated via the {@link #add(TopWindow, String, Icon) add}/
	 * {@link #remove(JFrame) remove} methods. 
	 * 
	 * @param name	The name of this window group.  This will also be the
	 * 				text displayed by the sub-menu entry in the
	 * 				{@link TaskBar#WINDOW_MENU}.
	 * @param icon	The icon of this window group.  This icon will be displayed
	 * 				both by the the sub-menu entry in the
	 * 				{@link TaskBar#WINDOW_MENU} and by the drop-down button on
	 * 				the {@link TaskBar#QUICK_LAUNCH_TOOLBAR}.
	 * @param tb	A reference to the task bar.
	 */
	public TopWindowGroup(String name, Icon icon, TaskBar tb)
	{
		if (tb == null)
			throw new NullPointerException("No reference to the TaskBar.");
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Must provide a name.");
		taskBar = tb;
		winSubMenu = new JMenu(name);
		winSubMenu.setIcon(icon);
		winSubMenu.setEnabled(false);
		dropDownButton = new ToolBarButtonMenu();
		taskBar.addToMenu(TaskBar.WINDOW_MENU, winSubMenu);
		taskBar.addToToolBar(TaskBar.QUICK_LAUNCH_TOOLBAR, dropDownButton);
		windows = new HashMap();
		makeCloseAllButtons();
	}

    /**
     * Returns the window sub-menu entry.
     * 
     * @return See above.
     */
     public JMenu getWinMenuEntry() { return winSubMenu; } 
	
	/**
	 * Adds the specified window to this group.
	 * This method also creates the display buttons for the window.  This means
	 * an entry will be added to the drop-down button in the 
	 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and to the sub-menu in the
	 * {@link TaskBar#WINDOW_MENU}.
	 * 
	 * @param window	The window to add.
	 * @param name		The text label for the display buttons.
	 * @param icon		The icon for the display buttons.
	 */
	public void add(TopWindow window, String name, Icon icon)
	{
		if (window == null)	return;
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Must provide a name.");
		WindowConfig cfg = (WindowConfig) windows.get(window);
		if (cfg == null) {
			cfg = makeConfigFor(window);
			windows.put(window, cfg);
		}
		
		cfg.winSubMenuEntry.setText(name);
		cfg.winSubMenuEntry.setIcon(icon);
		cfg.dropDownButtonEntry.setText(name);
		cfg.dropDownButtonEntry.setIcon(icon);
		//NOTE: If window has already a cfg, then we simply overwrite the
		//previous text and icon of the display buttons.
		
		if (windows.size() == 1) addCloseAllButtons();
		winSubMenu.setEnabled(true);
		winSubMenu.add(cfg.winSubMenuEntry);
		dropDownButton.addToMenu(cfg.dropDownButtonEntry, true);
	}
	
	/**
	 * Removes the specified window from this group.
	 * This method also removes the display buttons for the window.  However,
	 * this method doesn't affect the current state of the window &#151; so
	 * if the window is showing on screen, it won't be hidden or disposed.
	 * 
	 * @param window	The window to remove.
	 */
	public void remove(JFrame window)
	{
		WindowConfig cfg = (WindowConfig) windows.get(window);
		if (cfg == null) return;
		windows.remove(window);
		if (windows.size() == 0) clearMenus();
		winSubMenu.remove(cfg.winSubMenuEntry);
		dropDownButton.removeFromMenu(cfg.dropDownButtonEntry);
	}
	
	/**
	 * Removes the specified window from this group.
	 * This method also removes the display buttons for the window.  
	 * If <code>dispose</code> is <code>true</code>, then the window is also
	 * disposed, oterwise this method doesn't affect the current state of the 
	 * window &#151; so if the window is showing on screen, it won't be hidden
	 * or disposed.
	 * 
	 * @param window	The window to remove.
	 * @param dispose	Tells whether to dispose of the window.
	 */
	public void remove(JFrame window, boolean dispose)
	{
		if (window == null) return;
		remove(window);
		if (dispose) window.dispose();
	}
	
	/**
	 * Removes all windows currently in this group. 
	 * This method also removes the display buttons for each window.  However,
	 * this method doesn't affect the current state of the windows &#151; so
	 * if a window is showing on screen, it won't be hidden or disposed.
	 */
	public void removeAll() { removeAll(false); }
	
	/**
	 * Calls {@link #remove(JFrame, boolean) remove(window, dispose)} for each
	 * <code>window</code> currently in this group.
	 * 
	 * @param dispose	Tells whether to dispose of the windows.
	 */
	public void removeAll(boolean dispose)
	{
		int k = windows.size();
		if (k == 0) return;
		JFrame[] wIterator = new JFrame[k];  
		windows.keySet().toArray(wIterator);  
		//NOTE: Using Iterator would cause a ConcurrentModificationException. 
		
		for (k = 0; k < wIterator.length; ++k) 
			remove(wIterator[k], dispose);
	}

//NOTE: we may want to add methods hideAll, removeAndHideAll, and so on.

}
