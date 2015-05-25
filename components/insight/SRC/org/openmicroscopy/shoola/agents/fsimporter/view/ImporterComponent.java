/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterComponent 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportLocationSettings;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.events.LogOff;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
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
 * @since 3.0-Beta4
 */
class ImporterComponent
	extends AbstractComponent
	implements Importer
{

	/** Text displayed in dialog box when cancelling import.*/
	private static final String CANCEL_TEXT = "Are you sure you want to " +
			"cancel all imports that have not yet started?";
	
	/** Title displayed in dialog box when cancelling import.*/
	private static final String CANCEL_TITLE = "Cancel Import";
	
	/** If a given plane is larger than the size, the thumbnail is not loaded.*/
	private static final int MAX_SIZE = 2000*2000;
	
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
	 * Posts event if required indicating the status of the import process.
	 * 
	 * @param element The UI element to handle.
	 * @param result The formatted result.
	 * @param startUpload Pass <code>true</code> to indicate to start the 
	 * upload, <code>false</code> otherwise.
	 */
	private void handleCompletion(ImporterUIElement element, Object result,
			boolean startUpload)
	{
		boolean refreshTree = false;
		if (result instanceof Boolean) result = null;
		List<DataObject> containers = null;
		if (element != null) {
			if (element.isDone()) {
				refreshTree = element.hasToRefreshTree();
				containers = element.getExistingContainers();
				model.importCompleted(element.getID());
				view.onImportEnded(element);
			}
			
			if (markToclose) {
				view.setVisible(false);
			} else {
				if (element.isUploadComplete()) {
					element = view.getElementToStartImportFor();
					if (element != null && startUpload) {
					    // have to set uploadStarted flag immediately, otherwise it
					    // might be too late and the file gets imported twice
					    element.setUploadStarted(true);
					    importData(element);
					}
				}
			}
			fireStateChange();
		}

		//post an event
		if (!controller.isMaster()) {
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			ImportStatusEvent e = new ImportStatusEvent(hasOnGoingImport(),
					containers, result);
			e.setToRefresh(refreshTree);
			bus.post(e);
		}


		if (!hasOnGoingImport() && chooser.reloadHierarchies() && !markToclose)
		{
			//reload the hierarchies.
			Class<?> rootType = ProjectData.class;
			if (chooser != null && chooser.getType() == Importer.SCREEN_TYPE)
				rootType = ScreenData.class;
			model.fireContainerLoading(rootType, true, false, null);
			fireStateChange();
		}
	}
	
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
	 * Checks if there are on-going imports for the specified groups.
	 *
	 * @param ctx The security context to handle.
	 */
    boolean hasOnGoingImport(SecurityContext ctx)
    {
        if (model.getState() != DISCARDED) {
            return view.hasOnGoingImportFor(ctx);
        }
        return false;
    }
    
	/**
	 * Resets the identifier of the group.
	 * 
	 * @param groupID The id to set.
	 */
	void resetGroup(long groupID) { model.setGroupId(groupID); }
	
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
		ImportLocationDetails details = 
				new ImportLocationDetails(chooser.getType());
		refreshContainers(details);
		firePropertyChange(CHANGED_GROUP_PROPERTY, oldGroup, 
				model.getGroupId());
	}

	/** Shuts down the component.*/
    void shutDown()
    {
        view.setVisible(false);
        discard();
        model.setState(NEW);
    }
    
	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	void setDisplayMode(int displayMode)
	{
		
	}
	
	/** Refreshes the view when a user reconnects.*/
	void onReconnected()
	{
		ExperimenterData exp = ImporterAgent.getUserDetails();
		GroupData group = exp.getDefaultGroup();
		long oldGroup = -1;
		if (model.getExperimenterId() == exp.getId() &&
				group.getId() == model.getGroupId())
			return;
		view.reset();
		model.setGroupId(group.getId());
		
		ImportLocationDetails info = new ImportLocationDetails(chooser.getType());
		refreshContainers(info);
		firePropertyChange(CHANGED_GROUP_PROPERTY, oldGroup, 
				model.getGroupId());
	}
	
	/**
	 * Returns the sorted list of groups the current user has access to
	 * @return see above.
	 */
	private Collection<GroupData> loadGroups() {
		Collection set = ImporterAgent.getAvailableUserGroups();
		
        if (set == null || set.size() <= 1) return null;
        
        ViewerSorter sorter = new ViewerSorter();
        List<GroupData> sortedGroups = sorter.sort(set);
        
        return sortedGroups;
	}

	/**
	 * Brings up the dialog to select the import location.
	 *
	 * @param type The type of dialog e.g. screen view.
	 * @param selectedContainer The selected container e.g. dataset
	 * @param objects The collection of containers.
	 * @param userId The user to import for.
	 * @param display Pass <code>true</code> to display the view,
	 *                <code>false</code> otherwise.
	 */
    private void activate(int type, TreeImageDisplay selectedContainer,
           Collection<TreeImageDisplay> objects, long userId, boolean display)
    {
        if (model.getState() == DISCARDED) return;
        boolean reactivate = chooser != null;
        model.setImportFor(userId);
        if (chooser == null) {
            chooser = new ImportDialog(view, model.getSupportedFormats(),
                    selectedContainer, objects, type,
                    controller.getAction(ImporterControl.CANCEL_BUTTON), this);
            chooser.addPropertyChangeListener(controller);
            view.addComponent(chooser);
        } else {
            chooser.reset(selectedContainer, objects, type, model.getGroupId(),
                    model.getImportFor());
            chooser.requestFocusInWindow();
            view.selectChooser();
        }
        chooser.setSelectedGroup(getSelectedGroup());
        if (model.isMaster() || CollectionUtils.isEmpty(objects) || !reactivate)
            refreshContainers(new ImportLocationDetails(type));
        //load available disk space
        model.fireDiskSpaceLoading();
        if (display) {
            view.setOnScreen();
            view.toFront();
        }
    }

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#activate(int, TreeImageDisplay, Collection, long)
	 */
	public void activate(int type, TreeImageDisplay selectedContainer,
			Collection<TreeImageDisplay> objects, long userId)
	{
		activate(type, selectedContainer, objects, userId, true);
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
		if (model.getState() != IMPORTING && model.getState() != DISCARDED) {
			view.close();
			model.discard();
			fireStateChange();
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
		if (data == null || CollectionUtils.isEmpty(data.getFiles())) {
			UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Import", "No Files to import.");
			return;
		}
		view.showRefreshMessage(chooser.isRefreshLocation());
		if (data.hasNewTags()) model.setTags(null);
		ImporterUIElement element = view.addImporterElement(data);
		//if (model.getState() == IMPORTING) return;
		//Can I start the upload
		Collection<ImporterUIElement> list = view.getImportElements();
		Iterator<ImporterUIElement> i = list.iterator();
		ImporterUIElement e;
		boolean canImport = true;
		while (i.hasNext()) {
			e = i.next();
			if (e.hasStarted() && !e.isUploadComplete()) {
				canImport = false;
				break;
			}
		}
		if (!canImport) return;
		importData(element);
		if (!controller.isMaster()) {
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			ImportStatusEvent event;
			event = new ImportStatusEvent(hasOnGoingImport(), null, null);
			bus.post(event);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#uploadComplete(ImportableFile, Object, int)
	 */
	public void uploadComplete(ImportableFile f, Object result, int index)
	{
		if (model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getUIElement(index);
		if (element != null) {
			Object formattedResult = element.uploadComplete(f, result);
			handleCompletion(element, formattedResult, true);
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
	public void submitFiles() { controller.submitFiles(null); }

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
	public boolean hasFailuresToReupload()
	{
		if (model.getState() == DISCARDED)
			return false;
		return view.hasFailuresToReupload();
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
		view.setVisible(false);
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#retryUpload(FileImportComponent)
	 */
	public void retryUpload(FileImportComponent fc)
	{
		if (model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getSelectedPane();
		if (element == null) return;
		List<FileImportComponent> l;
		if (fc != null) {
			l = new ArrayList<FileImportComponent>(1);
			l.add(fc);
		} else l = element.getFilesToReupload();
		if (CollectionUtils.isEmpty(l)) return;
		Iterator<FileImportComponent> i = l.iterator();
		ImportableObject object = element.getData();
		List<ImportableFile> list = new ArrayList<ImportableFile>();
		while (i.hasNext()) {
			fc = i.next();
			fc.setReimported(true);
			list.add(fc.getImportableFile());
		}
		object.reUpload(list);
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
	public void refreshContainers(ImportLocationDetails details)
	{
		if (details == null) return;
		switch (model.getState()) {
			case DISCARDED:
				return;
		}
		view.showRefreshMessage(false);
		Class<?> rootType = ProjectData.class;
		if (details.getDataType() == Importer.SCREEN_TYPE)
			rootType = ScreenData.class;
		model.fireContainerLoading(rootType, false, false, details.getUser());
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setContainers(Collection, boolean, int, long)
	 */
	public void setContainers(Collection result, boolean refreshImport,
			boolean changeGroup, int type, long userID)
	{
		switch (model.getState()) {
			case DISCARDED:
				return;
		}
		if (chooser == null) return;
		Set nodes = TreeViewerTranslator.transformHierarchy(result);
		chooser.reset(nodes, type, model.getGroupId(), userID);
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
			if (CollectionUtils.isEmpty(list)) return;
			Iterator<ImporterUIElement> i = list.iterator();
			ImporterUIElement element;
			while (i.hasNext()) {
				element = i.next();
				if (element.hasImportToCancel())
					toImport.add(element);
			}
			if (toImport.size() > 0) {
				i = toImport.iterator();
				while (i.hasNext()) {
					i.next().cancelLoading();
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
	 * @see Importer#createDataObject(ObjectToCreate)
	 */
	public void createDataObject(ObjectToCreate data)
	{
		if (data == null)
			throw new IllegalArgumentException("No object to create.");
		model.fireDataCreation(data);
		fireStateChange();
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#getSelectedGroup()
	 */
	public GroupData getSelectedGroup()
	{
		Collection<GroupData> m = loadGroups();

		if (m == null) {
			ExperimenterData exp = ImporterAgent.getUserDetails();
			return exp.getDefaultGroup();
		}

		long id = model.getGroupId();

		for (GroupData group : m) {
			if (group.getId() == id)
				return group;
		}
		ExperimenterData exp = ImporterAgent.getUserDetails();
		return exp.getDefaultGroup();
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
			//case IMPORTING:
				return;
		}
		if (group == null) return;
		long oldId = model.getGroupId();
		if (group.getId() == oldId) return;
		model.setGroupId(group.getId());
		chooser.setSelectedGroup(getSelectedGroup());
		//refresh
		view.showRefreshMessage(false);
		Class rootType = ProjectData.class;
		if (chooser.getType() == Importer.SCREEN_TYPE)
			rootType = ScreenData.class;
		model.fireContainerLoading(rootType, false, true, null);
		firePropertyChange(CHANGED_GROUP_PROPERTY, oldId, group.getId());
	}

	/**
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setLogFile(Collection, long, int)
	 */
	public void setImportLogFile(Collection<FileAnnotationData> collection,
			long fileSetID, int index) {
		if (model.getState() == DISCARDED) {
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		}
		Collection<ImporterUIElement> list = view.getImportElements();
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		Iterator<ImporterUIElement> i = list.iterator();
		ImporterUIElement element;
		while (i.hasNext()) {
			element = i.next();
			if (element.getID() == index) {
				element.setImportLogFile(collection, index);
				break;
			}
		}
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

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#isMaster()
	 */
	public boolean isMaster() { return model.isMaster(); }
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Importer#getDisplayMode()
	 */
	public int getDisplayMode() { return model.getDisplayMode(); }
	
	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Importer#hasOnGoingUpload()
	 */
	public boolean hasOnGoingUpload()
	{
		if (model.getState() != DISCARDED) {
			Collection<ImporterUIElement> list = view.getImportElements();
			if (list == null || list.size() == 0) return false;
			Iterator<ImporterUIElement> i = list.iterator();
			ImporterUIElement element;
			while (i.hasNext()) {
				element = i.next();
				if (!element.isUploadComplete())
					return true;
			}
		}
		return false;
	}

	/** 
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Importer#onImportComplete(FileImportComponent)
	 */
	public void onImportComplete(FileImportComponent component)
	{
		if (component == null || model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getUIElement(component.getIndex());
		if (element == null) return;
		Object result = component.getImportResult();
		if (result instanceof Exception) {
			if (component.getFile().isFile()) {
				ImportErrorObject r = new ImportErrorObject(
				        component.getFile().getTrueFile(),
						(Exception) result, component.getGroupID());
				element.setImportResult(component, result);
				handleCompletion(element, r, !component.hasParent());
			}
			return;
		}
		
		element.setImportResult(component, result);
		handleCompletion(element, result, !component.hasParent());
		Collection<PixelsData> pixels = (Collection<PixelsData>) result;
		if (CollectionUtils.isEmpty(pixels)) return;
		List<DataObject> l = new ArrayList<DataObject>();
		Iterator<PixelsData> i = pixels.iterator();
		Class<?> klass = ThumbnailData.class;
		if (component.isHCS()) {
			klass = PlateData.class;
		}
		PixelsData pxd;
		List<ImageData> ids = new ArrayList<ImageData>();
		while (i.hasNext()) {
			pxd = i.next();
			ids.add(pxd.getImage());
			pxd.getImage().getId();
			if (pxd.getSizeX()*pxd.getSizeY() < MAX_SIZE) {
				l.add(pxd);
			}
		}
		model.saveROI(component, ids);
        if (l.size() > 0 && !PlateData.class.equals(klass)) {
            if (l.size() > FileImportComponent.MAX_THUMBNAILS) {
                l = l.subList(0, FileImportComponent.MAX_THUMBNAILS);
            }
            model.fireImportResultLoading(l, klass, component,
                    component.getImportableFile().getUser());
        }
	}

	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#onUploadComplete(FileImportComponent)
	 */
	public void onUploadComplete(FileImportComponent component)
	{
		if (model.getState() == DISCARDED) return;
		if (component == null || model.getState() == DISCARDED) return;
		ImporterUIElement element = view.getUIElement(component.getIndex());
		if (element == null) return;
		Object result = component.getImportResult();
		Object formattedResult = element.uploadComplete(component, result);
		if (!controller.isMaster()) {
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			ImportStatusEvent e = new ImportStatusEvent(hasOnGoingImport(),
					null, formattedResult);
			bus.post(e);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Importer} interface.
	 * @see Importer#setImportResult(Object, Object)
	 */
	public void setImportResult(Object result, Object component)
	{
		if (component == null || model.getState() == DISCARDED) return;
		FileImportComponent c = (FileImportComponent) component;
		ImporterUIElement element = view.getUIElement(c.getIndex());
		if (element == null) return;
		c.setStatus(result);
	}

    /** 
     * Implemented as specified by the {@link Importer} interface.
     * @see Importer#getImportFor()
     */
    public long getImportFor() { return model.getImportFor(); }

    /** 
     * Implemented as specified by the {@link Importer} interface.
     * @see Importer#canImportAs()
     */
    public boolean canImportAs() { return model.canImportAs(); }

    /** 
     * Implemented as specified by the {@link Importer} interface.
     * @see Importer#getAvailableGroups()
     */
    public Collection<GroupData> getAvailableGroups()
    {
        return model.getAvailableGroups();
    }

    /** 
     * Implemented as specified by the {@link Importer} interface.
     * @see Importer#isSystemGroup(long, String)
     */
    public boolean isSystemGroup(long groupID, String key)
    {
        return model.isSystemGroup(groupID, key);
    }

    /** 
     * Implemented as specified by the {@link Importer} interface.
     * @see Importer#importResults(ResultsObject, boolean)
     */
    public void importResults(ResultsObject object, boolean importImage)
    {
        if (object == null) return;
        if (importImage) {
            //Import images first
            activate(Importer.PROJECT_TYPE, null, null, getImportFor(), false);
            List<FileObject> files = (List) object.getRefObjects();
            ImportLocationSettings settings = chooser.createLocationDialog();
            if (settings != null) {
                view.setOnScreen();
                view.toFront();
                chooser.addImageJFiles(files, settings);
                chooser.importFiles();
            }
        }
    }
}