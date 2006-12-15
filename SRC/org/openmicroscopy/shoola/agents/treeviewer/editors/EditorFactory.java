/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorFactory
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.DataObject;

/** 
 * Factory to create and keep track of the {@link EditorUI editor}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class EditorFactory
    implements PropertyChangeListener
{
    
    /** The sole instance. */
    private static final EditorFactory singleton = new EditorFactory();
     
    /**
     * Sets the index of the selected pane in the editor 
     * when the editor mode is <code>Edit</code>.
     * 
     * @param index The pane index.
     */
    public static void setEditorSelectedPane(int index)
    { 
        singleton.editorSelectedPane = index;
    }
    
    /**
     * Returns the index of the selected pane. The value is taken into account
     * only the editor is in the <code>Edit</code> mode.
     * 
     * @return See above.
     */
    public static int getEditorSelectedPane()
    { 
        return singleton.editorSelectedPane;
    }
    
    /**
     * Returns the index of the sub-selected pane. 
     * The value is taken into account
     * only the editor is in the <code>Edit</code> mode
     * and the data object is an <code>Image</code>.
     * 
     * @return See above.
     */
    public static int getSubSelectedPane()
    { 
        return singleton.subSelectedPane;
    }
    
    /**
     * Sets the index of the sub selected pane in the editor 
     * when the editor mode is <code>Edit</code>.
     * 
     * @param index The pane index.
     */
    public static void setSubSelectedPane(int index)
    { 
        singleton.subSelectedPane = index;
    }
    
    /**
     * Returns the {@link Editor}.
     * 
     * @param model             Reference to {@link TreeViewer}.
     *                          Mustn't be <code>null</code>.
     * @param hierarchyObject   The {@link DataObject} to edit.
     * @param editorType        The type of editor. 
     *                          One of the following constants:
     *                          {@link Editor#CREATE_EDITOR}, 
     *                          {@link Editor#PROPERTIES_EDITOR}.   
     * @param parent            The parent of the object to create.
     *                          The value is taken into
     *                          account if the editor type is 
     *                          {@link Editor#CREATE_EDITOR}.             
     * @return A {@link Editor}
     */
    public static Editor getEditor(TreeViewer model,
                                    DataObject hierarchyObject,
                                    int editorType, TreeImageDisplay parent)
    { 
        return singleton.getDOEditor(model, hierarchyObject, editorType, 
                                    parent);
    }

    /** The tracked component. */
    private Editor  editor;
    
    /** The selected tabbed pane. */
    private int     editorSelectedPane;
    
    /** Either annotations or classifications. */
    private int     subSelectedPane;
    
    /** Creates a new instance. */
    private EditorFactory()
    {
        editor = null;
    }
    
    /**
     * Creates a editor.
     * 
     * @param model             Reference to {@link TreeViewer}.
     *                          Mustn't be <code>null</code>.
     * @param ho                The {@link DataObject} to edit.
     * @param editorType        The type of editor. 
     *                          One of the following constants:
     *                          {@link Editor#CREATE_EDITOR}, 
     *                          {@link Editor#PROPERTIES_EDITOR}.
     * @param parent            The parent of the object to create.
     *                          The value is taken into
     *                          account if the editor type is 
     *                          {@link Editor#CREATE_EDITOR}.                                                     
     * @return A {@link Editor}
     */
    private Editor getDOEditor(TreeViewer model, DataObject ho,
                                int editorType, TreeImageDisplay parent)
    { 
        model.addPropertyChangeListener(this);
       
        if (editor != null) return editor;
        EditorModel m = new EditorModel(model, editorType, ho, parent);
        EditorComponent component = new EditorComponent(m);
        m.initialize(component);
        component.initialize();
        editor = component;
        return editor;
    }
    
    /** 
     * Listens to property changed fired by the {@link TreeViewer}. 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(TreeViewer.REMOVE_EDITOR_PROPERTY)) {
            if (editor != null) {
                editor.discard();
                editor = null;  
            }
        }
    }
    
}
