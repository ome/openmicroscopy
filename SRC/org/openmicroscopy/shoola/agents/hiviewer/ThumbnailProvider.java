/*
 * org.openmicroscopy.shoola.agents.hiviewer.ThumbnailProvider
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.util.image.geom.Factory;

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
public class ThumbnailProvider
    implements Thumbnail
{
    
    static final int            THUMB_MAX_WIDTH = 96; 
    static final int            THUMB_MAX_HEIGHT = 96;
    
    private static final double SCALING_FACTOR = 0.5;
    
    private ImageSummary    imgInfo;
    private int             width;  //of displayThumb. 
    private int             height; //of displayThumb.
    private BufferedImage   fullScaleThumb;
    private BufferedImage   displayThumb;
    private ImageNode       display;
    
    //TODO: this duplicates code in env.data.views.calls.ThumbnailLoader,
    //but we need size b/f img is retrieved -- b/c we vis tree need to be
    //laid out.  Sort this out.
    private void computeDims()
    {
        PixelsDescription pxd = imgInfo.getDefaultPixels();
        int sizeX = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
        int sizeY = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
        width = sizeX;
        height = sizeY;
    }
    
    void setFullScaleThumb(BufferedImage t)
    {
        fullScaleThumb = t;
        //Scale down to 48x48.
        displayThumb = scale(SCALING_FACTOR);
        if (display != null) display.repaint();
    }
    
    /** Scale the original thumbnail. */
    public BufferedImage scale(double f)
    {
        if (fullScaleThumb == null) return null;
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        BufferedImage 
            displayImage= Factory.magnifyImage(fullScaleThumb, f, at, 0);
        return displayImage;
    }
    
    public ThumbnailProvider(ImageSummary is)
    {
        imgInfo = is;
        computeDims();
    }
    
    public int getWidth() { return width; }
    
    public int getHeight() { return height; }
    
    public BufferedImage getImageFor(ImageNode node) 
    { 
        display = node;
        return displayThumb; 
    }

}
