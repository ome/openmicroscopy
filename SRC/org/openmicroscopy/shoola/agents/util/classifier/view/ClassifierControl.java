/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierControl 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.classifier.actions.ClassifierAction;
import org.openmicroscopy.shoola.agents.util.classifier.actions.CloseAction;
import org.openmicroscopy.shoola.agents.util.classifier.actions.FinishAction;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;

/** 
 * The {@link Classifier}'s controller. 
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
class ClassifierControl
	implements ChangeListener, PropertyChangeListener
{

	 /** Identifies the <code>Cancel action</code> in the Edit menu. */
    static final Integer	CANCEL = new Integer(0);
    
    /** Identifies the <code>Properties action</code> in the Edit menu. */
    static final Integer	FINISH = new Integer(1);
    
    /** The default loading message.  */
    private static final String LOADING_MSG = "Loading...";
    
    /** The default saving message.  */
    private static final String SAVING_MSG = "Saving data...";
    
	/** 
     * Reference to the {@link Classifier} component, which, in this context,
     * is regarded as the Model.
     */
    private Classifier      	model;
    
    /** Reference to the View. */
    private ClassifierView	view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map             actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
    	actionsMap.put(CANCEL, new CloseAction(model));
    	actionsMap.put(FINISH, new FinishAction(model));
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window. 
     */
    private void attachListeners()
    {
    	model.addChangeListener(this);
    	view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
	/**
     * Creates a new instance.
     * The {@link #initialize(ClassifierView) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Classifier} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    ClassifierControl(Classifier model)
	{
		if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap();
	}
	
	/**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(ClassifierView view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        createActions();
        attachListeners();
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
	ClassifierAction getAction(Integer id)
	{
		return (ClassifierAction) actionsMap.get(id);
	}
	
	/**
	 * Reacts to state changes.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case DataHandler.READY:
				view.setStatus("", true);
				view.setOnScreen();	
				break;
			case DataHandler.LOADING:
				view.setStatus(LOADING_MSG, false);
				break;
			case DataHandler.DISCARDED:
				view.setVisible(false);
				view.dispose();
				break;
			case DataHandler.SAVING:
				view.setStatus(SAVING_MSG, false);
				break;
		}
		
	}

    /**
     * Reacts to the <code>NODE_SELECTED_PROPERTY</code>
     * changes fired by the {@link TreeCheck}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
    	String name = pce.getPropertyName();
    	if (TreeCheck.NODE_SELECTED_PROPERTY.equals(name)) {
    		int i = ((Integer) pce.getNewValue()).intValue();
    		getAction(FINISH).setEnabled(i > 0);
    	} 
    }
    
}
