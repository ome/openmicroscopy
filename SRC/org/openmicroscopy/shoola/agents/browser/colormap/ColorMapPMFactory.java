/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapPMFactory
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
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.AbstractPaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Generates paint methods for image classification purposes.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapPMFactory
{
    public static PaintMethod getOverlayMethod(final ColorPairModel colorModel)
    {
        PaintMethod pm = new AbstractPaintMethod()
        {
            // TODO figure out to do when image reclassified
            private Map thumbnailColorMap = new IdentityHashMap();
            private Color alpha = new Color(0,0,0,0);
            
            public void paint(PPaintContext context, Thumbnail t)
            {
                Graphics2D g = context.getGraphics();
                Rectangle2D bounds = t.getBounds().getBounds2D();
                if(thumbnailColorMap.containsKey(t))
                {
                    Paint p = (Paint)thumbnailColorMap.get(t.getModel());
                    g.setPaint(p);
                    g.fill(bounds);
                }
                else
                {
                    setupMethod(t.getModel(),colorModel);
                    Paint p = (Paint)thumbnailColorMap.get(t.getModel());
                    if(p != null) {
                        g.setPaint(p);
                        g.fill(bounds);
                    }
                }

            }
            
            private void setupMethod(ThumbnailDataModel tdm,
                                     ColorPairModel cpm)
            {
                AttributeMap attrMap = tdm.getAttributeMap();
                List attributes = attrMap.getAttributes("Classification");
                if(attributes == null)
                {
                    thumbnailColorMap.put(tdm,alpha);
                    return;
                }
                for(Iterator iter = attributes.iterator(); iter.hasNext();)
                {
                    Classification c = (Classification)iter.next();
                    Category cat = c.getCategory();
                    // BUG 117 FIX: don't show color for declassified images.
                    if(cpm.getColor(cat) != null &&
                       (c.isValid() == null || c.isValid().booleanValue()))
                    {
                        Color col = cpm.getColor(cat);
                        Color newColor = new Color(col.getRed(),col.getGreen(),
                                                   col.getBlue(),128);
                        thumbnailColorMap.put(tdm,newColor);
                        return;
                    }
                }
                thumbnailColorMap.put(tdm,alpha);
            }
        };
        return pm;
    }
    
    public static PaintMethod getHighlightMethod(final Category category)
    {
        PaintMethod pm = new AbstractPaintMethod()
        {
            private Set validSet = new HashSet();
            private Set invalidSet = new HashSet();
            
            public void paint(PPaintContext context, Thumbnail t)
            {
                if(!validSet.contains(t) && !invalidSet.contains(t))
                {
                    setupMethod(t);
                }
                if(validSet.contains(t))
                {
                    Rectangle2D bounds =
                        new Rectangle2D.Double(t.getX()-4,t.getY()-4,
                                               t.getWidth()+8,
                                               t.getHeight()+8);
                    
                    Graphics2D g2 = context.getGraphics();
                    g2.setPaint(Color.white);
                    g2.fill(bounds);
                }
            }
            
            private void setupMethod(Thumbnail t)
            {
                ThumbnailDataModel tdm = t.getModel();
                AttributeMap attrMap = tdm.getAttributeMap();
                List classifications = attrMap.getAttributes("Classification");
                for(Iterator iter = classifications.iterator(); iter.hasNext();)
                {
                    Classification c = (Classification)iter.next();
                    // BUG 117 FIX: hide this as well
                    if(c.getCategory().equals(category) &&
                       (c.isValid() == null || c.isValid().booleanValue()))
                    {
                        validSet.add(t);
                        return;
                    }
                }
            }

        };
        return pm;
    }
}
