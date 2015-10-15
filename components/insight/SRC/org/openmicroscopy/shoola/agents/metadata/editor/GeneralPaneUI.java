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


import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.editor.AnnotationTaskPane.AnnotationType;
import org.openmicroscopy.shoola.agents.metadata.editor.AnnotationTaskPaneUI.Filter;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
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
    /** The text for the id. */
    private static final String ID_TEXT = "ID: ";
    
    /** The text for the owner. */
    private static final String OWNER_TEXT = "Owner: ";
    
    /** Text indicating to edit the name. */
    private static final String EDIT_NAME_TEXT = "Edit the name";
    
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
	
	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** Collection of preview panels. */
	private List<PreviewPanel>			previews;
	
	/** Flag indicating to build the UI once. */
	private boolean 					init;
	
	/** The tool bar.*/
	private ToolBar toolbar;
	
	/** The button to filter the annotations i.e. show all, mine, others. */
    private JButton                         filterButton;
    
    /** The current annotation filter level */
    private Filter annotationsFilter;
    
    private EditableTextComponent namePane;
    
    boolean nameModified = false;
    
    /** The component hosting the id of the <code>DataObject</code>. */
    private JTextField              idLabel;
    
    /** 
     * The component hosting the owner of the <code>DataObject</code>.
     * if not the current user. 
     */
    private JLabel              ownerLabel;
    
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
		
       namePane = new EditableTextComponent(model.canEdit(), false, EDIT_NAME_TEXT);
       namePane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(EditableTextComponent.EDIT_PROPERTY)) {
                    updateName((String)evt.getNewValue());
                    nameModified = true;
                    view.saveData(true);
                    nameModified = false;
                }
            }
        });
       
       idLabel = new JTextField();
       idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD));
       idLabel.setEditable(false);
       idLabel.setBorder(BorderFactory.createEmptyBorder());
       
       ownerLabel = new JLabel();
       ownerLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
       ownerLabel.setFont(ownerLabel.getFont().deriveFont(Font.BOLD));
       
       IconManager icons = IconManager.getInstance();
       annotationsFilter = Filter.SHOW_ALL;
       filterButton = new JButton(annotationsFilter.name);
       filterButton.setToolTipText("Filter tags and attachments.");
       UIUtilities.unifiedButtonLookAndFeel(filterButton);
       Font font = filterButton.getFont();
       filterButton.setFont(font.deriveFont(font.getStyle(), 
               font.getSize()-2));
       filterButton.setIcon(icons.getIcon(IconManager.UP_DOWN_9_12));
       filterButton.setBackground(UIUtilities.BACKGROUND_COLOR);
       filterButton.addMouseListener(new MouseAdapter() {
           /** 
            * Brings up the menu. 
            * @see MouseListener#mouseReleased(MouseEvent)
            */
           public void mouseReleased(MouseEvent me)
           {
               Object source = me.getSource();
               if (source instanceof Component)
                   displayFilterMenu((Component) source, me.getPoint());
           } 
       });
       
		propertiesUI = new PropertiesUI(model, controller);

		components = new ArrayList<AnnotationUI>();
		components.add(propertiesUI);
		Iterator<AnnotationUI> i = components.iterator();
		while (i.hasNext()) {
			i.next().addPropertyChangeListener(EditorControl.SAVE_PROPERTY,
											controller);
		}
		previews = new ArrayList<PreviewPanel>();
		propertiesTaskPane = EditorUtil.createTaskPane("");
		propertiesTaskPane.add(propertiesUI);

		tagsTaskPane = new AnnotationTaskPane(AnnotationType.TAGS, view, model, controller);
	    
	    roiTaskPane = new AnnotationTaskPane(AnnotationType.ROIS, view, model, controller);
	    
	    mapTaskPane = new AnnotationTaskPane(AnnotationType.MAP, view, model, controller);
	    
	    attachmentTaskPane = new AnnotationTaskPane(AnnotationType.ATTACHMENTS, view, model, controller);
	    
	    otherTaskPane = new AnnotationTaskPane(AnnotationType.OTHER, view, model, controller);
	    
	    ratingTaskPane = new AnnotationTaskPane(AnnotationType.RATING, view, model, controller);
	    
	    commentTaskPane = new AnnotationTaskPane(AnnotationType.COMMENTS, view, model, controller); 
	}
	
	/**
     * Creates and displays the menu 
     * @param src The invoker.
     * @param p   The location where to show the menu.
     */
    private void displayFilterMenu(Component src, Point p)
    {
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem item = createFilterMenuItem(Filter.SHOW_ALL);
        group.add(item);
        menu.add(item);
        item = createFilterMenuItem(Filter.ADDED_BY_ME);
        group.add(item);
        menu.add(item);
        item = createFilterMenuItem(Filter.ADDED_BY_OTHERS);
        group.add(item);
        menu.add(item);
        menu.show(src, p.x, p.y);
    }
    
    /**
     * Creates a menu item.
     * 
     * @param index The index associated to the item.
     * @return See above.
     */
    private JCheckBoxMenuItem createFilterMenuItem(final Filter filter)
    {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(filter.name);
        Font f = item.getFont();
        item.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
        item.setSelected(filter == annotationsFilter);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationsFilter = filter;
                filterButton.setText(annotationsFilter.name);
                applyFilter();
            }
        });
        return item;
    }
    
    private void applyFilter() {
        tagsTaskPane.filter(annotationsFilter);
        roiTaskPane.filter(annotationsFilter);
        mapTaskPane.filter(annotationsFilter);
        attachmentTaskPane.filter(annotationsFilter);
        otherTaskPane.filter(annotationsFilter);
        ratingTaskPane.filter(annotationsFilter);
        commentTaskPane.filter(annotationsFilter);
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
		
		namePane.setBorder(BorderFactory.createEmptyBorder(2,2,0,2));
		add(namePane, c);
		c.gridy++;
		
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		p.add(idLabel);
		p.add(Box.createHorizontalGlue());
		p.add(ownerLabel);
		add(p, c);
        c.gridy++;
		
		p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(filterButton);
        add(p, c);
        c.gridy++;
		
        add(propertiesTaskPane, c);
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
            
            namePane.buildUI(model.getRefObjectName(), model.canEdit());
            
            Object refObject = model.getRefObject();
            
            String text = model.getObjectTypeAsString(refObject);
            if (model.getRefObjectID() > 0)
                text += " "+ID_TEXT+model.getRefObjectID();
            if (refObject instanceof WellSampleData) {
                WellSampleData wsd = (WellSampleData) refObject;
                text += " (Image ID: "+wsd.getImage().getId()+")";
            }
            idLabel.setText(text);
            
            String ownerName = model.getOwnerName();
            ownerLabel.setText("");
            if (ownerName != null && ownerName.length() > 0)
                ownerLabel.setText(OWNER_TEXT+ownerName);
            
            propertiesUI.buildUI();
            
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
		toAdd.addAll(tagsTaskPane.getAnnotationsToSave());
		toAdd.addAll(attachmentTaskPane.getAnnotationsToSave());
		toAdd.addAll(otherTaskPane.getAnnotationsToSave());
		toAdd.addAll(ratingTaskPane.getAnnotationsToSave());
		toAdd.addAll(mapTaskPane.getAnnotationsToSave());
		toAdd.addAll(commentTaskPane.getAnnotationsToSave());

		List<Object> toRemove = new ArrayList<Object>();
		toRemove.addAll(tagsTaskPane.getAnnotationsToRemove());
		toRemove.addAll(attachmentTaskPane.getAnnotationsToRemove());
		toRemove.addAll(otherTaskPane.getAnnotationsToRemove());
		toRemove.addAll(ratingTaskPane.getAnnotationsToRemove());
		toRemove.addAll(mapTaskPane.getAnnotationsToRemove());
		toRemove.addAll(commentTaskPane.getAnnotationsToRemove());
		
		return new DataToSave(toAdd, toRemove);
	}
	
	void updateName(String name) {
	    Object object =  model.getRefObject();
        if (object instanceof ProjectData) {
            ProjectData p = (ProjectData) object;
          if (name.length() > 0) p.setName(name);
        } else if (object instanceof DatasetData) {
            DatasetData p = (DatasetData) object;
          if (name.length() > 0) p.setName(name);
        } else if (object instanceof ImageData) {
            ImageData p = (ImageData) object;
          if (name.length() > 0) p.setName(name);
        } else if (object instanceof TagAnnotationData) {
            TagAnnotationData p = (TagAnnotationData) object;
          if (name.length() > 0) p.setTagValue(name);
        } else if (object instanceof ScreenData) {
            ScreenData p = (ScreenData) object;
          if (name.length() > 0) p.setName(name);
        } else if (object instanceof PlateData) {
            PlateData p = (PlateData) object;
          if (name.length() > 0) p.setName(name);
        } else if (object instanceof WellSampleData) {
            WellSampleData well = (WellSampleData) object;
            ImageData img = well.getImage();
          if (name.length() > 0) img.setName(name);
        } else if (object instanceof FileData) {
            FileData f = (FileData) object;
            if (f.getId() > 0) return;
        } else if (object instanceof PlateAcquisitionData) {
            PlateAcquisitionData pa = (PlateAcquisitionData) object;
          if (name.length() > 0) pa.setName(name);
        }
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
    	
    	tagsTaskPane.clearDisplay();
        roiTaskPane.clearDisplay();
        mapTaskPane.clearDisplay();
        attachmentTaskPane.clearDisplay();
        otherTaskPane.clearDisplay();
        ratingTaskPane.clearDisplay();
        commentTaskPane.clearDisplay();
        
    	browserTaskPane.removeAll();
    	browserTaskPane.setCollapsed(true);
    	
		revalidate();
		repaint();
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
		
		if(tagsTaskPane.hasDataToSave())
		    return true;
        
		if(mapTaskPane.hasDataToSave())
            return true;
		
		if(attachmentTaskPane.hasDataToSave())
            return true;
		
		if(otherTaskPane.hasDataToSave())
            return true;
		
		if(ratingTaskPane.hasDataToSave())
            return true;
		
		if(commentTaskPane.hasDataToSave())
            return true;
		
		return nameModified;
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
		nameModified = false;
		idLabel.setText("");
		ownerLabel.setText("");
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
	    return ((AttachmentsTaskPaneUI) attachmentTaskPane.getTaskPaneUI()).attachFiles(files);
	}

	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{ 
	    ((AttachmentsTaskPaneUI) attachmentTaskPane.getTaskPaneUI()).removeAttachedFile(file);
	}
	
	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> removeAttachedFiles()
	{
	    return ((AttachmentsTaskPaneUI) attachmentTaskPane.getTaskPaneUI()).removeAttachedFiles();
	}
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> removeTags()
	{
	    return ((TagsTaskPaneUI) tagsTaskPane.getTaskPaneUI()).removeTags();
	}
	
	/**
	 * Returns the collection of other annotation.
	 * 
	 * @return See above.
	 */
	List<AnnotationData> removeOtherAnnotations()
	{
	    return ((OtherTaskPaneUI) otherTaskPane.getTaskPaneUI()).removeOtherAnnotation();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAttachmentsToUnlink()
	{ 
	    return ((AttachmentsTaskPaneUI)attachmentTaskPane.getTaskPaneUI()).hasAttachmentsToUnlink();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasTagsToUnlink()
	{
	    return ((TagsTaskPaneUI)tagsTaskPane.getTaskPaneUI()).hasTagsToUnlink();
	}
	
	/**
	 * Returns <code>true</code> some other annotations can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasOtherAnnotationsToUnlink()
	{
	    return ((OtherTaskPaneUI)otherTaskPane.getTaskPaneUI()).hasOtherAnnotationsToUnlink();
	}
	
	/**
     * Removes a annotation from the view.
     * 
     * @param annotation The annotation to remove.
     */
    void removeAnnotation(AnnotationData annotation)
    {
        if (annotation == null)
            return;
        
        List<AnnotationData> toKeep = new ArrayList<AnnotationData>();
        AnnotationData data;
        if (annotation instanceof TagAnnotationData) {
            List<TagAnnotationData> tags = getCurrentTagsSelection();
            Iterator<TagAnnotationData> i = tags.iterator();
            while (i.hasNext()) {
                data = i.next();
                if (data.getId() != annotation.getId())
                    toKeep.add(data);
            }
            handleObjectsSelection(TagAnnotationData.class, toKeep);
        } else if (annotation instanceof TermAnnotationData ||
                annotation instanceof XMLAnnotationData ||
                annotation instanceof BooleanAnnotationData ||
                annotation instanceof LongAnnotationData ||
                annotation instanceof DoubleAnnotationData) {
            List<AnnotationData> tags = getCurrentOtherSelection();
            Iterator<AnnotationData> i = tags.iterator();
            while (i.hasNext()) {
                data = i.next();
                if (data.getId() != annotation.getId())
                    toKeep.add(data);
            }
            handleObjectsSelection(AnnotationData.class, toKeep);
        } 
        else if(annotation instanceof TextualAnnotationData) {
            ((CommentsTaskPaneUI)commentTaskPane.getTaskPaneUI()).removeTextualAnnotation((TextualAnnotationData)annotation);
        }
        
    }
    
    List<TagAnnotationData> getCurrentTagsSelection() {
        TagsTaskPaneUI p = (TagsTaskPaneUI) tagsTaskPane.getTaskPaneUI();
        return p.getCurrentSelection();
    }
    
    List<AnnotationData> getCurrentOtherSelection() {
        OtherTaskPaneUI p = (OtherTaskPaneUI) otherTaskPane.getTaskPaneUI();
        return p.getCurrentSelection();
    }
    
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type		The type of objects to handle.
	 * @param objects   The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
        if (objects == null)
            return;

        if (TagAnnotationData.class.equals(type)) 
            ((TagsTaskPaneUI)tagsTaskPane.getTaskPaneUI()).handleObjectsSelection(type, objects, true);
        else if (FileAnnotationData.class.equals(type)) 
            ((AttachmentsTaskPaneUI)attachmentTaskPane.getTaskPaneUI()).handleObjectsSelection(type, objects, true);
        else if (AnnotationData.class.equals(type)) 
            ((OtherTaskPaneUI)otherTaskPane.getTaskPaneUI()).handleObjectsSelection(type, objects, true);
	}
	
	/** Updates the UI when the related nodes have been set.*/
	void onRelatedNodesSet()
	{
	    nameModified = false;
	    tagsTaskPane.onRelatedNodesSet();
	    roiTaskPane.onRelatedNodesSet();
	    mapTaskPane.onRelatedNodesSet();
	    attachmentTaskPane.onRelatedNodesSet();
	    otherTaskPane.onRelatedNodesSet();
	    ratingTaskPane.onRelatedNodesSet();
	    commentTaskPane.onRelatedNodesSet();
	}
	
	/**
     * Returns the selected FileAnnotations or an empty Collection
     * if there are no FileAnnotations
     * 
     * @return See above
     */
	public Collection<FileAnnotationData> getSelectedFileAnnotations() {
	    AttachmentsTaskPaneUI p = (AttachmentsTaskPaneUI) attachmentTaskPane.getTaskPaneUI();
	    return p.getSelectedFileAnnotations();
	}
}
