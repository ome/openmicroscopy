/*
 * org.openmicroscopy.vis.chains.events.SelectionEvent
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.zoombrowser.events;

//Java Imports
import  java.util.EventObject;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.SelectionState;


/** 
 * An superclass for several classes of selection events
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class SelectionEvent extends EventObject {
	
	public static final int SET_SELECTED_EXECUTION =1;
	public static final int SET_SELECTED_CHAIN =   1 << 1;
	public static final int SET_SELECTED_PROJECT = 1 <<2;
	public static final int SET_SELECTED_DATASET = 1<<4;
	public static final int SET_ROLLOVER_CHAIN = 1<<6;
	
	public static final int SET_PROJECT =
			SET_SELECTED_DATASET | SET_SELECTED_PROJECT;
	
		
	
		
	private int mask =0;
	// store state so this event always has a source that is non-null
	
	public SelectionEvent(SelectionState state) {
		super(state);	
	}
	
	public SelectionEvent(SelectionState state,int mask) {
		super(state);
		this.mask = mask;	
	}
	
	public SelectionState getSelectionState() {
		return (SelectionState) getSource();
	}
	
	public int getMask() {
		return mask;
	}
	
	public boolean isEventOfType(int type) {
		return ((mask & type) == type);
	}
}