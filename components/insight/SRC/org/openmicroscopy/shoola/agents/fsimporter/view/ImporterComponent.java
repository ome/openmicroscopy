/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterComponent 
 *
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
 */
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.LogOff;
import org.openmicroscopy.shoola.env.data.events.SwitchUserGroup;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Implements the {@link Importer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.ImporterModel
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUI
 * @see org.openmicroscopy.shoola.agents.fsimporter.view.ImporterControl
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ImporterComponent 
	extends AbstractComponent
	implements Importer
{

	/** Text displayed in dialog box when cancelling import.*/
	private static final String CANCEL_TEXT = "Are you sure you want to " +
			"cancel all imports that have not yet started?";
	
	/** Text displayed in dialog box when cancelling import.*/
	private static final String CANCEL_SELECTED_TEXT = 
		"Are you sure you want to cancel the import that have not yet started?";
	
	/** Title displayed in dialog box when cancelling import.*/
	private static final String CANCEL_TITLE = "Cancel Import";
	
	/** The Model sub-component. */
	private ImporterModel 	model;
	
	/** The Controller sub-component. */
	private ImporterControl 	controller;
	
	/** The View sub-component. */
	private ImporterUI 		view;
	
	/** Reference to the chooser used to select the files to import. */
	private ImportDialog	chooser;

	/** Flag indicating that the window has been marked to be closed.*/
	private boolean 		markToclose;
	
	/**
	 * Imports the data for the specified import view.
	 * 
	 * @param element The import view. 
	 */
	private void importData(ImporterUIElement element)
	{
		if (element == null) return;
		view.setSelectedPane(element, true);
		model.fireImportData(element.getData(), element.getID());
		if (!model.isMaster()) {
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			bus.post(new ImportStatusEvent(true, 
					element.getExistingContainers()));
			fireStateChange();
		}
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	ImporterComponent(ImporterModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new ImporterControl(this);
		view = new ImporterUI();
		markToclose = false;
	}

	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(view);
		view.initialize(model, controller);
	}

	/**
	 * Returns <code>true</code> if the agent is the entry point
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMaster() { return model.isMaster(); }
	
	/** 
	 * Indicates that the group has been successfully switched if 
	 * <code>true</code>, unsuccessfully if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, 
	 * 					<code>false</code> otherwise.
	 */
	void onGroupSwitched(boolean success)
	{
		if (!model.isMaster()) return;
		if (!success) return;
		ExperimenterData exp = ImporterAgent.getUserDetails();
		GroupData group = exp.getDefaultGroup();
		long oldGroup = model.getGroupId();
		model.setGroupId(group.getId());
		refreshContainers(chooser.getType());
		firePropertyChange(CHANGED_GROUP_PROPERTY, oldGroup, 
				model.getGroupId());
	}

	
	/** 
	 * Indicate that it was possible to reconnect.
	 */
	void onReconnected()
	{
		if (!model.isMaster()) return;
		ExperimenterData exp = ImporterAgent.getUserDetails();
		GroupData group = exp.getDefaultGroup();
		long oldGroup = -1;
		if (model.getExperimenterId() == exp.getId() &&
				group.getId() == model.getGroupId())
			return;
		model.setGroupId(group.getId());
		chooser.onReconnected(view.buildToolBar());
		refreshContainers(chooser.getType());
		firePropertyChange(CHANGED_GROUP_PROPERTY, oldGroup, 
				model.getGroupId());
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#activate(int, TreeImageDisplay, Collection)
	 */
	public void activate(int type, TreeImageDisplay selectedContainer, 
			Collection<TreeImageDisplay> objects)
	{
		if (model.getState() == DISCARDED) return;
		if (chooser == null) {
			chooser = new ImportDialog(view, model.getSupportedFormats(), 
					selectedContainer, objects, type);
			chooser.addPropertyChangeListener(controller);
			//chooser.pack();
			view.addComponent(chooser);
		} else {
			chooser.reset(selectedContainer, objects, type);
			chooser.requestFocusInWindow();
			view.selectChooser();
		}
		if (model.isMaster()) refreshContainers(type);
		//load available disk space
		model.fireDiskSpaceLoading();
		view.setOnScreen();
		view.toFront();
		//view.setVisible(false);
		//UIUtilities.centerAndShow(chooser);
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#getView()
	 */
	public JFrame getView() { return view; }
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#discard()
	 */
	public void discard()
	{
		if (model.getState() == READY) {
			view.close();
			model.discard();
		}
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#getState()
	 */
	public int getState() { return model.getState(); }

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#importData(ImportableObject)
	 */
	public void importData(ImportableObject data)
	{
		if (model.getState() == DISCARDED) return;
		if (data == null || data.getFiles() == null || 
				data.getFiles().size() == 0) {
			UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Import", "No Files to import.");
			return;
		}
		view.showRefreshMessage(chooser.isRefreshLocation());
		if (data.hasNewTags()) model.setTags(null);
		ImporterUIElement element = view.addImporterElement(data);
		if (model.getState() == IMPORTING) return;
		importData(element);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setImportedFile(File, Object, int)
	 */
	public void setImportedFile(File f, Object result, int index)
	{
		if (model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getUIElement(index);
		if (element != null) {
			element.setImportedFile(f, result);
			if (element.isDone()) {
				model.importCompleted(element.getID());
				view.onImportEnded(element);
				boolean b = chooser.reloadHierarchies();//element.getData().hasNewObjects();
				if (markToclose) {
					view.setVisible(false);
					fireStateChange();
					return;
				}
				if (!b) {
					element = view.getElementToStartImportFor();
					if (element != null) 
						importData(element);
				} else {
					//reload the hierarchies.
					Class rootType = ProjectData.class;
					if (chooser != null && 
							chooser.getType() == Importer.SCREEN_TYPE)
						rootType = ScreenData.class;
					model.fireContainerLoading(rootType, true);
				}
			}	
			fireStateChange();
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		if (model.getState() == DISCARDED) return;
		model.setTags(tags);
		if (chooser != null) chooser.setTags(tags);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#loadExistingTags()
	 */
	public void loadExistingTags()
	{
		if (model.getState() == DISCARDED) return;
		Collection tags = model.getTags();
		if (tags != null) setExistingTags(tags);
		else model.fireTagsLoading();	
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#submitFiles()
	 */
	public void submitFiles() { controller.submitFiles(); }

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#removeImportElement(Object)
	 */
	public void removeImportElement(Object object)
	{
		if (model.getState() == DISCARDED || object == null) return;
		ImporterUIElement element = view.removeImportElement(object);
		if (element != null) {
			element.cancelLoading();
			model.cancel(element.getID());
			fireStateChange();
		}
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#cancel()
	 */
	public void cancel()
	{
		if (model.getState() != DISCARDED)
			model.cancel();
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#cancelImport(int)
	 */
	public void cancelImport(int id)
	{
		//if (model.getState() != DISCARDED)
		//	model.cancel(id);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#cancelImport()
	 */
	public void cancelImport()
	{
		if (model.getState() != DISCARDED) {
			ImporterUIElement element = view.getSelectedPane();
			if (element != null && !element.isDone()) {
				MessageBox box = new MessageBox(view, CANCEL_TITLE,
						CANCEL_SELECTED_TEXT);
				if (box.centerMsgBox() == MessageBox.NO_OPTION)
					return;
				element.cancelLoading();
				//if (element.isDone())
				model.cancel(element.getID());
			}
			model.setState(READY);
		}
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#hasFailuresToSend()
	 */
	public boolean hasFailuresToSend()
	{
		if (model.getState() == DISCARDED || model.getState() == IMPORTING)
			return false;
		return view.hasFailuresToSend();
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#hasFailuresToSend()
	 */
	public boolean hasFailuresToReimport()
	{
		if (model.getState() == DISCARDED || model.getState() == IMPORTING)
			return false;
		return view.hasFailuresToReimport();
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setDiskSpace(DiskQuota)
	 */
	public void setDiskSpace(DiskQuota quota)
	{
		if (quota == null) return;
		if (model.getState() == DISCARDED) return;
		if (chooser != null && chooser.isVisible())
			chooser.setDiskSpace(quota);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#close()
	 */
	public void close()
	{
		if (model.isMaster()) {
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			bus.post(new ExitApplication());
			return;
		}
		Collection<ImporterUIElement> list = view.getImportElements();
		List<ImporterUIElement> 
		toImport = new ArrayList<ImporterUIElement>();
		if (list == null || list.size() == 0) {
			 view.setVisible(false);
			return;
		}
		Iterator<ImporterUIElement> i = list.iterator();
		ImporterUIElement element;
		ImporterUIElement started = null;
		while (i.hasNext()) {
			element = i.next();
			if (element.hasStarted()) started = element;
			if (!element.isDone())
				toImport.add(element);
		}
		if (toImport.size() > 0) {
			MessageBox box = new MessageBox(view, CANCEL_TITLE,
					CANCEL_TEXT+"\n" +
					"If Yes, the window will close when the on-going " +
					"import is completed.");
			if (box.centerMsgBox() == MessageBox.NO_OPTION)
				return;
			markToclose = true;
			i = toImport.iterator();
			while (i.hasNext()) {
				element = i.next();
				element.cancelLoading();
				//if (!element.hasStarted())
				model.cancel(element.getID());
			}
			if (started != null && started.isDone()) {
				markToclose = false;
			}
		} else markToclose = false;
		if (!markToclose) view.setVisible(false);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#retryImport()
	 */
	public void retryImport()
	{
		if (model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getSelectedPane();
		if (element == null) return;
		List<FileImportComponent> l = element.getFilesToReimport();
		if (l == null || l.size() == 0) return;
		Iterator<FileImportComponent> i = l.iterator();
		FileImportComponent fc;
		ImportableObject object = element.getData();
		List<File> files = new ArrayList<File>();
		while (i.hasNext()) {
			fc = i.next();
			fc.setReimported(true);
			files.add(fc.getFile());
		}
		object.reImport(files);
		importData(object);
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#isLastImport()
	 */
	public boolean isLastImport()
	{
		ImporterUIElement element = view.getSelectedPane();
		if (element == null) return false;
		return element.isLastImport();
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#hasOnGoingImport()
	 */
	public boolean hasOnGoingImport()
	{
		if (model.getState() != DISCARDED) {
			Collection<ImporterUIElement> list = view.getImportElements();
			if (list == null || list.size() == 0) return false;
			Iterator<ImporterUIElement> i = list.iterator();
			ImporterUIElement element;
			while (i.hasNext()) {
				element = i.next();
				if (!element.isDone())
					return true;
			}
		}
		return false;
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#refreshContainers(int)
	 */
	public void refreshContainers(int type)
	{
		switch (model.getState()) {
			case DISCARDED:
				return;
		}
		view.showRefreshMessage(false);
		Class rootType = ProjectData.class;
		if (type == Importer.SCREEN_TYPE)
			rootType = ScreenData.class;
		model.fireContainerLoading(rootType, false);
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setContainers(Collection, boolean, int)
	 */
	public void setContainers(Collection result, boolean refreshImport, 
			int type)
	{
		switch (model.getState()) {
			case DISCARDED:
				return;
		}
		if (chooser == null) return;
		ExperimenterData exp = ImporterAgent.getUserDetails();
		Set nodes = TreeViewerTranslator.transformHierarchy(result, exp.getId(),
				-1);
		chooser.reset(nodes, type);
		if (refreshImport) {
			Collection<ImporterUIElement> l = view.getImportElements();
			Iterator<ImporterUIElement> i = l.iterator();
			ImporterUIElement element;
			while (i.hasNext()) {
				element = i.next();
				if (!element.isDone()) {
					element.resetContainers(result);
				}
			}
			//restarts The import.
			element = view.getElementToStartImportFor();
			if (element != null) 
				importData(element);
		}
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#cancelAllImports()
	 */
	public void cancelAllImports()
	{
		if (model.getState() != DISCARDED) {
			Collection<ImporterUIElement> list = view.getImportElements();
			List<ImporterUIElement> 
			toImport = new ArrayList<ImporterUIElement>();
			if (list == null || list.size() == 0) return;
			Iterator<ImporterUIElement> i = list.iterator();
			ImporterUIElement element;
			while (i.hasNext()) {
				element = i.next();
				if (!element.isDone())
					toImport.add(element);
			}
			if (toImport.size() > 0) {
				MessageBox box = new MessageBox(view, CANCEL_TITLE,
						CANCEL_TEXT);
				if (box.centerMsgBox() == MessageBox.NO_OPTION)
					return;
				i = toImport.iterator();
				while (i.hasNext()) {
					element = i.next();
					element.cancelLoading();
					//if (!element.hasStarted())
					model.cancel(element.getID());
				}
			}
		}
	}

	/**
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#moveToFront()
	 */
	public void moveToFront()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (!view.isVisible()) view.setVisible(true);
		view.toFront();
	}

	/**
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#onDataObjectSaved(DataObject, DataObject)
	 */
	public void onDataObjectSaved(DataObject d, DataObject parent)
	{
		//if (model.getState() != CREATING_CONTAINER) return;
		if (chooser == null) return;
		chooser.onDataObjectSaved(d, parent);
	}

	/**
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#createDataObject(DataObject, DataObject)
	 */
	public void createDataObject(DataObject child, DataObject parent)
	{
		if (child == null)
			throw new IllegalArgumentException("No object to create.");
		model.fireDataCreation(child, parent);
		fireStateChange();
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#getSelectedGroup()
	 */
	public GroupData getSelectedGroup()
	{
		Set m = ImporterAgent.getAvailableUserGroups();
		if (m == null) return null;
		Iterator i = m.iterator();
		long id = model.getGroupId();
		GroupData group = null;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() == id) {
				return group;
			}
		}
		return null;
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#showMenu(int, Component, Point)
	 */
	public void showMenu(int menuId, Component source, Point point)
	{
		if (model.getState() == DISCARDED) return;
		view.showMenu(menuId, source, point);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setUserGroup(GroupData)
	 */
	public void setUserGroup(GroupData group)
	{
		switch (model.getState()) {
			case DISCARDED:
			case IMPORTING:
				return;
		}
		if (group == null) return;
		ExperimenterData exp = ImporterAgent.getUserDetails();
		long oldId = model.getGroupId();
		if (group.getId() == oldId) return;
		Registry reg = ImporterAgent.getRegistry();
		reg.getEventBus().post(new SwitchUserGroup(exp, group.getId()));
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#logOff()
	 */
	public void logOff()
	{
		if (model.getState() == IMPORTING) return;
		Registry reg = ImporterAgent.getRegistry();
		reg.getEventBus().post(new LogOff());
	}

}
