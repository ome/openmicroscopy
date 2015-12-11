/*
 * org.openmicroscopy.shoola.util.ui.MessageBox 
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
package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.Point;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a messageBox dialog which will as the user a question and return 
 * a <code>Yes</code>, <code>No</code> or <code>Cancel</code> answer. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MessageBox
	extends OptionsDialog
{
	
	/** Bound property indicating the option selected by the user. */
	public static final String		OPTION_SELECTED_PROPERTY = "optionSelected";
	
	/** User have selected the yes option. */
	public final static int			YES_OPTION = 1;

	/** User have selected the no option. */
	public final static int			NO_OPTION = 0;
	
	/** User have selected the no option. */
	public final static int			CANCEL = 2;

	/** Option chosen by the user. */
	private int option; 
	
	/**
     * Overridden to perform the action as the user has selected the 
     * <code>Yes</code> option.
     */
    protected void onYesSelection()
    { 
    	option = YES_OPTION;
    	firePropertyChange(OPTION_SELECTED_PROPERTY, -1, option);
    }
    
    /**
     * Overridden to perform the action as the user has selected the 
     * <code>No</code> option.
     */
    protected void onNoSelection()
    { 
    	option = NO_OPTION;
    	firePropertyChange(OPTION_SELECTED_PROPERTY, -1, option);
    }
    
    /**
     * Overridden to perform the action as the user has selected the 
     * <code>Cancel</code> option.
     */
    protected void onCancel()
    { 
    	option = CANCEL; 
    	firePropertyChange(OPTION_SELECTED_PROPERTY, -1, option);
    }
    
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 */
	public MessageBox(JDialog owner, String title, String message)
	{
		this(owner, title, message, IconManager.getInstance().getIcon
				(IconManager.QUESTION_ICON_48));
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 */
	public MessageBox(JFrame owner, String title, String message)
	{
		this(owner, title, message, IconManager.getInstance().getIcon
				(IconManager.QUESTION_ICON_48));
	}

	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param icon			The icon to display.
	 */
	public MessageBox(JDialog owner, String title, String message, 
					Icon icon)
	{
		super(owner, title, message, icon);
		//setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		pack();
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param icon			The icon to display.
	 */
	public MessageBox(JFrame owner, String title, String message, Icon icon)
	{
		super(owner, title, message, icon);
		//setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		pack();
	}
	
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int showMsgBox()
    {
    	return showMsgBox(getParent().getLocation());
    }
   
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @param location The location of the top-left corner of the dialog.
     * @return The option selected by the user. 
     */
    public int showMsgBox(Point location)
    {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                MessageBox.this.option = CANCEL;
            }
        });
        
    	setLocation(location);
    	setVisible(true);
    	return option;	
    }
    
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int centerMsgBox()
    {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                MessageBox.this.option = CANCEL;
            }
        });
        
    	UIUtilities.centerAndShow(this);
    	return option;	
    }

}
