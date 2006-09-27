/*
 * org.openmicroscopy.shoola.env.ui.TaskBarImpl
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
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
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
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
	static final int	EXIT_MI = 0;
	
	/** 
	 * Identifies the connect to <i>OMEDS</i> menu item within the connect menu.
	 */
	static final int	CONNECT_MI = 1;

	/** 
	 * Identifies the disconnect from <i>OMEDS</i> menu item within the
	 * connect menu.
	 */	
	static final int	DISCONNECT_MI = 2;
	
	/** Identifies the welcome menu item within the help menu. */
	static final int	WELCOME_MI = 3;
	
	/** Identifies the help contents menu item within the help menu. */
	static final int	HELP_MI = 4;
	
	/** Identifies the howto menu item within the help menu. */
	static final int	HOWTO_MI = 5;

	/** Identifies the software updates menu item within the help menu. */
	static final int	UPDATES_MI = 6;

	/** Identifies the about menu item within the help menu. */
	static final int	ABOUT_MI = 7;
	
	/** Identifies the exit button within the toolbar. */
	static final int	EXIT_BTN = 8;
	
	/** Identifies the connect to <i>OMEDS</i> button within the toolbar. */
	static final int	CONNECT_BTN = 9;
	
	/** 
	 * Identifies the disconnect from <i>OMEDS</i> button within the toolbar.
	 */
	static final int	DISCONNECT_BTN = 10;
	
	/** Identifies the help button within the toolbar. */
	static final int	HELP_BTN = 11;
	
	/** 
	 * The maximum id of the buttons and menu items identifiers.
	 * Allows to size the {@link #buttons} array correctly.
	 */
	private static final int	MAX_ID = 11;
		
	/**
	 * All the button-like objects used by this view.
	 * These are all the menu items within the various menus in the menu bar
	 * and all icon buttons within the toolbar. 
	 * We do direct indexing on this array by using the constants specified by
	 * this class.
	 */
	private AbstractButton[]   buttons;
	
	/** 
	 * The menus specified by {@link TaskBar}.
	 * We do direct indexing on this array by using the constants specified by
	 * {@link TaskBar}.
	 */
	private JMenu[]            menus;
	
	/**
	 * The toolbars specified by {@link TaskBar}.
	 * Each toolbar contains the toolbar icon buttons and is part of the
	 * window's toolbars panel.
	 * We do direct indexing on this array by using the constants specified by
	 * {@link TaskBar}.
	 */
	private JToolBar[]		    toolbars;

	/** Cached reference to the {@link IconManager} singleton.*/
	private IconManager        iconManager;
	
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
		buttons[UPDATES_MI] = new JMenuItem("Software Updates",
								iconManager.getIcon(IconManager.SW_UPDATES));
		buttons[ABOUT_MI] = new JMenuItem("About OME",
												IconManager.getOMEIcon());
	}
	
	/**
	 * Helper method to create the file menu.
	 * 
	 * @return	The file menu.
	 */
	private JMenu createFileMenu()
	{
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		file.add(buttons[EXIT_MI]);
		return file;
	}
	
	/**
	 * Helper method to create the connect menu.
	 * 
	 * @return	The connect menu.
	 */
	private JMenu createConnectMenu()
	{	
		JMenu connect = new JMenu("Connect");
		connect.setMnemonic(KeyEvent.VK_C);
		connect.add(buttons[CONNECT_MI]);
		connect.add(buttons[DISCONNECT_MI]);
		return connect;
	}
	
	/**
	 * Helper method to create the tasks menu.
	 * 
	 * @return	The tasks menu.
	 */
	private JMenu createTasksMenu()
	{
		JMenu tasks = new JMenu("Tasks");
		tasks.setMnemonic(KeyEvent.VK_T);
		return tasks;
	}
	
	/**
	 * Helper method to create the window menu.
	 * 
	 * @return	The window menu.
	 */
	private JMenu createWindowMenu()
	{
		JMenu window = new JMenu("Window");
		window.setMnemonic(KeyEvent.VK_W);
		return window;
	}
	
	/**
	 * Helper method to create the help menu.
	 * 
	 * @return	The help menu.
	 */
	private JMenu createHelpMenu()
	{
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		help.add(buttons[WELCOME_MI]);
		help.add(buttons[HELP_MI]);
		help.add(buttons[HOWTO_MI]);
		help.add(buttons[UPDATES_MI]);
		help.addSeparator();
		help.add(buttons[ABOUT_MI]);
		return help;
	}
	
	/**
	 * Helper method to create the menu bar.
	 * 
	 * @return	The menu bar.
	 */
	private JMenuBar createMenuBar()
	{
		createMenuItems();
		///menus[FILE_MENU] = createFileMenu();
		//menus[CONNECT_MENU] = createConnectMenu();
		menus[TASKS_MENU] = createTasksMenu();
		menus[WINDOW_MENU] = createWindowMenu();
		menus[HELP_MENU] = createHelpMenu();
		JMenuBar bar = new JMenuBar();
		for (int i = 0; i < menus.length; ++i)	bar.add(menus[i]);
		return bar;
	}
	
	/** Helper method to create all buttons for the main toolbar. */
	private void createButtons()
	{
		buttons[CONNECT_BTN] = new JButton(
								iconManager.getIcon(IconManager.CONNECT_DS));
		buttons[CONNECT_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Connect to OMEDS."));
		buttons[DISCONNECT_BTN] = new JButton(
								iconManager.getIcon(IconManager.DISCONNECT_DS));
		buttons[DISCONNECT_BTN].setToolTipText(
					UIUtilities.formatToolTipText("Disconnect from OMEDS."));
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
	 * @return	See above.
	 */
	private JToolBar createToolBar()
	{
		JToolBar bar = new JToolBar();
		bar.setBorder(BorderFactory.createEtchedBorder());
		bar.setFloatable(true);
		bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		return bar;
	}
	
	/**
	 * Helper method to create the window's toolbars panel.
	 * This panel contains all the predefined toolbars (file, connect, and
	 * help) as well as those specified by {@link TaskBar}.
	 * 
	 * @return	The window's toolbars panel.
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
		bars.add(toolbars[TASKS_TOOLBAR]);
		bars.add(toolbars[QUICK_LAUNCH_TOOLBAR]);
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
		setJMenuBar(createMenuBar());
        createToolBarsPanel();
		//getContentPane().add(createToolBarsPanel());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param im	The {@link IconManager} singleton that we use to retrieve
	 * 				the various icons.
	 */
	TaskBarView(IconManager im)
	{
		super("Open Microscopy Environment");
		buttons = new AbstractButton[MAX_ID+1];
		menus = new JMenu[3];
		toolbars = new JToolBar[2];
		toolbars[QUICK_LAUNCH_TOOLBAR] = createToolBar();
		toolbars[TASKS_TOOLBAR] = createToolBar();
		iconManager = im;
		buildGUI();
	}
	
	/**
	 * Returns the specified icon button or menu item.
	 *  
	 * @param id	Identifies the button or menu item.  Must be one of the
	 * 				constants defined by this class.
	 * @return	See above.
	 */
	AbstractButton getButton(int id)
	{
		//Don't check this is only meant to be used by the TaskBarManager.
		return buttons[id];
	}

	/**
	 * Implemented as specifed by {@link TaskBar}.
	 * @see TaskBar#addToMenu(int, JMenuItem)
	 */
	public void addToMenu(int menuID, JMenuItem entry) 
	{
		if (menuID < 0 || menus.length <= menuID)
			throw new IllegalArgumentException("Invalid menu id: "+menuID+".");
		if (entry == null)
			throw new NullPointerException("No entry");
		menus[menuID].add(entry);	
	}

	/**
	 * Implemented as specifed by {@link TaskBar}.
	 * @see TaskBar#removeFromMenu(int, JMenuItem)
	 */
	public void removeFromMenu(int menuID, JMenuItem entry) 
	{
		if (menuID < 0 || menus.length <= menuID)
			throw new IllegalArgumentException("Invalid menu id: "+menuID+".");
		menus[menuID].remove(entry);
	}

	/**
	 * Implemented as specifed by {@link TaskBar}.
	 * @see TaskBar#addToToolBar(int, AbstractButton)
	 */
	public void addToToolBar(int toolBarID, AbstractButton entry) 
	{
		if (toolBarID < 0 || toolbars.length <= toolBarID)
			throw new IllegalArgumentException(
				"Invalid menu id: "+toolBarID+".");
		if (entry == null)
			throw new NullPointerException("No entry");
		toolbars[toolBarID].add(entry);
	}

	/**
	 * Implemented as specifed by {@link TaskBar}.
	 * @see TaskBar#removeFromToolBar(int, AbstractButton)
	 */
	public void removeFromToolBar(int toolBarID, AbstractButton entry)
	{
		if (toolBarID < 0 || toolbars.length <= toolBarID)
			throw new IllegalArgumentException(
				"Invalid menu id: "+toolBarID+".");
		toolbars[toolBarID].remove(entry);
	}

    /**
     * Overriden so that the task bar is never brought up on screen.
     * @see JFrame#setVisible(boolean)
     */
    public void setVisible(boolean b)
    {
        super.setVisible(false);
    }
    
	/** 
	 * Implemented as specifed by {@link TaskBar}.
	 * @see TaskBar#open()
	 */
    /*
	public void open() 
	{
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
        UIUtilities.centerAndShow(this);
        iconify();
	}
*/
    
    /**
     * Implemented as specifed by {@link TaskBar}.
     * @see TaskBar#getFrame()
     */
    public JFrame getFrame() { return this; }

    /**
     * Implemented as specifed by {@link TaskBar}.
     * @see TaskBar#getTaskBarMenu()
     */
    public JMenuBar getTaskBarMenu() { return getJMenuBar(); }

    /**
     * Implemented as specifed by {@link TaskBar}.
     * @see TaskBar#addToMenuBar(JMenu[], boolean)
     */
    public void addToMenuBar(JMenu[] menus, boolean before)
    {
        JMenuBar bar = getJMenuBar();
        if (before) {
            JMenu[] existingMenus = new JMenu[bar.getMenuCount()];
            for (int i = 0; i < existingMenus.length; i++) {
                existingMenus[i] = bar.getMenu(i);
            }
            bar.removeAll();
            for (int j = 0; j < menus.length; j++) {
                bar.add(menus[j]);
            }
            for (int i = 0; i < existingMenus.length; i++) {
                bar.add(existingMenus[i]);
            }
        } else {
            for (int j = 0; j < menus.length; j++) {
                bar.add(menus[j]);
            }
        }
    }

    /**
     * Implemented as specifed by {@link TaskBar}.
     * @see TaskBar#iconify()
     */
    //public void iconify() { setExtendedState(Frame.ICONIFIED); }

}
