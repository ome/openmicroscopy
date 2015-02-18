/*
 * org.openmicroscopy.shoola.env.ui.TaskBarImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The view component of the {@link TaskBar}.
 * This is just a dummy UI which is controlled by the {@link TaskBarManager}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class TaskBarView
	extends JFrame
	implements TaskBar
{
	
//NOTE: We make no assumption on the order in which entries are added/removed
//and when this happens.  We don't even impose a sort of internal order.  As a
//result, the variuos menus and toolbars might look different across time.
//Moreover, even if all agents always add their entries during the linking
//phase, reordering the agent tags in the configuration file would lead to
//a different order in which agents are linked by the container, which, in
//turn, would result in a different order in which entries are added to the
//menus/toolbars.


	/** Identifies the exit menu item within the file menu. */
	static final int EXIT_MI = 0;

	/** 
	 * Identifies the connect to <i>OMEDS</i> menu item within the connect menu.
	 */
	static final int CONNECT_MI = 1;

	/** 
	 * Identifies the disconnect from <i>OMEDS</i> menu item within the
	 * connect menu.
	 */
	static final int DISCONNECT_MI = 2;

	/** Identifies the welcome menu item within the help menu. */
	static final int WELCOME_MI = 3;

	/** Identifies the help contents menu item within the help menu. */
	static final int HELP_MI = 4;

	/** Identifies the how-to menu item within the help menu. */
	static final int HOWTO_MI = 5;

	/** Identifies the software updates menu item within the help menu. */
	static final int UPDATES_MI = 6;

	/** Identifies the about menu item within the help menu. */
	static final int ABOUT_MI = 7;

	/** Identifies the exit button within the tool bar. */
	static final int EXIT_BTN = 8;

	/** Identifies the connect to <i>OMEDS</i> button within the tool bar. */
	static final int CONNECT_BTN = 9;

	/** 
	 * Identifies the disconnect from <i>OMEDS</i> button within the tool bar.
	 */
	static final int DISCONNECT_BTN = 10;

	/** Identifies the help button within the tool bar. */
	static final int HELP_BTN = 11;

	/** Identifies the comment menu item within the help menu. */
	static final int COMMENT_MI = 12;

	/** Identifies the help contents menu item within the help menu. */
	static final int FORUM_MI = 13;

	/** Identifies the activity menu item within the windows menu. */
	static final int ACTIVITY_MI = 14;

	/** Identifies the log file location menu item within the windows menu. */
	static final int LOG_FILE_MI = 15;

	/** 
	 * The maximum id of the buttons and menu items identifiers.
	 * Allows to size the {@link #buttons} array correctly.
	 */
	private static final int MAX_ID = 15;

    /** The title of the frame. */
    private static final String TITLE = "Open Microscopy Environment";

	/**
	 * All the button-like objects used by this view.
	 * These are all the menu items within the various menus in the menu bar
	 * and all icon buttons within the tool bar.
	 * We do direct indexing on this array by using the constants specified by
	 * this class.
	 */
	private AbstractButton[] buttons;

	/** 
	 * The menus specified by {@link TaskBar}.
	 * We do direct indexing on this array by using the constants specified by
	 * {@link TaskBar}.
	 */
	private JMenu[] menus;

	/** Cached reference to the {@link IconManager} singleton.*/
	private IconManager iconManager;

    /** Collection of the copy of the window menu. */
    private Set<JMenu> windowMenus;
    
    /** Collection of the copy of the window menu. */
    private Set<JMenu> helpMenus;

    /** Collection of available menu bars. */
    private Set<JMenuBar> menubars;

    /** The original menu bar. */
    private JMenuBar originalBar;

	/** Reference to the manager. */
	private TaskBarManager manager;

	/** The bars hosting the components to register.*/
	private Map<Integer, List<JComponent>> bars;

    /**
     * Returns a copy of the <code>Help</code> menu.
     *
     * @return See above.
     */
    private JMenu getHelpMenu()
    {
        JMenu menu = createHelpMenu();
        helpMenus.add(menu);
        return menu;
    }

    /**
     * Returns a copy of the <code>Windows</code> menu.
     *
     * @return See above.
     */
    private JMenu getWindowsMenu()
    {
        JMenu menu = createWindowMenu();
        Component[] comps = menus[WINDOW_MENU].getPopupMenu().getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JMenu) 
                menu.add(copyItemsFromMenu((JMenu) comps[i]));
            else if (comps[i] instanceof JMenuItem) 
                menu.add(copyItem((JMenuItem) comps[i]));
        }
        windowMenus.add(menu);
        return menu;
    }

	/**
	 * Helper method to create all menu items for the various menus within
	 * the menu bar.
	 */
	private void createMenuItems()
	{
		buttons[EXIT_MI] = new JMenuItem("Exit",
		        iconManager.getIcon(IconManager.EXIT));
		buttons[CONNECT_MI] = new JMenuItem("Connect to OMEDS",
		        iconManager.getIcon(IconManager.CONNECT_DS));
		buttons[DISCONNECT_MI] = new JMenuItem("Disconnect from OMEDS",
		        iconManager.getIcon(IconManager.DISCONNECT_DS));
		buttons[WELCOME_MI] = new JMenuItem("Welcome...",
		        iconManager.getIcon(IconManager.WELCOME));
		buttons[HELP_MI] = new JMenuItem("Help Contents",
		        iconManager.getIcon(IconManager.HELP));
		buttons[HOWTO_MI] = new JMenuItem("How To...",
		        iconManager.getIcon(IconManager.HOW_TO));
		String aboutName = "About "+manager.getSoftwareName()+"...";
		buttons[UPDATES_MI] = new JMenuItem(aboutName,
		        iconManager.getIcon(IconManager.SW_UPDATES));
		buttons[ABOUT_MI] = new JMenuItem("About OMERO",
		        IconManager.getOMEIcon());
		buttons[COMMENT_MI] = new JMenuItem("Send Feedback...",
		        iconManager.getIcon(IconManager.COMMENT));
		buttons[FORUM_MI] = new JMenuItem("Forum",
				iconManager.getIcon(IconManager.FORUM));
		buttons[LOG_FILE_MI] = new JMenuItem("Show Log File",
				iconManager.getIcon(IconManager.LOG_FILE));
		buttons[ACTIVITY_MI] = new JMenuItem("Activities...",
				iconManager.getIcon(IconManager.ACTIVITY));
	}

    /**
     * Copies the items from the specified menu and creates a new menu.
     *
     * @param original The menu to handle.
     * @return See above.
     */
    private JMenu copyItemsFromMenu(JMenu original)
    {
        Component[] comps = original.getPopupMenu().getComponents();
        JMenu menu = new JMenu();
        menu.setText(original.getText());
        menu.setToolTipText(original.getToolTipText());
        ActionListener[] al = original.getActionListeners();
        for (int j = 0; j < al.length; j++)
            menu.addActionListener(al[j]);
        MenuKeyListener[] mkl = original.getMenuKeyListeners();
        for (int j = 0; j < mkl.length; j++)
            menu.addMenuKeyListener(mkl[j]);
        MenuListener[] ml = original.getMenuListeners();
        for (int j = 0; j < ml.length; j++)
            menu.addMenuListener(ml[j]);
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JMenu) {
                menu.add(copyItemsFromMenu((JMenu) comps[i]));
            } else if (comps[i] instanceof JMenuItem) {
                menu.add(copyItem((JMenuItem) comps[i]));
            } else if (comps[i] instanceof JSeparator) {
                menu.add(new JSeparator(JSeparator.HORIZONTAL));
            }
        }
        return menu;
    }

    /**
     * Makes and returns a copy of the specified item.
     *
     * @param original The item to handle.
     * @return See above.
     */
    private JMenuItem copyItem(JMenuItem original)
    {
        JMenuItem item = new JMenuItem(original.getAction());
        item.setIcon(original.getIcon());
        item.setText(original.getText());
        item.setToolTipText(original.getToolTipText());
        ActionListener[] al = original.getActionListeners();
        for (int j = 0; j < al.length; j++)
            item.addActionListener(al[j]);
        return item;
    }

	/**
	 * Helper method to create the file menu.
	 *
	 * @return The file menu.
	 */
	private JMenu createFileMenu()
	{
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		return file;
	}

	/**
	 * Helper method to create the window menu.
	 *
	 * @return The window menu.
	 */
	private JMenu createWindowMenu()
	{
		JMenu window = new JMenu("Window");
		window.setMnemonic(KeyEvent.VK_W);
		window.add(buttons[ACTIVITY_MI]);
		return window;
	}

	/**
	 * Helper method to create the help menu.
	 *
	 * @return The help menu.
	 */
	private JMenu createHelpMenu()
	{
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem i = copyItem((JMenuItem) buttons[HELP_MI]);
		if(i.getActionListeners().length==0) 
			i.addActionListener(manager.getHelpMenuAction(HELP_MI));
		help.add(i);
		
		i = copyItem((JMenuItem) buttons[FORUM_MI]);
		if(i.getActionListeners().length==0) 
			i.addActionListener(manager.getHelpMenuAction(FORUM_MI));
		help.add(i);
		
		i = copyItem((JMenuItem) buttons[COMMENT_MI]);
		if(i.getActionListeners().length==0) 
			i.addActionListener(manager.getHelpMenuAction(COMMENT_MI));
		help.add(i);
		
		help.add(new JSeparator(JSeparator.HORIZONTAL));
		
		i = copyItem((JMenuItem) buttons[LOG_FILE_MI]);
		if(i.getActionListeners().length==0) 
			i.addActionListener(manager.getHelpMenuAction(LOG_FILE_MI));
		help.add(i);
		
		i = copyItem((JMenuItem) buttons[UPDATES_MI]);
		if(i.getActionListeners().length==0) 
			i.addActionListener(manager.getHelpMenuAction(UPDATES_MI));
		help.add(i);
		
		return help;
	}

	/**
	 * Helper method to create the menu bar.
	 *
	 * @return The menu bar.
	 */
	private JMenuBar createMenuBar()
	{
		createMenuItems();
		menus[FILE_MENU] = createFileMenu();
		menus[WINDOW_MENU] = createWindowMenu();
		menus[HELP_MENU] = createHelpMenu();
		JMenuBar bar = new JMenuBar();
		for (int i = 0; i < menus.length; ++i)
			bar.add(menus[i]);
		return bar;
	}

	/** Helper method to create all buttons for the main toolbar. */
	private void createButtons()
	{
		buttons[CONNECT_BTN] = new JButton(
								iconManager.getIcon(IconManager.CONNECT_DS));
		buttons[CONNECT_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Connect to OMERO."));
		buttons[DISCONNECT_BTN] = new JButton(
								iconManager.getIcon(IconManager.DISCONNECT_DS));
		buttons[DISCONNECT_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Disconnect from OMERO."));
		buttons[HELP_BTN] = new JButton(iconManager.getIcon(IconManager.HELP));
		buttons[HELP_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Bring up help contents."));
		buttons[EXIT_BTN] = new JButton(iconManager.getIcon(IconManager.EXIT));
		buttons[EXIT_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Exit the application."));
	}

	/**
	 * Helper method to create an empty, floatable toolbar with rollover
	 * effect for the icon buttons and an etched border.
	 *
	 * @return See above.
	 */
	private JToolBar createToolBar()
	{
		JToolBar bar = new JToolBar();
		bar.setBorder(BorderFactory.createEtchedBorder());
		bar.setFloatable(false);
		bar.putClientProperty("JToolBar.isRollover", Boolean.valueOf(true));
		return bar;
	}

	/**
	 * Helper method to create the window's toolbars panel.
	 * This panel contains all the predefined toolbars (file, connect, and
	 * help) as well as those specified by {@link TaskBar}.
	 *
	 * @return The window's toolbars panel.
	 */
	private JPanel createToolBarsPanel()
	{
		createButtons();
		JToolBar file = createToolBar(), connect = createToolBar(),
		        help = createToolBar();
		file.add(buttons[EXIT_BTN]);
		connect.add(buttons[CONNECT_BTN]);
		connect.add(buttons[DISCONNECT_BTN]);
		help.add(buttons[HELP_BTN]);
		JPanel bars = new JPanel(), outerPanel = new JPanel();
		bars.setBorder(null);
		bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
		bars.add(file);
		bars.add(connect);
		bars.add(help);
		outerPanel.setBorder(null);
		outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
		outerPanel.add(bars);
		outerPanel.add(Box.createRigidArea(new Dimension(100, 16)));
		outerPanel.add(Box.createHorizontalGlue());
		return outerPanel;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
	    setIconImage(IconManager.getOMEImageIcon());
	    originalBar = createMenuBar();
	    menubars.add(originalBar);
	    createToolBarsPanel();
	}

	/**
	 * Creates and returns a copy of the original menu bar.
	 *
	 * @return See above.
	 */
	private JMenuBar copyMenuBar()
	{
		JMenuBar bar = new JMenuBar();
		JMenu menu, copy;
		for (int i = 0; i < originalBar.getMenuCount(); i++) {
			menu = originalBar.getMenu(i);
			copy = copyItemsFromMenu(menu);
			bar.add(copy);
			if (menu == menus[WINDOW_MENU])
				windowMenus.add(copy);
			else if (menu == menus[HELP_MENU])
				helpMenus.add(copy);
		}
		return bar;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param manager Reference to the manager.
	 * @param im The {@link IconManager} singleton that we use to retrieve
	 * the various icons.
	 */
	TaskBarView(TaskBarManager manager, IconManager im)
	{
		super(TITLE);
		this.manager = manager;
		buttons = new AbstractButton[MAX_ID+1];
		menus = new JMenu[3];
		iconManager = im;
        windowMenus = new HashSet<JMenu>();
        helpMenus = new HashSet<JMenu>();
        menubars = new HashSet<JMenuBar>();
        bars = new HashMap<Integer, List<JComponent>>();
		buildGUI();
	}

	/**
	 * Returns the specified icon button or menu item.
	 *  
	 * @param id Identifies the button or menu item.  Must be one of the
	 * constants defined by this class.
	 * @return See above.
	 */
	AbstractButton getButton(int id)
	{
		//Don't check this is only meant to be used by the TaskBarManager.
		return buttons[id];
	}

	/**
	 * Implemented as specified by {@link TaskBar}.
	 * @see TaskBar#addToMenu(int, JMenuItem)
	 */
	public void addToToolBar(int toolBarID, JComponent entry)
	{
		if (entry == null) return;
		//Check if tool bar id is supported
		switch (toolBarID) {
			case AGENTS:
			case ANALYSIS:
				break;
			default:
				return;
		}
		List<JComponent> list = bars.get(toolBarID);
		if (list == null) {
			list = new ArrayList<JComponent>();
			bars.put(toolBarID, list);
		}
		list.add(entry);
	}

	/**
	 * Implemented as specified by {@link TaskBar}.
	 * @see TaskBar#getToolBarEntries(int)
	 */
	public List<JComponent> getToolBarEntries(int toolBarID)
	{
		return bars.get(toolBarID);
	}

	/**
	 * Implemented as specified by {@link TaskBar}.
	 * @see TaskBar#addToMenu(int, JMenuItem)
	 */
	public void addToMenu(int menuID, JMenuItem entry)
	{
		if (menuID < 0 || menus.length <= menuID)
			throw new IllegalArgumentException("Invalid menu id: "+menuID+".");
		if (entry == null)
			throw new NullPointerException("No entry");
		menus[menuID].add(entry);
		Iterator<JMenu> i;
        JMenu menu;
        if (menuID == WINDOW_MENU) {
            i = windowMenus.iterator();
            while (i.hasNext()) {
                menu = i.next();
                if (entry instanceof JMenu)
                    menu.add(copyItemsFromMenu((JMenu) entry));
                else
                    menu.add(copyItem(entry));
            }
        } else if (menuID == HELP_MENU) {
            i = helpMenus.iterator();
            while (i.hasNext()) {
                menu = i.next();
                if (entry instanceof JMenu)
                    menu.add(copyItemsFromMenu((JMenu) entry));
                else
                    menu.add(copyItem(entry));
            }
        }
	}

	/**
	 * Implemented as specified by {@link TaskBar}.
	 * @see TaskBar#removeFromMenu(int, JMenuItem)
	 */
	public void removeFromMenu(int menuID, JMenuItem entry) 
	{
	    if (menuID < 0 || menus.length <= menuID)
	        throw new IllegalArgumentException("Invalid menu id: "+menuID+".");
	    Iterator<JMenu> i;
	    JMenu menu;
	    Component[] comps;
	    Component c;
	    if (menuID == WINDOW_MENU && entry instanceof JMenu) {
	        i = windowMenus.iterator();
	        //tmp solution to remove item from the copy of the windows menu.
	        while (i.hasNext()) {
	            menu = i.next();
	            comps = menu.getPopupMenu().getComponents();
	            for (int j = 0; j < comps.length; j++) {
	                c = comps[j];
	                if (c instanceof JMenu) {
	                    if (((JMenu) c).getText().equals(entry.getText())) 
	                        menu.remove(c);
	                }
	            }
	        }
	    } else if (menuID == HELP_MENU && entry instanceof JMenu) {
	        i = helpMenus.iterator();
	        //tmp solution to remove item from the copy of the windows menu.
	        while (i.hasNext()) {
	            menu = i.next();
	            comps = menu.getPopupMenu().getComponents();
	            for (int j = 0; j < comps.length; j++) {
	                c = comps[j];
	                if (c instanceof JMenu) {
	                    if (((JMenu) c).getText() == entry.getText()) {
	                        menu.remove(c);
	                    }
	                }
	            }
	        }
	    }
	    menus[menuID].remove(entry);
	}

    /**
     * Overridden so that the task bar is never brought up on screen.
     * @see JFrame#setVisible(boolean)
     */
    public void setVisible(boolean b) { super.setVisible(false); }

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#getFrame()
     */
    public JFrame getFrame() { return this; }

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#getTaskBarMenuBar()
     */
    public JMenuBar getTaskBarMenuBar() { return copyMenuBar(); }

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#getMenu(int)
     */
    public JMenu getMenu(int menuID)
    {
        switch (menuID) {
        case HELP_MENU: return getHelpMenu();
        case WINDOW_MENU: return getWindowsMenu();
        case FILE_MENU: return menus[FILE_MENU];
        }
        return null;
    }

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#getCopyMenuItem(int)
     */
	public JMenuItem getCopyMenuItem(int index)
	{
		switch (index) {
			case TaskBar.COMMENT:
				return copyItem((JMenuItem) buttons[COMMENT_MI]);
			case TaskBar.HELP_CONTENTS:
				return copyItem((JMenuItem) buttons[HELP_MI]);
			default:
				return null;
		}
	}

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#login()
     */
	public boolean login() { return manager.login(); }

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#openURL(String)
     */
	public void openURL(String path)
	{
		if (CommonsLangUtils.isEmpty(path)) return;
		manager.openURL(path);
	}

    /**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#sessionExpired(int)
     */
	public void sessionExpired(int index)
	{
		manager.sessionExpired(index);
	}

	/**
     * Implemented as specified by {@link TaskBar}.
     * @see TaskBar#getLibFileRelative(String)
     */
	public String getLibFileRelative(String file)
	{
		return manager.getLibFileRelative(file);
	}

}
