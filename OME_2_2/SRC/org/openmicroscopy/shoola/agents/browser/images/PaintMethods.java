/*
 * org.openmicroscopy.shoola.agents.browser.images.PaintMethods
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.shoola.agents.browser.UIConstants;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.util.StringPainter;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A repository of commonly used paint methods.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PaintMethods
{
    public static final PaintMethod DRAW_NAME_METHOD = new AbstractPaintMethod()
    {
        private Font nameFont = new Font(null,Font.BOLD,9);
        private Color nameColor = Color.yellow;
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.images.PaintMethod#paint(java.awt.Graphics, org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
         */
        public void paint(PPaintContext c, Thumbnail t)
        {
            Graphics2D g = c.getGraphics();
            Font oldFont = g.getFont();
            Color oldColor = g.getColor();
            g.setFont(nameFont);
            g.setColor(nameColor);
            StringPainter.drawString(g,t.getModel().getName(),4,15);
            g.setColor(oldColor);
            g.setFont(oldFont);
        }
    };
    
    public static final PaintMethod DRAW_WELLNO_METHOD = new AbstractPaintMethod()
    {
        private Font wellNoFont = new Font(null,Font.BOLD,12);
        private Color numberColor = Color.yellow;
        
        public void paint(PPaintContext c, Thumbnail t)
        {
            Graphics2D g = c.getGraphics();
            Font oldFont = g.getFont();
            Color oldColor = g.getColor();
            g.setFont(wellNoFont);
            g.setColor(numberColor);
            String wellNo = (String)t.getModel().
                                    getValue(UIConstants.WELL_KEY_STRING);
            StringPainter.drawString(g,wellNo,4,18);
            g.setColor(oldColor);
            g.setFont(oldFont);
        }
    };
    
    public static final PaintMethod DRAW_SELECT_METHOD = new AbstractPaintMethod()
    {
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.images.PaintMethod#paint(java.awt.Graphics2D, org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
         */
        public void paint(PPaintContext c, Thumbnail t)
        {
            Graphics2D g = c.getGraphics();
            Rectangle2D bounds = t.getBounds().getBounds2D();
            Paint oldPaint = g.getPaint();
            g.setPaint(new Color(0,0,255,128));
            g.fill(bounds);
            g.setPaint(oldPaint);
        }
    };
    
    public static final PaintMethod DRAW_ANNOTATION_METHOD =
        new ZoomDependentPaintMethod(0.5,Double.POSITIVE_INFINITY,
                                     new AbstractPaintMethod()
        {
            public void paint(PPaintContext c, Thumbnail t)
            {
                Graphics2D g = c.getGraphics();
                // TODO: change this to check for annotations
            
                AttributeMap map = t.getModel().getAttributeMap();
                if(map.getAttributes("ImageAnnotation") != null)
                {
                    PaintShapeGenerator gen = PaintShapeGenerator.getInstance();
                    g.setPaint(new Color(255,255,192));
                    Shape s = gen.getAnnotationNoteShape(t.getWidth()-10,
                                                         t.getHeight()-10);
                    g.fill(s);
                    g.setColor(Color.black);
                    g.draw(s);
                }
            }
        }
    );
    
    public static final PaintMethod MAGNIFIER_ICON_METHOD =
        new AbstractPaintMethod()
    {
        public void paint(PPaintContext c, Thumbnail t)
        {
            Graphics2D g = c.getGraphics();
            Rectangle2D bounds = t.getBounds().getBounds2D();
            double width = bounds.getWidth();
            double height = bounds.getHeight();
            DrawStyle noteStyle = DrawStyles.ANNOTATION_NODE_STYLE;
            DrawStyle oldStyle = noteStyle.applyStyle(g);
            PaintShapeGenerator generator = PaintShapeGenerator.getInstance();
            Shape glass = generator.getOuterMagnifierShape(width-10,height-40);
            g.fill(glass);
            g.draw(glass);
            oldStyle.applyStyle(g);
        }
    };
    
}
