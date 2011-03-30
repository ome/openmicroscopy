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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
import org.openmicroscopy.shoola.agents.fsimporter.actions.ImporterAction;
import org.openmicroscopy.shoola.agents.fsimporter.actions.SubmitFilesAction;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;
import pojos.ExperimenterData;

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
	}
	
	/** 
	 * Creates the windowsMenuItems. 
	 * 
	 * @param menu The menu to handle.
	 */
	private void createWindowsMenuItems(JMenu menu)
	{
		/*
		menu.removeAll();
		Importer viewer = ImporterFactory.getImporter();
		menu.add(new JMenuItem(new ActivateAction(viewer)));
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
		un.notifyError("Import Failures", "Files that failed to import", email, 
				toSubmit, this);
	}
	
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
		} else if (MessengerDialog.SEND_PROPERTY.equals(name)) {
			//mark the files.
			if (markedFailed == null) return;
			Iterator<FileImportComponent> i = markedFailed.iterator();
			FileImportComponent fc;
			while (i.hasNext()) {
				fc = i.next();
				fc.markAsSent();
			}
			getAction(SEND_BUTTON).setEnabled(model.hasFailuresToSend());
			markedFailed = null;
		} else if (ClosableTabbedPane.CLOSE_TAB_PROPERTY.equals(name)) {
			int index = (Integer) evt.getNewValue();
			model.removeImportElement(index);
		} else if (FileImportComponent.SUBMIT_ERROR_PROPERTY.equals(name)) {
			getAction(SEND_BUTTON).setEnabled(model.hasFailuresToSend());
		} 
	}
	
}
