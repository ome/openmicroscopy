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
public class ZoomCmd
{

    /** Identifies the <i>Zoom in</i> action. */
    public static final int     ZOOM_IN = 0;
    
    /** Identifies the <i>Zoom out</i> action. */
    public static final int     ZOOM_OUT = 1;
    
	/** The value by which the magnification factor is incremented. */
    static final double 		INCREMENT = 0.25;
    
    /** The value by which the magnification factor is incremented. */
    private static final double	MOUSE_INCREMENT = 0.10;
    
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
				return;
			default:
				throw new IllegalArgumentException("Index not valid.");
		}
    }
   
    /**
     * Determines and returns the index corresponding to the magnification 
     * factor.
     * 
     * @param factor The value to handle.
     * @return See above.
     */
    public static int getZoomIndex(double factor)
    {
		double f = Math.round(factor*100)/100.0;
    	
    	if (f < ZoomAction.MIN_ZOOM_FACTOR)
    		f = ZoomAction.MIN_ZOOM_FACTOR;
    	if (f > ZoomAction.MAX_ZOOM_FACTOR)
    		f = ZoomAction.MAX_ZOOM_FACTOR;
    	return ZoomAction.getIndex(f);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public ZoomCmd(ImViewer model, int index)
    {
    	if (model == null) 
    		throw new IllegalArgumentException("No model.");
    	checkIndex(index);
    	this.model = model;
    	this.index = index;
    }
    
    /** Executes the command. */
    public void execute()
    {
    	if (model.isBigImage()) {
    		int level = model.getSelectedResolutionLevel();
    		switch (index) {
				case ZOOM_IN:
					level++;
					break;
				case ZOOM_OUT:
					level--;
    		}
    		if (level >= model.getResolutionLevels() || level < 0)
    			return;
    		double f = ZoomAction.getZoomFactor(level);
    		model.setZoomFactor(f, level);
    		return;
		}
    	double f = model.getZoomFactor();
    	if (f < 0) return;
    	double zoomFactor = ZoomAction.DEFAULT_ZOOM_FACTOR;
    	switch (index) {
			case ZOOM_IN:
				if (f >= ZoomAction.MAX_ZOOM_FACTOR)
					zoomFactor = ZoomAction.MAX_ZOOM_FACTOR;
				else zoomFactor = f+MOUSE_INCREMENT;
				break;
			case ZOOM_OUT:
				if (f <= ZoomAction.MIN_ZOOM_FACTOR)
					zoomFactor = ZoomAction.MIN_ZOOM_FACTOR;
				else zoomFactor = f-MOUSE_INCREMENT;
		}
    	f = Math.round(zoomFactor*100)/100.0;
    	
    	if (f < ZoomAction.MIN_ZOOM_FACTOR)
    		f = ZoomAction.MIN_ZOOM_FACTOR;
    	if (f > ZoomAction.MAX_ZOOM_FACTOR)
    		f = ZoomAction.MAX_ZOOM_FACTOR;
    	
    	model.setZoomFactor(f, getZoomIndex(zoomFactor));
    }
    
}
