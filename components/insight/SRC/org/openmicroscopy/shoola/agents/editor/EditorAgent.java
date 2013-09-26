/*
 * org.openmicroscopy.shoola.agents.editor.EditorAgent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import omero.model.OriginalFile;

import org.openmicroscopy.shoola.agents.editor.actions.RegisterAction;
import org.openmicroscopy.shoola.agents.editor.view.AutosaveRecovery;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.agents.editor.view.EditorFactory;
import org.openmicroscopy.shoola.agents.events.editor.CopyEvent;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.ChangeUserGroupEvent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;

/**
 * The Editor agent.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta3
 */
public class EditorAgent implements Agent, AgentEventListener {

	/** Reference to the registry. */
	private static Registry registry;

	/** The group id if set. */
	private long groupId;

	/** Indicate that the editor is used as a stand-alone application.*/
	private boolean master;
	
	/**
	 * Helper method.
	 * 
	 * @return A reference to the {@link Registry}.
	 */
	public static Registry getRegistry() {
		return registry;
	}

	/**
	 * Helper method returning the current user's details.
	 * 
	 * @return See above.
	 */
	public static ExperimenterData getUserDetails() {
		return (ExperimenterData) registry
				.lookup(LookupNames.CURRENT_USER_DETAILS);
	}

	/**
	 * Returns the available user groups.
	 * 
	 * @return See above.
	 */
	public static Collection getAvailableUserGroups() {
		return (Collection) registry.lookup(LookupNames.USER_GROUP_DETAILS);
	}

	/**
	 * Returns <code>true</code> if an OMERO server is available,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isServerAvailable() {
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
		return env.isServerAvailable();
	}

	/**
	 * Returns the path of the 'omero home' directory, e.g. user/omero.
	 * 
	 * @return See above.
	 */
	public static String getOmeroHome() {
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
		String omeroDir = env.getOmeroHome();
		// make sure dir exists.
		File home = new File(omeroDir);
		if (!home.exists())
			home.mkdir();
		return omeroDir;
	}

	/**
	 * Returns the path of the 'editor home' directory, e.g. user/omero/editor.
	 * Folder will be created if it doesn't exist.
	 * 
	 * @return See above.
	 */
	public static String getEditorHome() {
		String editorDir = (String) registry.lookup("/services/editor/home");
		editorDir = getOmeroHome() + File.separator + editorDir;
		// make sure dir exists.
		File home = new File(editorDir);
		if (!home.exists())
			home.mkdir();
		return editorDir;
	}

	/**
	 * Returns the path of the 'editor autosave' directory, e.g.
	 * user/omero/editor/autosave. Folder will be created if it doesn't exist.
	 * 
	 * @return See above.
	 */
	public static String getEditorAutosave() {
		String editorDir = (String) registry
				.lookup("/services/editor/autosave");
		editorDir = getEditorHome() + File.separator + editorDir;
		// make sure dir exists.
		File home = new File(editorDir);
		if (!home.exists())
			home.mkdir();
		return editorDir;
	}

	/**
	 * Static method to open a local file. Creates or recycles an Editor to
	 * display the file.
	 * 
	 * @param file The local file to open.
	 * @param master Pass <code>true</code> if the agent is the entry point
	 * <code>false</code> otherwise.
	 */
	public static Editor openLocalFile(File file, boolean master) {

		if (file == null)
			return null;
		if (!file.exists())
			return null;

		// gets a blank editor (one that has been created with a 'blank' model),
		// OR an existing editor if one has the same
		// file ID (will be 0 if editor file is local) and same file name, OR
		// creates a new editor model and editor with this new file.

		ExperimenterData exp = getUserDetails();
		SecurityContext ctx = null;
		if (exp != null) {
			ctx = new SecurityContext(exp.getDefaultGroup().getId());
			
		}
			
		Editor editor = EditorFactory.getEditor(ctx, file, master);

		// activates the editor
		// if the editor is 'blank' or has just been created (above),
		// need to set the file
		if (editor != null) {
			if (editor.getState() == Editor.NEW) {
				editor.setFileToEdit(null, file);
			}

			// this simply brings the editor to the front / de-iconifies it.
			editor.activate();
		}
		return editor;
	}

