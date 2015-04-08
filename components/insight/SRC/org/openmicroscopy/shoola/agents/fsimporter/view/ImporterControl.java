/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterControl 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.actions.ActivateAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.CancelAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.CloseAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.ExitAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.ImporterAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.LogOffAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.RetryImportAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.SubmitFilesAction;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The {@link Importer}'s controller. 
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
class ImporterControl
	implements ActionListener, PropertyChangeListener
{

	/** Action ID indicating to send the files that could not imported. */
	static final Integer SEND_BUTTON = 0;
	
	/** Action ID indicating to close the window. */
	static final Integer CLOSE_BUTTON = 1;
	
	/** Action ID indicating to cancel. */
	static final Integer CANCEL_BUTTON = 2;
	
	/** Action ID indicating to retry failed import. */
	static final Integer RETRY_BUTTON = 3;
	
	/** Action ID indicating to switch between groups. */
	static final Integer GROUP_BUTTON = 4;
	
	/** Action ID indicating to exit the application. */
	static final Integer EXIT = 5;
	
	/** Action ID indicating to log off the current server. */
	static final Integer LOG_OFF = 6;
	
	/** 
	 * Reference to the {@link Importer} component, which, in this context,
	 * is regarded as the Model.
	 */
	private Importer 		model;
	
	/** Reference to the View. */
	private ImporterUI		view;

	/** Collection of files to submit. */
	private List<FileImportComponent> markedFailed;
	
	/** Maps actions identifiers onto actual <code>Action</code> object. */
	private Map<Integer, ImporterAction>	actionsMap;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap = new HashMap<Integer, ImporterAction>();
		actionsMap.put(SEND_BUTTON, new SubmitFilesAction(model));
		actionsMap.put(CLOSE_BUTTON, new CloseAction(model));
		actionsMap.put(CANCEL_BUTTON, new CancelAction(model));
		actionsMap.put(RETRY_BUTTON, new RetryImportAction(model));
		actionsMap.put(GROUP_BUTTON, new PersonalManagementAction(model));
		actionsMap.put(EXIT, new ExitAction(model));
		actionsMap.put(LOG_OFF, new LogOffAction(model));
	}
	
	/** 
	 * Creates the windowsMenuItems. 
	 * 
	 * @param menu The menu to handle.
	 */
	private void createWindowsMenuItems(JMenu menu)
	{
		menu.removeAll();
		menu.add(new ActivateAction(model));
	}
	
	/** Attaches listener to the window listener. */
	private void attachListeners()
	{
		if (UIUtilities.isMacOS() && model.isMaster()) {
			try {
				MacOSMenuHandler handler = new MacOSMenuHandler(view);
				handler.initialize();
				view.addPropertyChangeListener(this);
			} catch (Throwable e) {}
        }
		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { model.close(); }
		});
		JMenu menu = ImporterFactory.getWindowMenu();
		menu.addMenuListener(new MenuListener() {

			public void menuSelected(MenuEvent e)
			{ 
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuListener#menuCanceled(MenuEvent)
			 */ 
			public void menuCanceled(MenuEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuListener#menuDeselected(MenuEvent)
			 */ 
			public void menuDeselected(MenuEvent e) {}

		});

		//Listen to keyboard selection
		menu.addMenuKeyListener(new MenuKeyListener() {

			public void menuKeyReleased(MenuKeyEvent e)
			{
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
			 */
			public void menuKeyPressed(MenuKeyEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-operation implementation.
			 * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
			 */
			public void menuKeyTyped(MenuKeyEvent e) {}

		});
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(FSImportUI) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link Importer} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	ImporterControl(Importer model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
	}
	
	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(ImporterUI view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		createActions();
		attachListeners();
		ImporterFactory.attachWindowMenuToTaskBar();
	}
	
	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	ImporterAction getAction(Integer id) { return actionsMap.get(id); }

	
	/**
	 * Submits the files that failed to import. 
	 * 
	 * @param fc The component to handle or <code>null</code>.
	 */
	void submitFiles(FileImportComponent fc)
	{
		List<FileImportComponent> list;
		if (fc != null) {
			list = new ArrayList<FileImportComponent>();
			list.add(fc);
		} else {
			list = view.getMarkedFiles();
		}
		
		markedFailed = list;
		//Now prepare the list of object to send.
		Iterator<FileImportComponent> i = list.iterator();
		ImportErrorObject object;
		List<ImportErrorObject> toSubmit = new ArrayList<ImportErrorObject>();
		while (i.hasNext()) {
			fc = i.next();
			object = fc.getImportErrorObject();
			if (object != null)
				toSubmit.add(object);
		}
		ExperimenterData exp = ImporterAgent.getUserDetails();
		String email = exp.getEmail();
		if (email == null) email = "";
		if (CollectionUtils.isEmpty(toSubmit)) return;
		//Check reader used.
        Iterator<ImportErrorObject> j = toSubmit.iterator();
        boolean plate = false;
        while (j.hasNext()) {
            object = j.next();
            Boolean b = object.isHCS();
            if (b != null && b.booleanValue()) {
                plate = true;
                break;
            }
        }
        if (plate) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("To submit HCS data, please e-mail us directly at ");
            String address = (String) ImporterAgent.getRegistry().lookup(
                    LookupNames.DEBUGGER_ADDRESS);
            buffer.append(address);
            buffer.append("\n");
            buffer.append("Do you still wish to report the error?");
            MessageBox box = new MessageBox(view, "Submit Error", buffer.toString());
            if (box.centerMsgBox() == MessageBox.NO_OPTION) return;
            //only submit error and log
            j = toSubmit.iterator();
            while (j.hasNext()) {
                j.next().resetFile();
            }
        }
		UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
		un.notifyError("Import Failures", "Files that failed to import", email, 
				toSubmit, this);
	}

	
	/**
	 * Returns the list of group the user is a member of.
	 * 
	 * @return See above.
	 */
	List<GroupSelectionAction> getUserGroupAction()
	{
		List<GroupSelectionAction> l = new ArrayList<GroupSelectionAction>();
		Collection m = ImporterAgent.getAvailableUserGroups();
		if (m == null || m.size() == 0) return l;
		ViewerSorter sorter = new ViewerSorter();
		Iterator i = sorter.sort(m).iterator();
		GroupData group;
		GroupSelectionAction action;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			l.add(new GroupSelectionAction(model, group));
		}
		return l;
	}
	
	/**
	 * Returns <code>true</code> if the agent is the entry point
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMaster() { return view.isMaster(); }

    /**
     * Disable the Cancel All button if there are no cancellable imports.
     */
    private void checkDisableCancelAllButtons() {
        final ImporterAction cancelAction = actionsMap.get(CANCEL_BUTTON);
        if (!cancelAction.isEnabled()) {
            return;
        }
        for (final ImporterUIElement importerUIElement : view.getImportElements()) {
        	if (importerUIElement.hasImportToCancel()) {
                return;
            }
        }
        cancelAction.setEnabled(false);
	}

        /**
         * Reacts to property changes.
         * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
         */
        public void propertyChange(final PropertyChangeEvent evt) {
            if (EventQueue.isDispatchThread()) {
                // only handle this event directly if we are in the EDT!
                handlePropertyChangedEvent(evt);
            } else {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        handlePropertyChangedEvent(evt);
                    }
                };
                SwingUtilities.invokeLater(run);
            }
        }
    
        /**
         * Handles a PropertyChangedEvent
         * @param evt The event
         */
        private void handlePropertyChangedEvent(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (ImportDialog.IMPORT_PROPERTY.equals(name)) {
                actionsMap.get(CANCEL_BUTTON).setEnabled(true);
                model.importData((ImportableObject) evt.getNewValue());
            } else if (ImportDialog.LOAD_TAGS_PROPERTY.equals(name)) {
                    model.loadExistingTags();
            } else if (ImportDialog.CANCEL_SELECTION_PROPERTY.equals(name)) {
                    model.close();
            } else if (ClosableTabbedPane.CLOSE_TAB_PROPERTY.equals(name)) {
                    model.removeImportElement(evt.getNewValue());
            } else if (FileImportComponent.SUBMIT_ERROR_PROPERTY.equals(name)) {
                    submitFiles((FileImportComponent) evt.getNewValue());
            } else if (ImportDialog.REFRESH_LOCATION_PROPERTY.equals(name)) {
                    model.refreshContainers((ImportLocationDetails) evt.getNewValue());
            } else if (ImportDialog.CREATE_OBJECT_PROPERTY.equals(name)) {
                    ObjectToCreate l = (ObjectToCreate) evt.getNewValue();
                    model.createDataObject(l);
            } else if (StatusLabel.DEBUG_TEXT_PROPERTY.equals(name)) {
                    view.appendDebugText((String) evt.getNewValue());
            } else if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(name)) {
                    Action a = getAction(EXIT);
                    ActionEvent event = 
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
                    a.actionPerformed(event);
            } else if (ImportDialog.PROPERTY_GROUP_CHANGED.equals(name)) {
                    GroupData newGroup = (GroupData) evt.getNewValue();
                    model.setUserGroup(newGroup);
            } else if (StatusLabel.FILE_IMPORT_STARTED_PROPERTY.equals(name) ||
                    FileImportComponent.CANCEL_IMPORT_PROPERTY.equals(name)) {
                checkDisableCancelAllButtons();
            } else if (StatusLabel.IMPORT_DONE_PROPERTY.equals(name)) {
                    model.onImportComplete((FileImportComponent) evt.getNewValue());
            } else if (StatusLabel.UPLOAD_DONE_PROPERTY.equals(name)) {
                    model.onUploadComplete((FileImportComponent) evt.getNewValue());
            }
        }

	/** 
	 * Re-uploads the file.
	 * 
	 * @param fc The file to upload.
	 */
	void retryUpload(FileImportComponent fc)
	{
		model.retryUpload(fc);
	}
	
	/** 
	 * Re-uploads the file.
	 * 
	 * @param fc The file to upload.
	 */
	void cancel(FileImportComponent fc)
	{
		model.onUploadComplete(fc);
	}
	
	/**
	 * Handles group selection.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		if (index == GROUP_BUTTON) {
			JComboBox box = (JComboBox) e.getSource();
			Object ho = box.getSelectedItem();
			if (ho instanceof JComboBoxImageObject) {
				JComboBoxImageObject o = (JComboBoxImageObject) ho;
				if (o.getData() instanceof GroupData) {
					model.setUserGroup((GroupData) o.getData());
				}
			}
		}
	}
	
}
