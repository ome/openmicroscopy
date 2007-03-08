/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierComponent 
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
package org.openmicroscopy.shoola.agents.util.classifier.view;



//Java imports
import java.awt.Cursor;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandlerTranslator;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
* Implements the {@link Classifier} interface to provide the functionality
 * required of the classifier component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierModel
 * @see org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierView
 * @see org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierControl
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
class ClassifierComponent 
	extends AbstractComponent
	implements Classifier
{

	/** The Model sub-component. */
    private ClassifierModel     model;
    
    /** The Controller sub-component. */
    private ClassifierControl   controller;
    
    /** The View sub-component. */
    private ClassifierView       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    ClassifierComponent(ClassifierModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new ClassifierControl(this);
        view = new ClassifierView();
	}
	
	/** Links up the MVC triad. */
    void initialize()
    {
    	controller.initialize(view);
    	view.initialize(model, controller);
    }
    
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#activate()
     */
	public void activate()
	{
		switch (model.getState()) {
	        case NEW:
	        	model.fireClassificationPathsLoading();
	        	fireStateChange();
	            break;
	        case DISCARDED:
	            throw new IllegalStateException(
	                    "This method can't be invoked in the DISCARDED state.");
		} 
	}

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#discard()
     */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}
	
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#cancel()
     */
	public void cancel()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked "+
					"in the DISCARDED state.");
		discard();
	}

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#finish()
     */
	public void finish()
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be " +
					"invoked in the READY state.");
		Set categories = view.getSelectedPaths();
		if (categories == null || categories.size() == 0) {
			UserNotifier un = ClassifierFactory.getRegistry().getUserNotifier();
            un.notifyInfo("Categorisation", "No category selected."); 
            return; 
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		view.finish();
		controller.getAction(ClassifierControl.FINISH).setEnabled(false);
		model.fireClassificationsSaving(categories);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#getState()
     */
	public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#saveClassifications(Set)
     */
	public void saveClassifications(Set results)
	{
		if (model.getState() != SAVING)
			throw new IllegalStateException("This method can only be " +
					"invoked in the SAVING state.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		firePropertyChange(CLASSIFIED_PROPERTY, null, results);
	}

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#setClassifications(Set)
     */
	public void setClassifications(Set paths)
	{
		if (model.getState() != LOADING)
			throw new IllegalStateException("This method can only be " +
					"invoked in the LOADING state.");
		if (paths == null)
			throw new IllegalArgumentException("No classifications paths.");
		long userID = model.getUserID();
        long groupID = model.getRootGroupID();
		Set nodes = DataHandlerTranslator.transformDataObjectsCheckNode(paths,
                userID, groupID);
		model.setClassificationPaths(nodes);
		view.showClassifications();
		fireStateChange();
	}

}
