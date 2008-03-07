/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JRootPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;


/** 
 * Implements the {@link Editor} interface to the functionality
 * required of the classifier component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EditorComponent
    extends AbstractComponent
    implements Editor
{

    /** The Model sub-component. */
    private EditorModel     model;
    
    /** The Controller sub-component. */
    private EditorControl   controller;
    
    /** The View sub-component. */
    private EditorUI        view;
   
    /**
     * Controls if the specified data object operation is supported.
     * 
     * @param i The index to control.
     */
    private void checkDataObjectOperation(int i)
    {
        switch (i) {
            case TreeViewer.CREATE_OBJECT:
            case TreeViewer.UPDATE_OBJECT:  
                break;
            default:
                throw new IllegalArgumentException("DataObject operation not " +
                        "supported");
        }
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    EditorComponent(EditorModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new EditorControl(this);
        view = new EditorUI();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        view.initialize(controller, model);
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getState()
     */
    public void activate()
    {
        switch (model.getState()) {
            case NEW:
                if (model.getEditorType() == PROPERTIES_EDITOR) {
                	switch (model.getSelectedTabbedIndex()) {
						case Editor.PROPERTIES_INDEX:
							/*
							int subIndex = model.getSelectedSubPane();
	                		if (model.isAnnotatable() && 
	                			subIndex == Editor.ANNOTATION_INDEX) {
	                			retrieveAnnotations();
	                		} else if (model.isClassified() && 
	                			subIndex == Editor.CLASSIFICATION_INDEX)
	                			loadClassifications();
	                			*/
							loadTags();
							retrieveThumbnail();
							break;
						case Editor.ANNOTATIONS_INDEX:
							if (model.isAnnotatable())
								retrieveAnnotations();
							else retrieveThumbnail();
							break;

						default:
							model.setState(READY);
					}
                	
                    fireStateChange();
                }
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
        }   
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#discard()
     */
    public void discard()
    {
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setThumbnail(BufferedImage, long)
     */
    public void setThumbnail(BufferedImage thumbnail, long imageID)
    {
        if (model.getState() != DISCARDED) {
        	if (!model.isImage()) return;
            if (thumbnail == null || model.getDataObjectID() != imageID)
            	//try to reload maybe??
                return;
            model.setThumbnailLoaded(true);
            view.setThumbnail(thumbnail);
            model.setState(READY);
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#cancel()
     */
    public void cancel()
    {
        if (model.getState() != DISCARDED) {
            model.cancel();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getUI()
     */
    public JComponent getUI()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return view;
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#close()
     */
    public void close()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        firePropertyChange(CLOSE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setSaveResult(DataObject, int)
     */
    public void setSaveResult(DataObject object, int operation)
    {
        //if (model.getState() != SAVE_EDITION) return;
        checkDataObjectOperation(operation);
        if (object == null)
            throw new IllegalArgumentException("No DataObject to save.");
        model.setSaveResult(object, operation);
        //reload the annotation
        
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveObject(DataObject, int)
     */
    public void saveObject(DataObject object, int operation)
    {
        switch (model.getState()) {
            case DISCARDED:  
            case LOADING_TAGS:  
                throw new IllegalStateException(
                "This method cannot be invoked in the DISCARDED, " +
                "LOADING_ANNOTATION or LOADING_CLASSIFICATION state.");
        }
        if (object == null)
            throw new IllegalArgumentException("No DataObject.");
        checkDataObjectOperation(operation);
        switch (operation) {
            case TreeViewer.CREATE_OBJECT:
                model.fireDataObjectCreation(object);
                break;
            case TreeViewer.UPDATE_OBJECT:
                model.fireDataObjectUpdate(object);
                break;
        }
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#loadTags()
     */
    public void loadTags()
    {
        switch (model.getState()) {
            case DISCARDED:
            	//return;
        }
        if (!(model.isTagged())) {
        	view.showTags();
        	return;
        }
        if (model.isTagsLoaded()) return;
        //model.setClassifications(null);
        model.fireTagLoading();
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#hasDataToSave()
     */
    public boolean hasDataToSave()
    {
        if (model.getState() == DISCARDED) return false; //Need to review
            //throw new IllegalStateException("This method cannot be invoked " +
            //            "in the DISCARDED state.");
        return view.hasDataToSave();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#hasAnnotationToSave()
     */
    public boolean hasAnnotationToSave()
    {
        if (model.getState() == DISCARDED) return false; //Need to review
            //throw new IllegalStateException("This method cannot be invoked " +
            //            "in the DISCARDED state.");
        return view.hasAnnotationToSave();
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveData()
     */
    public void saveData()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                        "in the DISCARDED state.");
        
        view.finish();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveData()
     */
    public void retrieveChannelsData()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                        "in the DISCARDED state.");
        if (model.getChannelsData() != null) return;
        model.retrieveChannelsData();
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setChannelsData(List)
     */
    public void setChannelsData(List emissionWaves)
    {
        if (model.getState() != LOADING_CHANNEL_DATA) return;
        model.setChannelsData(emissionWaves);
        retrieveThumbnail();
        fireStateChange();
        view.setChannelsData();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#retrieveAnnotations()
     */
	public void retrieveAnnotations()
	{
		if (model.getEditorType() != PROPERTIES_EDITOR) return;
		/*
		if (model.isAnnotatable() && model.getSelectedTabbedIndex() == 
        			Editor.ANNOTATIONS_INDEX) {
			model.fireAnnotationsLoading();
	        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, 
	        								false);
        }  
        */
		if (model.isAnnotatable() && model.getSelectedTabbedIndex() == 
			Editor.ANNOTATIONS_INDEX) {
			model.fireAnnotationsLoading();
			model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, 
    								false);
		}  
		retrieveThumbnail();
	}

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getSelectedSubPane()
     */
	public int getSelectedSubPane()
	{
		return model.getSelectedSubPane();
	}
	
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#retrieveThumbnail()
     */
	public void retrieveThumbnail()
    {
    	if (model.hasThumbnail() && !model.isThumbnailLoaded())
    		firePropertyChange(TreeViewer.THUMBNAIL_LOADING_PROPERTY, 
                    null, model.getHierarchyObject()); 
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#retrieveThumbnail()
     */
	public void setFocusOnName()
	{
		if (model.getEditorType() != CREATE_EDITOR) return;
		view.setFocusOnName();
	}

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setSize(Dimension)
     */
	public void setSize(Dimension d)
	{
		if (model.getEditorType() != CREATE_EDITOR) return;
		view.setSize(d);
	}

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setDefaultButton(JRootPane)
     */
	public void setDefaultButton(JRootPane rootPane)
	{
		if (rootPane == null) return;
		view.setDefaultButton(rootPane);
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#addSiblings(List)
	 */
	public void addSiblings(List nodes)
	{
		// TODO ADD CONTROL
		view.addSelectedNodes(nodes);
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setTags(List, List)
	 */
	public void setTags(List linkedTags, List tagSets)
	{
		if (model.getState() == DISCARDED) return;
		model.setTags(linkedTags, tagSets);
		view.showTags();
		model.setTagOnNode();
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onTagsUpdate()
	 */
	public void onTagsUpdate()
	{
		if (model.getState() == DISCARDED) return;
		if (model.isImage()) {
			model.fireTagLoading();
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#loadAvailableTags()
	 */
	public void loadAvailableTags()
	{
		if (model.getState() == DISCARDED) return;
		if (model.isImage()) {
			model.fireAvailableTagsLoading();
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setAvailableTags(List)
	 */
	public void setAvailableTags(List tags)
	{
		if (model.getState() == DISCARDED) return;
		if (model.isImage()) {
			model.setAvailableTags(tags);
			view.showTags();
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#addTagToImage(DataObject)
	 */
	public void addTagToImage(DataObject object)
	{
		if (model.getState() == DISCARDED) return;
		if (model.isImage()) {
			model.fireTagAddition(object);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#removeTag(DataObject)
	 */
	public void removeTag(DataObject object)
	{
		if (model.getState() == DISCARDED) return;
		if (model.isImage()) {
			model.fireDataObjectDeletion(object);
			fireStateChange();
		}
	}
	
}
