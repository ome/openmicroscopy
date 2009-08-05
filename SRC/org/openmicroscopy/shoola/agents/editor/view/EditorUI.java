/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.actions.EditorAction;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;

/** 
 * The {@link Editor}'s View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class EditorUI
	extends TopWindow
{

	/** Reference to the Control. */
	private EditorControl 			controller;

	/** Reference to the Model. */
	private EditorModel   			model;
	
	/** Reference to the tool bar. */
	private EditorToolBar			toolBar;
	
	private Box						toolBarContainer;
	
	/** Reference to the status. */
	private EditorStatusBar			statusBar;
	
	/**
	 * A Panel to display in the center of this window if there is no file 
	 * to show. 
	 */
	private JPanel 					splashScreen;
	
	/** 
	 * Creates the file menu.
	 * 
	 * @return See above.
	 */
	private JMenu createMenu()
	{
		JMenu menu = new JMenu("File");
		
		addMenuItem(EditorControl.OPEN_LOCAL_FILE, menu, KeyEvent.VK_O);
		addMenuItem(EditorControl.OPEN_WWW_FILE, menu, 0);
		addMenuItem(EditorControl.SAVE_FILE, menu, KeyEvent.VK_S);
		addMenuItem(EditorControl.SAVE_FILE_LOCALLY, menu, 0);
		addMenuItem(EditorControl.SAVE_FILE_SERVER, menu, 0);
		addMenuItem(EditorControl.SAVE_AS_PROTOCOL, menu, 0);
		addMenuItem(EditorControl.NEW_BLANK_FILE, menu, KeyEvent.VK_N);
		addMenuItem(EditorControl.CLOSE_EDITOR, menu, KeyEvent.VK_W);
		
		return menu;
	}
	
	private void addMenuItem(int actionId, JMenu menu, int key)
	{
		EditorAction a = controller.getAction(actionId);
		JMenuItem item = new JMenuItem(a);
		if (key != 0)		
			setMenuItemAccelerator(item, key);
		menu.add(item);
	}
	
	/** 
	 * Creates the menu bar, Adding the 'Window' and 'Help' menus to the 
	 * menu returned by {@link #createMenu()}.
	 * 
	 * @param pref The user preferences.
	 * @return The menu bar. 
	 */
	private JMenuBar createMenuBar()
	{
		TaskBar tb = EditorAgent.getRegistry().getTaskBar();
        JMenu menu = createMenu();
        JMenuBar bar = tb.getTaskBarMenuBar();
        JMenu[] existingMenus = new JMenu[bar.getMenuCount()];
        
		for (int i = 0; i < existingMenus.length; i++) 
			existingMenus[i] = bar.getMenu(i);

		bar.removeAll();
		bar.add(menu);
		for (int i = 0; i < existingMenus.length; i++) 
			bar.add(existingMenus[i]);
        return bar;	
	}
	
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		
		JPanel toolBarBoxAlign = new JPanel(new BorderLayout());
		toolBarBoxAlign.setBackground(null);
		toolBarContainer = Box.createHorizontalBox();
		toolBarContainer.setBorder(new EmptyBorder(4,4,4,4));
		toolBarBoxAlign.add(toolBarContainer, BorderLayout.WEST);
		c.add(toolBarBoxAlign, BorderLayout.NORTH);
		
		splashScreen = new NoFileOpenUI(controller);
		c.add(splashScreen);
		
		toolBarContainer.add(toolBar);
		c.add(statusBar, BorderLayout.SOUTH);
	}
	
	/**
	 * Convenience method for setting the short-cut keys for menu items. 
	 * 
	 * @param menuItem
	 * @param character
	 */
	private static void setMenuItemAccelerator(JMenuItem menuItem, int key) {
		if (System.getProperty("os.name").contains("Mac OS")) {
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(key, ActionEvent.META_MASK));
		} else {
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(key, KeyEvent.CTRL_DOWN_MASK));
		}
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(EditorControl, EditorModel) initialize} 
	 * method should be called straight after to link this View 
	 * to the Controller.
	 * 
	 * @param title The window title.
	 */
	EditorUI(String title)
	{
		super(title);
	}

	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(EditorControl controller, EditorModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		toolBar = new EditorToolBar(controller);
		statusBar = new EditorStatusBar();
		setJMenuBar(createMenuBar());
		buildGUI();
		setOnScreen();
	}
    
	/** 
	 * Sets the status message.
	 * 
	 * @param text  The message to display.
	 * @param hide  Pass <code>true</code> to hide the progress bar, 
	 *              <code>false</otherwise>.
	 */
    void setStatus(String text, boolean hide)
    {
        statusBar.setStatus(text);
        statusBar.setProgress(hide);
    }
    
    /** 
     * Displays the browser by adding it to this UI.
     * 
     *  @param contents The contents of the text file.
     */
    void displayFile(String contents)
    {
    	remove(splashScreen);
    	JTextPane pane = new JTextPane();
    	pane.setEditable(false);
    	pane.setText(contents);
    	add(new JScrollPane(pane), BorderLayout.CENTER);
    	setTitle(model.getFileName());
    	validate();
    	repaint();
    }
    
    /** Displays the browser by adding it to this UI. */
    void displayFile()
    {
    	remove(splashScreen);
    	add(model.getBrowser().getUI(), BorderLayout.CENTER);
    	toolBarContainer.add(new JSeparator(SwingConstants.VERTICAL));
    	toolBarContainer.add(model.getBrowser().getToolBar());
    	
    	setTitle(model.getFileName());
    	validate();
    	repaint();
    }
    
    /** 
     * Overrides the {@link #setOnScreen() setOnScreen} method. 
     * Does not make the window visible, since this only occurs after 
     * the file has opened. 
     */
    public void setOnScreen()
    {	
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 8*(screenSize.width/10);
        int h = 8*(screenSize.height/10);

        w = Math.min(1100, w);
        h = Math.min(600, h);
        
        int x = screenSize.width/9;
        int y = screenSize.height/10;
        
    	int windowCount = EditorFactory.getEditorCount();
    	int offset = windowCount * 20;
    	
    	setSize(w, h);
    	x = x + offset;
    	y = y + offset;
    	setLocation(x, y);
    }
    
}
