/*
 * org.openmicroscopy.shoola.agents.browser.ui.ImageAnnotationNode
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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedString;

import org.openmicroscopy.ds.st.ImageAnnotation;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ImageAnnotationNode extends ResponsiveNode
{
    private ImageAnnotation annotation;
    private ResponsiveNode eventParent;
    private PNode actualParent;
    private final Color annotationColor = new Color(255,255,128);
    private final Color textColor = Color.black;
    private final Font annotationFont = new Font(null,Font.PLAIN,10);
    
    private PText textNode = null;
    
    public ImageAnnotationNode(ImageAnnotation annotation,
                               ResponsiveNode eventParent,
                               PNode actualParent)
    {
        this.annotation = annotation;
        this.eventParent = eventParent;
        this.actualParent = actualParent;
        setPaint(annotationColor);
        setBounds(0,0,150,100);
        
        if(annotation == null || eventParent == null || actualParent == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        actualParent.addChild(this);
    }
    
    public void respondMouseClick(PInputEvent event)
    {
        if(getParent() != null)
            getParent().removeChild(this);
        eventParent.respondMouseClick(pushParentNode(event,eventParent));
    }
    
    public void respondMouseDoubleClick(PInputEvent event)
    {
        if(getParent() != null)
            getParent().removeChild(this);
        eventParent.respondMouseDoubleClick(pushParentNode(event,eventParent));
    }
    
    public void respondMouseEnter(PInputEvent event)
    {
        System.err.println(event.getPickedNode());
        System.err.println("mouse enter");
    }
    
    public void respondMouseExit(PInputEvent event)
    {
        System.err.println(event.getPickedNode());
        System.err.println("mouse exit");
        if(getParent() != null)
        {
            getParent().removeChild(this);
        }
    }
    
    public void respondMousePress(PInputEvent event)
    {
        if(getParent() != null)
            getParent().removeChild(this);
        eventParent.respondMousePress(pushParentNode(event,eventParent));
    }
    
    public void respondMouseRelease(PInputEvent event)
    {
        if(getParent() != null)
            getParent().removeChild(this);
        eventParent.respondMouseRelease(pushParentNode(event,eventParent));
    }
    
    private PInputEvent pushParentNode(PInputEvent initialEvent,
                                       ResponsiveNode parent)
    {
        PPickPath path = initialEvent.getPath();
        path.popNode(path.getPickedNode());
        path.pushNode(parent);
        initialEvent.setPath(path);
        return initialEvent;
    }
    
    public void paint(PPaintContext context)
    {
        System.err.println("paint annotation node");
        Graphics2D g2 = context.getGraphics();
        g2.setPaint(annotationColor);
        g2.fill(getBounds().getBounds2D());
        g2.setColor(textColor);
        g2.setFont(annotationFont);
        
        float maxWidth = (float)getBounds().getWidth()-10;
        float maxYExtent = (float)getBounds().getHeight()-5;
        
        // line break method for displaying text
        Point pen = new Point(5,5);
        
        System.err.println("getting FRC");
        FontRenderContext frc = g2.getFontRenderContext();
        AttributedString text = new AttributedString(annotation.getContent());
        
        System.err.println("getting FM");
        FontMetrics fm = g2.getFontMetrics(annotationFont);
        System.err.println("getting ellipse width");
        float ellipsesWidth = (float)fm.getStringBounds("...",g2).getWidth();
        
        System.err.println("create measurer");
        LineBreakMeasurer measurer = new LineBreakMeasurer(text.getIterator(),frc);
        
        boolean overVertical = false;
        System.err.println("entering linebreak loop");
        while(measurer.getPosition() < annotation.getContent().length()
              && !overVertical)
        {
            // look ahead a bunch
            if(pen.y + fm.getAscent() +fm.getDescent() +
               fm.getLeading() + fm.getAscent() +
               fm.getDescent() > maxYExtent)
            {
                overVertical = true;
                TextLayout layout = measurer.nextLayout(maxWidth-ellipsesWidth);
                pen.y += layout.getAscent();
                float dx = layout.isLeftToRight() ? 0 :
                            (maxWidth-layout.getAdvance()-ellipsesWidth);
                layout.draw(g2,pen.x + dx, pen.y);
                g2.drawString("...",pen.x+dx+layout.getAdvance(),pen.y);
            }
            else
            {
                TextLayout layout = measurer.nextLayout(maxWidth);
             
                pen.y += (layout.getAscent());
                float dx = layout.isLeftToRight() ? 0 :
                        (maxWidth - layout.getAdvance());
                layout.draw(g2,pen.x+dx, pen.y);
                pen.y += layout.getDescent() + layout.getLeading();
            }
        }
        System.err.println("exiting linebreak loop");
    }
}
