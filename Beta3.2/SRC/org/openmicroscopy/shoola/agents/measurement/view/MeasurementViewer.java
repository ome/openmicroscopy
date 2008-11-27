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
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;

//Application-internal dependencies
import ome.model.core.Pixels;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
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
    
    /** Flag to denote the <i>Analyse shape</i> state. */
    public static final int     ANALYSE_SHAPE = 4;
    
    /** Flag to denote the <i>Analyse roi</i> state. */
    public static final int     ANALYSE_ROI = 5;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 6;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 7;

    /** Flag to denote the <i>Value adjusting</i> state. */
    public static final int     VALUE_ADJUSTING = 8;
 
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
     * Returns the {@link MeasurementViewerUI View}.
     * 
     * @return See above.
     */
    public JFrame getUI();

    /** Cancels any ongoing data loading. */
	public void cancel();

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
	 * Closes the window, before closing, ask the user if he/she wants
	 * to save the changes.
	 */
	public void close();

	/**
	 * Sets the visibilty of the component depending on the passed
	 * parameter.
	 * 
	 * @param b Pass <code>true</code> to display the component on screen.
	 * 			<code>false</code> otherwise.
	 */
	public void iconified(boolean b);

	/** The data has changed. */
	public void setDataChanged();
	
	/** Loads the ROI. */
	public void loadROI();

	/** Saves the ROI. */
	public void saveROI();

	/** Rebuild the results table in the measurement results component. */
	public void refreshResultsTable();
	
	/** Rebuild the table in the measurement manager component. */
	public void rebuildManagerTable();
	
	/** Save the results table in the measurement results component. */
	public void saveResultsTable();
	
	/**
	 * Invokes when a figures attributes have changed. 
	 * The model needs to manage the logic 
	 * of how this affects the ROI, ROIShape parent of the figure. 
	 * 
	 * @param key	The modified attribute.
	 * @param fig	The affected figure.
	 */
	public void figureAttributeChanged(AttributeKey key, ROIFigure fig);

	/** Brings up on screen the results wizard. */
	public void showResultsWizard();

	/** Brings up on screen the ROI Assistant. */
	public void showROIAssistant();
	
	/**
	 * Show the ROI assistant for the roi.
	 * @param roi see above.
	 */
	public void showROIAssistant(ROI roi);
	
	/** 
	 * Shows the measurements in the figures in Microns. 
	 * 
	 * @param show 	Sets the units to <code>microns</code> if <code>true</code>,
	 * 				to <code>pixels</code> otherwise.
	 */
	public void showMeasurementsInMicrons(boolean show);
	
	/**
	 * Sets the collection of pairs
	 * (active channel's index, active channel's color).
	 * 
	 * @param activeChannels The value to set.
	 */
	public void setActiveChannels(Map activeChannels);

	/**
	 * Sets the collection of pairs
	 * (active channel's index, active channel's color).
	 * 
	 * @param channels The value to set.
	 */
	public void setActiveChannelsColor(Map channels);
	
	/**
	 * Sets the stats computed on a collection of shapes.
	 * 
	 * @param result
	 */
	public void setStatsShapes(Map result);
	
	/**
	 * Analyses the specifed shape.
	 * 
	 * @param shape The shape to analyse. Mustn't be <code>null</code>.
	 */
	//public void analyseShape(ROIShape shape);

	/**
	 * Analyses the specifed list of ROIShapes.
	 * 
	 * @param shape The list of ROIShapes to analyse. Mustn't be <code>null</code>.
	 */
	public void analyseShapeList(List<ROIShape> shape);

	
	/**
	 * Analyses the specifed ROI.
	 * 
	 * @param roi The shape to analyse. Mustn't be <code>null</code>.
	 */
	//public void analyseROI(ROI roi);

	/**
	 * Sets the metadata.
	 * 
	 * @param list The value to set.
	 */
	public void setChannelMetadata(List list);
	
	/** 
	 * Returns the selected figures in the view.
	 * 
	 * @return the selected figures in the dataview.
	 */
	public Collection getSelectedFigures();
	
	/** 
	 * Attaches listeners to the newly loaded ROI.
	 * 
	 * @param roiList list of the newly loaded ROI.
	 */
	public void attachListeners(List<ROI> roiList);
	
	/**
	 * Creates single figures or multiple figures. 
	 * 
	 * @param createSingleFig Create a single figure and go back to selection 
	 * 						tool.
	 */
	public void createSingleFigure(boolean createSingleFig);

	/** Saves the roi data and discards. */
	public void saveAndDiscard();
	
	/** Moves the window to the front. */
    public void toFront();

    /**
     * Sets the icon of the window.
     * 
     * @param thumbnail The icon to set.
     */
	public void setIconImage(BufferedImage thumbnail);
	
}
