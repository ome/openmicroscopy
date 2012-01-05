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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
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
class EditorUI 
	extends JPanel
{
	
	/** Identifies the collection of data to add. */
	static final int	TO_ADD = 0;
	
	/** Identifies the collection of data to remove. */
	static final int 	TO_REMOVE = 1;
	
	/** Identifies the collection of data to delete. */
	static final int 	TO_DELETE = 2;
	
	/** Identifies the general component of the tab pane. */
	static final int	GENERAL_INDEX = 0;
	
	/** Identifies the acquisition component of the tab pane. */
	static final int	ACQUISITION_INDEX = 1;
	
	/** Identifies the rendering component of the tab pane. */
	static final int	RND_INDEX = 2;
	
	/** The name of the tab pane. */
	private static final String			RENDERER_NAME = "Preview";
	
	/** The description of the tab pane. */
	private static final String			RENDERER_DESCRIPTION = 
		"Preview the image";
	
	/** The name of the tab pane. */
	private static final String			RENDERER_NAME_SPECIFIC = "Settings";
	
	/** The description of the tab pane. */
	private static final String			RENDERER_DESCRIPTION_SPECIFIC = 
		"Adjust the rendering settings";
	
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
	
	/** The UI component displaying the group's information. */
	private GroupProfile				groupUI;

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
    
    /** The tab pane hosting the group's information. */
    private JComponent					groupTabbedPane;
    
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
		if (init) {
			if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC) {
				tabPane.addTab(RENDERER_NAME_SPECIFIC, null, dummyPanel, 
						RENDERER_DESCRIPTION_SPECIFIC);
			} else {
				tabPane.addTab(RENDERER_NAME, null, dummyPanel, 
						RENDERER_DESCRIPTION);
			}
		}	
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		dummyPanel = new JPanel();
		groupUI = new GroupProfile(model);
		groupUI.addPropertyChangeListener(controller);
		userUI = new UserUI(model, controller);
		toolBar = new ToolBar(model, controller);
		generalPane = new GeneralPaneUI(this, model, controller, toolBar);
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
		groupTabbedPane = new JScrollPane(groupUI);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		//add(toolBar, BorderLayout.NORTH);
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
    	boolean add = true;
    	if (uo instanceof ExperimenterData)  {
			//if (current.getId() == exp.getId()) {
    		toolBar.buildUI();
    		userUI.buildUI();
    		userUI.repaint();
    		component = userTabbedPane; 
			//} else add = false;
    	} else if (uo instanceof GroupData) {
    		toolBar.buildUI();
    		groupUI.buildUI();
    		groupUI.repaint();
    		component = groupTabbedPane; 
    	} else if (!(uo instanceof DataObject)) {	
    		toolBar.buildUI();
    		component = defaultPane;
    	} else {
        	toolBar.buildUI();
        	toolBar.setControls();
        	generalPane.layoutUI();
        	acquisitionPane.layoutCompanionFiles();
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
    	userUI.setParentRootObject();
    }
    
    /** Updates display when the new root node is set. */
	void setRootObject()
	{
		Object uo = model.getRefObject();
		tabPane.setComponentAt(RND_INDEX, dummyPanel);
		setDataToSave(false);
		toolBar.setRootObject();
		toolBar.buildUI();
		tabPane.setToolTipTextAt(RND_INDEX, "");
		boolean preview = false;
		int selected = getSelectedTab();
		if (!(uo instanceof DataObject)) {
			//saved = false;
			setDataToSave(false);
			toolBar.setStatus(false);
			toolBar.buildUI();
			remove(component);
			component = defaultPane;
			add(component, BorderLayout.CENTER);
			revalidate();
	    	repaint();
		} else if (uo instanceof ExperimenterData) {
			userUI.clearData();
			toolBar.setStatus(false);
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
					ImageData img = (ImageData) uo;
					tabPane.setEnabledAt(ACQUISITION_INDEX, img.getId() > 0);
					preview = model.isPreviewAvailable();
					tabPane.setEnabledAt(RND_INDEX, preview);
					if (!preview) {
						tabPane.setToolTipTextAt(RND_INDEX, 
								"Only available for image of size <= "+
								RenderingControl.MAX_SIZE+"x"+
								RenderingControl.MAX_SIZE);
					}
					
					if (selected == RND_INDEX) {
						tabPane.setComponentAt(RND_INDEX, dummyPanel);
						//tabPane.setSelectedIndex(GENERAL_INDEX);
						if (!preview && 
								model.getRndIndex() != 
									MetadataViewer.RND_SPECIFIC) 
							tabPane.setSelectedIndex(GENERAL_INDEX);
					}
				} else if (uo instanceof WellSampleData) {
					ImageData img = ((WellSampleData) uo).getImage();
					if (tabPane.getSelectedIndex() == RND_INDEX) {
						tabPane.setComponentAt(RND_INDEX, dummyPanel);
						if (model.isWritable())
							tabPane.setSelectedIndex(GENERAL_INDEX);
					}
					if (img != null && img.getId() >= 0) {
						load = true;
						tabPane.setEnabledAt(ACQUISITION_INDEX, true);
						preview = model.isPreviewAvailable();
						tabPane.setEnabledAt(RND_INDEX, preview);
						if (!preview) {
							tabPane.setToolTipTextAt(RND_INDEX, 
									"Only available for image of size <= "+
									RenderingControl.MAX_SIZE+"x"+
									RenderingControl.MAX_SIZE);
						}
						if (selected == RND_INDEX) {
							tabPane.setComponentAt(RND_INDEX, dummyPanel);
							//tabPane.setSelectedIndex(GENERAL_INDEX);
							if (!preview) tabPane.setSelectedIndex(GENERAL_INDEX);
						}
					} else {
						tabPane.setSelectedIndex(GENERAL_INDEX);
						tabPane.setEnabledAt(ACQUISITION_INDEX, false);
						tabPane.setEnabledAt(RND_INDEX, false);
					}
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
	
    /** 
     * Save data. 
     * 
     * @param asynch Pass <code>true</code> to save data asynchronously,
     * 				 <code>false</code> otherwise.
     */
	void saveData(boolean async)
	{
		saved = true;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		toolBar.setDataToSave(false);
		if (model.getRefObject() instanceof ExperimenterData) {
			Object exp = userUI.getExperimenterToSave();
			model.fireAdminSaving(exp, async);
			return;
		} else if  (model.getRefObject() instanceof GroupData) {
			AdminObject o = groupUI.getAdminObject();
			if (o == null) {
				saved = false;
				setCursor(Cursor.getDefaultCursor());
				toolBar.setDataToSave(true);
				return;
			}
			model.fireAdminSaving(o, async);
			return;
		}
		Map<Integer, List<AnnotationData>> m = generalPane.prepareDataToSave();
		List<AnnotationData> toAdd = m.get(TO_ADD);
		List<AnnotationData> toRemove = m.get(TO_REMOVE);
		List<Object> metadata = null;
		Object refObject = model.getRefObject();
		if (refObject instanceof ImageData)
			metadata = acquisitionPane.prepareDataToSave();

		model.fireAnnotationSaving(toAdd, toRemove, metadata, async);
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
		else if (ref instanceof GroupData)
			return groupUI.hasDataToSave();
		boolean b = generalPane.hasDataToSave();
		if (b) return b;
		//Check metadata.
		return acquisitionPane.hasDataToSave();
	}
    
	/** Clears data to save. */
	void clearData()
	{
		saved = false;
		userUI.clearData();
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
	 * @param quota The value to set.
	 */
	void setDiskSpace(DiskQuota quota) { userUI.setDiskSpace(quota); }

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
	 * Attaches the specified files.
	 * 
	 * @param files The files to attach.
	 * @return See above
	 */
	void attachFiles(File[] files)
	{
		if (files == null || files.length == 0) return;
		//Check if valid file
		//file w/o extension
		/*
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
		*/
		//if (generalPane.attachFile(file))
		generalPane.attachFiles(files);
		saveData(true);
	}
	
	/**
	 * Removes the object.
	 * 
	 * @param data The data to remove.
	 */
	void removeObject(DataObject data)
	{
		if (data == null) return;
		if (data instanceof TagAnnotationData || 
			data instanceof TextualAnnotationData) {
			generalPane.removeObject(data);
			if (data.getId() >= 0)
				saveData(true);
		}
	}
	
	/**
	 * Removes the tags.
	 * 
	 * @param location The location of the mouse pressed.
	 */
	void removeTags(Point location)
	{
		
		if (!generalPane.hasTagsToUnlink()) return;
		MessageBox box = new MessageBox(model.getRefFrame(),
				"Remove All Tags", 
				"Are you sure you want to remove all Tags?");
		Dimension d = box.getPreferredSize();
		Point p = new Point(location.x-d.width/2, location.y);
		if (box.showMsgBox(p) == MessageBox.YES_OPTION) {
			List<TagAnnotationData> list = generalPane.removeTags();
			if (list.size() > 0) saveData(true);
		}
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
		//if (TagAnnotationData.class.equals(type))
			saveData(true);	
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
		if (file instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) file;
			if (fa.getId() >= 0) saveData(true);	
		}
	}

	/**
	 * Returns the collection of attachments.
	 * 
	 * @param location The location of the mouse pressed.
	 */
	void removeAttachedFiles(Point location)
	{
		if (!generalPane.hasAttachmentsToUnlink()) return;
		MessageBox box = new MessageBox(model.getRefFrame(),
				"Remove All Attachments", 
				"Are you sure you want to remove all Attachments?");
		Dimension d = box.getPreferredSize();
		Point p = new Point(location.x-d.width/2, location.y);
		if (box.showMsgBox(p) == MessageBox.YES_OPTION) {
			List<FileAnnotationData> list = generalPane.removeAttachedFiles();
			if (list.size() > 0) saveData(true);
		}
	}
	
	/**
	 * Adds the annotation to the collection of objects to be deleted.
	 * 
	 * @param annotation The value to add.
	 */
	void deleteAnnotation(AnnotationData annotation)
	{
		model.deleteAnnotation(annotation);
	}
	
	/**
	 * Starts an asynchronous call to delete the annotations.
	 * 
	 * @param annotations The annotations to delete.
	 */
	void fireAnnotationsDeletion(List<FileAnnotationData> annotations)
	{
		if (annotations == null || annotations.size() == 0) return;
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		l.addAll(annotations);
		model.fireAnnotationsDeletion(l);
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
		if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC) {
			tabPane.addTab(RENDERER_NAME_SPECIFIC, null, 
					new JScrollPane(model.getRenderer().getUI()), 
					RENDERER_DESCRIPTION_SPECIFIC);
		} else {
			tabPane.addTab(RENDERER_NAME, null, 
					new JScrollPane(model.getRenderer().getUI()), 
					RENDERER_DESCRIPTION);
		}
		
		setSelectedTab(RND_INDEX);
		model.getRenderer().renderPreview();
	}
	
	/**
	 * Returns the rendering constants. 
	 * 
	 * @return See above.
	 */
	int getRndIndex() { return model.getRndIndex(); }
	
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

	/**
	 * Analyzes the data. 
	 * 
	 * @param index The index identifying the analysis to perform.
	 */
	void analyse(int index) { model.analyse(index); }

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

	/**
	 * Returns the object of reference.
	 * 
	 * @return See above.
	 */
	Object getRefObject() { return model.getRefObject(); }
	
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
	 * Brings up the activity options.
	 * 
	 * @param source   The source of the mouse pressed.
	 * @param location The location of the mouse pressed.
	 * @param index    Identifies the menu to pop up.
	 */
	void activityOptions(Component source, Point p, int index)
	{
		toolBar.launchOptions(source, p, index);
	}

	/**
	 * Creates a figure.
	 * 
	 * @param value The value containing the parameters for the figure.
	 */
	void createFigure(Object value) { model.createFigure(value); }
	
	/**
	 * Runs the passed script.
	 * 
	 * @param script The script to handle.
	 * @param index  Indicated to run, download or view.
	 */
	void manageScript(ScriptObject script, int index)
	{ 
		model.manageScript(script, index); 
	}
	
	/** 
	 * Discards the renderer. 
	 * 
	 * @param ref The object of reference.
	 */
	void discardRenderer(Object ref)
	{
		if (ref != model.getRefObject()) return;
		model.discardRenderer();
		clearData();
	}
	
	/** Refreshes the acquisition metadata. */
	void refreshAcquisition()
	{
		
	}
	
	/** Displays the scripts. */
	void setScripts() { toolBar.setScripts(); }

	/** 
	 * Sets the photo of the user.
	 * 
	 * @param photo The photo to set.
	 */
	void setUserPhoto(BufferedImage photo)
	{
		if (userUI != null) userUI.setUserPhoto(photo);
	}
	
	/** Notifies the parent to upload the script. */
    void uploadScript()
    {
    	model.uploadScript();
    }
	
    /** Reloads the scripts. */
    void reloadScript()
    {
    	model.setScripts(null);
    	model.loadScripts();
    	toolBar.setStatus(true);
    }
    
    /**
     * Returns the script corresponding to the specified name.
     * 
     * @return See above.
     */
    ScriptObject getScriptFromName(String name)
    { 
    	return model.getScriptFromName(name);
    }
    
	/**
	 * Returns <code>true</code> if it is an image with a lot of channels.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNumerousChannel() { return model.isNumerousChannel(); }
    
	void cancelAnalysisResultsLoading(AnalysisResultsItem item)
	{
		
	}
	
}
