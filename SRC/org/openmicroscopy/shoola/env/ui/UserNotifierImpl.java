/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierImpl
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
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.DetailedNotificationDialog;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
    
    /** Creates a new instance. */
    UserNotifierImpl()
    {
    	if (SHARED_FRAME == null) {
			SHARED_FRAME = new JFrame();
			SHARED_FRAME.setIconImage(IconManager.getOMEImageIcon());
    	}
    }
    
	/** Implemented as specified by {@link UserNotifier}. */     
	public void notifyError(String title, String summary)
	{
		if (title == null || title.length() == 0)	title = DEFAULT_ERROR_TITLE;
		showNotificationDialog(title, summary, 
								IconManager.getDefaultErrorIcon());
	}
	
	/** Implemented as specified by {@link UserNotifier}. */       
    public void notifyError(String title, String summary, Throwable detail)
    {
		notifyError(title, summary, 
						detail == null ? null : detail.getMessage());
    }
    
	/** Implemented as specified by {@link UserNotifier}. */     
	public void notifyError(String title, String summary, String detail)
	{
		if (title == null || title.length() == 0)	title = DEFAULT_ERROR_TITLE;
		showDetailedNotificationDialog(title, summary, 
										IconManager.getDefaultErrorIcon(),
										detail);
	}
	
	/** Implemented as specified by {@link UserNotifier}. */     
	public void notifyError(String title, String summary, Component component)
	{
		if (title == null || title.length() == 0)	title = DEFAULT_ERROR_TITLE;
		showDetailedNotificationDialog(title, summary, 
										IconManager.getDefaultErrorIcon(),
										component);
	}
    
	/** Implemented as specified by {@link UserNotifier}. */ 
	public void notifyWarning(String title, String message)
	{
		if (title == null || title.length() == 0)
			title = DEFAULT_WARNING_TITLE;
		showNotificationDialog(title, message, 
								IconManager.getDefaultWarnIcon());
	}
	
	/** Implemented as specified by {@link UserNotifier}. */ 
	public void notifyWarning(String title, String summary, String detail) 
	{
		if (title == null || title.length() == 0)
			title = DEFAULT_WARNING_TITLE;
		showDetailedNotificationDialog(title, summary, 
										IconManager.getDefaultWarnIcon(),
										detail);
	}
    
	/** Implemented as specified by {@link UserNotifier}. */ 
	public void notifyWarning(String title, String summary, Component component) 
	{
		if (title == null || title.length() == 0)
			title = DEFAULT_WARNING_TITLE;
		showDetailedNotificationDialog(title, summary, 
										IconManager.getDefaultWarnIcon(),
										component);
	}
    
	
	/** Implemented as specified by {@link UserNotifier}. */ 
	public void notifyWarning(String title, String summary, Throwable detail) 
	{
		notifyWarning(title, summary, 
						detail == null ? null : detail.getMessage());
	}

	/** Implemented as specified by {@link UserNotifier}. */ 
	public void notifyInfo(String title, String message)
	{  
		if (title == null || title.length() == 0)
			title = DEFAULT_INFO_TITLE;
		showNotificationDialog(title, message, 
								IconManager.getDefaultInfoIcon());
	}
    
    /** Implemented as specified by {@link UserNotifier}. */ 
    public void notifyInfo(String title, String message, Icon icon)
    {  
        if (title == null || title.length() == 0)
            title = DEFAULT_INFO_TITLE;
        if (icon == null) icon = IconManager.getDefaultInfoIcon();
        showNotificationDialog(title, message, icon);
    }
    
    /**
     * Brings up a notification dialog.
     * 
     * @param title		The dialog title.
     * @param message	The dialog message.
     * @param icon		The icon to show by the message.
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
	 * @param title		The dialog title.
	 * @param message	The dialog message.
	 * @param icon		The icon to show by the message.
	 * @param detail	The detailed message.
	 */
	private void showDetailedNotificationDialog(String title, String message, 
												Icon icon, String detail)
	{
		DetailedNotificationDialog dialog = new DetailedNotificationDialog(
												SHARED_FRAME, title, message, 
												icon, detail);
		dialog.pack();										
		UIUtilities.centerAndShow(dialog);
	}
	
	/**
	 * Brings up a detailed notification dialog.
	 * 
	 * @param title		The dialog title.
	 * @param message	The dialog message.
	 * @param icon		The icon to show by the message.
	 * @param component  The details in a component
	 */
	private void showDetailedNotificationDialog(String title, String message, 
												Icon icon, Component component)
	{
		DetailedNotificationDialog dialog = new DetailedNotificationDialog(
												SHARED_FRAME, title, message, 
												icon, component);
		dialog.pack();										
		UIUtilities.centerAndShow(dialog);
	}
	
}
