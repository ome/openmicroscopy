/*
 * org.openmicroscopy.shoola.agents.roi.defs.ROIShape
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.roi.defs;


//Java imports
import java.awt.Color;
import java.awt.Shape;

import org.openmicroscopy.shoola.agents.viewer.defs.ImageAffineTransform;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ROIShape
{
    /** Shape to draw. */
    private Shape                   shape;
    
    /** 
     * Type of shape, one of the constants by defined
     *  {@link org.openmicroscopy.shoola.agents.roi.ROIAgt}.
     */
    private int                     index;
    
    /** Color of the shape. */
    private Color                   lineColor;
    
    /** Channel index. */
    private int                     channelIndex;
    
    private String                  label;
    
    private int                     shapeType;
    
    private ImageAffineTransform    affineTransform;
    
    private String                  annotation;
    
    public ROIShape(Shape shape, int index, Color lineColor, int shapeType,
                    int channelIndex, ImageAffineTransform affineTransform)
    {
        this.shape = shape;
        this.index = index;
        this.lineColor = lineColor;
        this.channelIndex = channelIndex;
        this.shapeType = shapeType;
        this.affineTransform = affineTransform;
        label = "#"+index;
        annotation = null;
    }

    public ImageAffineTransform getAffineTransform()
    {
        return affineTransform;
    }
    
    public int getShapeType() { return shapeType; }
    
    public int getIndex() { return index; }

    public Shape getShape() { return shape; }
    
    public Color getLineColor() { return lineColor; }
    
    public int getChannelIndex() { return channelIndex; }
    
    public String getLabel() { return label; }
    
    public String getAnnotation() { return annotation; }
    
    public void setShape(Shape shape) { this.shape = shape; }
    
    public void setLabel(String txt) { label = txt;}
    
    public void setIndex(int index) { this.index = index; }
    
    public void setAnnotation(String txt) { annotation = txt;}
    
}
