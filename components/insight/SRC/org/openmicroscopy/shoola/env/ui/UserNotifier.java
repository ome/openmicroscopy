/*
 * org.openmicroscopy.shoola.env.ui.UserNotifier
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;

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
 	 * @param listener  The listener to add.
     */
	public void notifyError(String title, String summary, 
			String email, List<ImportErrorObject> toSubmit, 
			PropertyChangeListener listener);
	
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
     * @param comment The text to display in the comment box.
     */
    public void submitMessage(String emailAddress, String comment);
    
    /**
     * Notifies the user of an activity such as movie creation.
     * 
     * @param ctx The security context.
     * @param activity The activity to register.
     */
    public void notifyActivity(SecurityContext ctx, Object activity);
    
    /**
     * Opens the specified application.
     * 
     * @param data The application to handle.
     * @param path The file argument or <code>null</code>.
     */
    public void openApplication(ApplicationData data, String path);
    
    /**
     * Sets the status of the saving.
     * 
     * @param node The node returned.
     */
    public void setStatus(Object node);
    
    /**
     * Returns <code>true</code> if some activities are still running,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	public boolean hasRunningActivities();
	
    /**
     * Removes all activities
     */
    public void clearActivities();
	
}
