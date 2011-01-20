/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserControl
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

//Third-party libraries

//Application-internal dependencies

/** 
 * The Browser's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class BrowserControl
	implements PropertyChangeListener
{

    /** Reference to the Model. */
    private Browser    	model;
    
    /** The View controlled by this Controller.*/
    private BrowserUI	view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserModel, BrowserUI) initialize} 
     * method should be called straight
     * after to link this Controller to the other MVC components.
     */
    BrowserControl() {}
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param model Reference to the Model component. 
     *              Mustn't be <code>null</code>.
     * @param view  Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(Browser model, BrowserUI view)
    {
        if (model == null) throw new NullPointerException("no model");
        if (view == null) throw new NullPointerException("no view");
        this.model = model;
        this.view = view;
    }

    /**
     * Listen to property changes fired by <code>ImViewer</code>.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ImViewer.COLOR_MODEL_CHANGED_PROPERTY.endsWith(name)) {
			model.onColorModelChange();
		}
	}
    
}
