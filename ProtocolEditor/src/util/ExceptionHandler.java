package util;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import xmlMVC.XMLModel;

/**
 * This class handles exceptions and allows users to submit bugs and comments.
 * 
 * This code below comes from 
 * org.openmicroscopy.shoola.env.ui.UserNotifier.Impl
 * org.openmicroscopy.shoola.env.ui.UserNotifierManager
 * org.openmicroscopy.shoola.env.AbnormalExitHandler
 * 
 * @author will
 *
 */

public class ExceptionHandler implements PropertyChangeListener {

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
	
    
    /** Default title for the error dialog. */
    private static final String     DEFAULT_ERROR_TITLE = "Error";
 
	private static final String INVOKER_COMMENT = "editor_comments";
	
	private static final String INVOKER_ERROR = "editor_errors";
    
    /**
     * This is the common parent frame that we use to build every notification
     * dialog.
     * We don't use the one already provided by <i>Swing</i> because we need
     * to set the <i>OME</i> icon in the title bar, so that notification dialogs
     * can inherit it.
     */
    private static JFrame			SHARED_FRAME = null;
	
    
    
	// called when exceptions are caught
    public static void showErrorDialog(String title, String summary, Throwable t) {
    	// use LogMessage to convert Throwable to string
		LogMessage msg = new LogMessage();
		msg.println("");
		msg.print(t);
		
		showErrorDialog(title, summary, msg.toString());
		
    }
	
    /**
	 * Brings up a messenger dialog.
	 * 
	 * @param title     		The dialog title.
	 * @param summary   		The dialog message.
	 * @param detail			The detailed error message.
	 * @param softwareVersion 	The version of the software.
	 */
	public static void showErrorDialog(String title, String summary, String detail)
	{
		Exception e;
		if (detail == null) e = new Exception(summary);
		else e = new Exception(detail);
		if (title == null || title.length() == 0) title = DEFAULT_ERROR_TITLE;
		MessengerDialog d = new MessengerDialog(SHARED_FRAME, title, "", e); 
		d.setVersion(XMLModel.EDITOR_RELEASE_ID);
		d.addPropertyChangeListener(new ExceptionHandler());
		d.setModal(true);
		UIUtilities.centerAndShow(d);
	}

	// for users to submit comments
    public static void showCommentDialog() {
    	String email = "";
    	String title = "Comment";
    	MessengerDialog d = new MessengerDialog(SHARED_FRAME, title, email);
    	
    	d.setVersion(XMLModel.EDITOR_RELEASE_ID);
    	d.addPropertyChangeListener(new ExceptionHandler());
    	// Insight has setModal(true), but I prefer false, so that users can reproduce some
    	// behavior while composing a message.
    	d.setModal(false);
    	UIUtilities.centerAndShow(d);
    }
	
	private void handleSendMessage(MessengerDialog source, 
			MessengerDetails details)
	{
		String url;
		boolean bug = true;
		String error = details.getError();
		if (error == null || error.length() == 0) bug = false;
		if (bug) url = "http://users.openmicroscopy.org.uk/~brain/omero/bugcollector.php";
		else url = "http://users.openmicroscopy.org.uk/~brain/omero/commentcollector.php";

		String teamAddress = "comments@openmicroscopy.org.uk";

		CommunicatorDescriptor desc = new CommunicatorDescriptor
			(HttpChannel.CONNECTION_PER_REQUEST, url, -1);

		try {
			Communicator c = SvcRegistry.getCommunicator(desc);

			String reply = "";
			if (!bug)
				c.submitComment(INVOKER_COMMENT, details.getEmail(), details.getComment(), 
						details.getExtra(), reply);

			else c.submitError(INVOKER_ERROR, details.getEmail(), details.getComment(), 
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
	 * Reacts to property changes fired by dialogs.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		System.out.println("ExceptionHandler propertyChange name = " + name);
		if (name == null) return;
		if (name.equals(MessengerDialog.SEND_PROPERTY)) {
			MessengerDialog source = (MessengerDialog) pce.getSource();
			handleSendMessage(source, (MessengerDetails) pce.getNewValue());
		}
	}
}
