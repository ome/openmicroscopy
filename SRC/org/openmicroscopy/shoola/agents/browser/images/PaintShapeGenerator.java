/*
 * org.openmicroscopy.shoola.agents.browser.images.PaintShapeGenerator
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.images;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * A generator for rapidly reusing shape-based icon objects in paint overlay
 * methods.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class PaintShapeGenerator
{
    private static PaintShapeGenerator singleton;
    private Shape annotationNoteShape;
    
    // singleton constructor
    private PaintShapeGenerator()
    {
        annotationNoteShape = getAnnotationNoteShape();
    }
    
    // returns the annotation node shape.
    private Shape getAnnotationNoteShape()
    {
        GeneralPath paperPath = new GeneralPath();
        GeneralPath foldPath = new GeneralPath();
        GeneralPath linesPath = new GeneralPath();
        
        paperPath.moveTo(0,0);
        paperPath.lineTo(14,0);
        paperPath.lineTo(21,7);
        paperPath.lineTo(21,27);
        paperPath.lineTo(0,27);
        paperPath.closePath();
        
        foldPath.moveTo(14,0);
        foldPath.lineTo(21,7);
        foldPath.lineTo(14,7);
        foldPath.closePath();
        
        linesPath.moveTo(3,3);
        linesPath.lineTo(14,3);
        linesPath.moveTo(3,7);
        linesPath.lineTo(14,7);
        linesPath.moveTo(3,11);
        linesPath.lineTo(18,11);
        linesPath.moveTo(3,15);
        linesPath.lineTo(18,15);
        linesPath.moveTo(3,19);
        linesPath.lineTo(18,19);
        linesPath.moveTo(3,23);
        linesPath.lineTo(18,23);
        
        paperPath.append(foldPath,false);
        paperPath.append(linesPath,false);
        return paperPath;
    }
    
    /**
     * Get an instance of the PaintShapeGenerator.
     * @return See above.
     */
    public static PaintShapeGenerator getInstance()
    {
        if(singleton == null)
        {
            singleton = new PaintShapeGenerator();
        }
        return singleton;
    }
    
    /**
     * Gets the annotation note shape and shifts it to the location
     * specified by offsetX and offsetY.
     * @param offsetX The x-offset of the shape.
     * @param offsetY The y-offset of the shape.
     * @return An annotation note shape with upper-left corner shifted to (x,y).
     */
    public Shape getAnnotationNoteShape(double offsetX, double offsetY)
    {
        AffineTransform xform =
            AffineTransform.getTranslateInstance(offsetX,offsetY);
        return xform.createTransformedShape(annotationNoteShape);
    }
}
