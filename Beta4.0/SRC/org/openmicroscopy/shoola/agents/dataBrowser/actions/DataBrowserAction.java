/*
 * org.openmicroscopy.shoola.agents.dataBrowser.actions.DataBrowserAction 
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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;


//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;

/** 
 * Top class that each action should extend.
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
public class DataBrowserAction 
	extends AbstractAction
	implements ChangeListener, PropertyChangeListener
{

	 /** A reference to the Model. */
    protected DataBrowser      model;
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the {@link Browser}. Subclasses override the method.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay) {}
    
    /**
     * Callback to notify of a state change in the  {@link Browser}.
     * Subclasses override the method.
     */
    protected void onStateChange() {}
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public DataBrowserAction(DataBrowser model)
    {
    	super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("No Model.");
        this.model = model;
        model.addChangeListener(this);
    }
    
	/** 
	 * Subclasses should implement the method.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e) {}

    /**
     * Reacts to property changes in the {@link Browser}.
     * Highlights the selected node, and update the status of the
     * action.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
    	String name = evt.getPropertyName();
    	if (Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(name)) {
    		Object node = evt.getNewValue();
    		if (node instanceof ImageDisplay)
    			onDisplayChange((ImageDisplay) node);
    		else onDisplayChange(null);
    	}
    }

    /** 
     * Listens to {@link Browser} change events. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
    	int state = model.getState();
    	if (state == DataBrowser.LOADING || (state == DataBrowser.READY &&
    			model.getBrowser().getImages().size() == 0)) {
    		model.getBrowser().addPropertyChangeListener(
    				Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY, this);
    		onStateChange();
    	} else if (state == DataBrowser.READY)
    		onStateChange();
    }

}
