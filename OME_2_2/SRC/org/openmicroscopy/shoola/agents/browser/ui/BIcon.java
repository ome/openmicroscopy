/*
 * org.openmicroscopy.shoola.agents.browser.ui.BIcon
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
import java.awt.Image;
import java.awt.Paint;

import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Specifies an icon (action when selected/moused over) in the browser UI,
 * and stores the events bound to it.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BIcon extends PNode implements MouseOverSensitive,
                                            MouseDownSensitive
{   
    /**
     * The presentation (viewable) node... this is kept separate so an
     * icon can be either text, a glyph, or an image.
     */
    protected PNode presentationNode;
    protected PNode tooltipNode;
    
    protected int vertBuffer = 2; // border buffer (vertical)
    protected int horizBuffer = 2; // border buffer (horiz)
    protected int minWidth = 16; // minimum icon width
    protected int minHeight = 16; // minimum icon height
    
    protected boolean sticky = false;
    protected boolean activated = false; // only relevant if sticky
    
    /**
     * The actions to currently execute, and always execute if the icon
     * does not maintain sticky state.
     */
    protected MouseDownActions mouseDownActions;
    
    /**
     * The actions to execute when the icon is in the activated state.
     */
    protected MouseDownActions activatedActions;
    
    /**
     * The actions to execute when the icon is in the deactivated state.
     */
    protected MouseDownActions deactivatedActions;
    
    /**
     * The action to take when the user mouses over the icon.
     */
    protected MouseOverActions mouseOverActions;
    
    protected boolean tooltipWaiting;
    protected String tooltipText = "";
    
    protected Color tooltipColor = new Color(255,255,192);
    protected Font tooltipFont = new Font(null, Font.PLAIN, 10);
    
    // initialization method
    private void init()
    {
        mouseDownActions = new MouseDownActions();
        mouseOverActions = new MouseOverActions();
        activatedActions = new MouseDownActions();
        deactivatedActions = new MouseDownActions();
    }
    
    private PNode getTooltipNode(double offsetX, double offsetY)
    {
        PText tooltipNode = new PText(tooltipText);
        tooltipNode.setFont(tooltipFont);
        
        PNode boxComponent = new PNode()
        {
            public void paint(PPaintContext context)
            {
                Graphics2D g2 = context.getGraphics();
                Color oldColor = g2.getColor();
                Paint oldPaint = g2.getPaint();
                
                g2.setPaint(tooltipColor);
                g2.fill(getBounds().getBounds2D());
                g2.setColor(Color.black);
                g2.draw(getBounds().getBounds2D());
                g2.setColor(oldColor);
                g2.setPaint(oldPaint);
            }
        };
        
        double textWidth = tooltipNode.getWidth()+4;
        double textHeight = tooltipNode.getHeight()+4;
        
        boxComponent.setBounds(0,0,textWidth,textHeight);
        
        boxComponent.addChild(tooltipNode);
        tooltipNode.setOffset(2,2);
        
        addChild(boxComponent);
        boxComponent.setOffset(offsetX,offsetY);
        
        return boxComponent;
    }
    
    // centers the node inside the icon, especially if it is smaller than
    // the minimum icon dimensions
    private void placeNode(PNode presentationNode)
    {
        if(presentationNode == null)
        {
            // do nothing
            return;
        }
        double offsetX, offsetY;
        double nodeWidth = presentationNode.getWidth();
        double nodeHeight = presentationNode.getHeight();
        
        // in each case, this code centers the presentation node
        // the node is too skinny (like me)
        if(nodeWidth < minWidth-horizBuffer*2)
        {
            offsetX = (minWidth-nodeWidth)/2;
            setBounds(getX(),getY(),minWidth,getHeight());
        }
        else
        {
            offsetX = horizBuffer;
            setBounds(getX(),getY(),(horizBuffer*2)+nodeWidth,getHeight());
        }
        
        // the node is short (Andrea?)
        if(nodeHeight < minHeight-vertBuffer*2)
        {
            offsetY = (minHeight-nodeHeight)/2;
            setBounds(getX(),getY(),getWidth(),minHeight);
        }
        else
        {
            offsetY = vertBuffer;
            setBounds(getX(),getY(),getWidth(),(vertBuffer*2)+nodeHeight);
        }
        
        // now do the centering
        presentationNode.setOffset(offsetX,offsetY);
        
    }
    
    /**
     * Constructs an icon with the specified image.
     * @param imageIcon The image to show in the icon.
     * @param tooltipText The text to show on mouse over.
     * @param sticky Whether or not this state is sticky; that is, whether or
     *               not this icon represents a state that can be activated
     *               and deactivated.
     */
    public BIcon(Image imageIcon, String tooltipText, boolean sticky)
    {
        this.sticky = sticky;
        init();
        // presentationNode = new PImage(imageIcon,true); [BUG 257-- Piccolo 1.0]
        presentationNode = new PImage(imageIcon);
        addChild(presentationNode);
        placeNode(presentationNode);
        this.tooltipText = tooltipText;
    }
    
    /**
     * Constructs a text icon with the specified string.
     * @param text The string to show in the icon.
     * @param sticky Whether or not this state is sticky; that is, whether or
     *               not this icon represents a state that can be activated
     *               and deactivated. 
     */
    public BIcon(String text, boolean sticky)
    {
        this.sticky = sticky;
        init();
        presentationNode = new PText(text);
        // TODO set font
        addChild(presentationNode);
        placeNode(presentationNode);
    }
    
    /**
     * Constructs an icon with a predefined node.  Overrides
     * click behavior for the child node.  In this manner,
     * supports image+text icons and glyph-based icons.
     * 
     * @param childNode The node to embed in this icon.
     * @param sticky Whether or not this state is sticky; that is, whether or
     *               not this icon represents a state that can be activated
     *               and deactivated. 
     */
    public BIcon(PNode childNode, boolean sticky)
    {
        this.sticky = sticky;
        init();
        this.presentationNode = childNode;
        addChild(presentationNode);
        placeNode(presentationNode);
    }
    
    /**
     * Paints the icon (paints a border around it)
     */
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        g2.setColor(new Color(153,153,153));
        g2.draw(getBounds().getBounds2D());
        if(sticky && activated)
        {
            g2.setPaint(new Color(102,102,102));
            g2.fill(getBounds().getBounds2D());
        }
    }
    
    /**
     * Returns whether or not the icon is sticky (can be activated
     * and deactivated) 
     * @return See above.
     */
    public boolean isSticky()
    {
        return sticky;
    }
    
    /**
     * Returns whether or not the icon is activated (only valid if the
     * icon is sticky)
     * @return See above.
     */
    public boolean isActivated()
    {
        return activated;
    }
    
    /**
     * Sets the activation state of the icon (only valid if the icon is
     * sticky)
     * @param activated Which state to put the icon in.
     */
    public void setActivated(boolean activated)
    {
        this.activated = activated;
        if(activated)
        {
            mouseDownActions = activatedActions;
        }
        else
        {
            mouseDownActions = deactivatedActions;
        }
        repaint();
        
    }
    
    /********************* INHERITED INTERFACE METHODS *********************/
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
     */
    public MouseDownActions getMouseDownActions()
    {
        return mouseDownActions;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#getMouseOverActions()
     */
    public MouseOverActions getMouseOverActions()
    {
        return mouseOverActions;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseClick(PInputEvent event)
    {
        if(sticky)
        {
            if(activated)
            {
                setActivated(false);
            }
            else
            {
                setActivated(true);
            }
        }
        PiccoloAction action =
            mouseDownActions.getMouseClickAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseDoubleClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseDoubleClick(PInputEvent event)
    {
        // ignore double; pass through to single
        respondMouseClick(event);
    }

    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseEnter(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseEnter(PInputEvent e)
    {
        // negative, good buddy, we're overriding unless explicitly stated
        if(mouseOverActions.getMouseEnterAction(PiccoloModifiers.getModifier(e)) ==
           PiccoloAction.PNOOP_ACTION)
        {
            // TODO: start tooltip timer, add & show tooltip node
        }
        
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseExit(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseExit(PInputEvent e)
    {
        // that's a negative, good buddy, we're overriding.
        if(mouseOverActions.getMouseExitAction(PiccoloModifiers.getModifier(e)) ==
           PiccoloAction.PNOOP_ACTION)
        {
            // TODO: kill tooltip timer, clear tooltip node
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMousePress(PInputEvent event)
    {
        PiccoloAction action =
            mouseDownActions.getMousePressAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseRelease(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseRelease(PInputEvent event)
    {
        PiccoloAction action =
            mouseDownActions.getMouseReleaseAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    /**
     * Sets the mouse actions (both activated, deactivated, or the default
     * if the icon does not have sticky state) to the specified actions.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
     */
    public void setMouseDownActions(MouseDownActions actions)
    {
        if(actions != null)
        {
            deactivatedActions = actions;
            activatedActions = actions;
            mouseDownActions = actions;
        }
    }
    
    /**
     * A BIcon, if sticky, may be in an activated or deactivated state.  If
     * that is true, it is likely the case that two different actions should
     * be taken dependent on whether the icon is activated or not.  The
     * activated action is the action to take when the icon is currently
     * activated, and the deactivated action is the action to take when the
     * icon is currently deactivated.  If either action set is null, the
     * assignment will be ignored.
     * 
     * @param activatedActions See above.
     * @param deactivatedActions See above.
     */
    public void setMouseDownStickyActions(MouseDownActions activatedActions,
                                          MouseDownActions deactivatedActions)
    {
        if(activatedActions != null)
        {
            this.activatedActions = activatedActions;
        }
        if(deactivatedActions != null)
        {
            this.deactivatedActions = deactivatedActions;
        }
        
        // make assignment now
        if(activated)
        {
            this.mouseDownActions = this.activatedActions;
        }
        else
        {
            this.mouseDownActions = this.deactivatedActions;
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#setMouseOverActions(org.openmicroscopy.shoola.agents.browser.events.MouseOverActions)
     */
    public void setMouseOverActions(MouseOverActions actions)
    {
        if(actions != null)
        {
            mouseOverActions = actions;
        }
    }

}
