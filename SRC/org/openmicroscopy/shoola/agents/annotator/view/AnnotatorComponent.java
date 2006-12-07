/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorComponent 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.annotator.view;

//Java imports
import java.awt.Cursor;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.AnnotationData;

/** 
 * Implements the {@link Annotator} interface to provide the functionality
 * required of the annotator component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorModel
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorView
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorControl
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
class AnnotatorComponent
	extends AbstractComponent
	implements Annotator
{

	 /** The Model sub-component. */
    private AnnotatorModel     model;
    
    /** The Controller sub-component. */
    private AnnotatorControl   controller;
    
    /** The View sub-component. */
    private AnnotatorView       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
	AnnotatorComponent(AnnotatorModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new AnnotatorControl(this);
        view = new AnnotatorView();
	}
	
	/** Links up the MVC triad. */
    void initialize()
    {
    	controller.initialize(view);
    	view.initialize(model, controller);
    }
    
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see Annotator#activate()
     */
	public void activate()
	{
		switch (model.getState()) {
	        case NEW:
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
     * @see Annotator#discard()
     */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see Annotator#getState()
     */
	public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see Annotator#finish()
     */
	public void finish()
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be " +
					"invoked in the READY state.");
		AnnotationData d = model.getAnnotationType();
		d.setText(view.getAnnotationText());
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		model.fireAnnotationSaving(d);
		fireStateChange();
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see Annotator#cancel()
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
     * @see Annotator#setAnnotations(Map)
     */
	public void setAnnotations(Map annotations)
	{
		if (model.getState() != LOADING)
			throw new IllegalStateException("This method can only be invoked "+
					"in the LOADING state.");
		model.setAnnotations(annotations);
		view.showAnnotations();
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see Annotator#saveAnnotations(List)
     */
	public void saveAnnotations(List results)
	{
		if (model.getState() != SAVING)
			throw new IllegalStateException("This method can only be invoked "+
					"in the SAVING state.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		firePropertyChange(ANNOTATED_PROPERTY, null, results);
	}

}
