/*
 * org.openmicroscopy.shoola.agents.browser.ui.BPalette
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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Represents a detachable palette overlay in a browser.  Icon nodes are added
 * in a horizontal position based on index.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BPalette extends PNode
{
    /*
     * INVARIANT: the first child node is *always* the palette header.
     */
    private String paletteName;
    private int maxWidth; // the maximum width of the 
    private int measuredWidth = 150; // 150 to start
    private int iconSpacing;
    
    private TitleBar titleBar;
    
    /**
     * Constructs a palette with the given name.
     * 
     * @param name The name of the palette.
     */
    public BPalette(String name)
    {
        final BPalette refCopy = this;
        this.paletteName = name;
        titleBar = new TitleBar(name);
        addChild(titleBar);
        
        addInputEventListener(new PDragSequenceEventHandler()
        {
            /* (non-Javadoc)
             * @see edu.umd.cs.piccolo.event.PDragSequenceEventHandler#startDrag(edu.umd.cs.piccolo.event.PInputEvent)
             */
            public void startDrag(PInputEvent arg0)
            {
                // TODO Auto-generated method stub
                super.startDrag(arg0);
                System.err.println(arg0.getPickedNode());
            }
            
            public void drag(PInputEvent arg0)
            {
                super.drag(arg0);
                Dimension2D dim = arg0.getDeltaRelativeTo(refCopy);
                refCopy.translate(dim.getWidth(),dim.getHeight());
                arg0.setHandled(true);
            }
            
            public void endDrag(PInputEvent arg0)
            {
                super.endDrag(arg0);
                System.err.println("end drag");
                System.err.println(arg0.getPickedNode());
            }    
        });
        setBounds(titleBar.getBounds());
    }
    
    /**
     * Sets the maximum width of the palette (in pixels)  Specifying a
     * negative number will lift the maxWidth restriction and let icons
     * flow out horizontally unrestricted.
     * 
     * @param width The maximum width of the palette.
     */
    public void setMaxWidth(int width)
    {
        this.maxWidth = width;
    }
    
    class TitleBar extends PNode
    {
        private String titleName;
        private Color backgroundColor;
        private Rectangle2D bounds =
            new Rectangle2D.Double(0,0,measuredWidth,20);
        
        private PText titleNode;
        private PNode minimizeNode;
        private PNode hideNode;
        private PNode closeNode;
        
        private Font titleFont = new Font(null,Font.BOLD,14);
        
        TitleBar(String name)
        {
            setBounds(bounds);
            titleName = name;
            
            backgroundColor = new Color(102,102,102,128);
            titleNode = new PText(name);
            titleNode.setPaint(Color.white);
            titleNode.setFont(titleFont);
            
            addChild(titleNode);
            titleNode.setOffset(4,4);
            
            minimizeNode = new MinimizeIcon();
            addChild(minimizeNode);
            minimizeNode.setOffset(measuredWidth-60,0);
            
            hideNode = new HideIcon();
            addChild(hideNode);
            hideNode.setOffset(measuredWidth-40,0);
            
            closeNode = new CloseIcon();
            addChild(closeNode);
            closeNode.setOffset(measuredWidth-20,0);
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(backgroundColor);
            g2.fill(bounds);
            g2.setPaint(oldPaint);
        }
    }
    
    class MinimizeIcon extends PNode
    {
        private Rectangle2D bounds = new Rectangle2D.Double(0,0,20,20);
        private Rectangle2D visibleIcon = new Rectangle2D.Double(3,7,14,6);
        
        // TODO: need to pass a Palette reference in here?
        public MinimizeIcon()
        {
            setBounds(bounds);
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(Color.white);
            g2.fill(visibleIcon);
            g2.setPaint(oldPaint);                     
        }
    }
    
    class HideIcon extends PNode
    {
        private Rectangle2D bounds = new Rectangle2D.Double(0,0,20,20);
        private Ellipse2D visibleIcon = new Ellipse2D.Double(5,5,10,10);
        
        // TODO: pass a Palette reference in here?
        public HideIcon()
        {
            setBounds(bounds);
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(Color.white);
            g2.fill(visibleIcon);
            g2.setPaint(oldPaint);
        }
    }
    
    class CloseIcon extends PNode
    {
        private Rectangle2D bounds = new Rectangle2D.Double(0,0,20,20);
        private Shape xPath;
        
        private GeneralPath generatePath(float xAnchor, float yAnchor)
        {
            GeneralPath path = new GeneralPath();
            path.moveTo(xAnchor+7,yAnchor+4);
            path.lineTo(xAnchor+10,yAnchor+7);
            path.lineTo(xAnchor+13,yAnchor+4);
            path.lineTo(xAnchor+16,yAnchor+7);
            path.lineTo(xAnchor+13,yAnchor+10);
            path.lineTo(xAnchor+16,yAnchor+13);
            path.lineTo(xAnchor+13,yAnchor+16);
            path.lineTo(xAnchor+10,yAnchor+13);
            path.lineTo(xAnchor+7,yAnchor+16);
            path.lineTo(xAnchor+4,yAnchor+13);
            path.lineTo(xAnchor+7,yAnchor+10);
            path.lineTo(xAnchor+4,yAnchor+7);
            path.closePath();
            return path;
        }
        
        public CloseIcon()
        {
            setBounds(bounds);
            xPath = generatePath(0,0);
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(Color.white);
            g2.fill(xPath);
            g2.setPaint(oldPaint);
        }
    }
}
