/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorFactory
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.editors;




//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies
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
     * Returns the {@link Editor}.
     * 
     * @param model             Reference to {@link TreeViewer}.
     *                          Mustn't be <code>null</code>.
     * @param hierarchyObject   The {@link DataObject} to edit.
     * @param editorType        The type of editor. 
     *                          One of the following constants:
     *                          {@link Editor#CREATE_EDITOR}, 
     *                          {@link Editor#PROPERTIES_EDITOR}.                  
     * @return A {@link Editor}
     */
    public static Editor getEditor(TreeViewer model,
                                    DataObject hierarchyObject,
                                    int editorType)
    { 
        return singleton.getDOEditor(model, hierarchyObject, editorType);
    }

    /** The tracked component. */
    private Editor editor;
    
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
     * @param previousType      The previous editor type.                              
     * @return A {@link Editor}
     */
    private Editor getDOEditor(TreeViewer model, DataObject ho,
                                int editorType)
    { 
        model.addPropertyChangeListener(this);
        if (editor != null) return editor;
        EditorModel m = new EditorModel(model, editorType, ho);
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
