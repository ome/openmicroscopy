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


import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.jhotdraw.draw.AttributeKey;

import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.TagAnnotationData;
import pojos.WorkflowData;

import org.openmicroscopy.shoola.agents.util.ui.PermissionMenu;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.FileAnnotationData;

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

	/** Identifies <code>all</code> the objects.*/
	public static final int ALL = PermissionMenu.ALL;
	
	/** Identifies the objects added by current user.*/
	public static final int ME = PermissionMenu.ME;
	
	/** Identifies the objects added by others.*/
	public static final int OTHER = PermissionMenu.OTHER;
	
	/** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading data</i> state. */
    public static final int     LOADING_DATA = 2;
    
    /** Flag to denote the <i>Loading ROI</i> state. */
    public static final int     LOADING_ROI = 3;
    
    /** Flag to denote the <i>Analyse shape</i> state. */
    public static final int     ANALYSE_SHAPE = 4;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 5;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 6;

    /** Flag to denote the <i>Value adjusting</i> state. */
    public static final int     VALUE_ADJUSTING = 7;
 
    /** Flag to denote the <i>Saving ROI</i> state. */
    public static final int     SAVING_ROI = 8;
    
    /** Bound property indicating that the ROI component has changed. */
    public static final String	ROI_CHANGED_PROPERTY = "roiChanged";
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @param measurements The measurements to load if any.
     * @param HCSData Flag indicating if the tool is for HCS data.
     * @param isBigImage Flag indicating if the tool is for Big Image data. 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate(List<FileAnnotationData> measurements, boolean HCSData,
    		boolean isBigImage);
    
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
	 * Closes the window, before closing, ask the user if he/she wants
	 * to save the changes.
	 */
	public void close();

	/**
	 * Sets the visibility of the component depending on the passed
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
	
	/**
	 * Invokes when a figures attributes have changed. 
	 * The model needs to manage the logic 
	 * of how this affects the ROI, ROIShape parent of the figure. 
	 * 
	 * @param key	The modified attribute.
	 * @param fig	The affected figure.
	 */
	public void figureAttributeChanged(AttributeKey key, ROIFigure fig);

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
	 * Analyses the specified list of ROIShapes.
	 * 
	 * @param shape The list of ROIShapes to analyse. 
	 * 				Mustn't be <code>null</code>.
	 */
	public void analyseShapeList(List<ROIShape> shape);
	
	/** 
	 * Returns the selected figures in the view.
	 * 
	 * @return the selected figures in the dataview.
	 */
	public Collection getSelectedFigures();
	
	/**
	 * Creates single figures or multiple figures. 
	 * 
	 * @param createSingleFig Create a single figure and go back to selection 
	 * 						tool.
	 */
	public void createSingleFigure(boolean createSingleFig);
	
	/** Moves the window to the front. */
    public void toFront();

    /**
     * Sets the icon of the window.
     * 
     * @param thumbnail The icon to set.
     */
	public void setIconImage(BufferedImage thumbnail);
	
	/**
	 * Sets the rendered image either a buffered image or a texture data.
	 * 
	 * @param rndImage	The rendered image.
	 */
	public void setRndImage(Object rndImage);
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasROIToSave();

	/**
	 * Sets the ROI loaded from the server.
	 * 
	 * @param result The ROI.
	 */
	public void setServerROI(Collection result);
	
	/**
	 * Returns <code>true</code> if the tool hosts server ROI,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isHCSData();

	/** 
	 * Returns the title of the window.
	 * 
	 * @return See above
	 */
	public String getViewTitle();

	/**
	 * Saves the ROI to the server. 
	 * 
	 * @param close Indicate to close or not the window after saving.
	 */
	public void saveROIToServer(boolean close);

	/** 
	 * Called when the results  have been loaded from the server.
	 * If the results are null, then try and load ROI from an XML file if 
	 * possible.
	 * 
	 * @param result The ROI .
	 */
	public void setLoadingFromServerClient(Collection result);

	/**
	 * The return result after the ROI has been saved to the server. 
	 * 
	 * @param result The List of ROIData that have been saved.
	 */
	public void setUpdateROIComponent(Collection result);

	/**
	 * Create a new workflow
	 */
	public void createWorkflow();

	/**
	 * Set the current workflow to workflowNamespace. 
	 * @param workflowNamespace The general name of the workflow. 
	 */
	public void setWorkflow(String workflowNamespace);

	/**
	 * Set the current keyword of the workflow to keyword. 
	 * @param keyword The keyword of the workflow. 
	 */
	public void setKeyword(List<String> keyword);
	
	/**
	 * Returns <code>true</code> if the specified image can be annotated
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @return See above.
	 */
	public boolean canAnnotate();
	
	/**
	 * Returns <code>true</code> if the specified image can be annotated
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @return See above.
	 */
	public boolean canDelete();
	
	/**
	 * Set the workflows in the measurement tool to be list passed.
	 * @param workflows See above.
	 */
	public void setWorkflowList(List<WorkflowData> workflows);

	/** 
	 * Deletes all ROIs owned by the user currently logged in. 
	 * 
	 * @param index One of the delete levels indicating by this class.
	 */
	public void deleteAllROIs(int index);
	
	/**
	 * Returns <code>true</code> if the current user has ROI that can be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasROIToDelete();

	/**
	 * Returns <code>true</code> if the user is not an owner nor an admin,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMember();

	/**
	 * Invokes when the channels have been modified. Updates the values
	 * displayed in the measurement tool.
	 * 
	 * @param channels The channels to handle.
	 */
	public void onUpdatedChannels(List<ChannelData> channels);

	/**
	 * Exports the graph as JPEG or PNG.
	 */
	public void exportGraph();

	/**
	 * Sets the annotations associated to the shapes.
	 *
	 * @param result The roi annotations to set.
	 */
    public void setROIAnnotations(Map<DataObject, StructuredDataResults> result);

    /**
     * Sets the tags.
     *
     * @param tags The value to set.
     */
    public void setExistingTags(Collection tags);

    /** Loads and displays the existing tags.*/
    public void loadTags();

    /**
     * Tags the selected figures.
     *
     * @param tags The tags to use.
     */
    public void tagSelectedFigures(List<AnnotationData> tags);

    /** Notifies that the annotations have been saved. Reloads.*/
    public void onAnnotationSaved();
}
