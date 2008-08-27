 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 *	The Controller in the Browser MVC.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BrowserControl 
	implements ChangeListener
{

	/** Identifies the <code>Collapse</code> action. */
	static final Integer    COLLAPSE = new Integer(0);
	
	/** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser     			model;
    
    /** Reference to the View. */
    private BrowserUI   			view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
       // actionsMap.put(COLLAPSE, new CollapseAction(model));
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserUI) initialize} method 
     * should be called straight after to link this Controller to the other 
     * MVC components.
     * 
     * @param model  Reference to the {@link Browser} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    BrowserControl(Browser model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap<Integer, Action>();
        createActions();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }
    
    
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }
	
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
    	/*	Not sure if I need to use states??
    
    	int state = model.getState();
    	switch (state) {
			case Browser.BROWING_DATA:
				
				break;
	
			default:
				break;
		}
		*/
		//view.onStateChanged(state == Browser.READY);
    }
}
