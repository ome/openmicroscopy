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
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellSampleData;

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
	
	/** Identifies the collection of data to add. */
	static final int	TO_ADD = 0;
	
	/** Identifies the collection of data to remove. */
	static final int 	TO_REMOVE = 1;
	
	/** Identifies the general component of the tab pane. */
	static final int	GENERAL_INDEX = 0;
	
	/** Identifies the acquisition component of the tab pane. */
	static final int	ACQUISITION_INDEX = 1;
	
	/** Identifies the rendering component of the tab pane. */
	static final int	RND_INDEX = 2;
	
	/** The name of the tab pane. */
	private static final String			RENDERER_NAME = "Renderer";
	
	/** The description of the tab pane. */
	private static final String			RENDERER_DESCRIPTION = 
		"Renderer Control";
	
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
	
    /** The tab pane hosting the metadata. */
    private JTabbedPane					tabPane;
    
    /** The tab pane hosting the user's information. */
    private JComponent					userTabbedPane;
    
    /** The component currently displayed.. */
    private JComponent					component;
    
    /** The default component. */
    private JPanel						defaultPane;
    
    /** The dummy panel displayed instead of the rendering component. */
    private JPanel						dummyPanel;
    
    /** Adds the renderer to the tab pane. 
     * 
     * @param init 	Pass <code>true</code> if it is invoked at initialization
     * 				time, <code>false</code> otherwise.
     */
	private void populateTabbedPane(boolean init)
	{
		tabPane.addTab("General", null, generalPane, "General Information.");
		tabPane.addTab("Acquisition", null, new JScrollPane(acquisitionPane), 
			"Acquisition Metadata.");
		if (init) tabPane.addTab(RENDERER_NAME, null, dummyPanel, 
				RENDERER_DESCRIPTION);
			
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		dummyPanel = new JPanel();
		userUI = new UserUI(model, controller);
		toolBar = new ToolBar(model, controller);
		generalPane = new GeneralPaneUI(this, model, controller);
		acquisitionPane = new AcquisitionDataUI(this, model, controller);
		tabPane = new JTabbedPane();
		tabPane.addChangeListener(controller);
		tabPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		populateTabbedPane(true);
		tabPane.setEnabledAt(ACQUISITION_INDEX, false);
		defaultPane = new JPanel();
		defaultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		component = defaultPane;
		userTabbedPane = new JScrollPane(userUI);
		//userTabbedPane.add(userUI);
		//userTabbedPane = new JTabbedPane();
		//userTabbedPane.addTab("Profile", null, new JScrollPane(userUI),
		//		"User's details.");
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
    	boolean add = true;
    	if (uo instanceof ExperimenterData)  {
    		ExperimenterData exp = (ExperimenterData) uo;
			ExperimenterData current = MetadataViewerAgent.getUserDetails();
			if (current.getId() == exp.getId()) {
				toolBar.buildUI();
	    		userUI.buildUI();
	    		userUI.repaint();
	    		component = userTabbedPane; 
			} else add = false;
    		
    	} else if (!(uo instanceof DataObject)) {	
    		toolBar.buildUI();
    		component = defaultPane;
    	} else {
        	toolBar.buildUI();
        	toolBar.setControls();
        	generalPane.layoutUI();
        	component = tabPane;
    	}
    	if (add) add(component, BorderLayout.CENTER);
    	validate();
    	repaint();
    }
    
    /** Updates display when the parent of the root node is set. */
    void setParentRootObject()
    {
    	generalPane.setParentRootObject();
    }
    
    /** Updates display when the new root node is set. */
	void setRootObject()
	{
		Object uo = model.getRefObject();
		tabPane.setComponentAt(RND_INDEX, dummyPanel);
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
				tabPane.setSelectedIndex(GENERAL_INDEX);
				tabPane.setEnabledAt(ACQUISITION_INDEX, false);
				tabPane.setEnabledAt(RND_INDEX, false);
			} else {
				if (uo instanceof ImageData) {
					load = true;
					tabPane.setEnabledAt(ACQUISITION_INDEX, true);
					tabPane.setEnabledAt(RND_INDEX, true);
					if (tabPane.getSelectedIndex() == RND_INDEX) {
						tabPane.setComponentAt(RND_INDEX, dummyPanel);
						tabPane.setSelectedIndex(GENERAL_INDEX);
					}
					/*
					if (model.getRndIndex() == MetadataViewer.RND_GENERAL) {
						tabPane.setEnabledAt(ACQUISITION_INDEX, true);
						if (tabPane.getComponentCount() > 2) {
							boolean b = 
								tabPane.getSelectedIndex() == RND_INDEX;
							tabPane.remove(RND_INDEX);
							if (b) tabPane.setSelectedIndex(GENERAL_INDEX);
						}
					}
					*/
				} else if (uo instanceof WellSampleData) {
					ImageData img = ((WellSampleData) uo).getImage();
					if (tabPane.getSelectedIndex() == RND_INDEX) {
						tabPane.setComponentAt(RND_INDEX, dummyPanel);
						tabPane.setSelectedIndex(GENERAL_INDEX);
					}
					if (img != null && img.getId() >= 0) {
						load = true;
						tabPane.setEnabledAt(ACQUISITION_INDEX, true);
						tabPane.setEnabledAt(RND_INDEX, true);
					} else {
						tabPane.setSelectedIndex(GENERAL_INDEX);
						tabPane.setEnabledAt(ACQUISITION_INDEX, false);
						tabPane.setEnabledAt(RND_INDEX, false);
					}
					/*
					if (tabPane.getComponentCount() > 2) {
						boolean b = 
							tabPane.getSelectedIndex() == RND_INDEX;
						tabPane.remove(RND_INDEX);
						if (b) tabPane.setSelectedIndex(GENERAL_INDEX);
					}
					*/
				} else {
					tabPane.setSelectedIndex(GENERAL_INDEX);
					tabPane.setEnabledAt(ACQUISITION_INDEX, false);
					tabPane.setEnabledAt(RND_INDEX, false);
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
		Map<Integer, List<AnnotationData>> m = generalPane.prepareDataToSave();
		List<AnnotationData> toAdd = m.get(TO_ADD);
		List<AnnotationData> toRemove = m.get(TO_REMOVE);
		List<Object> metadata = null;
		Object refObject = model.getRefObject();
		if (refObject instanceof ImageData)
			metadata = acquisitionPane.prepareDataToSave();

		model.fireAnnotationSaving(toAdd, toRemove, metadata);
	}

	/** Lays out the thumbnails. */
	void setThumbnails() { generalPane.setThumbnails(); }

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
	
	/** Shows the image's info. */
    void showChannelData()
    { 
    	generalPane.setChannelData();
    	acquisitionPane.setChannelData();
    }

    /**
     * Displays the movie components depending on the passed value.
     * 
     * @param b Pass <code>true</code> if movie creation,
     * 			<code>false</code> when it is done.
     */
    void createMovie(boolean b) { toolBar.createMovie(b); }
    
    /**
     * displays the analyze components depending on the passed value.
     * 
     * @param b Pass <code>true</code> if movie creation,
     * 			<code>false</code> when it is done.
     */
    void analyse(boolean b) { toolBar.analyse(b); }
    
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
		tabPane.setComponentAt(RND_INDEX, dummyPanel);
		tabPane.repaint();
		setCursor(Cursor.getDefaultCursor());
	}
	
	/** Clears the password fields. */
	void passwordChanged() { userUI.passwordChanged(); }
 
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
	 * Shows or hides the component indicating the progress.
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
	
	/** 
	 * Attaches the specified file.
	 * 
	 * @param file The file to attach.
	 */
	void attachFile(File file)
	{
		if (file == null) return;
		//Check if valid file
		//file w/o extension
		String name = file.getName();
		int dot = name.lastIndexOf(".")+1;
		String extension = name.substring(dot);
		if (extension == null ||extension.trim().length() == 0 || 
			extension.equals(name)) {
			UserNotifier un = 
				MetadataViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Attachment Selection", "The selected file " +
					"has no extension. It is not possible to upload it.");
			return;
		}
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
		//if (model.isMultiSelection()) {
		if (TagAnnotationData.class.equals(type))
			saveData();
		//}	
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

	/**
	 * Sets the plane info for the specified channel.
	 * 
	 * @param index  The index of the channel.
	 */
	void setPlaneInfo(int index)
	{
		acquisitionPane.setPlaneInfo(index);
	}
	
	/**
	 * Returns <code>true</code> if the display is for a single 
	 * object, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleMode() { return model.isSingleMode(); }
	
	/** Posts an event to create a new experiment. */
	void createNewExperiment()
	{
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		String name = model.getObjectPath();
		Object object = model.getRefObject();
		if ((object instanceof ProjectData) || 
				(object instanceof DatasetData) ||
				(object instanceof ImageData) || 
				(object instanceof ScreenData) ||
				(object instanceof PlateData)) {
			if (name != null && name.trim().length() > 0) {
				name += ShowEditorEvent.EXPERIMENT_EXTENSION;
				ShowEditorEvent event = new ShowEditorEvent(
						(DataObject) object, name, 
						ShowEditorEvent.EXPERIMENT);
				bus.post(event);
			}
		}
	}

	/**
	 * Brings up the dialog to create a movie.
	 * 
	 * @param scaleBar 	   The value of the scale bar. 
	 * 					   If not greater than <code>0</code>, the value is not 
	 * 					   taken into account.
	 * @param overlayColor The color of the scale bar and text. 
	 */
	void makeMovie(int scaleBar, Color overlayColor)
	{
		model.makeMovie(scaleBar, overlayColor);
	}
	
	/** Sets the renderer. */
	void setRenderer()
	{
		tabPane.removeAll();
		
		populateTabbedPane(false);
		tabPane.addTab(RENDERER_NAME, null, 
				new JScrollPane(model.getRenderer().getUI()), 
				RENDERER_DESCRIPTION);
		setSelectedTab(RND_INDEX);
		/*
		if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC) {
			tabPane.removeAll();
			tabPane.addTab("Renderer", null, 
					new JScrollPane(model.getRenderer().getUI()), 
			"Rendering Control.");
			populateTabbedPane();
		} else {
			if (tabPane.getComponentCount() == 2) {
				tabPane.addTab("Renderer", null, 
						new JScrollPane(model.getRenderer().getUI()), 
				"Rendering Control.");
			}
			tabPane.setSelectedIndex(RND_INDEX);
		}
		*/
		
	}
	
	/**
	 * Sets the selected tab.
	 * 
	 * @param index The index of the tab to select.
	 */
	void setSelectedTab(int index)
	{
		tabPane.removeChangeListener(controller);
		switch (index) {
			case GENERAL_INDEX:
			case ACQUISITION_INDEX:
			case RND_INDEX:
				tabPane.setSelectedIndex(index);
		}
		tabPane.addChangeListener(controller);
	}

	/** Analyzes the data. */
	void analyse() { model.analyse(); }

	/** Sets the instrument and its components. */
	void setInstrumentData() { acquisitionPane.setInstrumentData(); }

	/**
	 * Returns the index of the selected tab.
	 * 
	 * @return See above.
	 */
	int getSelectedTab() { return tabPane.getSelectedIndex(); }

	/**
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelColorChanged(int index)
	{
		acquisitionPane.onChannelColorChanged(index);
	}

	/**
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() { return model.getRefObjectName(); }

	/** Cleans up the view or adds the components
	 * 
	 * @param cleanup 	Pass <code>true</code> to clean up, <code>false</code>
	 * 					to add the component.
	 */
	void onSettingsApplied(boolean cleanup)
	{ 
		if (cleanup) tabPane.setComponentAt(RND_INDEX, dummyPanel);
		else tabPane.setComponentAt(RND_INDEX, 
				new JScrollPane(model.getRenderer().getUI()));
	}

	/** 
	 * Discards the renderer. 
	 * 
	 * @param ref The object of reference.
	 */
	void discardRenderer(Object ref)
	{
		if (ref != model.getRefObject());
		model.discardRenderer();
		clearData();
	}
	
	/** Refreshes the acquisition metadata. */
	void refreshAcquisition()
	{
		acquisitionPane.refresh();
	}
	
}
