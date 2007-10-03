/*
 * org.openmicroscopy.shoola.util.ui.SaveChangesDialog 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

import javax.swing.Icon;
import javax.swing.JFrame;

import org.openmicroscopy.shoola.env.Container;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class SaveChangesDialog
	extends OptionsDialog
{

    /** The title of this window. */
    private static final String TITLE = "";
    
    /** The message displayed. */
    private static final String MESSAGE = "Do you want to exit and " +
    										"discard changes?";
    
    /** The choice by user. */
    private boolean choice;
    
    /** Parent frame. */
    private JFrame frame;
    
    /**
     * Closes the application.
     * @see OptionsDialog#onYesSelection()
     */
    protected void onYesSelection() { choice = true; setVisible(false);}
    
    /**
     * Creates a new instance.
     * 
     * @param parent    The parent of this dialog.
     * @param icon      The icon displayed next to the message.
     */
    public SaveChangesDialog(JFrame parent, Icon icon)
    {
        super(parent, TITLE, MESSAGE, icon);
        setAlwaysOnTop(true);
        frame = parent;
        choice = false;
    }
    
    /**
     * Show dialog.
     * @return user choice.
     */
    public boolean showDialog()
    {
    	UIUtilities.centerAndShow(this);
    	return choice;
    }
    
}



