/*
 * org.openmicroscopy.shoola.agents.browser.images.Thumbnail
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

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Iterator;
import java.util.List;

import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A view of a thumbnail/small image within the browser framework.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Thumbnail extends PImage
{
    /**
     * The base model of the thumbnail.
     */
    protected ThumbnailDataModel model;

    /**
     * Defines the list of paint methods to be executed prior to drawing
     * the image.
     */
    protected List backgroundPaintMethods;
    
    /**
     * Defines the list of paint methods to be executed prior to drawing the
     * overlays, but after drawing the image.
     */
    protected List middlePaintMethods;
    
    /**
     * Defines the list of paint methods to be executed after drawing the
     * overlays and image.
     */
    protected List foregroundPaintMethods;
    
    /**
     * The order in which thumbnail-specific overlays should be drawn.
     * Normally, a browser should call setPaintMethodZOrder() to establish the
     * z-ordering of the paint methods.
     */    
    protected PaintMethodZOrder defaultZOrder;

    /**
     * Defines if this thumbnail is mip-mapped.
     */
    protected boolean usesImageSet;

    /**
     * Constructs a thumbnail around this model (no renderer specified yet)
     * @param tdm The data model.
     */
    public Thumbnail(ThumbnailDataModel tdm) // TODO: fix
    {
        super();
        setAccelerated(true);
        this.model = tdm;
        defaultZOrder = new PaintMethodZOrder(); // not established yet
    }
    
    /**
     * Constructs a thumbnail with the corresponding backing model and the
     * specified image.
     * @param thumbImage The image of the thumbnail.
     * @param tdm The data model behind the thumbnail.
     */
    public Thumbnail(Image thumbImage, ThumbnailDataModel tdm)
    {
        super(thumbImage,true);
        this.model = tdm;
        defaultZOrder = new PaintMethodZOrder();
    }

    /**
     * Gets the underlying model for the thumbnail.
     * 
     * @return This thumbnail's underlying model.
     */
    public ThumbnailDataModel getModel()
    {
        return model;
    }

    /**
     * Sets the underlying model for the thumbnail.
     * 
     * @param model This thumbnail's new data model.
     */
    public void setModel(ThumbnailDataModel model)
    {
        if (model == null)
        {
            this.model = model;
        }
    }
    
    /**
     * Sets the set and ordering of paint methods to the specified
     * order.  These paint methods (for overlays) will always be executed
     * first.
     * 
     * @param order The set and order of paint methods 
     */
    public void setPaintMethodZOrder(PaintMethodZOrder order)
    {
        this.defaultZOrder = order;
    }
    
    /**
     * Adds this paint method to the list of overlays/paint methods to
     * be executed, in addition to the overlays specified in the Z order.
     * This paint method will be executed before the image is drawn.
     * Preferred for per-image or per-group drawing methods (such as selection)
     * where the thumbnail cannot infer its own state in an external context.
     * 
     * @param p The PaintMethod to add to the background.
     */
    public void addBackgroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            backgroundPaintMethods.add(p);
        }
    }
    
    /**
     * Adds this paint method to the list of overlays/paint methods to
     * be executed, in addition to the overlays specified in the Z order.
     * This paint method will be executed after the image is drawn, but
     * before the overlays.  Preferred for per-image or per-group drawing
     * methods (such as in response to thumbnail selection) where the thumbnail
     * cannot infer its own state in an external context.
     * 
     * @param p The PaintMethod to add between the image and standard overlays.
     */
    public void addMiddlePaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            middlePaintMethods.add(p);
        }
    }
    
    /**
     * Adds this paint method to the list of overlays/paint methods to be
     * added, in addition to the overlays specified in the Z order. This
     * paint method will be executed after the image and overlays are
     * drawn.
     * 
     * @param p The PaintMethod to add in front of the image and standard
     *          overlays.
     */
    public void addForegroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            foregroundPaintMethods.add(p);
        }
    }
    
    /**
     * Paints the Thumbnail and all its overlays.
     * @param context The Piccolo paint context.
     */
    public void paintComponent(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        
        // paint the background overlays
        for(Iterator iter = backgroundPaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(g2,this);
        }
        
        // now draw the image (methinks this will work)
        super.paint(context);
        
        // now draw the middle paint methods
        for(Iterator iter = middlePaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(g2,this);
        }
        
        // now draw the default overlays
        for(Iterator iter = defaultZOrder.getMethodOrder().iterator();
            iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(g2,this);
        }
        
        // now draw the foreground paint methods
        for(Iterator iter = foregroundPaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(g2,this);
        }
    }

}
