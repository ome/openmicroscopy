/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardControl
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;





//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.AnnotationData;

/** 
 * 
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardControl
    implements ChangeListener, PropertyChangeListener
{

    /** The {@link ClipBoardUI} View. */
    private ClipBoardUI         view;
    
    /** The {@link ClipBoardModel} Model. */
    private ClipBoardModel      model;
    
    /** The {@link ClipBoard} Component. */
    private ClipBoard  component;
    
    /**
     * Handles the selection of a new node in the {@link Browser} component.
     * 
     * @param pce The event.
     */
    private void handleBrowserSelectedDisplay(PropertyChangeEvent pce)
    {
        if (!(pce.getNewValue().equals(pce.getOldValue())) 
                && pce.getNewValue() != null) {
            ImageDisplay oldNode, newNode;
            Colors colors = Colors.getInstance();
            newNode = (ImageDisplay) pce.getNewValue();
            newNode.setHighlight(colors.getSelectedHighLight(newNode));
            if (pce.getOldValue() != null) {
                oldNode = (ImageDisplay) pce.getOldValue();
                oldNode.setHighlight(
                        colors.getDeselectedHighLight(oldNode));
            }
        }
        view.onDisplayChange(
                model.getParentModel().getBrowser().getSelectedDisplay());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param component A reference to the model. Mustn't be <code>null</code>.
     */
    ClipBoardControl(ClipBoard component)
    {
        if (component == null) throw new NullPointerException("No component.");
        this.component = component;
    }

    /**
     * Links the MVC triad.
     * 
     * @param view The {@link ClipBoardUI} view.
     * @param model The {@link ClipBoardModel} model.
     */
    void initialize(ClipBoardUI view, ClipBoardModel model)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.view = view;
        this.model = model;
        model.getParentModel().addChangeListener(this);
    }

    /**
     * Retrieves the annotations for the specified data object.
     * 
     * @param objectID The ID of the data object.
     * @param annotationIndex The annotation index.
     */
    void retrieveAnnotations(int objectID, int annotationIndex)
    {
        component.retrieveAnnotations(objectID, annotationIndex);
    }
    
    /**
     * Sets the selected tabbed pane. If the Annotation tabbed pane is
     * deselected any ongoing data loading is cancelled.
     * 
     * @param index The index of the selected pane.
     */
    void setPaneIndex(int index)
    {
        model.setPaneIndex(index);
        view.onDisplayChange(
                model.getParentModel().getBrowser().getSelectedDisplay());
    }
    
    /**
     * Updates the specified annotation.
     * 
     * @param data The annotation data object.
     */
    void updateAnnotation(AnnotationData data)
    {
        component.updateAnnotation(data);
    }
    
    /**
     * Creates a new annotation for the currently selected data object.
     * 
     * @param txt The text of the annotation.
     */
    void createAnnotation(String txt)
    {
        component.createAnnotation(txt);
    }
    
    /**
     * Deletes the specified annotation.
     * 
     * @param data The annotation data objetc.
     */
    void deleteAnnotation(AnnotationData data)
    {
        component.deleteAnnotation(data);
    }
    
    void discardAnnotation() { component.discardAnnotation(); }
    
    /**
     * Reacts to a specific property change fired by the browser.
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propName = pce.getPropertyName();
        if (propName.equals(Browser.SELECTED_DISPLAY_PROPERTY))
            handleBrowserSelectedDisplay(pce); 
    }

    /** Listens to change events. */
    public void stateChanged(ChangeEvent ce)
    {
        Object source = ce.getSource();
        if (source instanceof HiViewer) {
            HiViewer parentModel = model.getParentModel();
            if (parentModel.getState() == HiViewer.LOADING_THUMBNAILS) {
                parentModel.getBrowser().addPropertyChangeListener(
                        Browser.SELECTED_DISPLAY_PROPERTY, this);
            }  
        }
    }

}
