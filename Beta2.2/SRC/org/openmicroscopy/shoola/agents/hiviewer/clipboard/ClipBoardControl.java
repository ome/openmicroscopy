/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardControl
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;


//Java imports
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;

import pojos.DataObject;

/** 
 * The {@link ClipBoard}'s controller.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk 
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
    
    /** Message displayed when the annotation is sent back to the server. */
    private static final String LOADING_MSG = "Loading...";
    
    /** The {@link ClipBoardUI} View. */
    private ClipBoardUI         view;
    
    /** The {@link ClipBoardModel} Model. */
    private ClipBoardModel      model;
    
    /** The {@link ClipBoard} Component. */
    private ClipBoard           component;
    
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
     * @param view The {@link ClipBoardUI} view. Mustn't be <code>null</code>.
     * @param model The {@link ClipBoardModel} model.
     *              Mustn't be <code>null</code>.
     */
    void initialize(ClipBoardUI view, ClipBoardModel model)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.view = view;
        this.model = model;
        component.addChangeListener(this);
        view.initListeners();
        model.getParentModel().addChangeListener(this);
        AnnotationPane pane = (AnnotationPane) 
        			model.getClipboardPane(ClipBoard.ANNOTATION_PANE);
        pane.registerListener(this);
    }
    
    /**
     * Sets the selected tabbed pane. If the Annotation tabbed pane is
     * deselected any ongoing data loading is cancelled.
     * 
     * @param index The index of the selected pane.
     */
    void setSelectedPane(int index) { component.setSelectedPane(index, null); }
    
    /**
     * Reacts to a specific property change fired by the browser.
     * and the component hosting the result of a <code>Search</code> action.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(Browser.SELECTED_DISPLAY_PROPERTY)) {
        	Browser browser = model.getParentModel().getBrowser();
        	if (browser != null) {
        		 ImageDisplay node = browser.getLastSelectedDisplay();
                 view.onDisplayChange(node);
        	}
           
        } else if (name.equals(FindPane.SELECTED_PROPERTY)) {
        	Browser browser = model.getParentModel().getBrowser();
        	if (browser != null) {
        		ImageDisplay node = (ImageDisplay) pce.getNewValue();
        		browser.setSelectedDisplay(node);
                model.getParentModel().scrollToNode(node);
        	}
        	
        } else if (name.equals(Browser.MOUSE_OVER_PROPERTY)) {
        	Object n = pce.getNewValue();
        	if (n instanceof ImageDisplay)
        		view.onDisplayChange((ImageDisplay) n);
        	else view.onDisplayChange(null);
        } else if (AnnotatorEditor.ANNOTATED_PROPERTY.equals(name)) {
        	DataObject object = (DataObject) pce.getNewValue();
        	AnnotationPane p = ((AnnotationPane) 
            		model.getClipboardPane(ClipBoard.ANNOTATION_PANE));
        	if (object == null) p.retrieveAnnotation(object);
            view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             
            List<DataObject> l = new ArrayList<DataObject>(1);
            l.add(object);
            model.getParentModel().onDataObjectSave(l);
            p.retrieveAnnotation(object);
        }
    }

    /**
     * Listens to change events. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent ce)
    {
        Object source = ce.getSource();
        if (source instanceof HiViewer) {
            HiViewer parentModel = model.getParentModel();
            if (parentModel.getState() == HiViewer.READY) {
                parentModel.getBrowser().addPropertyChangeListener(
                        Browser.SELECTED_DISPLAY_PROPERTY, this);
                parentModel.getBrowser().addPropertyChangeListener(
                        Browser.MOUSE_OVER_PROPERTY, this);
                //view.initListeners();
            }  
        } else if (source instanceof ClipBoard) {
            switch (model.getState()) {
                //We don't know how long the call is going to take so set to
                // indeterminate
                case ClipBoard.LOADING_CLASSIFICATIONS:
                case ClipBoard.LOADING_CHANNELS_METADATA:
                    model.getParentModel().setStatus(LOADING_MSG, -1);
                    break;
                case ClipBoard.READY:
                case ClipBoard.CLASSIFICATIONS_READY:
                    model.getParentModel().setStatus("", -1);
                    break;
            }
        }
    }

    /** Forwards call to the {@link ClipBoard}. */
	void removeRollOver() { component.removeRollOver(); }

}
