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
 * Acts as a centralized place where errors are collected and then notified 
 * to the user. 
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
 	 * error notification.
 	 * 
 	 * @param title		title of the Dialog window.
 	 * @param summary	error's summary.
 	 * @param detail	error's details.
 	 */
    public void notifyError(String title, String summary, Exception detail);
    
	/**
	 * error notification.
	 * 
	 * @param title		title of the Dialog window.
	 * @param summary	error's summary.* @param detail	error's details.
	 */
    public void notifyError(String title, String message);
    
	/**
	 * 
	 * @param title		title of the Dialog window.
	 * @param message	info's message.
	 */
    public void notifyInfo(String title, String message);
    
    /**
     * 
     * @param title		title of the Dialog window.
     * @param message	warning's message.
     */
    public void notifyWarning(String title, String message);

}
