/*
 * org.openmicroscopy.shoola.agents.browser.ui.SemanticLayer
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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;

/**
 * Holds the current selected semantic node for a particular browser, and
 * manages the triggering and timing for the node.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HoverManager
{
    private Timer showNodeTimer;
    
    private PNode displayedNode;
    private boolean awaitingDisplay = false;
    private boolean displayingNode = false;
    
    private JComponent parentComponent;
    
    private TimerTask pendingTask;
    
    public HoverManager(JComponent parent)
    {
        showNodeTimer = new Timer();
        this.parentComponent = parent;
    }
    
    public void nodeEntered(final PCamera target,
                            final PNode node,
                            int delayInMillis)
    {
        if(awaitingDisplay)
        {
            pendingTask.cancel();
        }
        
        if(displayingNode)
        {
            hideSemanticNode(target);
        }
        
        pendingTask = new TimerTask()
        {
            public void run()
            {
                target.addChild(node);
                awaitingDisplay = false;
                displayingNode = true;
                displayedNode = node;
                target.repaint();
                
                // TODO fix this hack
                if(displayedNode instanceof SemanticZoomNode)
                {
                    SemanticZoomNode node = (SemanticZoomNode)displayedNode;
                    node.loadCompositeImages();
                    
                    Point parentPoint = parentComponent.getLocationOnScreen();
                    Point2D offset = node.getOffset();
                    int x = (int)Math.round(offset.getX());
                    int y = (int)Math.round(offset.getY());
                    node.setAbsoluteLocation(new Point(parentPoint.x+x,
                                                       parentPoint.y+y));
                }
            }
        };
        
        
        showNodeTimer.schedule(pendingTask,delayInMillis);
        awaitingDisplay = true;
    }
    
    public PNode getDisplayedNode()
    {
        return displayedNode;
    }
    
    public void nodeExited()
    {
        if(awaitingDisplay)
        {
            pendingTask.cancel();
            awaitingDisplay = false;
        }
    }
    
    public void hideSemanticNode(PCamera camera)
    {
        if(displayingNode)
        {
            displayingNode = false;
            
            try
            {
                camera.removeChild(displayedNode);
                displayedNode = null;
            }
            // was already removed (TODO: better way?)
            catch(Exception e) {}
        }
    }
}
