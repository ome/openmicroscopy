/*
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
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;

import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.ui.PermissionMenu;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FilesetData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellSampleData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * Component hosting the various {@link AnnotationUI} entities.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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

    /** The menu showing the option to remove tags.*/
    private PermissionMenu tagMenu;
    
    /** The menu showing the option to remove attachments.*/
    private PermissionMenu docMenu;
    
    /** The menu showing the option to remove the other annotation.*/
    private PermissionMenu otherAnnotationMenu;
    
    /**
     * Adds the renderer to the tab pane. 
     * 
     * @param init 	Pass <code>true</code> if it is invoked at initialization
     * 				time, <code>false</code> otherwise.
     */
	private void populateTabbedPane(boolean init)
	{
		addTab("General", generalPane, "General Information.");
		addTab("Acquisition", acquisitionPane, "Acquisition Metadata.");
		if (init) {
			if (model.getRndIndex() == MetadataViewer.RND_SPECIFIC) {
				addTab(RENDERER_NAME_SPECIFIC, dummyPanel, 
						RENDERER_DESCRIPTION_SPECIFIC);
			} else {
				addTab(RENDERER_NAME, dummyPanel, 
						RENDERER_DESCRIPTION);
			}
		}	
	}
	
	/**
	 * Adds a component to the tabPane wrapped inside a JScrollPane
	 * @param title The title of the tab
	 * @param comp The component
	 * @param desc The description of (i. e. tooltip for) the tap
	 */
	private void addTab(String title, Component comp, String desc) {
		JScrollPane sp = new JScrollPane(comp);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    tabPane.addTab(title, null, sp, desc);
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		dummyPanel = new JPanel();
		groupUI = new GroupProfile(model, this);
		groupUI.addPropertyChangeListener(controller);
		userUI = new UserUI(model, controller, this);
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
    	if (uo instanceof ExperimenterData)  {
    		toolBar.buildUI();
    		userUI.buildUI();
    		userUI.repaint();
    		component = userTabbedPane;
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
        	generalPane.layoutUI();
        	acquisitionPane.layoutCompanionFiles();
        	component = tabPane;
        	if (model.isMultiSelection()) {
				tabPane.setSelectedIndex(GENERAL_INDEX);
				tabPane.setEnabledAt(ACQUISITION_INDEX, false);
				tabPane.setEnabledAt(RND_INDEX, false);
			}
    	}
    	add(component, BorderLayout.CENTER);
    	validate();
    	repaint();
    }
    
    /** Updates display when the parent of the root node is set. */
    void setParentRootObject()
    {
    	generalPane.setParentRootObject();
    	userUI.setParentRootObject();
    }

    /** Resets the selected tab when an image or well sample is selected.*/
    void handleImageSelection()
    {
        ImageData img = model.getImage();
        if (img == null) return;
        boolean multi = model.isMultiSelection();
        boolean preview = model.isPreviewAvailable();
        tabPane.setEnabledAt(RND_INDEX, preview && !multi);
        tabPane.setEnabledAt(ACQUISITION_INDEX, !multi && img.getId() > 0);
        if (!preview) {
            tabPane.setToolTipTextAt(RND_INDEX, 
                    "Only available for non big images.");
        }
        
        if (getSelectedTab() == RND_INDEX) {
            tabPane.setComponentAt(RND_INDEX, dummyPanel);
            if (!preview && 
                    model.getRndIndex() != 
                        MetadataViewer.RND_SPECIFIC) 
                tabPane.setSelectedIndex(GENERAL_INDEX);
        }
    }
    /**
     * Updates display when the new root node is set.
     * 
     * @param oldObject The previously selected object.
     */
	void setRootObject(Object oldObject)
	{
		Object uo = model.getRefObject();
		tabPane.setComponentAt(RND_INDEX, dummyPanel);
		setDataToSave(false);
		toolBar.buildUI();
		tabPane.setToolTipTextAt(RND_INDEX, RENDERER_DESCRIPTION);
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
			userUI.clearData(oldObject);
			toolBar.setStatus(false);
			layoutUI();
		} else if (uo instanceof GroupData) {
		    groupUI.clearData(oldObject);
		    toolBar.setStatus(false);
		    layoutUI();
		} else {
			boolean load = false;
			if (model.isMultiSelection()) {
				tabPane.setSelectedIndex(GENERAL_INDEX);
				tabPane.setEnabledAt(ACQUISITION_INDEX, false);
				tabPane.setEnabledAt(RND_INDEX, false);
			} else {
				if (uo instanceof ImageData || uo instanceof WellSampleData) {
					handleImageSelection();
				} else {
					tabPane.setSelectedIndex(GENERAL_INDEX);
					tabPane.setEnabledAt(ACQUISITION_INDEX, false);
					tabPane.setEnabledAt(RND_INDEX, false);
				}
				load = true;
			}
			generalPane.setRootObject(oldObject);
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
		DataToSave object = generalPane.prepareDataToSave();
		List<Object> metadata = null;
		Object refObject = model.getRefObject();
		if (refObject instanceof ImageData)
			metadata = acquisitionPane.prepareDataToSave();

		model.fireAnnotationSaving(object, metadata, async);
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
		userUI.clearData(null);
		groupUI.clearData(null);
		generalPane.clearData(null);
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
			data instanceof TextualAnnotationData ||
			data instanceof TermAnnotationData ||
			data instanceof XMLAnnotationData ||
			data instanceof DoubleAnnotationData ||
			data instanceof LongAnnotationData ||
			data instanceof BooleanAnnotationData) {
			generalPane.removeObject(data);
			if (data.getId() >= 0)
				saveData(true);
		}
	}
	
	/**
	 * Removes the links, tags attachments.
	 * 
	 * @param level One of the constants defined by this class.
	 */
	private void removeLinks(int level, Collection l)
	{
		saved = true;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		toolBar.setDataToSave(false);
		Iterator<AnnotationData> i = l.iterator();
		AnnotationData o;
		List<Object> toRemove = new ArrayList<Object>();
		List<Object> links;
		while (i.hasNext()) {
			o = i.next();
			links = model.getLinks(level, o);
			if (links != null)
			{
			    toRemove.addAll(links);
			}
		}
		DataToSave object = new DataToSave(new ArrayList<AnnotationData>(), 
				toRemove);
		model.fireAnnotationSaving(object, null, true);
	}
    
	/**
	 * Removes the tags.
	 * 
	 * @param src The mouse clicked location.
	 * @param location The location of the mouse pressed.
	 */
	void removeTags(JComponent src, Point location)
	{
		if (!generalPane.hasTagsToUnlink()) return;
		if (model.isGroupLeader() || model.isAdministrator()) {
			if (tagMenu == null) {
				tagMenu = new PermissionMenu(PermissionMenu.REMOVE, "Tags");
				tagMenu.addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent evt) {
						String n = evt.getPropertyName();
						if (PermissionMenu.SELECTED_LEVEL_PROPERTY.equals(n)) {
							removeLinks((Integer) evt.getNewValue(), 
								model.getAllTags());
						}
					}
				});
			}
			tagMenu.show(src, location.x, location.y);
			return;
		}
		SwingUtilities.convertPointToScreen(location, src);
		MessageBox box = new MessageBox(model.getRefFrame(),
				"Remove All Your Tags", 
		"Are you sure you want to remove all your Tags?");
		Dimension d = box.getPreferredSize();
		Point p = new Point(location.x-d.width/2, location.y);
		if (box.showMsgBox(p) == MessageBox.YES_OPTION) {
			List<TagAnnotationData> list = generalPane.removeTags();
			if (list.size() > 0) saveData(true);
		}
	}
	
	/**
	 * Removes the other annotations.
	 * 
	 * @param src The mouse clicked location.
	 * @param location The location of the mouse pressed.
	 */
	void removeOtherAnnotations(JComponent src, Point location)
	{
		if (!generalPane.hasOtherAnnotationsToUnlink()) return;
		if (model.isGroupLeader() || model.isAdministrator()) {
			if (otherAnnotationMenu == null) {
				otherAnnotationMenu = new PermissionMenu(PermissionMenu.REMOVE, 
						"Other annotations");
				otherAnnotationMenu.addPropertyChangeListener(
						new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent evt) {
						String n = evt.getPropertyName();
						if (PermissionMenu.SELECTED_LEVEL_PROPERTY.equals(n)) {
							removeLinks((Integer) evt.getNewValue(), 
								model.getAllOtherAnnotations());
						}
					}
				});
			}
			otherAnnotationMenu.show(src, location.x, location.y);
			return;
		}
		SwingUtilities.convertPointToScreen(location, src);
		MessageBox box = new MessageBox(model.getRefFrame(),
				"Remove All Your Other Annotations", 
		"Are you sure you want to remove all your other annotations?");
		Dimension d = box.getPreferredSize();
		Point p = new Point(location.x-d.width/2, location.y);
		if (box.showMsgBox(p) == MessageBox.YES_OPTION) {
			List<AnnotationData> list = generalPane.removeOtherAnnotations();
			if (list.size() > 0) saveData(true);
		}
	}
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type The type of objects to handle.
	 * @param objects The objects to handle.
	 */
	void handleObjectsSelection(Class<?> type, Collection objects)
	{
		if (objects == null) return;
		List<Object> selection = new ArrayList<Object>();
		if (CollectionUtils.isNotEmpty(objects)) {
		    selection.addAll(objects);
		}
		 AnnotationData data;
		if (TagAnnotationData.class.equals(type)) {
		    Collection<TagAnnotationData> l = model.getCommonTags();
	        if (CollectionUtils.isNotEmpty(l)) {
	            Iterator<TagAnnotationData> k = l.iterator();
	            while (k.hasNext()) {
	                data = k.next();
	                if (!model.isAnnotationUsedByUser(data)) {
	                    selection.add(data);
	                }
	            }
	        }
		} else if (FileAnnotationData.class.equals(type)) {
		    Collection<FileAnnotationData> l = model.getCommonAttachments();
            if (CollectionUtils.isNotEmpty(l)) {
                Iterator<FileAnnotationData> k = l.iterator();
                while (k.hasNext()) {
                    data = k.next();
                    if (!model.isAnnotationUsedByUser(data)) {
                        selection.add(data);
                    }
                }
            }
		}
		generalPane.handleObjectsSelection(type, selection);
		saveData(true);
	}
	
	/** 
	 * Removes the attached file.
	 * 
	 * @param file The file to remove.
	 */
	void unlinkAttachedFile(Object file)
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
	 * @param src The source of the mouse pressed.
	 * @param location The location of the mouse pressed.
	 */
	void removeAttachedFiles(Component src, Point location)
	{
		if (!generalPane.hasAttachmentsToUnlink()) return;
		if (model.isAdministrator() || model.isGroupLeader()) {
			if (docMenu == null) {
				docMenu = new PermissionMenu(PermissionMenu.REMOVE,
						"Attachments");
				docMenu.addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent evt) {
						String n = evt.getPropertyName();
						if (PermissionMenu.SELECTED_LEVEL_PROPERTY.equals(n)) {
						    List<FileAnnotationData> toRemove = model.getFileAnnotatationsByLevel((Integer) evt.getNewValue());
						    model.fireFileAnnotationRemoveCheck(toRemove);
						}
					}
				});
			}
			docMenu.show(src, location.x, location.y);
			return;
		}
		SwingUtilities.convertPointToScreen(location, src);
		MessageBox box = new MessageBox(model.getRefFrame(),
				"Remove All Attachments", 
				"Are you sure you want to remove all Attachments?");
		Dimension d = box.getPreferredSize();
		Point p = new Point(location.x-d.width/2, location.y);
		if (box.showMsgBox(p) == MessageBox.YES_OPTION) {
			List<FileAnnotationData> list = generalPane.removeAttachedFiles();
			if (list.size() > 0) 
			    model.fireFileAnnotationRemoveCheck(list);
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
			addTab(RENDERER_NAME_SPECIFIC, 
					model.getRenderer().getUI(), 
					RENDERER_DESCRIPTION_SPECIFIC);
		} else {
			addTab(RENDERER_NAME, 
					model.getRenderer().getUI(), 
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
				model.getRenderer().getUI());
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
	 * Returns <code>true</code> if it is an image with a lot of channels.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNumerousChannel() { return model.isNumerousChannel(); }
    
	void cancelAnalysisResultsLoading(AnalysisResultsItem item)
	{
		
	}

	/**
	 * Returns <code>true</code> if the tab is enabled, <code>false</code>
	 * otherwise. if it is not enabled, reset to the default tab, this should
	 * use to reset to the <code>General</code> tab.
	 * 
	 * @param index The index of the tab.
	 * @return See above.
	 */
	boolean checkIfTabEnabled(int index)
	{
		if (index == -1) return false;
		if (tabPane.isEnabledAt(index)) return true;
		tabPane.setSelectedIndex(GENERAL_INDEX);
		return false;
	}

	/** Updates the UI when the related nodes have been set.*/
	void onRelatedNodesSet()
	{
		generalPane.onRelatedNodesSet();
	}

	/** Invokes when the size is loaded.*/
	void onSizeLoaded()
	{
		toolBar.onSizeLoaded();
	}

	/** Displays the file set.
 	 *
	 * @param trigger The action which triggered the loading,
	 * see {@link EditorControl#FILE_PATH_TOOLBAR}
	 * */
	void displayFileset() { 
	    toolBar.displayFileset();
	}
	
	/**
	 * Shows the location dialog
	 */
	void displayLocation() {
	    toolBar.displayLocation();
	}
	
	/**
	 * Returns the file set.
	 * 
	 * @return See above.
	 */
	Set<FilesetData> getFileset() { return model.getFileset(); }
	
	/**
	 * Returns the image or <code>null</code> if the primary select
	 * node is an image or a well.
	 * 
	 * @return See above.
	 */
	ImageData getImage() { return model.getImage(); }

	/**
	 * Returns the selected objects.
	 * 
	 * @return See above.
	 */
	List<DataObject> getSelectedObjects() { return model.getSelectedObjects(); }
	
	/**
	 * Returns the companion file generated while importing the file
	 * and containing the metadata found in the file, or <code>null</code>
	 * if no file was generated.
	 * 
	 * @return See above
	 */
	FileAnnotationData getOriginalMetadata()
	{
		return model.getOriginalMetadata();
	}

	/**
     * Sets the LDAP details.
     *
     * @param userID The user's id.
     */
	void setLDAPDetails(String result) {
	    userUI.setLDAPDetails(result);
	}

	/**
     * Returns the selected FileAnnotations or an empty Collection
     * if there are no FileAnnotations
     * 
     * @return See above
     */
	public Collection<FileAnnotationData> getSelectedFileAnnotations() { 
	    return generalPane.getSelectedFileAnnotations();
	}
}
