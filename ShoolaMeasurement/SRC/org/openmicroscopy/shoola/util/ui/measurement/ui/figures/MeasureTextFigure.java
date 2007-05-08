/*
 * org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureTextFigure 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.figures;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.swing.Action;
import javax.swing.event.UndoableEditListener;

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
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
//Java imports

//Third-party libraries

//Application-internal dependencies

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
	extends MeasureTextArea
	implements ROIFigure
{
	public final static AttributeKey<AffineTransform>TRANSFORM = new AttributeKey<AffineTransform>("transform", null, true);

	private Rectangle2D.Double rectangle;
    /**
     * This is used to perform faster drawing and hit testing.
     */
	
	
    private Shape cachedTransformedShape;
    
	private	Rectangle2D bounds;
	private ROI			roi;
	private ROIShape 	shape;

	private MeasureEllipseFigure rect;
	
    /** Creates a new instance. */
    public MeasureTextFigure() 
    {
        this(0, 0, 0, 0);
    }
    
    public MeasureTextFigure(double x, double y, double width, double height) 
    {
    	super();
    	rect = new MeasureEllipseFigure();
    	this.setDecorator(rect);
    	FILL_COLOR.set(this, new Color(0,0,0,0));
    	STROKE_COLOR.set(this, new Color(0,0,0,0));
    	
    }
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#calculateMeasurements()
	 */
	public void calculateMeasurements() 
	{
		rect.calculateMeasurements();
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROI()
	 */
	public ROI getROI() 
	{
		return rect.getROI();
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROIShape()
	 */
	public ROIShape getROIShape() 
	{
		return rect.getROIShape();
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROI(org.openmicroscopy.shoola.util.ui.roi.model.ROI)
	 */
	public void setROI(ROI roi) {
		rect.setROI(roi);
		
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROIShape(org.openmicroscopy.shoola.util.ui.roi.model.ROIShape)
	 */
	public void setROIShape(ROIShape shape) {
		rect.setROIShape(shape);
		
	}

	
	
}


