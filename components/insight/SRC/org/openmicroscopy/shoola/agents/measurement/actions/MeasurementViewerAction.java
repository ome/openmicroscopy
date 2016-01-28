/*
 * org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction 
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

package org.openmicroscopy.shoola.agents.measurement.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;

/** 
 * Top class that each action should extend.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class MeasurementViewerAction 
	extends AbstractAction
	implements ChangeListener
{
	
	/** Reference to the Model. */
	protected MeasurementViewer model;
	
	 /** The name of the action. */
    protected String			name;
    
    /** Sub-classes should implement this method. */
    protected void onStateChange() {}
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model. Mustn't be <code>null</code>.
	 */
	public MeasurementViewerAction(MeasurementViewer model)
	{
		 super();
	     if (model == null) throw new NullPointerException("No model.");
	     this.model = model;
	     setEnabled(false);
	     model.addChangeListener(this);
	}

	/**
	 * Returns the name of the action.
	 * 
	 * @return See above.
	 */
    public String getName()
    { 
    	 if (name == null || name.length() == 0)
             return (String) getValue(Action.NAME);  
    	return name; 
    } 
    
	/** 
     * Reacts to state changes in the {@link MeasurementViewer}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		setEnabled(model.getState() == MeasurementViewer.READY);
		onStateChange();
	}

	/** 
     * Implemented by sub-classes.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e) {}
	
	/**
     * Overridden to return the name of the action.
     * @see java.lang.Object#toString()
     */
    public String toString() { return getName(); }

}
