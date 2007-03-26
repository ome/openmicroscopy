/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator.view;



//Java imports
import java.awt.Cursor;
import java.util.Map;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Implements the {@link AnnotatorEditor} interface to provide the functionality
 * required of the annotator editor component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorModel
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorView
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorControl
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorEditorComponent 
	extends AbstractComponent
	implements AnnotatorEditor
{

	/** The Model sub-component. */
    private AnnotatorEditorModel     model;
    
    /** The Controller sub-component. */
    private AnnotatorEditorControl   controller;
    
    /** The View sub-component. */
    private AnnotatorEditorView       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     * @param layout One of the constant defined by {@link AnnotatorEditor}.
     */
    AnnotatorEditorComponent(AnnotatorEditorModel model, int layout)
	{
		if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new AnnotatorEditorControl(this);
        view = new AnnotatorEditorView(layout);
	}
    
    /** Links up the MVC triad. */
    void initialize()
    {
    	controller.initialize(view);
    	view.initialize(model, controller);
    }
    
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#activate()
     */
	public void activate()
	{
		switch (model.getState()) {
	        case NEW:
	        case READY:
	        	view.onSelectedDisplay(true);
	        	model.fireAnnotationsRetrieval();
	        	fireStateChange();
	            break;
	        case DISCARDED:
	            throw new IllegalStateException(
	                    "This method can't be invoked in the DISCARDED state.");
		} 
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#discard()
     */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#getState()
     */
	public int getState() { return model.getState(); }

	 /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#cancel()
     */
	public void cancel()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked "+
					"in the DISCARDED state.");
		discard();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#setAnnotations(Map)
     */
	public void setAnnotations(Map annotations)
	{
		if (model.getState() != LOADING) {
			firePropertyChange(ANNOTATION_LOADED_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
			return;
		}
		model.setAnnotations(annotations);
		view.showAnnotations();
		firePropertyChange(ANNOTATION_LOADED_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#saveAnnotation(DataObject)
     */
	public void saveAnnotation(DataObject result)
	{
		if (model.getState() != SAVING)
			throw new IllegalStateException("This method can only be invoked "+
					"in the SAVING state.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		//model.fireAnnotationsRetrieval();
		model.setState(READY);
    	fireStateChange();
		firePropertyChange(ANNOTATED_PROPERTY, null, result);
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#delete()
     */ 
	public void delete()
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be invoked "+
			"in the READY state.");
		AnnotationData data = model.getAnnotationData();
		if (data == null) {
			UserNotifier un = AnnotatorFactory.getRegistry().getUserNotifier();
			un.notifyInfo("Annotation", "No annotation to delete");
			return;
		}
		model.fireAnnotationDelete(data);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#save()
     */
	public void save() 
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be invoked "+
			"in the READY state.");
		AnnotationData data = model.getAnnotationData();
		//if (!view.isAnnotatable()) return;
		if (!model.isAnnotated()) return;
		if (data == null) {
			DataObject ho = model.getDataObject();
            if (ho instanceof ImageData)
                data = new AnnotationData(AnnotationData.IMAGE_ANNOTATION);
            else 
                data = new AnnotationData(
                        AnnotationData.DATASET_ANNOTATION); 
            data.setText(view.getAnnotationText());
            model.fireAnnotationCreate(data);
		} else {
			data.setText(view.getAnnotationText());
			model.fireAnnotationUpdate(data);
		}
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#getUI()
     */
	public JComponent getUI() { return view; }

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#hasAnnotation()
     */
	public boolean hasAnnotation() 
	{
		if (model.getState() != READY) return false;
		return view.hasAnnotation();
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#hasDataToSave()
     */
	public boolean hasDataToSave()
	{
		if (model.getState() != READY) return false;
		return view.hasDataToSave();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#retrieveAnnotations(DataObject)
     */
	public void retrieveAnnotations(DataObject object) 
	{
		switch (model.getState()) {
			case READY:
			case NEW:
				break;
			default:
				model.cancel();
		}
		model.setDataObject(object);
		view.onSelectedDisplay(false);
		//if (object == null)
		//	view.onSelectedDisplay(false);
		//else
		if (object != null)	model.fireAnnotationsRetrieval(object);
	}
	
}
