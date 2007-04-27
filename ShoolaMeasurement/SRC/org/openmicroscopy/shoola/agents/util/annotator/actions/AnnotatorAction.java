/*
 * org.openmicroscopy.shoola.agents.util.annotator.actions.AnnotatorAction 
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

package org.openmicroscopy.shoola.agents.util.annotator.actions;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.view.Annotator;

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
public class AnnotatorAction
	extends AbstractAction
	implements ChangeListener
{
	
	/** Reference to the <code>Model</code>. */
	protected Annotator model;
	
	/**
     * Callback to notify of a change in the data retrieval state.
     * Subclasses override the method.
     */
	protected void onStateChange() {}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public AnnotatorAction(Annotator model)
	{
		super();
	    setEnabled(false);
	    if (model == null) throw new IllegalArgumentException("No Annotator");
	    this.model = model;
	    model.addChangeListener(this);
	}
	
	/** 
	 * Subclasses implement the method.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {}
	
	/** 
	 * Reacts to state changes fired by the Model.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case DataHandler.DISCARDED:
			case DataHandler.LOADING:
			case DataHandler.SAVING:
				setEnabled(false);
				break;
			default:
				setEnabled(true);
				break;
		}
		onStateChange();
	}

}
