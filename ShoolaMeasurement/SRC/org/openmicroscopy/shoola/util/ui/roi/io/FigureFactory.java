/*
 * org.openmicroscopy.shoola.util.ui.roi.io.FigureFactory 
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
package org.openmicroscopy.shoola.util.ui.roi.io;

//Java imports
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.text.StyledDocument;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.samples.svg.Gradient;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;

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
public interface FigureFactory 
{
	public ROIFigure createRect(
            double x, double y, double width, double height,  
            Map<AttributeKey,Object> attributes);
    
    /*public ROIFigure createCircle(
            double cx, double cy, double r, 
            Map<AttributeKey,Object> attributes);*/
    
    public ROIFigure createEllipse(
            double cx, double cy, double rx, double ry, 
            Map<AttributeKey,Object> attributes);

    public ROIFigure createLine(
            double x1, double y1, double x2, double y2, 
            Map<AttributeKey,Object> attributes);

    public ROIFigure createPolyline(
            Point2D.Double[] points, 
            Map<AttributeKey,Object> attributes);
    
    public ROIFigure createPolygon(
            Point2D.Double[] points, 
            Map<AttributeKey,Object> attributes);

    /*public ROIFigure createPath(
            BezierPath[] beziers, 
            Map<AttributeKey,Object> attributes);*/
     
    /*public ROIFigure createText(
            Point2D.Double[] coordinates, double[] rotate,
            StyledDocument text,  
            Map<AttributeKey,Object> attributes);*/
    
    
    /**
     * Creates a Figure from an image element.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param width The width.
     * @param height The height.
     * @param imageData Holds the image data. Can be null, if the buffered image
     * has not been created from a file.
     * @param bufferedImage Holds the buffered image. Can be null, if the 
     * image data has not been interpreted.
     * @param attributes Figure attributes.
     */
    /*public ROIFigure createImage(double x, double y, double width, double height, 
           byte[] imageData, BufferedImage bufferedImage, Map<AttributeKey,Object> attributes);*/


    public Gradient createLinearGradient(
            double x1, double y1, double x2, double y2, 
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds);
    
    public Gradient createRadialGradient(
            double cx, double cy, double r, 
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds);
    
}


