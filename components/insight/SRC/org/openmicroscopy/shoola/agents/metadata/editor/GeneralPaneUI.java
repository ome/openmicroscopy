/*
 * org.openmicroscopy.shoola.agents.metadata.editor.GeneralPaneUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;



import javax.swing.JSeparator;

//Third-party libraries
import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DoubleAnnotationData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.WellSampleData;
import pojos.XMLAnnotationData;

/** 
 * Component displaying the annotation.
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
		browserTaskPane = EditorUtil.createTaskPane("Attached to");
		browserTaskPane.addPropertyChangeListener(controller);
		browserTaskPane.setVisible(false);
		 
		if (model.getBrowser() != null && model.getRefObject() instanceof FileAnnotationData) {
             browserTaskPane.add(model.getBrowser().getUI());
             browserTaskPane.setVisible(true);
        }
		
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
		
		annotationTaskPane = EditorUtil.createTaskPane("Annotations");
		annotationTaskPane.setCollapsed(false);
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(annotationUI);
		p.add(new JSeparator());
		p.add(textualAnnotationsUI);
		annotationTaskPane.add(p);
	}
	
	@Override
	public Dimension getPreferredSize() {
		// Workaround:
		// Without this, the JTextAreas displaying comments will expand, when the slider is moved
		// to the left, but never shrink again, when the slider is again moved to the right.
		// TODO: Replace if a better solution for this is found
		return new Dimension(getParent().getSize().width, super.getPreferredSize().height);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		JPanel container = new JPanel();
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		
		container.add(toolbar, c);
		c.gridy++;
		
		container.add(propertiesTaskPane, c);
		c.gridy++;
		
		container.add(browserTaskPane, c);
		c.gridy++;
		
		container.add(annotationTaskPane, c);
		
		setLayout(new BorderLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		add(container, BorderLayout.NORTH);
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
            propertiesTaskPane.setTitle(propertiesUI.getText() + DETAILS);
            
            boolean multi = model.isMultiSelection();
            Object refObject = model.getRefObject();
    
            if (refObject instanceof ImageData && !multi && model.getChannelData()==null) {
                propertiesUI.onChannelDataLoading();
                controller.loadChannelData();
            }
    
            if (refObject instanceof WellSampleData && !multi) {
                controller.loadChannelData();
            }
    
            browserTaskPane.setVisible(false);
            propertiesTaskPane.setVisible(false);
            
            if (!multi) {
            	propertiesTaskPane.setVisible(true);
            	
                if (refObject instanceof FileAnnotationData) {
                    browserTaskPane.removeAll();
                	browserTaskPane.add(model.getBrowser().getUI());
                    browserTaskPane.setVisible(true);
                    if (!browserTaskPane.isCollapsed())
                        loadParents(true);
                }
            }
    
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
		if  (source.equals(browserTaskPane)) 
			loadParents(!browserTaskPane.isCollapsed());
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
	
}
