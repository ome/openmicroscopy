/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserControl
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Handles input events originating from the {@link Browser}'s View.
 * That is, from the {@link RootDisplay} containing all the visualization
 * trees. 
 * This class takes on the role of the browser's Controller (as in MVC).
 *
 * @see BrowserModel
 * @see RootDisplay
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserControl
    implements MouseListener, ImageDisplayVisitor
{
    
    //TODO: Implement scroll listener.  When the currently selected node is 
    //scrolled out of the parent's viewport then it has to be deselected. 
    
    /** The Model controlled by this Controller. */
    private BrowserModel    model;
    
    /** The View controlled by this Controller.*/
    private RootDisplay     view;
    
    
    /**
     * Creates a new Controller for the specified <code>model</code> and
     * <code>view</code>.
     * You need to call the {@link #initialize() initialize} method after
     * creation to complete the MVC set up.
     * 
     * @param model The Model.
     * @param view The View.
     */
    BrowserControl(BrowserModel model, RootDisplay view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
    }
    
    /**
     * Subscribes for mouse events notification with each node in the
     * various visualization trees.
     */
    void initialize() { model.accept(this); }

    /**
     * Registers this object as mouse listeners with each node.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) 
    { 
        //node.addMouseListener(this);  TODO: restore!
        node.getGlassPane().addMouseListener(this); 
    }

    /**
     * Registers this object as mouse listeners with each node.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) 
    {
        //node.addMouseListener(this);  TODO: restore!
        node.getGlassPane().addMouseListener(this); 
    }
    
    /* (non-Javadoc)
     * @see MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {
        //TODO: sort out!!!
        System.out.println("-----------------> MOUSE PRESSED");
        Component src = (Component) me.getSource();
        //ImageDisplay src = (ImageDisplay) me.getSource();
        ImageDisplay node = (ImageDisplay) src.getParent().getParent();
        
        System.out.println("Title: "+node.getTitle());
        System.out.println("Is Over Title: "+
                node.isPointOverTitleBar(me.getPoint()));
        System.out.println("Is Pop-up: "+me.isPopupTrigger());
    }
    
    /* (non-Javadoc)
     * @see MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me)
    {
        //TODO: sort out!!!
        System.out.println("-----------------> MOUSE CLICKED");
        Component src = (Component) me.getSource();
        //ImageDisplay src = (ImageDisplay) me.getSource();
        ImageDisplay node = (ImageDisplay) src.getParent().getParent();
        
        System.out.println("Title: "+node.getTitle());
        System.out.println("Is Over Title: "+
                node.isPointOverTitleBar(me.getPoint()));
        System.out.println("Is Pop-up: "+me.isPopupTrigger());
    }

    /**
     * No-op implementation.
     * @see MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * No-op implementation.
     * @see MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent me) {}
    
    /**
     * No-op implementation.
     * @see MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {}

}
