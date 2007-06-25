/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ZoomCmd 
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * Sets the zoom factor.
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
class ZoomCmd
{

	/** The value by the magnification factor is incremented. */
    static final double INCREMENT = 0.25;
    
    /** Identifies the <i>Zoom in</i> action. */
    static final int     ZOOM_IN = 0;
    
    /** Identifies the <i>Zoom out</i> action. */
    static final int     ZOOM_OUT = 1;
    
    /** Identifies the <i>Zoom fit</i> action. */
    static final int     ZOOM_FIT = 2;
    
    /** One of the constants defined by this class. */
    private int  index;
    
    /** The Model. */
    private ImViewer  	model;
    
    /**
     * Controls if the passed index is valid.
     * 
     * @param i The index to control
     */
    private void checkIndex(int i)
    {
    	switch (i) {
			case ZOOM_IN:
			case ZOOM_OUT:
			case ZOOM_FIT:
				return;
	
			default:
				throw new IllegalArgumentException("Index not valid.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    ZoomCmd(ImViewer model, int index)
    {
    	if (model == null) 
    		throw new IllegalArgumentException("No model.");
    	checkIndex(index);
    	this.model = model;
    	this.index = index;
    }
    
    /** Executes the command. */
    void execute()
    {
    	double f = model.getZoomFactor();
    	if (f < 0) return;
    	double zoomFactor = ZoomAction.DEFAULT_ZOOM_FACTOR;
    	switch (index) {
			case ZOOM_IN:
				if (f >= ZoomAction.MAX_ZOOM_FACTOR)
					zoomFactor = ZoomAction.MAX_ZOOM_FACTOR;
				else zoomFactor = f+INCREMENT;
				break;
			case ZOOM_OUT:
				if (f <= ZoomAction.MIN_ZOOM_FACTOR)
					zoomFactor = ZoomAction.MIN_ZOOM_FACTOR;
				else zoomFactor = f-INCREMENT;
				break;
			case ZOOM_FIT:
				zoomFactor = ZoomAction.DEFAULT_ZOOM_FACTOR;
		}
    	model.setZoomFactor(zoomFactor, ZoomAction.getIndex(zoomFactor));
    }
    
}
