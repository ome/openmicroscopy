/*
 * org.openmicroscopy.shoola.env.ui.UserNotifier
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

// Java Imports
import java.util.Map;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Acts as a centralized place where user notifications are collected and 
 * then displayed on screen.
 * The various methods defined by this service will bring up a modal dialog to
 * notify the user of the specified message.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface UserNotifier
{
 	
 	/**
 	 * Brings up a modal dialog to notify the user of an error.
 	 * The dialog will just show the error summary.  However the user can press
 	 * a <i>details</i> button to have the dialog show the error detail.
 	 * 
 	 * @param title		The title of the dialog.
 	 * @param summary	A brief description of the error.
 	 * @param detail	The cause of the error.
 	 */
    public void notifyError(String title, String summary, Throwable detail);
    
    /**
     * Brings up a modal dialog to notify the user of an error.
 	 * The dialog will just show the error summary.  However the user can press
 	 * a <i>details</i> button to have the dialog show the error detail.
 	 * 
     * @param title		The title of the dialog.
 	 * @param summary	A brief description of the error.
 	 * @param detail	A more detailed description of the cause of the error.
     */
	public void notifyError(String title, String summary, String detail);
    
    /**
     * Brings up a modal dialog to notify the user of an error.
 	 * The dialog will just show the error summary.  However the user can press
 	 * a <i>details</i> button to have the dialog show the error detail.
 	 * 
     * @param title		The title of the dialog.
 	 * @param summary	A brief description of the error.
 	 * @param email		The e-mail address of the user.
 	 * @param toSubmit	The objects to submit to the development team.
     */
	public void notifyError(String title, String summary, String email, 
			Map toSubmit);
	
	/**
	 * Brings up a modal dialog to notify the user of an error.
 	 * The dialog will just show the error summary.
	 * 
	 * @param title		The title of the dialog.
	 * @param message	A brief description of the error.
	 */
    public void notifyError(String title, String message);
    
	/**
	 * Brings up a modal dialog to notify the user of the specified warning.
	 * The dialog will just show the warning summary.  However the user can
	 * press a <i>details</i> button to have the dialog show the warning detail.
	 * 
	 * @param title		The title of the dialog.
	 * @param summary	A brief description of the warning.
	 * @param detail	The cause of the warning.
	 */
	public void notifyWarning(String title, String summary, Throwable detail);
    
	/**
	 * Brings up a modal dialog to notify the user of the specified warning.
	 * The dialog will just show the warning summary.  However the user can
	 * press a <i>details</i> button to have the dialog show the warning detail.
	 * 
	 * @param title		The title of the dialog.
	 * @param summary	A brief description of the warning.
	 * @param detail	A more detailed description of the cause of the warning.
	 */
	public void notifyWarning(String title, String summary, String detail);
	
	/**
	 * Brings up a modal dialog to notify the user of the specified warning.
	 * 
	 * @param title		The title of the dialog.
	 * @param message	The warning message that will be shown.
	 */
	public void notifyWarning(String title, String message);
	
	/**
	 * Brings up a modal dialog to notify the user of the specified message.
 	 * 
	 * @param title		The title of the dialog.
	 * @param message	The message that will be shown.
	 */
    public void notifyInfo(String title, String message);
    
    /**
     * Brings up a modal dialog to notify the user of the specified message.
     * 
     * @param title     The title of the dialog.
     * @param message   The message that will be shown.
     * @param icon      The icon to display in the dialog.
     */
    public void notifyInfo(String title, String message, Icon icon);
    
    /** 
     * Submits a message to the development team. 
     * 
     * @param emailAddress	The e-mail address of the current user.
     */
    public void submitMessage(String emailAddress);
    
    /**
     * Notifies the user of an activity such as movie creation.
     * 
     * @param activity The activity to register.
     */
    public void notifyActivity(Object activity);
    
}
