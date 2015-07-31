/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.log.LogMessage;
import omero.log.Logger;
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
	private static final String MESSAGE_START = "Sorry, but due to an error " +
								"we were not able to automatically \n";
	
	/** The default message if an error occurred while transferring data. */
	private static final String MESSAGE_END = "\n\n"+
								"You can still send us the error message by " +
								"clicking on the \n" +
								"error message tab, copying the error " +
								"message to the clipboard, \n" +
								"and sending it to ";
	
	/** Message if the dialog's type is {@link MessengerDialog#ERROR_TYPE}. */
	private static final String ERROR_MSG = "send your debug information.";
	
	/** Message if the dialog's type is {@link MessengerDialog#COMMENT_TYPE}. */
	private static final String COMMENT_MSG = "send your comment.";
	
	/** Reply when sending the comments. */
	private static final String COMMENT_REPLY = "Thanks, your comments have " +
											"been successfully posted.";
	
	/** Reply when sending the error message. */
	private static final String ERROR_REPLY = "Thanks, the error message " +
										"has been successfully posted.";
	
	/** Default title for the comment dialog. */
    private static final String DEFAULT_COMMENT_TITLE = "Comment";

    /** Reference to the container. */
	private Container container;
	
	/** Back pointer to the component. */
	private UserNotifier component;

	/** Map keeping track of the ongoing data loading. */
	private Map<String, UserNotifierLoader> loaders;
	
	/** The Dialog used to send comments. */
	private MessengerDialog commentDialog;
	
	/** The dialog keeping track of the activity files. */
	private DownloadsDialog activityDialog;
	
	/** The collection of running activities. */
	private List<ActivityComponent> activities;

	/**
	 * Returns the invoker depending on which application is running e.g.
	 * insight, importer or editor.
	 * 
	 * @param comment Pass <code>true</code> to return the invoker for the 
	 * comments, <code>false </code>
	 * 
	 * @return
	 */
	private String getInvoker(boolean comment)
	{
		Registry reg = container.getRegistry();
		String master = (String) reg.lookup(LookupNames.MASTER);
		if (comment) {
			if (LookupNames.MASTER_IMPORTER.equals(master))
				return "importer_comments";
			return "insight_comments";
		}
		if (LookupNames.MASTER_IMPORTER.equals(master))
			return "importer_bugs";
		return "insight_bugs";
	}
	
	/**
	 * Sends a message.
	 * 
	 * @param source The source of the message.
	 * @param details The values to send.
	 */
	private void handleSendMessage(MessengerDialog source, 
								MessengerDetails details)
	{
		Registry reg = container.getRegistry();
		if (details.getObjectToSubmit() != null) {
			ExperimenterData exp = (ExperimenterData) reg.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			SecurityContext ctx = new SecurityContext(
					exp.getDefaultGroup().getId());
			FileUploader loader = new FileUploader(component, 
					container.getRegistry(), ctx, source, details);
			loader.load();
			return;
		}
		
		boolean bug = true;
		String error = details.getError();
		if (CommonsLangUtils.isBlank(error)) bug = false;
		String url = (String) reg.lookup(LookupNames.TOKEN_URL);
		String appName; 
		if (bug) 
			appName = (String) reg.lookup(LookupNames.APPLICATION_NAME_BUG);
		else 
			appName = (String) reg.lookup(LookupNames.APPLICATION_NAME_COMMENT);
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
			String invoker = getInvoker(bug);
			if (!bug) c.submitComment(invoker, details.getEmail(),
					details.getComment(), details.getExtra(), appName, v, 
					builder);
			else c.submitError(invoker, details.getEmail(),
					details.getComment(), details.getExtra(), error, appName, v,
					builder);
			if (!bug) reply = COMMENT_REPLY;
			else reply = ERROR_REPLY;
			
			
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
			String address = (String)
				container.getRegistry().lookup(LookupNames.DEBUGGER_ADDRESS);
			if (address != null && address.trim().length() > 0) {
				s += MESSAGE_END;
				s += address;
				s += ".";
			}
			JOptionPane.showMessageDialog(source, s);
		}
		source.setVisible(false);
		source.dispose();
	}
	
	/** Creates the activity dialog. */
	private void createActivity()
	{
		if (activityDialog != null) return;
		Registry reg = getRegistry();
		JFrame f = reg.getTaskBar().getFrame();
		activityDialog = new DownloadsDialog(f, 
				IconManager.getInstance(reg), DownloadsDialog.ACTIVITY);
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
		JFrame f = getRegistry().getTaskBar().getFrame();
		if (f != null)
			f.addPropertyChangeListener(TaskBarManager.ACTIVITIES_PROPERTY,
					this);
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
	 * @param activity   The activity to register.
	 * @param uiRegister Pass <code>true</code> to display the activity in the
	 *                   activity dialog, <code>false</code> otherwise.
	 */
	void registerActivity(ActivityComponent activity, boolean uiRegister)
	{
		if (activity == null) return;
		if (uiRegister) {
			createActivity();
			activityDialog.addActivityEntry(activity);
			showActivity();
		}
		if (activities == null) activities = new ArrayList<ActivityComponent>();
		activity.addPropertyChangeListener(this);
		activities.add(activity);
	}

	/**
	 * Returns the version number of the server.
	 * 
	 * @return See above.
	 */
	String getServerVersion()
	{
		if (container == null) return "";
		String name = container.getRegistry().getAdminService().getServerName();
		if (name == null) name = "";
		String version = 
			container.getRegistry().getAdminService().getServerVersion();
		if (name == null || name.trim().length() == 0) return "";
		if (version == null || version.trim().length() == 0)
			version = "not available";
		
		return "Server name: "+name+", version: "+version;
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
	
	/** Displays the activity window. */
	void showActivity()
	{
		if (activityDialog == null) createActivity();
		if (!activityDialog.isVisible())
			UIUtilities.centerAndShow(activityDialog);
		activityDialog.requestFocusInWindow();
		activityDialog.toFront();
	}
	
	/**
	 * Returns the number of running activities.
	 * 
	 * @return See above.
	 */
	int getRunningActivitiesCount()
	{ 
		if (activities == null) return 0;
		return activities.size();
	}
	
	/**
	 * Removes all activities
	 */
	void clearActivities() {
	    if (activities != null)
	        activities.clear();
	    
	    if (activityDialog != null) {
	        activityDialog.setVisible(false);
	        activityDialog = null;
	    }
	}
	
	/** 
	 * Starts the specified activity.
	 * 
	 * @param start Pass <code>true</code> to update view, <code>false</code>
	 * 				otherwise.
	 * @param comp The component to activate.
	 */
	void startActivity(boolean start, ActivityComponent comp)
	{
		if (comp == null) return;
		if (start) comp.startActivity();
		EventBus bus = getRegistry().getEventBus();
		bus.post(new ActivityProcessEvent(comp, false));
		UserNotifierLoader loader = comp.createLoader();
		if (loader != null) loader.load();
	}
	
	/**
	 * Returns <code>true</code> if there is an on-going activity of
	 * the specified type, <code>false</code> otherwise.
	 * 
	 * @param type The type of activity to handle.
	 * @return See above.
	 */
	boolean hasRunningActivityOfType(Class type)
	{
		if (type == null || getRunningActivitiesCount() == 0) return false;
		if (ExportActivity.class.equals(type)) {
			Iterator<ActivityComponent> i = activities.iterator();
			ActivityComponent ac;
			while (i.hasNext()) {
				ac = i.next();
				if (ExportActivity.class.equals(ac.getClass()))
					return true;
			}
		}
		return false;
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
		} else if (ActivityComponent.UNREGISTER_ACTIVITY_PROPERTY.equals(name))
		{
			ActivityComponent c = (ActivityComponent) pce.getNewValue();
			if (c != null) activities.remove(c);
			//Depending on the activity check if need to do another one.
			if (c instanceof ExportActivity) {
				Iterator<ActivityComponent> i = activities.iterator();
				ActivityComponent ac;
				while (i.hasNext()) {
					ac = i.next();
					if (ExportActivity.class.equals(ac.getClass())) {
						startActivity(true, ac);
						break;
					}
				}
			}
		}
	}

}
