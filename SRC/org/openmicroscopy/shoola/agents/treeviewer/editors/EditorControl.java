/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorControl
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
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.util.FolderChooserDialog;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.DataObject;


/** 
 * The {@link Editor}'s controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class EditorControl
    implements ChangeListener, PropertyChangeListener
{
    
    /** 
     * Reference to the {@link Editor} component, which, in this context,
     * is regarded as the Model.
     */
    private Editor      model;
    
    /** Reference to the View. */
    private EditorUI    view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize(EditorUI) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Editor} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    EditorControl(Editor model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
    }
    
    /**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(EditorUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }
    
    /**
     * Browses or views the specified <code>DataObject</code>.
     * 
     * @param object The object to browse or view.
     */
    void browse(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No object to browse.");
    }

    /**
     * Closes the {@link Editor}.
     * 
     * @param b Passed <code>true</code> to close the {@link Editor}. 
     */
    void close(boolean b) { if (b) model.close(); }

    /**
     * Creates the specified <code>DataObject</code>.
     * 
     * @param object The object to create.
     */
    void createObject(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No Data object to create.");
        model.saveObject(object, TreeViewer.CREATE_OBJECT);
    }
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param object The object to update.
     */
    void updateObject(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No Data object to update.");
        model.saveObject(object, TreeViewer.UPDATE_OBJECT);
    }
  
    /**
     * Updates the specified <code>DataObject</code> and creates a annotation 
     * for this object.
     * 
     * @param data      The object to update.
     * @param object    The annotation to create.
     */
    void createAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to create.");
        model.saveObjectAndAnnotation(data, object, Editor.CREATE_ANNOTATION);
    }
    
    /**
     * Updates the specified <code>DataObject</code> and updates the specified 
     * annotation..
     * 
     * @param data      The object to update.
     * @param object    The annotation to update.
     */
    void updateAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to update.");
        model.saveObjectAndAnnotation(data, object, Editor.UPDATE_ANNOTATION);
    }
    
    /**
     * Updates the specified <code>DataObject</code> and removes the specified 
     * annotation..
     * 
     * @param data      The object to update.
     * @param object    The annotation to remove.
     */
    void deleteAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to remove.");
        model.saveObjectAndAnnotation(data, object, Editor.DELETE_ANNOTATION);
    }
    
    /** Reloads the classifications. */
    void loadClassifications() { model.loadClassifications(); }
    
    /** Retrieves the annotations. */
    void retrieveAnnotations() { model.retrieveAnnotations(); }
    
    /** Downloads the archived files. */
    void download()
    { 
    	FolderChooserDialog d = new FolderChooserDialog(view.getParentUI()); 
    	d.addPropertyChangeListener(FolderChooserDialog.LOCATION_PROPERTY, 
    								this);
    	UIUtilities.centerAndShow(d);
    }
    
    /**
     * Reacts to state changes in the {@link Editor}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane tab = (JTabbedPane) e.getSource();
            Component c = tab.getSelectedComponent();
            int index = tab.getSelectedIndex();
            if (c instanceof DOInfo) {
                DOInfo info = (DOInfo) c;
                switch (info.getInfoType()) {
					case DOInfo.INFO_TYPE:
						model.retrieveChannelsData();
						break;
				} 
                view.setEditorSelectedPane(index);
            } else {
            	view.setEditorSelectedPane(index);
                 if (index == EditorUI.PROPERTIES_INDEX) {
                 	int subIndex = model.getSelectedSubPane();
                 	if (subIndex == EditorUI.ANNOTATION_SUB_INDEX) 
                 		retrieveAnnotations();
                 	else if (subIndex == EditorUI.CLASSIFICATION_SUB_INDEX)
                 		loadClassifications();
                 }
            }
            
        } else {
        	view.onStateChanged(model.getState() == Editor.READY);
        }
    }

    /**
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FolderChooserDialog.LOCATION_PROPERTY.equals(name)) {
			String dir = (String) evt.getNewValue();
			model.download(dir);
		}
	}

}
