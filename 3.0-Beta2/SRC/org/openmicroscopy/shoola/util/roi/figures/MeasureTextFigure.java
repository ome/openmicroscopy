/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure 
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
package org.openmicroscopy.shoola.util.roi.figures;


//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.swing.Action;
import javax.swing.event.UndoableEditListener;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Connector;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.LabelFigure;
import org.jhotdraw.draw.LabeledLineConnectionFigure;
import org.jhotdraw.draw.TextAreaFigure;
import org.jhotdraw.draw.TextFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Dimension2DDouble;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
//Application-internal dependencies

import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.AREA;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.HEIGHT;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.PERIMETER;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.WIDTH;

import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasureTextFigure 
	extends TextFigure
	implements ROIFigure
{
	
	private Shape				cachedTransformedShape;
	private	Rectangle2D 		bounds;
	private ROI					roi;
	private ROIShape 			shape;

    /** Creates a new instance. */
    public MeasureTextFigure() 
    {
        this(0, 0);
    }
    
    public MeasureTextFigure(double x, double y) 
    {
    	super();
    	this.willChange();
    	this.basicSetBounds(new Point2D.Double(x, y), new Point2D.Double(x, y));
    	this.changed();
    	shape = null;
   		roi = null;
     }
    /* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROI()
	 */
	public ROI getROI() 
	{
		return roi;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROIShape()
	 */
	public ROIShape getROIShape() 
	{
		return shape;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROI(org.openmicroscopy.shoola.util.ui.roi.model.ROI)
	 */
	public void setROI(ROI roi) 
	{
		this.roi = roi;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROIShape(org.openmicroscopy.shoola.util.ui.roi.model.ROIShape)
	 */
	public void setROIShape(ROIShape shape) 
	{
		this.shape = shape;
	}


	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#calculateMeasurements()
	 */
	public void calculateMeasurements()
	{
			if(shape==null)
				return;
		
	}

	
	
}


