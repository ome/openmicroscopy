/*
 * org.openmicroscopy.shoola.env.ui.UserNotifier
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

// Java Imports

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
 	 * The dialog will just show the error summary.
	 * 
	 * @param title		The title of the dialog.
	 * @param summary	A brief description of the error.
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

}
