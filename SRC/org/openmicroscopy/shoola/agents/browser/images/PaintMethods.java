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
        private Font nameFont = new Font(null,Font.BOLD,30);
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
            g.drawString(t.getModel().getName(),4,32);
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
        new AbstractPaintMethod()
    {
        public void paint(PPaintContext c, Thumbnail t)
        {
            Graphics2D g = c.getGraphics();
            // TODO: change this to check for annotations
            boolean dummyVariable = false;
            if(dummyVariable)
            {
                Rectangle2D bounds = t.getBounds().getBounds2D();
                double width = bounds.getWidth();
                double height = bounds.getHeight();
                DrawStyle noteStyle = DrawStyles.ANNOTATION_NODE_STYLE;
                DrawStyle oldStyle = noteStyle.applyStyle(g);
                PaintShapeGenerator generator = PaintShapeGenerator.getInstance();
                Shape note = generator.getAnnotationNoteShape(width-10,height-15);
                g.fill(note);
                g.draw(note);
                oldStyle.applyStyle(g);
            }
        }
    };
    
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
