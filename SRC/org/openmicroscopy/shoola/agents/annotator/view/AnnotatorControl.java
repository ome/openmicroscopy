/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorControl 
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
import java.util.HashMap;
import java.util.Map;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.actions.AnnotatorAction;
import org.openmicroscopy.shoola.agents.util.annotator.actions.CloseAction;
import org.openmicroscopy.shoola.agents.util.annotator.actions.FinishAction;

/** 
 * The {@link Annotator}'s controller. 
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
class AnnotatorControl
	implements ChangeListener
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
     * Reference to the {@link Annotator} component, which, in this context,
     * is regarded as the Model.
     */
    private Annotator      	model;
    
    /** Reference to the View. */
    private AnnotatorView	view;
    
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
     * The {@link #initialize(AnnotatorView) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Annotator} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
	AnnotatorControl(Annotator model)
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
    void initialize(AnnotatorView view)
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
	AnnotatorAction getAction(Integer id)
	{
		return (AnnotatorAction) actionsMap.get(id);
	}
	
	/**
	 * Reacts to state changes.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case Annotator.READY:
				view.setStatus("", true);
				view.setOnScreen();	
				break;
			case Annotator.LOADING:
				view.setStatus(LOADING_MSG, false);
				break;
			case Annotator.DISCARDED:
				view.setVisible(false);
				view.dispose();
				break;
			case Annotator.SAVING:
				view.setStatus(SAVING_MSG, false);
				break;
		}
		
	}
	
}
