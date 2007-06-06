/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer 
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
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.io.InputStream;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;

import org.jhotdraw.draw.AttributeKey;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the measurement component. 
 * The Viewer provides a top-level window hosting the controls and 
 * UI components displaying information about Regions of Interest.
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
public interface MeasurementViewer
	extends ObservableComponent
{

	/** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading data</i> state. */
    public static final int     LOADING_DATA = 2;
    
    /** Flag to denote the <i>Loading ROI</i> state. */
    public static final int     LOADING_ROI = 3;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 4;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 5;
 
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
	
	/**
	 * Returns <code>true</code> if the viewer is recycled, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRecycled();
	
	/**
     * Returns the {@link MeasurementViewerUI View}.
     * 
     * @return See above.
     */
    public JFrame getUI();

    /** Cancels any ongoing data loading. */
	public void cancel();
	
	/**
	 * Sets the dimensions of the pixels set.
	 * 
	 * @param dims The value to set.
	 */
	public void setPixelsDimensions(PixelsDimensions dims);

	/**
	 * Invokes when a new plane is selected or the image has been magnified.
	 * 
	 * @param defaultZ		The selected z-section.
	 * @param defaultT		The selected timepoint.
	 * @param magnification	The image's magnification factor.
	 */
	public void setMagnifiedPlane(int defaultZ, int defaultT, 
									double magnification);
	
	/**
	 * Sets the collection of ROIs for the pixels set.
	 * 
	 * @param rois The value to set.
	 */
	public void setROI(InputStream rois);

	/**
	 * Sets the set of pixels this measurement tool is for.
	 * 
	 * @param pixels The value to set.
	 */
	public void setPixels(Pixels pixels);
	
	/** 
	 * Closes the application.
	 * 
	 *  @param post Pass <code>true</code> to post an event, 
	 *  			<code>false</code> otherwise.
	 */
	public void close(boolean post);

	/**
	 * Sets the visibilty of the component depending on the passed
	 * parameter.
	 * 
	 * @param b Pass <code>true</code> to display the component on screen.
	 * 			<code>false</code> otherwise.
	 */
	public void iconified(boolean b);

	/** Loads the ROI. */
	public void loadROI();

	/** Saves the ROI. */
	public void saveROI();

	/** Rebuild the ROI table in the ROI manager component. */
	public void rebuildManagerTable();

	/** Rebuild the results table in the measurement results component. */
	public void refreshResultsTable();

	/** Save the results table in the measurement results component. */
	public void saveResultsTable();
	
	/**
	 * A figures attributes hae changed. The model needs to manage the logic 
	 * of how this affects the ROI, ROIShape parent of the figure. 
	 * 
	 * @param key
	 * @param fig
	 */
	public void figureAttributeChanged(AttributeKey key, ROIFigure fig);

}
