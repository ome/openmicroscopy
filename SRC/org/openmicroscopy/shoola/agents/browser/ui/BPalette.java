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
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActions;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.PNode;
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
    private IconBar iconBar;
    
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
        iconBar = new IconBar(measuredWidth);
        addChild(titleBar);
        titleBar.addChild(iconBar); // cheap way to get the whole thing to move
        iconBar.setOffset(0,titleBar.getHeight());
        // sets the bounds
        setBounds(0,0,measuredWidth,titleBar.getHeight()+iconBar.getHeight());
    }
    
    public void addIcon(BIcon icon)
    {
        iconBar.addIcon(icon);
    }
    
    public void removeIcon(BIcon icon)
    {
        iconBar.removeIcon(icon);
    }
    
    public void setMaxWidth(int width)
    {
        iconBar.setMaxWidth(width);
    }
    
    /**
     * The palette's icon bar.
     */
    class IconBar extends PNode
    {
        private int maxWidth;
        private Color backgroundColor;
        private int currentHeight = 20;
        
        /**
         * Constructs an icon bar with a maximum specified width.
         * @param maxWidth The width of the icon bar.
         */
        public IconBar(int maxWidth)
        {
            this.maxWidth = maxWidth;
            backgroundColor = new Color(153,153,153,128);
            setBounds(getX(),getY(),maxWidth,currentHeight);
        }
        
        /**
         * Adds an icon to the bar.
         * @param icon The icon to add.
         */
        public void addIcon(BIcon icon)
        {
            addChild(icon);
            layoutChildren();
        }
        
        /**
         * Removes an icon from the bar.
         * @param icon The icon to remove.
         */
        public void removeIcon(BIcon icon)
        {
            removeChild(icon);
            layoutChildren();
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
            setBounds(getX(),getY(),width,getHeight());
            maxWidth = width;
            layoutChildren();
        }
        
        /**
         * Overrides the PNode's layoutChildren() method to make sure all
         * the icons fit in the area of maximum space.
         */
        public void layoutChildren()
        {
            int currentOffsetX = 2;
            int currentOffsetY = 2;
            boolean firstPlaced = false;
            int maxHeight = 0; // simplistic LM for now
            Iterator iter = getChildrenIterator();
            for(int i=0;i<getChildrenCount();i++)
            {
                PNode node = getChild(i);
                if(!firstPlaced && node.getWidth() > maxWidth)
                {
                    // craptastic error condition, just place the thing
                    setMaxWidth((int)Math.ceil(node.getWidth()));
                    maxHeight = (int)Math.ceil(node.getHeight());
                    node.setOffset(currentOffsetX,currentOffsetY);
                    currentOffsetY += maxHeight+2;
                    currentOffsetX = 2;
                }
                else if(!firstPlaced)
                {
                    firstPlaced = true;
                    node.setOffset(currentOffsetX,currentOffsetY);
                    maxHeight = (int)Math.ceil(node.getHeight());
                    currentOffsetX += Math.ceil(node.getWidth())+2;
                }
                else
                {
                    if(currentOffsetX + node.getWidth() > maxWidth)
                    {
                        currentOffsetY += maxHeight+2;
                        maxHeight = 0;
                        firstPlaced = false;
                        currentOffsetX = 2;
                        i--; // break and repeat
                    }
                    else
                    {
                        if(node.getWidth() > maxHeight)
                        {
                            maxHeight = (int)Math.ceil(node.getHeight());
                        }
                        node.setOffset(currentOffsetX,currentOffsetY);
                        currentOffsetX += (int)Math.ceil(node.getWidth())+2;
                    }
                }
            }
            setBounds(getX(),getY(),maxWidth,currentOffsetY+maxHeight+4);
        }
        
        public void paint(PPaintContext context)
        {
             Graphics2D graphics = context.getGraphics();
             Paint oldPaint = graphics.getPaint();
             graphics.setPaint(backgroundColor);
             graphics.fill(getBounds().getBounds2D());
             graphics.setPaint(oldPaint);
        }
    }
   
    
    class TitleBar extends PNode implements MouseDragSensitive
    {
        private String titleName;
        private Color backgroundColor;
        private Rectangle2D bounds =
            new Rectangle2D.Double(0,0,measuredWidth,20);
        
        private PText titleNode;
        private MinimizeIcon minimizeNode;
        private HideIcon hideNode;
        private CloseIcon closeNode;
        
        private MouseDragActions actionSet;
        
        private Font titleFont = new Font(null,Font.BOLD,14);
        
        TitleBar(String name)
        {
            setBounds(bounds);
            titleName = name;
            
            backgroundColor = new Color(0,51,153,128);
            titleNode = new PText(name);
            titleNode.setPaint(Color.white);
            titleNode.setFont(titleFont);
            
            actionSet = new MouseDragActions();
            actionSet.setDragAction(PiccoloModifiers.NORMAL,
                                    PiccoloActions.DRAG_MOVE_ACTION);
            
            addChild(titleNode);
            titleNode.setOffset(4,4);
            
            minimizeNode = new MinimizeIcon();
            addChild(minimizeNode);
            minimizeNode.setOffset(measuredWidth-60,0);
            MouseDownActions mouseActions = minimizeNode.getMouseDownActions();
            mouseActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                             new PiccoloAction()
            {
                /* (non-Javadoc)
                 * @see org.openmicroscopy.shoola.agents.browser.events.PiccoloAction#execute()
                 */
                public void execute()
                {
                    System.err.println("Minimize clicked");
                }
                
                public void execute(PInputEvent e)
                {
                    execute();
                }
            });
            minimizeNode.setMouseDownActions(mouseActions);
            
            hideNode = new HideIcon();
            addChild(hideNode);
            hideNode.setOffset(measuredWidth-40,0);
            
            closeNode = new CloseIcon();
            addChild(closeNode);
            closeNode.setOffset(measuredWidth-20,0);
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#getMouseDragActions()
         */
        public MouseDragActions getMouseDragActions()
        {
            return actionSet;
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondDrag(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondDrag(PInputEvent e)
        {
            PiccoloAction action =
                actionSet.getDragAction(PiccoloModifiers.getModifier(e));
            action.execute(e);
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondEndDrag(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondEndDrag(PInputEvent e)
        {
            PiccoloAction action =
                actionSet.getEndDragAction(PiccoloModifiers.getModifier(e));
            action.execute(e);
        }
        
        public void respondStartDrag(PInputEvent e)
        {
            PiccoloAction action =
                actionSet.getStartDragAction(PiccoloModifiers.getModifier(e));
            action.execute(e);
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#setMouseDragActions(org.openmicroscopy.shoola.agents.browser.events.MouseDragActions)
         */
        public void setMouseDragActions(MouseDragActions actions)
        {
            if(actions != null)
            {
                actionSet = actions;
            }
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
    
    class MinimizeIcon extends PNode implements MouseDownSensitive
    {
        private Rectangle2D bounds = new Rectangle2D.Double(0,0,20,20);
        private Rectangle2D visibleIcon = new Rectangle2D.Double(3,7,14,6);
        
        private MouseDownActions actionSet;
        
        // TODO: need to pass a Palette reference in here?
        public MinimizeIcon()
        {
            setBounds(bounds);
            actionSet = new MouseDownActions();
        }
        
        public void paint(PPaintContext context)
        {
            Graphics2D g2 = context.getGraphics();
            Paint oldPaint = g2.getPaint();
            g2.setPaint(Color.white);
            g2.fill(visibleIcon);
            g2.setPaint(oldPaint);                     
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
         */
        public MouseDownActions getMouseDownActions()
        {
            return actionSet;
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMouseClick(PInputEvent event)
        {
            PiccoloAction action =
                actionSet.getMouseClickAction(PiccoloModifiers.getModifier(event));
            action.execute(event);
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMousePress(PInputEvent event)
        {
            PiccoloAction action =
                actionSet.getMousePressAction(PiccoloModifiers.getModifier(event));
            action.execute(event);
        }
        
        public void respondMouseRelease(PInputEvent event)
        {
            PiccoloAction action =
                actionSet.getMouseReleaseAction(PiccoloModifiers.getModifier(event));
            action.execute(event);
        }
        
        /* (non-Javadoc)
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
         */
        public void setMouseDownActions(MouseDownActions actions)
        {
            if(actions != null)
            {
                this.actionSet = actions;
            }
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