	/**
	 * Creates or recycles an editor.
	 * 
	 * @param event
	 *            The event to handle.
	 */
	private void handleFileEdition(EditFileEvent event) {
		if (event == null)
			return;
		Boolean available = (Boolean) registry
				.lookup(LookupNames.BINARY_AVAILABLE);
		if (available != null && !available.booleanValue())
			return;

		Editor editor = null;
		FileAnnotationData data = event.getFileAnnotation();
		if (data == null) {
			if (event.getFileAnnotationID() > 0)
				editor = EditorFactory.getEditor(event.getSecurityContext(),
						event.getFileAnnotationID(), master);
		} else {
			if (data.getId() <= 0)
				return;
			String name = data.getFileName();
			String ns = data.getNameSpace();
			if (name == null)
				return;
			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns)
					|| FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns)
					|| FileAnnotationData.COMPANION_FILE_NS.equals(ns)
					|| EditorUtil.isEditorFile(name))
				editor = EditorFactory.getEditor(event.getSecurityContext(),
						data.getId(), master);
			else {
				UserNotifier un = getRegistry().getUserNotifier();
				OriginalFile f = (OriginalFile) data.getContent();
				Environment env = (Environment) getRegistry().lookup(
						LookupNames.ENV);
				DownloadAndLaunchActivityParam activity;
				final long dataId = data.getId();
				final File dir = new File(env.getOmeroFilesHome() + File.separatorChar + "file annotation " + dataId);
				if (!dir.exists()) {
				    dir.mkdir();
				}
				if (f != null && f.isLoaded()) {
					activity = new DownloadAndLaunchActivityParam(f, dir, null);
				} else {
					activity = new DownloadAndLaunchActivityParam(dataId,
							DownloadAndLaunchActivityParam.FILE_ANNOTATION,
							new File(dir, name), null);
				}
				un.notifyActivity(event.getSecurityContext(), activity);
				return;
			}
		}
		if (editor != null)
			editor.activate();// starts file downloading
	}

	/**
	 * Creates a {@link Editor#NEW} editor with no file, or recycles an editor.
	 * 
	 * @param evt The event to handle.
	 * @param master Pass <code>true</code> if the importer is used a
	 * stand-alone application, <code>false</code> otherwise.
	 */
	private void handleShowEditor(ShowEditorEvent evt, boolean master)
	{
		// need to check for auto-saved files for recovery, after showing editor
		AutosaveRecovery autosaveRecovery = new AutosaveRecovery();
		Editor editor = null;
		if (evt == null) {
			editor = EditorFactory.getEditor(null, master);
			if (editor != null)
				editor.activate();
			autosaveRecovery.checkForRecoveredFiles(); // now check
			return;
		}
		if (evt.getParent() == null)
			editor = EditorFactory.getEditor(evt.getSecurityContext(), master);
		else {
			int editorType = Editor.PROTOCOL;
			if (evt.getType() == ShowEditorEvent.EXPERIMENT)
				editorType = Editor.EXPERIMENT;
			editor = EditorFactory.getEditor(evt.getSecurityContext(),
					evt.getParent(), evt.getName(), editorType, master);
		}
		autosaveRecovery.checkForRecoveredFiles(); // now check
		if (editor != null)
			editor.activate();
	}

	/**
	 * Handles the {@link UserGroupSwitched} event.
	 * 
	 * @param evt
	 *            The event to handle.
	 */
	private void handleUserGroupSwitched(UserGroupSwitched evt) {
		if (evt == null)
			return;
		EditorFactory.onGroupSwitched(evt.isSuccessful());
	}

	/**
	 * Handles the {@link ReconnectedEvent} event.
	 * 
	 * @param evt
	 *            The event to handle.
	 */
	private void handleReconnectedEvent(ReconnectedEvent evt) {
		if (evt == null)
			return;
		EditorFactory.onGroupSwitched(true);
	}

	/**
	 * Handles the copying event, by passing the copied data from this event to
	 * the {@link EditorFactory} where it can be accessed by any Editor
	 * instance.
	 * 
	 * @param evt
	 *            The CopyEvent event.
	 */
	private void handleCopyData(CopyEvent evt) {
		EditorFactory.setCopiedData(evt.getCopiedData());
	}

	/** Registers the agent with the tool bar. */
	private void register() {
		groupId = -1;
		TaskBar tb = registry.getTaskBar();
		IconManager icons = IconManager.getInstance();
		JButton b = new JButton(icons.getIcon(IconManager.EDITOR));
		b.setToolTipText(RegisterAction.DESCRIPTION);
		ActionListener l = new ActionListener() {

			/** Posts an event to start the agent. */
			public void actionPerformed(ActionEvent e) {
				ExperimenterData exp = (ExperimenterData) registry
						.lookup(LookupNames.CURRENT_USER_DETAILS);
				if (exp == null)
					return;
				GroupData gp = exp.getDefaultGroup();
				long id = -1;
				if (gp != null) id = gp.getId();
				if (groupId == -1) groupId = id;
				
				handleShowEditor(new ShowEditorEvent(
						new SecurityContext(groupId)), false);
			}
		};
		b.addActionListener(l);
		tb.addToToolBar(TaskBar.AGENTS, b);
		// register with File menu
		JMenuItem item = new JMenuItem(icons.getIcon(IconManager.EDITOR));
		item.setToolTipText(RegisterAction.DESCRIPTION);
		item.addActionListener(l);
		item.setText(RegisterAction.NAME);
		tb.addToMenu(TaskBar.FILE_MENU, item);
	}

	/** Creates a new instance. */
	public EditorAgent() {
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#activate(boolean)
	 */
	public void activate(boolean master) {
		this.master = master;
		if (master)
			handleShowEditor(null, true);
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#terminate()
	 */
	public void terminate() {
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
		if (env.isRunAsPlugin())
			EditorFactory.onGroupSwitched(true);
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#setContext(Registry)
	 */
	public void setContext(Registry ctx) {
		registry = ctx;
		EventBus bus = registry.getEventBus();
		bus.register(this, EditFileEvent.class);
		bus.register(this, ShowEditorEvent.class);
		bus.register(this, CopyEvent.class);
		bus.register(this, UserGroupSwitched.class);
		bus.register(this, ReconnectedEvent.class);
		bus.register(this, ChangeUserGroupEvent.class);
		register();
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#canTerminate()
	 */
	public boolean canTerminate() { return true; }

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#getDataToSave()
	 */
	public AgentSaveInfo getDataToSave() {
		try { // random error thrown. Needs to investigate why
			List<Object> instances = EditorFactory.getInstancesToSave();
			if (instances == null || instances.size() == 0)
				return null;
			return new AgentSaveInfo("Editors", instances);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Implemented as specified by {@link Agent}.
	 * 
	 * @see Agent#save(List)
	 */
	public void save(List<Object> instances) {
		EditorFactory.saveInstances(instances);
	}

	/**
	 * Responds to an event fired trigger on the bus.
	 * 
	 * @see AgentEventListener#eventFired(AgentEvent)
	 */
	public void eventFired(AgentEvent e) {
		if (e instanceof EditFileEvent)
			handleFileEdition((EditFileEvent) e);
		else if (e instanceof ShowEditorEvent)
			handleShowEditor((ShowEditorEvent) e, false);
		else if (e instanceof CopyEvent)
			handleCopyData((CopyEvent) e);
		else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
		else if (e instanceof ReconnectedEvent)
			handleReconnectedEvent((ReconnectedEvent) e);
		else if (e instanceof ChangeUserGroupEvent) {
			ChangeUserGroupEvent evt = (ChangeUserGroupEvent) e;
			groupId = evt.getGroupID();
		}
	}

}