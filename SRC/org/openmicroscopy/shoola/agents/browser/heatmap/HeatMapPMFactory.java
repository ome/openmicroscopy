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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.AbstractPaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.images.ZoomDependentPaintMethod;
import org.openmicroscopy.shoola.agents.browser.util.StringPainter;

import edu.umd.cs.piccolo.util.PPaintContext;

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
        PaintMethod pm = new AbstractPaintMethod()
        {
            private Map thumbnailColorMap = new IdentityHashMap();
            private Color alpha = new Color(0,0,0,0);
            
            public void paint(PPaintContext c, Thumbnail t)
            {
                Graphics2D g = c.getGraphics();
                Rectangle2D region = t.getBounds().getBounds2D();
                if(thumbnailColorMap.containsKey(t.getModel()))
                {
                    Paint p = (Paint)thumbnailColorMap.get(t.getModel());
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
                    thumbnailColorMap.put(tdm,alpha);
                    return;
                }
                Attribute[] attrs = new Attribute[attributes.size()];
                attributes.toArray(attrs);
                
                double val = mode.computeValue(attrs,elementName);
                Color color = scale.getColor(val,coldColor,warmColor);
                Color alphaColor = new Color(color.getRed(),color.getGreen(),
                                             color.getBlue(),153);
                thumbnailColorMap.put(tdm,alphaColor);
            }
        };
        return pm;
    }
    
    /**
     * Returns the paint method that should be executed to display the value
     * of the element, at the specified zoom level or greater.
     * @param attributeName The name of the attribute to query.
     * @param elementName The name of the element to query.
     * @param minZoomLevel The minimum zoom level which to display the value.
     * @param textColor The color at which to display the value.
     * @return
     */
    public static PaintMethod getShowValueMethod(final HeatMapMode mode,
                                                 final String attributeName,
                                                 final String elementName,
                                                 final double minZoomLevel,
                                                 final Color textColor)
    {
        final Font displayFont = new Font("null",Font.BOLD,11);
        final NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMaximumFractionDigits(3);
        final NumberFormat intFormat = NumberFormat.getIntegerInstance();
        final double epsilon = .001;
        
        PaintMethod pm = new AbstractPaintMethod()
        {
            private Map valueStringMap = new IdentityHashMap();
            private Map stringLocationMap = new IdentityHashMap();
            
            public void paint(PPaintContext context, Thumbnail t)
            {
                Graphics2D g = context.getGraphics();
                Rectangle2D bounds = t.getBounds().getBounds2D();
                
                if(valueStringMap.containsKey(t.getModel()))
                {
                    Point2D anchorPoint = (Point2D)stringLocationMap.get(t.getModel());
                    String valueString = (String)valueStringMap.get(t.getModel());
                    g.setColor(textColor);
                    StringPainter.drawString(g,valueString,(float)anchorPoint.getX(),
                                             (float)anchorPoint.getY());
                }
                else
                {
                    setupMethod(g,bounds,t.getModel());
                    Point2D anchorPoint = (Point2D)stringLocationMap.get(t.getModel());
                    String valueString = (String)valueStringMap.get(t.getModel());
                    g.setColor(textColor);
                    StringPainter.drawString(g,valueString,(float)anchorPoint.getX(),
                                             (float)anchorPoint.getY());
                }
            }
            
            private void setupMethod(Graphics2D context,
                                     Rectangle2D thumbBounds,
                                     ThumbnailDataModel model)
            {
                AttributeMap attrMap = model.getAttributeMap();
                List attributes = attrMap.getAttributes(attributeName);
                if(attributes == null  || attributes.size() == 0)
                {
                    valueStringMap.put(model,"");
                    stringLocationMap.put(model,new Point2D.Double());
                    return;
                }
                Attribute[] attrs = new Attribute[attributes.size()];
                attributes.toArray(attrs);
                double val = mode.computeValue(attrs,elementName);
                String valueString;
                if(Math.abs(Math.round(val)-val) < epsilon)
                {
                    valueString = intFormat.format(val);
                }
                else
                {
                    valueString = doubleFormat.format(val);
                }
                
                FontMetrics fm = context.getFontMetrics(displayFont);
                Rectangle2D rect = fm.getStringBounds(valueString,context);
                
                Point2D anchorPoint =
                    new Point2D.Double((thumbBounds.getWidth()-rect.getWidth())/2,
                                       (thumbBounds.getHeight()-4));
                
                valueStringMap.put(model,valueString);
                stringLocationMap.put(model,anchorPoint);
            }

        };
        return new ZoomDependentPaintMethod(minZoomLevel,Double.POSITIVE_INFINITY,pm);
    }                
}
