/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapPMFactory
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

/**
 * Constructs paint methods based on current heat map parameters.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapPMFactory
{
    /**
     * Returns an overlay paint method for the thumbnail.
     * @param mode
     * @param attributeName
     * @param elementName
     * @param scale
     * @param coldColor
     * @param warmColor
     * @return
     */
    public static PaintMethod getPaintMethod(final HeatMapMode mode,
                                             final String attributeName,
                                             final String elementName,
                                             final Scale scale,
                                             final Color coldColor,
                                             final Color warmColor)
    {
        PaintMethod pm = new PaintMethod()
        {
            private Map thumbnailColorMap = new IdentityHashMap();
            private Color alpha = new Color(0,0,0,0);
            
            public void paint(Graphics2D g, Thumbnail t)
            {
                Rectangle2D region = t.getBounds().getBounds2D();
                if(thumbnailColorMap.containsKey(t.getModel()))
                {
                    Paint p = (Paint)thumbnailColorMap.get(t);
                    if(p != null)
                    {
                        g.setPaint(p);
                        g.fill(region);
                    }
                }
                else
                {
                    setupMethod(t.getModel()); // TODO: change to t, show variance
                    Paint p = (Paint)thumbnailColorMap.get(t.getModel());
                    if(p != null)
                    {
                        g.setPaint(p);
                        g.fill(region);
                    }
                }
            }
            
            /**
             * Sets up the paint method so the data doesn't have to be
             * reassessed with each paint() call.  That would suck.
             * TODO: probably include an update() method if the underlying
             * value changes.
             */
            public void setupMethod(ThumbnailDataModel tdm)
            {
                AttributeMap attrMap = tdm.getAttributeMap();
                List attributes = attrMap.getAttributes(attributeName);
                if(attributes == null || attributes.size() == 0)
                {
                    thumbnailColorMap.put(tdm,null);
                    return;
                }
                Attribute[] attrs = new Attribute[attributes.size()];
                attributes.toArray(attrs);
                
                double val = mode.computeValue(attrs,elementName);
                Color color = scale.getColor(val,coldColor,warmColor);
                thumbnailColorMap.put(tdm,color);
            }
        };
        return pm;
    }
}
