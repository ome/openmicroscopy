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

import org.openmicroscopy.shoola.env.config.Registry;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
 * Implements the {@link UserNotifier} interface.
 * 
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
    
    private static final String     DEFAULT_ERROR_TITLE = "Error";
    private static final String     DEFAULT_ERROR_SUMMARY = "Sorry, an error " +
    														"occurred";    
    private static final String     DEFAULT_INFO_TITLE = "Info";
    private static final String     DEFAULT_INFO_MESSAGE = "Message Info";
    private static final String     DEFAULT_WARNING_TITLE = "Warning";
    private static final String     DEFAULT_WARNING_MESSAGE = "Message Warning"; 
    private TopFrameImpl            topFrame;
    private Registry				reg;
    /** Creates a new instance of UserNotifierImpl. */
    public UserNotifierImpl(TopFrameImpl topFrame, Registry reg)
    {
        this.topFrame = topFrame;
        this.reg = reg;
    }
    // TODO: to be modified. Display message using a JDialog:
    // will implement code soon
    
	/** Implemented as specified by {@link UserNotifier}. */       
    public void notifyError(String title, String summary, Exception detail)
    {
        if (title == null || title.length() == 0)
            title = DEFAULT_ERROR_TITLE;
        StringBuffer buf = new StringBuffer();
        if (summary == null || summary.length() == 0)   
            buf.append(DEFAULT_ERROR_SUMMARY);
        else    buf.append(summary);
        String  d = detail == null ? null : detail.getMessage();
        if (d != null && d.length() != 0) {
            buf.append(":\n\n");
            buf.append(d);
        }
		showMessageDialog(title, buf.toString(), d, 
						UserNotifierDialog.INFORMATION_MESSAGE);
    }
    
	/** Implemented as specified by {@link UserNotifier}. */     
    public void notifyError(String title, String summary)
    {
        notifyError(title, summary, null);
    }
    
	/** Implemented as specified by {@link UserNotifier}. */ 
    public void notifyInfo(String title, String message)
    {  
        if (title == null || title.length() == 0)
            title = DEFAULT_INFO_TITLE;
        StringBuffer buf = new StringBuffer();
        if (message == null || message.length() == 0)   
            buf.append(DEFAULT_INFO_MESSAGE);
        else    buf.append(message);
		showMessageDialog(title, buf.toString(), 
						UserNotifierDialog.INFORMATION_MESSAGE);
    }
    
	/** Implemented as specified by {@link UserNotifier}. */ 
    public void notifyWarning(String title, String message)
    {
        if (title == null || title.length() == 0)
            title = DEFAULT_WARNING_TITLE;
        StringBuffer buf = new StringBuffer();
        if (message == null || message.length()==0)   
            buf.append(DEFAULT_WARNING_MESSAGE);
        else    buf.append(message);
        showMessageDialog(title, buf.toString(), 
        				UserNotifierDialog.WARNING_MESSAGE);
    }
       
	/**
	 * Brings up a dialog {@link UserNotifierDialog} with a specified title, 
	 * summary, icon.
	 * 
	 * @param title			dialog window title.	
	 * @param summary		message.
	 * @param iconID		iconID as specified in {@link UserNotifierDialog}.
	 */
	private void showMessageDialog(String title, String summary, int iconID)
	{	
		new UserNotifierDialog(reg, topFrame, title, summary, iconID);
	}
	
	/**
	 * Brings up a dialog {@link UserNotifierDialog} with a specified title, 
	 * summary, icon.
	 * 
	 * @param title			dialog window title.	
	 * @param summary		message.
	 * @param detail		message's details. 
	 * @param iconID		iconID as specified in {@link UserNotifierDialog}.
	 */
	private void showMessageDialog(String title, String summary, 
									String detail, int iconID)
	{
		if (detail == null) 
			new UserNotifierDialog(reg, topFrame, title, summary, iconID);
		else	
			new UserNotifierDialog(reg, topFrame, title, summary, detail, iconID); 
	}
	
}
