/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.TagAnnotationData;

/** 
 * Component hosting the various {@link AnnotationUI} entities.
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
public class EditorUI 
	extends JPanel
{
	
	/** Identifies the general component of the tabbed pane. */
	private static final int			GENERAL_INDEX = 0;
	
	/** Indetifies the acquisition component of the tabbed pane. */
	private static final int			ACQUISITION_INDEX = 1;
	
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
	/** The UI component displaying the general metadata. */
	private GeneralPaneUI				generalPane;
	
	/** The UI component displaying the acquisition metadata. */
	private AcquisitionDataUI			acquisitionPane;
	
	/** The UI component displaying the user's information. */
	private UserUI						userUI;

	/** The tool bar with various controls. */
	private ToolBar						toolBar;

    /** 
     * Flag indicating that the data has already been saved and no new changes.
     */
    private boolean						saved;
	
    /** The tabbed pane hosting the metadata. */
    private JTabbedPane					tabbedPane;
    
    /** The tabbed pane hosting the user's information. */
    private JTabbedPane					userTabbedPane;
    
    /** The component currently displayed.. */
    private JComponent					component;
    
    /** The default component. */
    private JPanel						defaultPane;
    
	/** Initializes the UI components. */
	private void initComponents()
	{
		userUI = new UserUI(model, controller);
		toolBar = new ToolBar(model, controller);
		generalPane = new GeneralPaneUI(this, model, controller);
		acquisitionPane = new AcquisitionDataUI(this, model, controller);
		tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		tabbedPane.addTab("General", null, generalPane, "General Information.");
		tabbedPane.addTab("Acquisition", null, new JScrollPane(acquisitionPane), 
			"Acquisition Metadata.");
		tabbedPane.setEnabledAt(ACQUISITION_INDEX, false);
		defaultPane = new JPanel();
		defaultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		component = defaultPane;
		userTabbedPane = new JTabbedPane();
		userTabbedPane.addTab("Profile", null, userUI, "User's details.");
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(toolBar, BorderLayout.NORTH);
		add(component, BorderLayout.CENTER);
	}
	
	/** Creates a new instance. */
	EditorUI() {}
	
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param model         Reference to the Model. 
     * 						Mustn't be <code>null</code>.
     * @param controller	Reference to the Controller.
     * 						Mustn't be <code>null</code>.
     */
    void initialize(EditorModel model, EditorControl controller)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /** Lays out the UI when data are loaded. */
    void layoutUI()
    {
    	Object uo = model.getRefObject();
    	remove(component);
    	setDataToSave(false);
    	toolBar.setStatus(false);
    	if (uo instanceof ExperimenterData)  {
    		toolBar.buildUI();
    		userUI.buildUI();
    		userUI.repaint();
    		component = userTabbedPane;
    	} else if (!(uo instanceof DataObject)) {	
    		toolBar.buildUI();
    		component = defaultPane;
    	} else {
        	toolBar.buildUI();
        	toolBar.setControls();
        	generalPane.layoutUI();
        	component = tabbedPane;
    	}
    	add(component, BorderLayout.CENTER);
    	validate();
    	repaint();
    }
    
    /** Updates display when the new root node is set. */
	void setRootObject()
	{
		Object uo = model.getRefObject();
		setDataToSave(false);
		toolBar.buildUI();
		toolBar.setStatus(false);
		if (!(uo instanceof DataObject)) {
			setDataToSave(false);
			toolBar.buildUI();
    		toolBar.setStatus(false);
			remove(component);
			component = defaultPane;
			add(component, BorderLayout.CENTER);
			revalidate();
	    	repaint();
		} else if (uo instanceof ExperimenterData) {
			layoutUI();
		} else {
			boolean load = false;
			if (model.isMultiSelection()) {
				tabbedPane.setSelectedIndex(GENERAL_INDEX);
				tabbedPane.setEnabledAt(ACQUISITION_INDEX, false);
			} else {
				if (uo instanceof ImageData) {
					load = true;
					tabbedPane.setEnabledAt(ACQUISITION_INDEX, true);
				} else {
					tabbedPane.setSelectedIndex(GENERAL_INDEX);
					tabbedPane.setEnabledAt(ACQUISITION_INDEX, false);
				}
				load = true;
			}
			generalPane.setRootObject();
			acquisitionPane.setRootObject(load);
		}
	}
	
    /** Save data. */
	void saveData()
	{
		saved = true;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		toolBar.setDataToSave(false);
		if (model.getRefObject() instanceof ExperimenterData) {
			ExperimenterData exp = userUI.getExperimenterToSave();
			model.fireDataObjectSaving(exp);
			return;
		}
		//if (!model.isMultiSelection()) propertiesUI.updateDataObject();
		//
		List<AnnotationData>[] array = generalPane.prepareDataToSave();
		List<AnnotationData> toAdd = array[0];
		List<AnnotationData> toRemove = array[1];
		List<Object> metadata = null;
		Object refObject = model.getRefObject();
		if (refObject instanceof ImageData) {
			metadata = acquisitionPane.prepareDataToSave();
		}
		
		model.fireAnnotationSaving(toAdd, toRemove, metadata);
	}

	/** Lays out the thumbnails. */
	void setThumbnails() { generalPane.setThumbnails(); }

	/** Sets the existing tags. */
	void setExistingTags()
	{
		generalPane.setExistingTags();
		revalidate();
    	repaint();
	}

	/**
	 * Returns the list of tags currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> getCurrentTagsSelection()
	{
		return generalPane.getCurrentTagsSelection();
	}
	
	/**
	 * Returns the list of attachments currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getCurrentAttachmentsSelection()
	{
		return generalPane.getCurrentAttachmentsSelection();
	}
	
	/**
	 * Returns the value of the autocomplete flag.
	 * 
	 * @return See above.
	 */
	boolean isAutoComplete() { return generalPane.isAutoComplete(); }
	
	/** Shows the image's info. */
    void showChannelData()
    { 
    	generalPane.setChannelData();
    	acquisitionPane.setChannelData();
    }

    /**
     * Enables the saving controls depending on the passed value.
     * 
     * @param b Pass <code>true</code> to save the data,
     * 			<code>false</code> otherwise.
     */
    void setDataToSave(boolean b) { toolBar.setDataToSave(b); }
    
    /**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		if (saved) return false;
		Object ref = model.getRefObject();
		if (!(ref instanceof DataObject)) return false;
		if (ref instanceof ExperimenterData)
			return userUI.hasDataToSave();
		boolean b = generalPane.hasDataToSave();
		if (b) return b;
		//Check metadata.
		return acquisitionPane.hasDataToSave();
	}
    
	/** Clears data to save. */
	void clearData()
	{
		saved = false;
		generalPane.clearData();
		setCursor(Cursor.getDefaultCursor());
	}
	
	/** Clears the password fields. */
	void passwordChanged() { userUI.passwordChanged(); }

	/** Displays the wizard with the collection of files already uploaded. */
	void setExistingAttachements()
	{ 
		generalPane.setExistingAttachments();
		revalidate();
		repaint();
	}

	/** Displays the wizard with the collection of URLs already uploaded. */
	void setExistingURLs() { }//linksUI.showSelectionWizard(); }
	 
	/**
	 * Sets the disk space information.
	 * 
	 * @param space The value to set.
	 */
	void setDiskSpace(List space) { userUI.setDiskSpace(space); }

	/**
	 * Handles the expansion or collapsing of the passed component.
	 * 
	 * @param source The component to handle.
	 */
	void handleTaskPaneCollapsed(JXTaskPane source)
	{
		generalPane.handleTaskPaneCollapsed(source);
	}

	/**
	 * Shows or hides the component indicating the progresss.
	 * 
	 * @param busy Pass <code>true</code> to show, <code>false</code> to hide.
	 */
	void setStatus(boolean busy) { toolBar.setStatus(busy); }

	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExistingTags() { return model.getExistingTags(); }

	void onTagsLoading(boolean wizard)
	{
		// TODO Auto-generated method stub
		
	}

	/** 
	 * Attaches the specified file.
	 * 
	 * @param file The file to attach.
	 */
	void attachFile(File file)
	{
		if (file == null) return;
		generalPane.attachFile(file);
	}

	/**
	 * Removes a tag from the view.
	 * 
	 * @param tag The tag to remove.
	 */
	void removeTag(TagAnnotationData tag)
	{
		if (tag == null) return;
		generalPane.removeTag(tag);
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type		The type of objects to handle.
	 * @param objects 	The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
		if (objects == null) return;
		generalPane.handleObjectsSelection(type, objects);
	}
	
	/** 
	 * Removes the attached file.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{
		if (file == null) return;
		generalPane.removeAttachedFile(file);
	}

	/** Sets the image acquisition metadata. */
	void setImageAcquisitionData()
	{
		acquisitionPane.setImageAcquisitionData();
	}

	/**
	 * Sets the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		acquisitionPane.setChannelAcquisitionData(index);
	}
	
}
