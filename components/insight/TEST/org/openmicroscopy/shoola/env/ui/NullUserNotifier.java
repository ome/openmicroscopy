/*
 * org.openmicroscopy.shoola.env.ui.NullUserNotifier
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


//Java imports
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import pojos.FileAnnotationData;

/** 
 * Implements the {@link UserNotifier} interface to be a Null Object, that is
 * to do nothing.
 * So this implementation has no UI associated with it.
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
public class NullUserNotifier
    implements UserNotifier
{

    /**
     * @see UserNotifier#notifyError(String, String, Throwable)
     */
    public void notifyError(String title, String summary, Throwable detail) {}

    /**
     * @see UserNotifier#notifyError(String, String, String)
     */
    public void notifyError(String title, String summary, String detail) {}
    
    /**
     * @see UserNotifier#notifyError(String, String)
     */
    public void notifyError(String title, String message) {}

    /**
     * @see UserNotifier#notifyWarning(String, String, Throwable)
     */
    public void notifyWarning(String title, String summary, Throwable detail) {}

    /**
     * @see UserNotifier#notifyWarning(String, String, String)
     */
    public void notifyWarning(String title, String summary, String detail) {}

    /**
     * @see UserNotifier#notifyWarning(String, String)
     */
    public void notifyWarning(String title, String message) {}
    
    /**
     * @see UserNotifier#notifyInfo(String, String)
     */
    public void notifyInfo(String title, String message) {}

    /**
     * @see UserNotifier#notifyInfo(String, String, Icon)
     */
    public void notifyInfo(String title, String message, Icon icon) {}

    /**
     * @see UserNotifier#submitMessage(String, String)
     */
	public void submitMessage(String emailAddress, String comment) {}

	/**
	 * @see UserNotifier#notifyDownload(FileAnnotationData)
	 */
	public void notifyDownload(FileAnnotationData data) {}
	
	/**
	 * @see UserNotifier#notifyDownload(FileAnnotationData, File)
	 */
	public void notifyDownload(FileAnnotationData data, File directory) {}
	
	/**
	 * @see UserNotifier#notifyDownload(Collection)
	 */
	public void notifyDownload(Collection data) {}
	
	/**
	 * @see UserNotifier#notifyDownload(Collection, File)
	 */
	public void notifyDownload(Collection data, File directory) {}

	/**
	 * @see UserNotifier#setLoadingStatus(int, long, String)
	 */
	public void setLoadingStatus(int percent, long id, String name) {}

	/**
	 * @see UserNotifier#notifyActivity(SecurityContext, Object)
	 */
	public void notifyActivity(SecurityContext ctx, Object activity) {}

	/**
	 * @see UserNotifier#openApplication(ApplicationData, String)
	 */
	public void openApplication(ApplicationData data, String path) {}

	/**
	 * @see UserNotifier#setStatus(Object)
	 */
	public void setStatus(Object node) {}

	/**
	 * @see UserNotifier#hasRunningActivities()
	 */
	public boolean hasRunningActivities() { return false; }

	/**
	 * @see UserNotifier#notifyError(String, String, String, List, 
	 * PropertyChangeListener)
	 */
	public void notifyError(String title, String summary, String email, 
		List<ImportErrorObject> toSubmit, PropertyChangeListener listener) {}

        public void clearActivities() {}
        
}
