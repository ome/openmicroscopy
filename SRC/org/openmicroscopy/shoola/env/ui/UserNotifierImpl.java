/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierImpl
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
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.model.FileAnnotation;
import omero.model.OriginalFile;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.FileAnnotationData;

/** 
 * Implements the {@link UserNotifier} interface. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class UserNotifierImpl 
    implements UserNotifier
{
	
	/** Default title for the comment dialog. */
    private static final String     DEFAULT_COMMENT_TITLE = "Comment";
    
    /** Default title for the error dialog. */
    private static final String     DEFAULT_ERROR_TITLE = "Error";
 
	/** Default title for the warning dialog. */	
	private static final String     DEFAULT_WARNING_TITLE = "Warning";
											
	/** Default title for the info dialog. */														    
    private static final String     DEFAULT_INFO_TITLE = "Information";
    
    /**
     * This is the common parent frame that we use to build every notification
     * dialog.
     * We don't use the one already provided by <i>Swing</i> because we need
     * to set the <i>OME</i> icon in the title bar, so that notifcation dialogs
     * can inherit it.
     */
    private static JFrame			SHARED_FRAME = null;
	
    /** Reference to the manager. */
    private UserNotifierManager		manager;
    
    /**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	private String printErrorText(Throwable e) 
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
    /**
     * Brings up a notification dialog.
     * 
     * @param title     The dialog title.
     * @param message   The dialog message.
     * @param icon      The icon to show by the message.
     */
    private void showNotificationDialog(String title, String message, Icon icon)
    {
        NotificationDialog dialog = new NotificationDialog(
                                                SHARED_FRAME, title, message, 
                                                icon);
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }
    
    /**
     * Brings up a detailed notification dialog.
     * 
     * @param title     The dialog title.
     * @param message   The dialog message.
     * @param icon      The icon to show by the message.
     * @param detail    The detailed message.
     */
    /*
    private void showDetailedNotificationDialog(String title, String message, 
                                                Icon icon, String detail)
    {
        DetailedNotificationDialog dialog = new DetailedNotificationDialog(
                                                SHARED_FRAME, title, message, 
                                                icon, detail);
        dialog.pack();                                      
        UIUtilities.centerAndShow(dialog);
    }
*/
    
    /**
     * Brings up a messenger dialog.
     * 
     * @param title     		The dialog title.
     * @param summary   		The dialog message.
     * @param detail			The detailed error message.
     * @param softwareVersion 	The version of the software.
     */
    private void showErrorDialog(String title, String summary, String detail)
    {
    	Exception e;
    	if (detail == null) e = new Exception(summary);
    	else e = new Exception(detail);
    	if (title == null || title.length() == 0) title = DEFAULT_ERROR_TITLE;
    	MessengerDialog d = new MessengerDialog(SHARED_FRAME, title, "", e); 
    	d.setVersion(manager.getVersionNumber());
    	d.addPropertyChangeListener(manager);
    	d.setModal(true);
    	UIUtilities.centerAndShow(d);
    }
    
    /** 
     * Creates a new instance. 
     * @param c	Reference to the singleton {@link Container}.
     */
    UserNotifierImpl(Container c)
    {
    	manager = new UserNotifierManager(this, c);
    	if (SHARED_FRAME == null) {
			SHARED_FRAME = new JFrame();
			SHARED_FRAME.setIconImage(AbstractIconManager.getOMEImageIcon());
    	}
    }
    
	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyError(String, String)
     */     
	public void notifyError(String title, String summary)
	{
		if (title == null || title.length() == 0) title = DEFAULT_ERROR_TITLE;
		showNotificationDialog(title, summary, 
								IconManager.getDefaultErrorIcon());
	}
	
	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyError(String, String, Throwable)
     */       
    public void notifyError(String title, String summary, Throwable detail)
    {
    	
		notifyError(title, summary, 
						detail == null ? null : printErrorText(detail));
    }
    
	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyError(String, String, String)
     */     
	public void notifyError(String title, String summary, String detail)
	{
		if (title == null || title.length() == 0) title = DEFAULT_ERROR_TITLE;
		showErrorDialog(title, summary, detail);
	}
    
	/**
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyWarning(String, String)
     */ 
	public void notifyWarning(String title, String message)
	{
		if (title == null || title.length() == 0)
			title = DEFAULT_WARNING_TITLE;
		showNotificationDialog(title, message, 
								IconManager.getDefaultWarnIcon());
	}
	
	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyWarning(String, String, String)
     */ 
	public void notifyWarning(String title, String summary, String detail) 
	{
		if (title == null || title.length() == 0)
			title = DEFAULT_WARNING_TITLE;
		showErrorDialog(title, summary, detail);
	}

	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyWarning(String, String, Throwable)
     */ 
	public void notifyWarning(String title, String summary, Throwable detail) 
	{
		notifyWarning(title, summary, 
						detail == null ? null : detail.getMessage());
	}

	/** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyInfo(String, String)
     */ 
	public void notifyInfo(String title, String message)
	{  
		if (title == null || title.length() == 0)
			title = DEFAULT_INFO_TITLE;
		showNotificationDialog(title, message, 
								IconManager.getDefaultInfoIcon());
	}
    
    /** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#notifyInfo(String, String, Icon)
     */ 
    public void notifyInfo(String title, String message, Icon icon)
    {  
        if (title == null || title.length() == 0)
            title = DEFAULT_INFO_TITLE;
        if (icon == null) icon = IconManager.getDefaultInfoIcon();
        showNotificationDialog(title, message, icon);
    }

    /** 
     * Implemented as specified by {@link UserNotifier}. 
     * @see UserNotifier#submitMessage(String)
     */ 
	public void submitMessage(String email)
	{
		MessengerDialog d = new MessengerDialog(SHARED_FRAME, 
												DEFAULT_COMMENT_TITLE, 
												email);   
		d.setVersion(manager.getVersionNumber());
    	d.addPropertyChangeListener(manager);
    	// allow user to type in dialog while continue to use app. 
    	d.setModal(false);
    	d.setAlwaysOnTop(false);
    	UIUtilities.centerAndShow(d);
	}

	/** 
	 * Implemented as specified by {@link UserNotifier}. 
	 * @see UserNotifier#notifyDownload(FileAnnotationData, File)
	 */ 
	public void notifyDownload(FileAnnotationData data, File directory)
	{
		if (data == null) return;
		OriginalFile f = ((FileAnnotation) data.asAnnotation()).getFile();
		manager.saveFileToDisk(f, directory);
	}
	
	/** 
	 * Implemented as specified by {@link UserNotifier}. 
	 * @see UserNotifier#notifyDownload(Collection, File)
	 */ 
	public void notifyDownload(Collection data, File directory)
	{
		manager.saveFileToDisk(data, directory);
	}
	
	/** 
	 * Implemented as specified by {@link UserNotifier}. 
	 * @see UserNotifier#notifyDownload(FileAnnotationData)
	 */ 
	public void notifyDownload(FileAnnotationData data)
	{
		notifyDownload(data, null);
	}

	/** 
	 * Implemented as specified by {@link UserNotifier}. 
	 * @see UserNotifier#notifyDownload(Collection)
	 */ 
	public void notifyDownload(Collection data)
	{
		manager.saveFileToDisk(data, null);
	}
	
	/** 
	 * Implemented as specified by {@link UserNotifier}. 
	 * @see UserNotifier#setLoadingStatus(int, long, String)
	 */ 
	public void setLoadingStatus(int percent, long fileID, String fileName)
	{
		manager.setLoadingStatus(percent, fileID, fileName);
	}


	
}
