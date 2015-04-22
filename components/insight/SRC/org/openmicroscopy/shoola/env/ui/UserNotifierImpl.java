/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.model.AnalysisActivityParam;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadAndZipParam;
import org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam;
import org.openmicroscopy.shoola.env.data.model.ExportActivityParam;
import org.openmicroscopy.shoola.env.data.model.FigureActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieActivityParam;
import org.openmicroscopy.shoola.env.data.model.OpenActivityParam;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptActivityParam;
import org.openmicroscopy.shoola.env.data.model.TransferableActivityParam;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;

/**
 * Implements the {@link UserNotifier} interface.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public class UserNotifierImpl implements UserNotifier, PropertyChangeListener {

	/** Default title for the error dialog. */
	private static final String DEFAULT_ERROR_TITLE = "Error";

	/** Default title for the warning dialog. */
	private static final String DEFAULT_WARNING_TITLE = "Warning";

	/** Default title for the info dialog. */
	private static final String DEFAULT_INFO_TITLE = "Information";

	/**
	 * This is the common parent frame that we use to build every notification
	 * dialog. We don't use the one already provided by <i>Swing</i> because we
	 * need to set the <i>OME</i> icon in the title bar, so that notification
	 * dialogs can inherit it.
	 */
	private static JFrame SHARED_FRAME = null;

	/** Reference to the manager. */
	private UserNotifierManager manager;

	/** The dialog displaying the progress of the save. */
	private ChangesDialog dialog;

	/**
	 * Returns <code>true</code> if it is possible to write in the folder,
	 * <code>false</code> otherwise.
	 * 
	 * @param folder
	 *            The folder to handle.
	 * @return See above.
	 */
	private boolean canWriteInFolder(File folder) {
		if (!folder.canWrite()) {
			notifyInfo("Download file",
					"The selected folder is write protected.\nPlease"
							+ " select a different one.");
			return false;
		}
		return true;
	}

	/**
	 * Utility method to print the error message
	 * 
	 * @param e
	 *            The exception to handle.
	 * @return See above.
	 */
	private String printErrorText(Throwable e) {
		if (e == null)
			return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Brings up a notification dialog.
	 * 
	 * @param title
	 *            The dialog title.
	 * @param message
	 *            The dialog message.
	 * @param icon
	 *            The icon to show by the message.
	 */
	private void showNotificationDialog(String title, String message, Icon icon) {
		NotificationDialog dialog = new NotificationDialog(SHARED_FRAME, title,
				message, icon);
		dialog.pack();
		UIUtilities.centerAndShow(dialog);
	}

	/**
	 * Returns the e-mail address of the user currently logged in, if no address
	 * specified.
	 * 
	 * @param email
	 *            The address to check.
	 * @return See above.
	 */
	private String getEmail(String email) {
		if (email != null && email.trim().length() != 0)
			return email;
		ExperimenterData exp = manager.getExperimenter();
		if (exp != null)
			email = exp.getEmail();
		if (email == null)
			email = "";
		return email;
	}

	/**
	 * Brings up a messenger dialog.
	 * 
	 * @param title
	 *            The dialog title.
	 * @param summary
	 *            The dialog message.
	 * @param detail
	 *            The detailed error message.
	 * @param email
	 *            The e-mail address of the user.
	 * @param toSubmit
	 *            The version of the software.
	 */
	private void showErrorDialog(String title, String summary, String detail,
			String email) {
		Exception e;
		if (detail == null) {
			if (summary == null)
				summary = "";
			e = new Exception(summary);
		} else
			e = new Exception(detail);
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_ERROR_TITLE;
		MessengerDialog d = new MessengerDialog(SHARED_FRAME, title,
				getEmail(email), e);
		d.setServerVersion(manager.getServerVersion());
		d.addPropertyChangeListener(manager);
		d.setModal(true);
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param c
	 *            Reference to the singleton {@link Container}.
	 */
	UserNotifierImpl(Container c) {
		manager = new UserNotifierManager(this, c);
		if (SHARED_FRAME == null) {
			SHARED_FRAME = new JFrame();
			SHARED_FRAME.setIconImage(AbstractIconManager.getOMEImageIcon());
		}
	}

	/** Displays the activity. */
	void showActivity() {
		manager.showActivity();
	}

	/**
	 * Notifies that data are saved before closing or switching group.
	 * 
	 * @param nodes
	 *            The nodes to handle.
	 * @param listener
	 *            The listener to register.
	 * @param ctx
	 *            The security context.
	 * @return See above
	 */
	void notifySaving(List<Object> nodes, PropertyChangeListener listener,
			SecurityContext ctx) {
		dialog = new ChangesDialog(SHARED_FRAME, nodes, ctx);
		dialog.addPropertyChangeListener(this);
		if (listener != null)
			dialog.addPropertyChangeListener(listener);
		UIUtilities.centerAndShow(dialog);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyError(String, String)
	 */
	public void notifyError(String title, String summary) {
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_ERROR_TITLE;
		showNotificationDialog(title, summary,
				IconManager.getDefaultErrorIcon());
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyError(String, String, Throwable)
	 */
	public void notifyError(String title, String summary, Throwable detail) {
		notifyError(title, summary, detail == null ? null
				: printErrorText(detail));
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyError(String, String, String, List)
	 */
	public void notifyError(String title, String summary, String email,
			List<ImportErrorObject> toSubmit, PropertyChangeListener listener) {
		if (CommonsLangUtils.isEmpty(title)) title = DEFAULT_ERROR_TITLE;
		if (email == null) email = "";
		MessengerDialog d = new MessengerDialog(SHARED_FRAME, title,
				getEmail(email), toSubmit);
		d.setServerVersion(manager.getServerVersion());
		d.addPropertyChangeListener(manager);
		if (listener != null)
			d.addPropertyChangeListener(listener);
		if (!CollectionUtils.isEmpty(toSubmit)) {
			d.setModal(false);
			d.setAlwaysOnTop(false);
		}
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyError(String, String, String)
	 */
	public void notifyError(String title, String summary, String detail) {
		showErrorDialog(title, summary, detail, null);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyWarning(String, String)
	 */
	public void notifyWarning(String title, String message) {
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_WARNING_TITLE;
		showNotificationDialog(title, message, IconManager.getDefaultWarnIcon());
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyWarning(String, String, String)
	 */
	public void notifyWarning(String title, String summary, String detail) {
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_WARNING_TITLE;
		showErrorDialog(title, summary, detail, null);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyWarning(String, String, Throwable)
	 */
	public void notifyWarning(String title, String summary, Throwable detail) {
		notifyWarning(title, summary,
				detail == null ? null : detail.getMessage());
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyInfo(String, String)
	 */
	public void notifyInfo(String title, String message) {
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_INFO_TITLE;
		showNotificationDialog(title, message, IconManager.getDefaultInfoIcon());
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyInfo(String, String, Icon)
	 */
	public void notifyInfo(String title, String message, Icon icon) {
		if (CommonsLangUtils.isEmpty(title))
			title = DEFAULT_INFO_TITLE;
		if (icon == null)
			icon = IconManager.getDefaultInfoIcon();
		showNotificationDialog(title, message, icon);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#submitMessage(String, String)
	 */
	public void submitMessage(String email, String comment) {
		MessengerDialog d = manager.getCommentDialog(SHARED_FRAME,
				getEmail(email));
		d.setComment(comment);
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#notifyActivity(SecurityContext, Object)
	 */
	public void notifyActivity(SecurityContext ctx, Object activity) {
		if (activity == null)
			return;
		ActivityComponent comp = null;
		boolean register = true;
		boolean uiRegister = true;
		boolean startActivity = true;
		if (activity instanceof MovieActivityParam) {
			MovieActivityParam p = (MovieActivityParam) activity;
			comp = new MovieActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof ExportActivityParam) {
			if (manager.hasRunningActivityOfType(ExportActivity.class)) {
				startActivity = false;
			}
			ExportActivityParam p = (ExportActivityParam) activity;
			comp = new ExportActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof DownloadAndLaunchActivityParam) {
			DownloadAndLaunchActivityParam p = 
					(DownloadAndLaunchActivityParam) activity;
			comp = new DownloadAndLaunchActivity(this, manager.getRegistry(),
					ctx, p);
			uiRegister = p.isUIRegister();
		} else if (activity instanceof DownloadActivityParam) {
			DownloadActivityParam p = (DownloadActivityParam) activity;
			File f = p.getFolder();
			if (!f.isDirectory()) f = f.getParentFile();
			if (!canWriteInFolder(f))
				return;
			uiRegister = p.isUIRegister();
			comp = new DownloadActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof FigureActivityParam) {
			FigureActivityParam p = (FigureActivityParam) activity;
			comp = new FigureActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof AnalysisActivityParam) {
			AnalysisActivityParam p = (AnalysisActivityParam) activity;
			comp = new AnalysisActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof ScriptActivityParam) {
			ScriptActivityParam p = (ScriptActivityParam) activity;
			comp = new ScriptActivity(this, manager.getRegistry(), ctx,
					p.getScript(), p.getIndex());
		} else if (activity instanceof DownloadArchivedActivityParam) {
			DownloadArchivedActivityParam p = (DownloadArchivedActivityParam) activity;
			comp = new DownloadArchivedActivity(this, manager.getRegistry(),
					ctx, p);
		} else if (activity instanceof DeleteActivityParam) {
			DeleteActivityParam p = (DeleteActivityParam) activity;
			comp = new DeleteActivity(this, manager.getRegistry(), p);
			uiRegister = p.isUIRegister();
		} else if (activity instanceof OpenActivityParam) {
			OpenActivityParam p = (OpenActivityParam) activity;
			comp = new OpenObjectActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof DownloadAndZipParam) {
			DownloadAndZipParam p = (DownloadAndZipParam) activity;
			if (!canWriteInFolder(p.getFolder()))
				return;
			comp = new DownloadAndZipActivity(this, manager.getRegistry(), ctx,
					p);
		} else if (activity instanceof SaveAsParam) {
			SaveAsParam p = (SaveAsParam) activity;
			File folder = p.getFolder();
			if (folder != null) {
				if (folder.isFile() || !folder.exists())
					folder = folder.getParentFile();
			}
			if (!canWriteInFolder(folder))
				return;
			comp = new SaveAsActivity(this, manager.getRegistry(), ctx, p);
		} else if (activity instanceof TransferableActivityParam) {
			TransferableActivityParam p = (TransferableActivityParam) activity;
			comp = new DataTransferActivity(this, manager.getRegistry(), p);
		} else if (activity instanceof ResultsObject) {
		    ResultsObject p = (ResultsObject) activity;
		    comp = new SaveResultsActivity(this, manager.getRegistry(), ctx, p);
		}
		if (comp != null) {
			if (startActivity) {
				if (register)
					comp.startActivity();
			}
			manager.registerActivity(comp, uiRegister);
			if (startActivity) {
				manager.startActivity(false, comp);
			}
		}
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#hasRunningActivities()
	 */
	public boolean hasRunningActivities() {
		return manager.getRunningActivitiesCount() > 0;
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
         * 
         * @see UserNotifier#clearActivities()
	 */
	public void clearActivities() {
	    manager.clearActivities();
	}
	
	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#openApplication(Object)
	 */
	public void openApplication(ApplicationData data, String path) {

		if (data == null && path == null)
			return;
		
		Logger logger = manager.getRegistry().getLogger();
		try {
			String[] commandLineElements = ApplicationData.buildCommand(data,
					path == null ? null : new File(path));

			logger.info(this, "Executing command & args: " + 
					Arrays.toString(commandLineElements));

			Runtime runtime = Runtime.getRuntime();
			runtime.exec(commandLineElements);
		} catch (Exception e) {
			logger.error(this, e.getMessage());
		}
	}

	/**
	 * Implemented as specified by {@link UserNotifier}.
	 * 
	 * @see UserNotifier#setStatus(Object)
	 */
	public void setStatus(Object node) {
		if (dialog == null)
			return;
		dialog.setStatus(node);
	}

	/**
	 * Listens to property fired by the <code>ChangesDialog</code>
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (ChangesDialog.DONE_PROPERTY.equals(name)) {
			if (dialog != null) {
				dialog.setVisible(false);
				dialog.dispose();
				dialog = null;
			}
		}
	}

}
