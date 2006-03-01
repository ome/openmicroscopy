/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardComponent
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
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.util.LoadingWin;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;

/** 
 * Implements the {@link ClipBoard} interface to provide the functionality
 * required of the clip board component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 * @see org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardModel
 * @see org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardControl
 * @see org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardUI
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
class ClipBoardComponent
    extends AbstractComponent
    implements ClipBoard
{

    /** The Model sub-component. */
    private ClipBoardModel      model;
    
    /** The View sub-component. */
    private ClipBoardUI         view;
    
    /** The Controller sub-component. */
    private ClipBoardControl    controller;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    ClipBoardComponent(ClipBoardModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new ClipBoardControl(this);
        view = new ClipBoardUI();
    }
    
    /**
     * Links up the MVC triad.
     */
    void initialize()
    {
        model.initialize(this);
        view.initialize(controller, model);
        controller.initialize(view, model);
    }
    
    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#retrieveAnnotations(int, int)
     */
    public void retrieveAnnotations(int objectID, int annotationIndex)
    {
        model.fireAnnotationsLoading(objectID, annotationIndex);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getUI()
     */
    public JComponent getUI() { return view; }
    
    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#setAnnotations(Map)
     */
    public void setAnnotations(Map map)
    {
        if (model.getState() != ClipBoard.LOADING_ANNOTATIONS)
            throw new IllegalStateException(
                    "This method can only be invoked in the " +
                    "LOADING_ANNOTATIONS state.");
        model.setAnnotations(map);
        view.showAnnotations();
        model.updateNodeAnnotation();
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#discardAnnotation()
     */
    public void discardAnnotation()
    {
        if (model.getState() != ClipBoard.DISCARDED_ANNOTATIONS) {
            model.discardAnnotation();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#setSearchResults(Set)
     */
    public void setSearchResults(Set foundNodes)
    {
        view.setSearchResults(foundNodes);
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#discard()
     */
    public void discard()
    {
        discardAnnotation();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#createAnnotation(String)
     */
    public void createAnnotation(String text)
    {
        if (model.getState() != ClipBoard.ANNOTATIONS_READY)
            throw new IllegalStateException("This method can only be invoked " +
                    "in the ANNOTATIONS_READY state.");
        model.fireCreateAnnotation(text);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#updateAnnotation(AnnotationData)
     */
    public void updateAnnotation(AnnotationData data)
    {
        if (model.getState() != ClipBoard.ANNOTATIONS_READY)
            throw new IllegalStateException("This method can only be invoked " +
                    "in the ANNOTATIONS_READY state.");
        model.fireUpdateAnnotation(data);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#deleteAnnotation(AnnotationData)
     */
    public void deleteAnnotation(AnnotationData data)
    {
        if (model.getState() != ClipBoard.ANNOTATIONS_READY)
            throw new IllegalStateException("This method can only be invoked " +
                    "in the ANNOTATIONS_READY state.");
        model.fireDeleteAnnotation(data);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#manageAnnotationEditing(boolean)
     */
    public void manageAnnotationEditing(boolean b)
    {
        if (model.getState() != ClipBoard.EDIT_ANNOTATIONS)
            throw new IllegalStateException("This method can only be invoked " +
                    "in the EDIT_ANNOTATIONS state.");
        if (b) {
            view.manageAnnotation();
            retrieveAnnotations(model.getAnnotatedObjectID(), 
                    model.getAnnotatedObjectIndex());
        } else {
            model.setState(ANNOTATIONS_READY);
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#setPaneIndex(int, ImageDisplay)
     */
    public void setPaneIndex(int index, ImageDisplay node)
    {
        if (index != SEARCH_PANEL && index != ANNOTATION_PANEL)
            throw new IllegalArgumentException("Pane index not valid.");
        if (model.getPaneIndex() == index) return;
        model.setPaneIndex(index);
        view.setSelectedPane(index);
        if (node == null)
            view.onDisplayChange(
                    model.getParentModel().getBrowser().getSelectedDisplay());
        else
            firePropertyChange(ClipBoard.LOCALIZE_IMAGE_DISPLAY, null, node);
        if (index != ANNOTATION_PANEL) discardAnnotation();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getLoadingWin()
     */
    public LoadingWin getLoadingWin()
    {
        if (model.getState() == DISCARDED_ANNOTATIONS)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
        return model.getLoadingWin();
    }
    
}
