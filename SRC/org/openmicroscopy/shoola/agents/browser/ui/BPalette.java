/*
 * org.openmicroscopy.shoola.agents.browser.ui.BMenu
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
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
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
    
    private PNode headerNode;
    
    /**
     * Constructs a palette with the given name.
     * 
     * @param name The name of the palette.
     */
    public BPalette(String name)
    {
        this.paletteName = name;
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
    
    class TitleBar extends PPath
    {
        private String titleName;
        private Color backgroundColor;
        private Rectangle2D bounds;
        
        private PText titleNode;
        private PNode minimizeNode;
        private PNode closeNode;
        
        private Font titleFont = new Font(null,Font.BOLD,10);
        
        TitleBar(String name)
        {
            super(new Rectangle2D.Double(0,0,measuredWidth,20));
            bounds = getPathReference().getBounds2D();
            titleName = name;
            
            backgroundColor = new Color(102,102,102,192);
            titleNode = new PText(name);
            titleNode.setPaint(Color.white);
            titleNode.setFont(titleFont);
            
            addChild(titleNode);
            titleNode.setOffset(1,1);
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
        
        // TODO: need to pass a Palette reference in here?
        public MinimizeIcon()
        {
            addInputEventListener(new PBasicInputEventHandler()
            {
                public void mouseClicked(PInputEvent arg0)
                {
                    // figure out how to minimize the sucker
                }
            });
        }
        
        public PBounds getBounds()
        {
            return new PBounds(bounds);
        }
        
        public boolean setBounds(double x, double y,
                                 double width, double height)
        {
            if(super.setBounds(x,y,width,height))
            {
                bounds.setFrame(x,y,width,height);
                return true;
            }
            return false;
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(Color.white);
            g2.fill(new Rectangle2D.Double(bounds.getX()+3,
                                           bounds.getY()+7,14,6));
            g2.setPaint(oldPaint);                     
        }
    }
}
