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


import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.editor.AnnotationTaskPane.AnnotationType;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellSampleData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * Component displaying the annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class GeneralPaneUI 
	extends JPanel//JScrollPane
{

	/** The default text. */
	private static final String			DETAILS = "'s details";
	
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
	/** Reference to the Model. */
	private EditorUI					view;
	
	/** The UI component displaying the object's properties. */
	private PropertiesUI				propertiesUI;
	
	/** The UI component displaying the textual annotations. */
	private TextualAnnotationsUI		textualAnnotationsUI;
	
	/** Component hosting the tags, rating, URLs and attachments. */
	private AnnotationDataUI			annotationUI;
	
	/** The component hosting the {@link #browser}. */
	private JXTaskPane 					browserTaskPane;

	/** The component hosting the {@link #propertiesUI}. */
	private JXTaskPane 					propertiesTaskPane;
	
	/** The component hosting the annotation component. */
    private AnnotationTaskPane                  tagsTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  roiTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  mapTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  attachmentTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  otherTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  ratingTaskPane;
    
    /** The component hosting the annotation component. */
    private AnnotationTaskPane                  commentTaskPane;
    
	/** The component hosting the annotation component. */
	private JXTaskPane 					annotationTaskPane;
	
	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** Collection of preview panels. */
	private List<PreviewPanel>			previews;
	
	/** Flag indicating to build the UI once. */
	private boolean 					init;
	
	/** The tool bar.*/
	private ToolBar toolbar;
	
	/**;
	 * Loads or cancels any on-going loading of containers hosting
	 * the edited object.
	 * 
	 * @param b Pass <code>true</code> to load, <code>false</code> to cancel.
	 */
	private void loadParents(boolean b)
	{
		if (b) controller.loadParents();
		else model.cancelParentsLoading();
	}
	
    /** Initializes the UI components. */
	private void initComponents()
	{	 
       browserTaskPane = EditorUtil.createTaskPane(Browser.TITLE);
       browserTaskPane.addPropertyChangeListener(controller);
		
		propertiesUI = new PropertiesUI(model, controller);
		textualAnnotationsUI = new TextualAnnotationsUI(model, controller);
		annotationUI = new AnnotationDataUI(view, model, controller);

		components = new ArrayList<AnnotationUI>();
		components.add(propertiesUI);
		components.add(textualAnnotationsUI);
		components.add(annotationUI);
		Iterator<AnnotationUI> i = components.iterator();
		while (i.hasNext()) {
			i.next().addPropertyChangeListener(EditorControl.SAVE_PROPERTY,
											controller);
		}
		previews = new ArrayList<PreviewPanel>();
		propertiesTaskPane = EditorUtil.createTaskPane("");
		propertiesTaskPane.setCollapsed(false);
		propertiesTaskPane.add(propertiesUI);
		
		// new annotation taskpanes
		
		tagsTaskPane = new AnnotationTaskPane(AnnotationType.TAGS, view, model, controller);
	    
	    roiTaskPane = new AnnotationTaskPane(AnnotationType.ROIS, view, model, controller);
	    
	    mapTaskPane = new AnnotationTaskPane(AnnotationType.MAP, view, model, controller);
	    
	    attachmentTaskPane = new AnnotationTaskPane(AnnotationType.ATTACHMENTS, view, model, controller);
	    
	    otherTaskPane = new AnnotationTaskPane(AnnotationType.OTHER, view, model, controller);
	    
	    ratingTaskPane = new AnnotationTaskPane(AnnotationType.RATING, view, model, controller);
	    
	    commentTaskPane = new AnnotationTaskPane(AnnotationType.COMMENTS, view, model, controller); 
		
		
		// old annotation taskpane - to be removed later!
		annotationTaskPane = EditorUtil.createTaskPane("Annotations");
		annotationTaskPane.setCollapsed(false);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new GridBagLayout());
		p.add(annotationUI,c );
		c.gridy++;
		p.add(new JSeparator(), c);
		c.gridy++;
		p.add(textualAnnotationsUI, c);
		annotationTaskPane.add(p);
		// --
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		
		add(toolbar, c);
		c.gridy++;
		
		add(propertiesTaskPane, c);
		c.gridy++;
		
		add(annotationTaskPane, c);
		c.gridy++;
		
		add(tagsTaskPane, c);
        c.gridy++;
        
        add(roiTaskPane, c);
        c.gridy++;
        
        add(mapTaskPane, c);
        c.gridy++;
        
        add(attachmentTaskPane, c);
        c.gridy++;
        
        add(otherTaskPane, c);
        c.gridy++;
        
        add(ratingTaskPane, c);
        c.gridy++;
        
        add(commentTaskPane, c);
        c.gridy++;
        
        add(browserTaskPane, c);
        
        UIUtilities.addFiller(this, c, true);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 * @param tooBar 		The tool Bar
	 */
	GeneralPaneUI(EditorUI view, EditorModel model, EditorControl controller, 
			ToolBar toolBar)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.toolbar = toolBar;
		this.view = view;
		initComponents();
		init = false;
	}

        /** Lays out the UI when data are loaded. */
        void layoutUI() {
            if (!init) {
                buildGUI();
                init = true;
            }
            propertiesUI.buildUI();
            annotationUI.buildUI();
            textualAnnotationsUI.buildUI();
            
            tagsTaskPane.refreshUI();
            roiTaskPane.refreshUI();
            mapTaskPane.refreshUI();
            attachmentTaskPane.refreshUI(); 
            otherTaskPane.refreshUI(); 
            ratingTaskPane.refreshUI();
            commentTaskPane.refreshUI();  
            
            propertiesTaskPane.setTitle(propertiesUI.getText() + DETAILS);
            
            boolean multi = model.isMultiSelection();
            boolean showBrowser = false;
            Object refObject = model.getRefObject();
    
            if (refObject instanceof ImageData && !multi && model.getChannelData()==null) {
                propertiesUI.onChannelDataLoading();
                controller.loadChannelData();
                showBrowser = true;
            }
    
            if (refObject instanceof WellSampleData && !multi) {
                controller.loadChannelData();
                showBrowser = true;
            }
            
            if ((refObject instanceof DatasetData
                    || refObject instanceof FileAnnotationData || refObject instanceof PlateAcquisitionData)
                    && !multi) {
                showBrowser = true;
            }
    
            browserTaskPane.setVisible(showBrowser);
    
            if (showBrowser) {
                if (refObject instanceof FileAnnotationData)
                    browserTaskPane.setTitle("Attached to");
                else
                    browserTaskPane.setTitle("Located in");
            }
            
            propertiesTaskPane.setVisible(!multi);
    
            revalidate();
        }
	
	/**
	* Get a reference to the PropertiesUI
	* 
	* @return See above
	*/
	public PropertiesUI getPropertiesUI() {
            return propertiesUI;
        }

    	/** 
	 * Returns the object hosting the annotation/link to save.
	 * 
	 * @return See above.
	 */
	DataToSave prepareDataToSave()
	{
		if (!model.isMultiSelection()) propertiesUI.updateDataObject();
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		List<Object> toRemove = new ArrayList<Object>();
		List<AnnotationData> l = annotationUI.getAnnotationToSave();
		//To add
		if (CollectionUtils.isNotEmpty(l))
			toAdd.addAll(l);
		l = textualAnnotationsUI.getAnnotationToSave();
		if (CollectionUtils.isNotEmpty(l))
			toAdd.addAll(l);
		//To remove
		List<Object> ll = annotationUI.getAnnotationToRemove();
		if (CollectionUtils.isNotEmpty(ll))
			toRemove.addAll(ll);
		ll = textualAnnotationsUI.getAnnotationToRemove();
		if (CollectionUtils.isNotEmpty(ll))
			toRemove.addAll(ll);
		
		return new DataToSave(toAdd, toRemove);
	}
	
	/** Updates display when the parent of the root node is set. */
	void setParentRootObject()
	{
		propertiesUI.setParentRootObject();
	}
	
	/** 
	 * Updates display when the new root node is set.
	 * 
	 *  @param oldObject The object previously selected.
	 */
	void setRootObject(Object oldObject)
	{
		if (!init) {
			buildGUI();
			init = true;
		}	
		clearData(oldObject);
		propertiesUI.clearDisplay();
		annotationUI.clearDisplay();
    	textualAnnotationsUI.clearDisplay();
    	browserTaskPane.removeAll();
    	browserTaskPane.setCollapsed(true);
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
		return annotationUI.getCurrentTagsSelection();
	}
	
	/**
	 * Returns the list of attachments currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getCurrentAttachmentsSelection() 
	{
		return annotationUI.getCurrentAttachmentsSelection();
	}
	
	/** Shows the image's info. */
    void setChannelData()
    { 
    	Object refObject = model.getRefObject();
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData))
    		propertiesUI.setChannelData(model.getChannelData());
    }
    
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			if (ui.hasDataToSave())
				return true;
		}
		Iterator<PreviewPanel> p = previews.iterator();
		PreviewPanel pp;
		while (p.hasNext()) {
			pp = p.next();
			if (pp.hasDataToSave()) return true;
		}
		return false;
	}
	
	/** 
	 * Clears data to save.
	 * 
	 * @param oldObject The previously selected object.
	 */
	void clearData(Object oldObject)
	{
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			ui.clearData(oldObject);
			ui.clearDisplay();
		}
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * Handles the expansion or collapsing of the passed component.
	 * 
	 * @param source The component to handle.
	 */
	void handleTaskPaneCollapsed(JXTaskPane source)
	{
		if (source == null) return;
		if  (source.equals(browserTaskPane))  {
		    if(browserTaskPane.isCollapsed()) {
		        loadParents(false);
		    }
		    else {
    		    browserTaskPane.removeAll();
                browserTaskPane.add(model.getBrowser().getUI());
    			loadParents(true);
		    }
		}
	}

	/**
	 * Attaches the passed files.
	 * Returns <code>true</code> if the files
	 * do not already exist, <code>false</code> otherwise.
	 * 
	 * @param files The files to attach.
	 * @return See above
	 */
	boolean attachFiles(File[] files)
	{ 
		return annotationUI.attachFiles(files); 
	}

	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{ 
		annotationUI.removeAttachedFile(file);
	}
	
	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> removeAttachedFiles()
	{
		return annotationUI.removeAttachedFiles();
	}
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> removeTags()
	{
		return annotationUI.removeTags();
	}
	
	/**
	 * Returns the collection of other annotation.
	 * 
	 * @return See above.
	 */
	List<AnnotationData> removeOtherAnnotations()
	{
		return annotationUI.removeOtherAnnotation();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAttachmentsToUnlink()
	{ 
		return annotationUI.hasAttachmentsToUnlink();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasTagsToUnlink()
	{
		return annotationUI.hasTagsToUnlink();
	}
	
	/**
	 * Returns <code>true</code> some other annotations can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasOtherAnnotationsToUnlink()
	{
		return annotationUI.hasOtherAnnotationsToUnlink();
	}
	
	/**
	 * Removes the annotation from the view.
	 * 
	 * @param annotation The annotation to remove.
	 */
	void removeObject(DataObject annotation)
	{
		if (annotation == null) return;
		if (annotation instanceof TagAnnotationData ||
			annotation instanceof TermAnnotationData ||
			annotation instanceof XMLAnnotationData ||
			annotation instanceof LongAnnotationData ||
			annotation instanceof DoubleAnnotationData ||
			annotation instanceof BooleanAnnotationData)
			annotationUI.removeAnnotation((AnnotationData) annotation);
		else if (annotation instanceof TextualAnnotationData)
			textualAnnotationsUI.removeTextualAnnotation(
					(TextualAnnotationData) annotation);
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type		The type of objects to handle.
	 * @param objects   The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
		if (objects == null) return;
		annotationUI.handleObjectsSelection(type, objects, true);
	}
	
	/** Updates the UI when the related nodes have been set.*/
	void onRelatedNodesSet()
	{
		annotationUI.onRelatedNodesSet();
	}
	
	/**
     * Returns the selected FileAnnotations or an empty Collection
     * if there are no FileAnnotations
     * 
     * @return See above
     */
	public Collection<FileAnnotationData> getSelectedFileAnnotations() {
	    return annotationUI.getSelectedFileAnnotations();
	}
}
