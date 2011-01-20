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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;

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
    
	/** The default message if an error occurred while transferring data. */
	private static final String	MESSAGE_START = "Sorry, but due to an error " +
								"we were not able to automatically \n";
	
	/** The default message if an error occurred while transferring data. */
	private static final String	MESSAGE_END = "\n\n"+
								"You can still send us the error message by " +
								"clicking on the \n" +
								"error message tab, copying the error " +
								"message to the clipboard, \n" +
								"and sending it to ";
	
	/** Message if the dialog's type is {@link MessengerDialog#ERROR_TYPE}. */
	private static final String	ERROR_MSG = "send your debug information.";
	
	/** Message if the dialog's type is {@link MessengerDialog#COMMENT_TYPE}. */
	private static final String	COMMENT_MSG = "send your comment.";
	
	/** Reply when sending the comments. */
	private static final String	COMMENT_REPLY = "Thanks, your comments have " +
											"been successfully posted.";
	
	/** Reply when sending the error message. */
	private static final String	ERROR_REPLY = "Thanks, the error message " +
										"has been successfully posted.";
	
	/** The tool invoking the service. */
	private static final String INVOKER_ERROR = "insight_bugs";
	
	/** The tool invoking the service. */
	private static final String INVOKER_COMMENT = "insight_comments";
	
	/** Default title for the comment dialog. */
    private static final String	DEFAULT_COMMENT_TITLE = "Comment";

    /** Reference to the container. */
	private Container						container;
	
	/** Back pointer to the component. */
	private UserNotifier					component;

	/** Map keeping track of the ongoing data loading. */
	private Map<String, UserNotifierLoader> loaders;
	
	/** The Dialog used to send comments. */
	private MessengerDialog					commentDialog;
	
	/** The dialog keeping track of the activity files. */
	private DownloadsDialog					activityDialog;
	
	/**
	 * Submits files that failed to import.
	 * 
	 * @param source	The source of the message.
	 * @param details 	The values to send.
	 */
	private void submitFiles(MessengerDialog source, 
								MessengerDetails details)
	{
		FileUploader loader = new FileUploader(component, container.getRegistry(), 
				source, details);
		loader.load();
		/*
		Registry reg = container.getRegistry();
		String tokenURL = (String) reg.lookup(LookupNames.TOKEN_URL);
		String processURL = (String) reg.lookup(LookupNames.PROCESSING_URL);
		String appName = (String) reg.lookup(LookupNames.APPLICATION_NAME_BUG);
		String teamAddress = (String) reg.lookup(LookupNames.DEBUG_EMAIL);
		int timeout = (Integer) reg.lookup(LookupNames.POST_TIMEOUT);
		
		try {
			Communicator c; 
			CommunicatorDescriptor desc = new CommunicatorDescriptor
			(HttpChannel.CONNECTION_PER_REQUEST, tokenURL, -1);
			List l = (List) details.getObjectToSubmit();
			Iterator i = l.iterator();
			FileTableNode node;
			File f;
			ImportException ex;
			StringBuilder token;
			StringBuilder reply;
			while (i.hasNext()) {
				node = (FileTableNode) i.next();
				f = node.getFile();
				ex = (ImportException) node.getException();
				if (f != null) {
					c = SvcRegistry.getCommunicator(desc);
					token = new StringBuilder();
					c.submitError(INVOKER_ERROR,
							details.getEmail(), details.getComment(), 
							details.getExtra(), ex.toString(), appName, token);
					desc = new CommunicatorDescriptor(
							HttpChannel.CONNECTION_PER_REQUEST, processURL, 
							timeout);
					c = SvcRegistry.getCommunicator(desc);
					reply = new StringBuilder();
					c.submitFile(token.toString(), f, ex.getReaderType(), 
							reply); 
				}
			}
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
            msg.println("Failed to send files.");
            msg.println("Reason: "+e.getMessage());
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);
			String s = MESSAGE_START;
			if (source.getDialogType() == MessengerDialog.ERROR_TYPE)
				s += ERROR_MSG;
			else s += COMMENT_MSG;
			s += MESSAGE_END;
			JOptionPane.showMessageDialog(source, s+teamAddress+".");
		}
		*/
		//source.setVisible(false);
		//source.dispose();
	}
	
	/**
	 * Sends a message.
	 * 
	 * @param source	The source of the message.
	 * @param details 	The values to send.
	 */
	private void handleSendMessage(MessengerDialog source, 
								MessengerDetails details)
	{
		if (details.getObjectToSubmit() != null) {
			submitFiles(source, details);
			return;
		}
		Registry reg = container.getRegistry();
		boolean bug = true;
		String error = details.getError();
		if (error == null || error.length() == 0) bug = false;
		String url = (String) reg.lookup(LookupNames.TOKEN_URL);
		String appName; 
		//if (bug) url = (String) reg.lookup(LookupNames.DEBUG_URL_BUG);
		//else url = (String) reg.lookup(LookupNames.DEBUG_URL_COMMENT);
		if (bug) 
			appName = (String) reg.lookup(LookupNames.APPLICATION_NAME_BUG);
		else 
			appName = (String) reg.lookup(LookupNames.APPLICATION_NAME_COMMENT);
		String teamAddress = (String) reg.lookup(LookupNames.DEBUG_EMAIL);
		CommunicatorDescriptor desc = new CommunicatorDescriptor
						(HttpChannel.CONNECTION_PER_REQUEST, url, -1);
		Object version = reg.lookup(LookupNames.VERSION);
		String v = "";
    	if (version != null && version instanceof String)
    		v = (String) version;
		try {
			Communicator c = SvcRegistry.getCommunicator(desc);
			
			StringBuilder builder = new StringBuilder();
			String reply = "";
			if (!bug) c.submitComment(INVOKER_COMMENT,
								details.getEmail(), details.getComment(), 
								details.getExtra(), appName, v, builder);
			else c.submitError(INVOKER_ERROR, 
							details.getEmail(), details.getComment(), 
					details.getExtra(), error, appName, v, builder);
			if (!bug) reply += COMMENT_REPLY;
			else reply += ERROR_REPLY;
			
			
			JOptionPane.showMessageDialog(source, reply);
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
            msg.println("Failed to send message.");
            msg.println("Reason: "+e.getMessage());
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);
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
	 * @param component Back pointer to the component.
	 * @param c 		Reference to the container.
	 */
	UserNotifierManager(UserNotifier component, Container c)
	{
		container = c;
		this.component = component;
		loaders = new HashMap<String, UserNotifierLoader>();
	}
	
	/**
	 * Returns the details of the user currently logged in if any.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenter()
	{
		Object exp = 
			container.getRegistry().lookup(LookupNames.CURRENT_USER_DETAILS);
		if (exp == null) return null;
		return (ExperimenterData) exp;
	}
	
	/**
	 * Registers the passed activity.
	 * 
	 * @param activity The activity to register.
	 */
	void registerActivity(ActivityComponent activity)
	{
		if (activity == null) return;
		if (activityDialog == null) {
			Registry reg = getRegistry();
			JFrame f = reg.getTaskBar().getFrame();
			activityDialog = new DownloadsDialog(f, 
					IconManager.getInstance(reg), DownloadsDialog.ACTIVITY);
		}
		activityDialog.addDownloadEntry(activity);
		if (!activityDialog.isVisible())
			UIUtilities.centerAndShow(activityDialog);
		activityDialog.requestFocusInWindow();
		activityDialog.toFront();
	}
	
	/**
	 * Returns the version number.
	 * 
	 * @return See above.
	 */
	String getServerVersion()
	{
		if (container == null) return "";
		String version = 
			container.getRegistry().getDataService().getServerVersion();
		return "Server version: "+version;
	}
	
	/**
	 * Returns the reference to the registry.
	 * 
	 * @return See above.
	 */
	Registry getRegistry() { return container.getRegistry(); }
	
	/**
	 * Creates or recycles the messenger dialog.
	 * 
	 * @param frame The owner of the dialog.
	 * @param email The e-mail address.
	 * @return See above.
	 */
	MessengerDialog getCommentDialog(JFrame frame, String email)
	{
		if (commentDialog != null) return commentDialog;
		commentDialog = new MessengerDialog(frame, DEFAULT_COMMENT_TITLE, 
				email);   
		commentDialog.setServerVersion(getServerVersion());
		commentDialog.addPropertyChangeListener(this);
		commentDialog.setModal(false);
		commentDialog.setAlwaysOnTop(false);
		return commentDialog;
	}
	
	/**
	 * Reacts to property changes fired by dialogs.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (MessengerDialog.SEND_PROPERTY.equals(name)) {
			MessengerDialog source = (MessengerDialog) pce.getSource();
			handleSendMessage(source, (MessengerDetails) pce.getNewValue());
		} else if (MessengerDialog.CLOSE_MESSENGER_PROPERTY.equals(name)) {
			commentDialog = null;
		} else if (OpeningFileDialog.SAVE_TO_DISK_PROPERTY.equals(name)) {
			/*
			Object value = pce.getNewValue();
			if (value instanceof FileAnnotationData) 
				saveFileToDisk((FileAnnotationData) value);
				*/
		} else if (DownloadsDialog.CANCEL_LOADING_PROPERTY.equals(name)) {
			String fileName = (String) pce.getNewValue();
			UserNotifierLoader loader = loaders.get(fileName);
			if (loader != null) loader.cancel();
			
		}
	}
	
}
