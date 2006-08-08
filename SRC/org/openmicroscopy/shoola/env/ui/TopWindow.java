/*
 * org.openmicroscopy.shoola.env.ui.TopWindow
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
import java.awt.Frame;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A superclass for windows that are to be linked to the {@link TaskBar} by
 * means of one quick-launch button and a menu entry in the 
 * {@link TaskBar#WINDOW_MENU}.
 * <p>The constructor of this class automatically adds a button to the 
 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and an entry in the
 * {@link TaskBar#WINDOW_MENU} &#151; subclasses use the <code>configure</code>
 * methods to specify icons, names, and tooltips. These are display-trigger
 * buttons that cause the window to be shown on screen. This class uses the
 * {@link TopWindowManager} to control mouse clicks on these buttons as well as
 * to manage the display state of the window.</p>
 * <p>Agents with a single top level window typically have their window inherit
 * from this class. The {@link #enableButtons(boolean) enableButtons} method
 * is provided so as to let agents control when the display buttons should be
 * disabled/enabled &#151; by default they're enabled.</p>
 *  
 * @see org.openmicroscopy.shoola.env.ui.TopWindowManager
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
public abstract class TopWindow
	extends JFrame
{
	
	/** 
	 * The icon button that we add to the quick launch toolbar within the
	 * {@link TaskBar}.
	 */
	private JButton		quickLaunchBtn;
	
	/** 
	 * The menu entry that we add to the window menu within the {@link TaskBar}.
	 */
	private JMenuItem	winMenuEntry;
	
	/** Cached reference to the {@link TaskBar}. */
	private TaskBar		taskBar;
	
	/** Adds the display buttons to the task bar and enables them. */
	private void configureButtons()
	{
		taskBar.addToMenu(TaskBar.WINDOW_MENU, winMenuEntry);
		taskBar.addToToolBar(TaskBar.QUICK_LAUNCH_TOOLBAR, quickLaunchBtn);
		enableButtons(true);
	}
	
	/**
	 * Sets the icon and tooltip of the quick-launch button.
	 * 
	 * @param icon	The icon.
	 * @param tooltip	The tooltip.
	 */
	protected void configureQuickLaunchBtn(Icon icon, String tooltip)
	{
		quickLaunchBtn.setIcon(icon);
		quickLaunchBtn.setToolTipText(UIUtilities.formatToolTipText(tooltip));
	}
	
	/**
	 * Sets the text label and the icon of the entry in the 
	 * {@link TaskBar#WINDOW_MENU}.
	 * 
	 * @param name	A text label for the menu item.
	 * @param icon	An icon for the menu item.
	 */
	protected void configureWinMenuEntry(String name, Icon icon)
	{
		winMenuEntry.setText(name);
		winMenuEntry.setIcon(icon);
	}
	
	/**
	 * Called by subclasses to perform initialization.
	 * This constructor adds a button to the 
	 * {@link TaskBar#QUICK_LAUNCH_TOOLBAR} and an entry to the
	 * {@link TaskBar#WINDOW_MENU} &#151; subclasses can then use the 
	 * <code>configure</code> methods to specify icons, names, and tooltips.  
	 * Also an instance of {@link TopWindowManager} is created to control mouse
	 * clicks on these buttons as well as to manage the display state of the
	 * window.
	 * 
	 * @param title	The title of the window.
	 * @param tb	A reference to the task bar.
	 */
	protected TopWindow(String title, TaskBar tb) 
	{
		super(title);
		setIconImage(IconManager.getOMEImageIcon());  //Default.
		if (tb == null)
			throw new NullPointerException("No reference to the TaskBar.");
		taskBar = tb;
		quickLaunchBtn = new JButton();
		winMenuEntry = new JMenuItem();
		configureButtons();
		new TopWindowManager(this, 
                    new AbstractButton[] {quickLaunchBtn, winMenuEntry});
	}
	
	/**
	 * Called by subclasses to perform initialization.
	 * 
	 * @param title	The title of the window.
	 */
	protected TopWindow(String title)
	{
		super(title);
		setIconImage(IconManager.getOMEImageIcon());  //Default.
	}
	
	/** 
	 * Deiconifies the frame if the frame is in the {@link Frame#ICONIFIED}
     * state.
	 */
	public void deIconify()
	{
		if (getExtendedState() == Frame.ICONIFIED)
			setExtendedState(Frame.NORMAL);
		setVisible(true);	
	}
	
    /** Iconifies the frame depending on the extended state. */
    public void iconify()
    {
        if (getExtendedState() == Frame.NORMAL)
            setExtendedState(Frame.ICONIFIED);
        setVisible(false);
    }
    
	/**
	 * Enables or disables the display buttons.
	 * 
	 * @param b	Pass <code>true</code> to enable, <code>false</code> to disable. 
	 */
	public void enableButtons(boolean b)
	{
		quickLaunchBtn.setEnabled(b);
		winMenuEntry.setEnabled(b);
	}
	
	/** Shows the window. */
	public void open() { quickLaunchBtn.doClick(); }
	
	/**
	 * Disposes of the window and removes the display buttons from the
	 * {@link TaskBar}.
	 */
	public void close()
	{
		dispose();
		taskBar.removeFromMenu(TaskBar.WINDOW_MENU, winMenuEntry);
		taskBar.removeFromToolBar(TaskBar.QUICK_LAUNCH_TOOLBAR, quickLaunchBtn);
	}
	
    /**
     * Packs and shows the window at the center of the screen.
     * Default location. Any class can override the method to specify the size
     * and the location of the window.
     */
    public void setOnScreen()
    {
        pack();
        UIUtilities.centerAndShow(this);
    }
     
}
