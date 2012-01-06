/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterControl 
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

//Third-party libraries

//Application-internal dependencies
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
import org.openmicroscopy.shoola.agents.fsimporter.util.ErrorDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
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
	implements PropertyChangeListener
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
		/*
		menu.removeAll();
		Collection<ImporterUIElement> elements = view.getImportElements();
		if (elements == null || elements.size() == 0) return;
		Iterator<ImporterUIElement> i = elements.iterator();
		ImporterUIElement e;
		ActivateAction a;
		while (i.hasNext()) {
			e = i.next();
			a = new ActivateAction(model, e.getName(), e.getImportIcon(),
					e.getID());
			menu.add(new JMenuItem(a));
		}
		*/
	}
	
	/** Attaches listener to the window listener. */
	private void attachListeners()
	{
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

	
	/** Submits the files that failed to import. */
	void submitFiles()
	{
		List<FileImportComponent> list = view.getMarkedFiles();
		UserNotifier un = ImporterAgent.getRegistry().getUserNotifier();
		if (list == null || list.size() == 0) {
			un.notifyInfo("Import Failures", "No files to submit.");
			return;
		}
		markedFailed = list;
		//Now prepare the list of object to send.
		Iterator<FileImportComponent> i = list.iterator();
		FileImportComponent fc;
		List<ImportErrorObject> toSubmit = new ArrayList<ImportErrorObject>();
		ImportErrorObject object;
		while (i.hasNext()) {
			fc = i.next();
			object = fc.getImportErrorObject();
			if (object != null)
				toSubmit.add(object);
		}
		ExperimenterData exp = ImporterAgent.getUserDetails();
		String email = exp.getEmail();
		if (email == null) email = "";
		//Get log File
		File f = new File(ImporterAgent.getRegistry().getLogger().getLogFile());
		object = new ImportErrorObject(f, null);
		toSubmit.add(object);
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
		Set m = ImporterAgent.getAvailableUserGroups();
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
	 * Reacts to property changes.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ImportDialog.IMPORT_PROPERTY.equals(name)) {
			model.importData((ImportableObject) evt.getNewValue());
		} else if (ImportDialog.LOAD_TAGS_PROPERTY.equals(name)) {
			model.loadExistingTags();
		} else if (ImportDialog.CANCEL_SELECTION_PROPERTY.equals(name)) {
			model.close();
		} else if (ImportDialog.CANCEL_ALL_IMPORT_PROPERTY.equals(name)) {
			model.cancelAllImports();
		} else if (MessengerDialog.SEND_PROPERTY.equals(name)) {
			//mark the files.
			if (markedFailed == null) return;
			Iterator<FileImportComponent> i = markedFailed.iterator();
			while (i.hasNext())
				i.next().markAsSent();
			getAction(SEND_BUTTON).setEnabled(model.hasFailuresToSend());
			markedFailed = null;
		} else if (ClosableTabbedPane.CLOSE_TAB_PROPERTY.equals(name)) {
			model.removeImportElement(evt.getNewValue());
		} else if (FileImportComponent.SUBMIT_ERROR_PROPERTY.equals(name)) {
			//getAction(SEND_BUTTON).setEnabled(model.hasFailuresToSend());
			getAction(SEND_BUTTON).setEnabled(view.hasSelectedFailuresToSend());
			getAction(RETRY_BUTTON).setEnabled(view.hasFailuresToReimport());
		} else if (FileImportComponent.DISPLAY_ERROR_PROPERTY.equals(name)) {
			ErrorDialog d = new ErrorDialog(view, 
					(Throwable) evt.getNewValue());
			UIUtilities.centerAndShow(d);
		} else if (FileImportComponent.CANCEL_IMPORT_PROPERTY.equals(name)) {
			
		} else if (ImportDialog.REFRESH_LOCATION_PROPERTY.equals(name)) {
			Integer value = (Integer) evt.getNewValue();
			int v = Importer.PROJECT_TYPE;
			if (value != null) v = value.intValue();
			model.refreshContainers(v);
		} else if (ImportDialog.CREATE_OBJECT_PROPERTY.equals(name)) {
			List<DataObject> l = (List<DataObject>) evt.getNewValue();
			if (l == null) return;
			switch (l.size()) {
				case 1:
					model.createDataObject(l.get(0), null);
					break;
				case 2:
					model.createDataObject(l.get(0), l.get(1));
			}
		} else if (StatusLabel.DEBUG_TEXT_PROPERTY.equals(name)) {
			view.appendDebugText((String) evt.getNewValue());
		}
	}
	
}
