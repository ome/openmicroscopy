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
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

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

	/** The Model sub-component. */
	private ImporterModel 	model;
	
	/** The Controller sub-component. */
	private ImporterControl 	controller;
	
	/** The View sub-component. */
	private ImporterUI 		view;
	
	/** Reference to the chooser used to select the files to import. */
	private ImportDialog	chooser;

	/**
	 * Imports the data for the specified import view.
	 * 
	 * @param element The import view. 
	 */
	private void importData(ImporterUIElement element)
	{
		if (element == null) return;
		view.setSelectedPane(element);
		element.startImport();
		model.fireImportData(element.getData(), element.getID());
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		bus.post(new ImportStatusEvent(true, element.getExistingContainers()));
		fireStateChange();
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
	}

	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(view);
		view.initialize(model, controller);
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
			chooser.pack();
		} else {
			chooser.reset(selectedContainer, objects, type);
			chooser.requestFocusInWindow();
		}
		view.setVisible(false);
		UIUtilities.centerAndShow(chooser);
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
				//now check if we have other import to start.
				element = view.getElementToStartImportFor();
				if (element != null) 
					importData(element);
				fireStateChange();
			}	
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
	 * @see Importer#removeImportElement(int)
	 */
	public void removeImportElement(int index)
	{
		if (model.getState() == DISCARDED) return;
		ImporterUIElement element = view.removeImportElement(index);
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
			//cancel all the on-going import.
			List<ImporterUIElement> list = view.getElementsWithDataToImport();
			if (list.size() > 0) {
				Iterator<ImporterUIElement> i = list.iterator();
				ImporterUIElement element;
				while (i.hasNext()) {
					element = i.next();
					element.cancelLoading();
					model.cancel(element.getID());
				}
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
	
}
