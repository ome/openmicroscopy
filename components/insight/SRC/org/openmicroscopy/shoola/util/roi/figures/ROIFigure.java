/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.roi.figures;

import java.awt.Font;
import java.awt.Point;
import java.util.List;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;

import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;

import omero.gateway.model.ShapeSettingsData;

/** 
 * Interface that all areas of the Euclidean space <b>R</b><sup>2</sup> must
 * implement.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface ROIFigure 
	extends Figure
{
		
	/** The pattern for the numerical format. */
	public static final String FORMAT_PATTERN = "###.#";
	
	/** The default font name. */
	public static final String FONT_FAMILY = 
		ShapeSettingsData.DEFAULT_FONT_FAMILY;
	
	/** The default font name. */
	public static final int FONT_SIZE = ShapeSettingsData.DEFAULT_FONT_SIZE;
	
	/** The default font name. */
	public static final int    FONT_STYLE = Font.PLAIN; 
	
	/** The default font. */
	public static final Font  DEFAULT_FONT = new Font(FONT_FAMILY, FONT_STYLE, 
			FONT_SIZE);
	
	/** The default text. */
	public static final String DEFAULT_TEXT = "";
	
	/** Identified the <code>IDLE</code> status. */
	public static final int IDLE = 0;
	
	/** Identified the <code>MOVING</code> status. */
	public static final int MOVING = 1;
	
	/** 
	 * Sets the measurement units of the ROIFigure. 
	 * 
	 * @param units The class holding the values of the 
	 * 				microns per pixel and whether the figure should display 
	 * 				the measurements in microns or pixels.
	 */
	public void setMeasurementUnits(MeasurementUnits units);
	
	/**
	 * Sets the passed ROI.
	 * 
	 * @param roi The roi to set.
	 */
	public void setROI(ROI roi);
	
	/**
	 * Sets the shape.
	 * 
	 * @param shape The shape to set.
	 */
	public void setROIShape(ROIShape shape);
	
	/**
	 * Returns the ROI.
	 * 
	 * @return See above.
	 */
	public ROI  getROI();
	
	/**
	 * Returns the ROI shape.
	 * 
	 * @return See above.
	 */
	public ROIShape getROIShape();
	
	/** 
	 * Calculates some measurements for the ROI.
	 * Does nothing if the ROI shape is <code>null</code>.
	 */
	public void calculateMeasurements();
	
	/**
	 * Returns the type of the ROI. 
	 * 
	 * @return See above.
	 */
	public String getType();
	
	/** 
     * Returns an array of {@link Point} contained in the ROIFigure. 
     * 
     * @return See above.
     */
	public List<Point> getPoints();

	/**
	 * Returns the number of points contained in the figure.
	 * 
	 * @return See above.
	 */
	public int getSize();
	
	/**
	 * Sets the status of the figure.
	 * 
	 * @param status The value to set.
	 */
	public void setStatus(int status);
	
	/**
	 * Returns the status of the figure.
	 * 
	 * @return See above.
	 */
	public int getStatus();
	
	/**
	 * Is this roi ReadOnly, if so it will not be possible to move or
	 * resize it.
	 * @return See above.
	 */
	public boolean isReadOnly(); 
	
	/**
	 * Set this figure to be ReadOnly if readOnly true. if so it will not be 
	 * possible to move or resize it.
	 * @param readOnly See above.
	 */
	public void setReadOnly(boolean readOnly);
	
	/**
	 * Sets to <code>true</code> if the ROIFigure has been created by user 
	 * on client, <code>false</code> otherwise.
	 * 
	 * @param clientSide See above.
	 */
	public void setClientObject(boolean clientSide);
	
	/**
	 * Returns <code>true</code> if the ROIFigure has been created by user 
	 * on client, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isClientObject();
	
	/**
	 * Sets to <code>true</code> if the ROIFigure has been modified by user 
	 * on client, <code>false</code> otherwise.
	 * 
	 * @param dirty See above.
	 */
	public void setObjectDirty(boolean dirty);
	
	/**
	 * Returns <code>true</code> if the ROIFigure has been modified by user 
	 * on client, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDirty();
	
	/**
	 * Returns the list of listeners or an empty list.
	 * 
	 * @return See above.
	 */
	public List<FigureListener> getFigureListeners();
	
	/** 
	 * Indicates if the ROI can be edited if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canEdit();
	
	/** 
	 * Indicates if the ROI can be annotated if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canAnnotate();
	
	/** 
	 * Indicates if the ROI can be deleted if <code>true</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canDelete();
	
	/**
	 * Sets to <code>true</code> if the user can move/resize the shape,
	 * <code>false</code> otherwise.
	 * 
	 * @param interactable The value to set.
	 */
	public void setInteractable(boolean interactable);
	
	/** 
	 * Indicates if the user can or cannot move/resize the figure.
	 * 
	 * @return See above.
	 */
	public boolean canInteract();

}