/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageTransformMng
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

package org.openmicroscopy.shoola.agents.viewer.canvas;


//Java imports
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.viewer.ImageFactory;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ImageTransformMng
{
    
    /** Magnification factor of the main image. */
    private double                      magFactor;
    
    private List                        filters;
    
    ImageTransformMng()
    {
        setDefault();
    }
    
    void setDefault()
    {
        filters = new ArrayList();
        magFactor = 1.0;
    }
    
    double getMagFactor() { return magFactor; }
    
    void setMagFactor(double f) { magFactor = f; }
    
    void removeAllFilters() { filters.removeAll(filters); }
    
    BufferedImage buildDisplayImage(BufferedImage image)
    {
        AffineTransform at = new AffineTransform();
        at.scale(magFactor, magFactor);
        BufferedImage displayImage;
        displayImage = ImageFactory.magnifyImage(image, magFactor, at, 0);
        Iterator i = filters.iterator();
        while (i.hasNext()) {
            displayImage = ImageFactory.convolveImage(displayImage, 
                            (float[]) i.next());
            
        }
        return displayImage;
    }
    
    BufferedImage buildDisplayImage(BufferedImage image, double f)
    {
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        BufferedImage displayImage;
        displayImage = ImageFactory.magnifyImage(image, f, at, 0);
        Iterator i = filters.iterator();
        while (i.hasNext()) {
            displayImage = ImageFactory.convolveImage(displayImage, 
                            (float[]) i.next());
            
        }
        return displayImage;
    }
    
    BufferedImage filterImage(BufferedImage img, float[] filter)
    {
        filters.add(filter);
        return ImageFactory.convolveImage(img, filter); 
    }
    
}
