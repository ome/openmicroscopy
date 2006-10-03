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
import java.awt.Cursor;
import java.awt.Point;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindData;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClearCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

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
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
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
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        view.initialize(model, controller);
        controller.initialize(view, model);
    }
    
    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#retrieveAnnotations(DataObject)
     */
    public void retrieveAnnotations(DataObject object)
    {
        //TODO: Check state
        if ((object instanceof ImageData) || (object instanceof DatasetData)) {
            model.fireAnnotationsLoading(object);
            fireStateChange();
        }
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
     * @see ClipBoard#setFindResults(Set)
     */
    public void setFindResults(Set foundNodes)
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
     * @see ClipBoard#setAnnotationEdition(DataObject)
     */
    public void setAnnotationEdition(DataObject object)
    {
        if (model.getState() != ClipBoard.EDIT_ANNOTATIONS)
            throw new IllegalStateException("This method can only be invoked " +
                    "in the EDIT_ANNOTATIONS state.");
        if (object == null) retrieveAnnotations(object);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        retrieveAnnotations(object);
        model.getParentModel().setAnnotationEdition(object);
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#setSelectedPane(int, ImageDisplay)
     */
    public void setSelectedPane(int index, ImageDisplay node)
    {
        switch (index) {
            case FIND_PANE:
            case ANNOTATION_PANE:
            case INFO_PANE:
            case EDITOR_PANE:
                break;
            default:
                throw new IllegalArgumentException("Pane index not valid.");
        }
        if (model.getPaneIndex() == index) return;
        model.setPaneIndex(index);
        view.setSelectedPane(index);
        if (node == null)
            view.onDisplayChange(
                    model.getParentModel().getBrowser().getLastSelectedDisplay());
        if (index != ANNOTATION_PANE) discardAnnotation();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#showMenu(JComponent, Point, ImageDisplay)
     */
    public void showMenu(JComponent invoker, Point p, ImageDisplay node)
    {
        //TODO: check state
        if (node == null) throw new IllegalArgumentException("No node");
        if (invoker == null) throw new IllegalArgumentException("No invoker");
        if (p == null) throw new IllegalArgumentException("No point.");
        view.showMenu(invoker, p, node);
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getAnnotations()
     */
    public Map getAnnotations()
    {
        // TODO: check state.
        return model.getAnnotations();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getSelectedPaneIndex()
     */
    public int getSelectedPaneIndex()
    {
        //TODO: Check state
        return model.getPaneIndex();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getUserDetails()
     */
    public ExperimenterData getUserDetails()
    {
        //      TODO: Check state
        return model.getUserDetails();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#clear()
     */
    public void clear()
    {
        // TODO: check the state
        ClearCmd cmd = new ClearCmd(model.getParentModel());
        cmd.execute();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#find(Pattern, FindData)
     */
    public void find(Pattern p, FindData context)
    {
        //TODO: Check state.
        if (p == null) throw new IllegalArgumentException("No pattern.");
        if (context == null)
            throw new IllegalArgumentException("No context");
        FindRegExCmd cmd = new FindRegExCmd(model.getParentModel(), p, context);
        cmd.execute();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#editAnnotation(AnnotationData, int)
     */
    public void editAnnotation(AnnotationData data, int index)
    {
        if (data == null) throw new IllegalArgumentException("No annotation.");
        switch (index) {
            case CREATE_ANNOTATION:
                model.fireCreateAnnotation(data);
                break;
            case UPDATE_ANNOTATION:  
                model.fireUpdateAnnotation(data);
                break;
            case DELETE_ANNOTATION:
                model.fireDeleteAnnotation(data);
                break;
            default:
                throw new IllegalArgumentException("Annotation index not " +
                                                    "supported");
        }
        fireStateChange();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getUserAnnotationData()
     */
    public AnnotationData getUserAnnotationData()
    {
        // TODO Auto-generated method stub
        return model.getUserAnnotationData();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#getHierarchyObject()
     */
    public Object getHierarchyObject()
    {
        return model.getParentModel().getHierarchyObject();
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#isObjectWritable(DataObject)
     */
    public boolean isObjectWritable(DataObject ho)
    {
        // TODO Auto-generated method stub
        return model.getParentModel().isObjectWritable(ho);
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#saveObject(DataObject)
     */
    public void saveObject(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object to save.");
        model.getParentModel().saveObject(object);
    }

    /**
     * Implemented as specified by the {@link ClipBoard} interface.
     * @see ClipBoard#showProperties(DataObject)
     */
    public void showProperties(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object to edit.");
        if (model.getPaneIndex() != ClipBoard.EDITOR_PANE) {
            model.setPaneIndex(ClipBoard.EDITOR_PANE);
            view.setSelectedPane(ClipBoard.EDITOR_PANE);
        }
        EditorPane pane = ((EditorPane) model.getClipboardPane(
                                ClipBoard.EDITOR_PANE));
        pane.edit(object);
    }

}
