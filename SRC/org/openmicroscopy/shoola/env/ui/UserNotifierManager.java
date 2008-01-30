/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierManager 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;

/** 
 * Acts a controller. Listens to property changes fired by the 
 * <code>MessengerDialog</code>s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class UserNotifierManager
	implements PropertyChangeListener
{
    
	/** The default message if an error occured while transfering data. */
	private static final String	MESSAGE_START = "Sorry, but due to an error " +
								"we were not able to automatically \n" +
								"send your . ";
	
	/** The default message if an error occured while transfering data. */
	private static final String	MESSAGE_END = "\n\n"+
								"You can still send us the error message by " +
								"clicking on the \n" +
								"error message tab, copying the error " +
								"message to the clipboard, \n" +
								"and sending it to ";
	
	/** Message if the dialog's type is {@link MessengerDialog#ERROR_TYPE}. */
	private static final String	ERROR_MSG = "debug information.";
	
	/** Message if the dialog's type is {@link MessengerDialog#COMMENT_TYPE}. */
	private static final String	COMMENT_MSG = "comment.";
	
	/** Reply when sending the comments. */
	private static final String	COMMENT_REPLY = "Thanks, your comments have " +
											"been successfully posted.";
	
	/** Reply when sending the error message. */
	private static final String	ERROR_REPLY = "Thanks, the error message " +
										"has been successfully posted.";
	
	/** The tool invoking the service. */
	private static final String INVOKER_ERROR = "insight_bugs";
	
	/** The tool invoking the service. */
	private static final String INVOKER_COMMENT = "insight_comment";
	
    /** Reference to the container. */
	private Container		container;
	
	/**
	 * Sends a message.
	 * 
	 * @param source	The source of the message.
	 * @param details 	The values to send.
	 */
	private void handleSendMessage(MessengerDialog source, 
								MessengerDetails details)
	{
		Registry reg = container.getRegistry();
		String url;
		boolean bug = true;
		String error = details.getError();
		if (error == null || error.length() == 0) bug = false;
		if (bug) url = (String) reg.lookup(LookupNames.DEBUG_URL_BUG);
		else url = (String) reg.lookup(LookupNames.DEBUG_URL_COMMENT);
		
		String teamAddress = (String) reg.lookup(LookupNames.DEBUG_EMAIL);
		CommunicatorDescriptor desc = new CommunicatorDescriptor
						(HttpChannel.CONNECTION_PER_REQUEST, url, -1);
		try {
			Communicator c = SvcRegistry.getCommunicator(desc);
			
			String reply = "";
			if (!bug)
				c.submitComment(INVOKER_COMMENT,
								details.getEmail(), details.getComment(), 
								details.getExtra(), reply);
			else c.submitError(INVOKER_ERROR, 
							details.getEmail(), details.getComment(), 
					details.getExtra(), error, reply);
			if (!bug) reply += COMMENT_REPLY;
			else reply += ERROR_REPLY;
			
			JOptionPane.showMessageDialog(source, reply);
		} catch (Exception e) {
			String s = MESSAGE_START;
			if (source.getDialogType() == MessengerDialog.ERROR_TYPE)
				s += ERROR_MSG;
			else s += COMMENT_MSG;
			s += MESSAGE_END;
			JOptionPane.showMessageDialog(source, s+teamAddress+".");
		}
		source.setVisible(false);
		source.dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param c Reference to the container.
	 */
	UserNotifierManager(Container c)
	{
		container = c;
	}

	/**
	 * Reacts to property changes fired by dialogs.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (name == null) return;
		if (name.equals(MessengerDialog.SEND_PROPERTY)) {
			if (container == null) return;
			MessengerDialog source = (MessengerDialog) pce.getSource();
			handleSendMessage(source, (MessengerDetails) pce.getNewValue());
		}
	}
	
}
