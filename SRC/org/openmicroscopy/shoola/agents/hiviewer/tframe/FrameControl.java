/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.FrameControl
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Handles input events directed to the {@link TinyFrame} components and also 
 * observes the frame's state so to update the UI upon state changes.
 * This class takes on the role of the frame's Controller (as in MVC), but also
 * factors the Observer code out of the frame's View.
 * The reason is that we only have one View and one Controller.
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
class FrameControl
    implements PropertyChangeListener, ActionListener, ComponentListener
{
    
    /** The frame this controller is for. */
    private TinyFrame       model;
    
    /** The frame's UI. */
    private TinyFrameUI     view;
    
    
    /**
     * Creates a new instance to control a given {@link TinyFrame} and
     * notify its View.
     * 
     * @param model The frame.  Mustn't be <code>null</code>.
     * @param view The frame's UI.  Mustn't be <code>null</code>.
     */
    FrameControl(TinyFrame model, TinyFrameUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        model.addPropertyChangeListener(this);
        model.addComponentListener(this);
        view.attachActionListener(this);
    }
    
    /**
     * Monitors frame's state changes and updates the UI accordingly.
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propChanged = pce.getPropertyName();
        if (TinyFrame.IS_SELECTED_PROPERTY.equals(propChanged) ||
                TinyFrame.TITLE_PROPERTY.equals(propChanged))
            view.updateTitleBar();
        else if (TinyFrame.HIGHLIGHT_PROPERTY.equals(propChanged)) {
            view.updateTitleBar();
            model.moveToFront();
        } else if (TinyFrame.COLLAPSED_PROPERTY.equals(propChanged)) 
            //The size button has been pressed. The frame state changed
            //and we're getting notified.
            view.updateCollapsedState();
    }

    /**
     * Reacts to input events in the UI.
     */
    public void actionPerformed(ActionEvent ae)
    {  
        //The size button has been pressed.
        if (model.isCollapsed()) model.setCollapsed(false);
        else model.setCollapsed(true);
    }

    /**
     * Resets the inner desktop preferred size so to make sure every
     * contained component will still be reachable after resizing.
     * 
     * @see ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent ce)
    {
        Rectangle b = model.getContentsBounds();
        Dimension d = new Dimension(b.width, b.height);
        if (b.x < 0) d.width += b.x;
        if (b.y < 0) d.height += b.y;
        model.getInternalDesktop().setPreferredSize(d);
    }
    
    /** 
     * No-op implementation.
     * Required by {@link ComponentListener}, but not needed here.
     */
    public void componentHidden(ComponentEvent ce) {}

    /** 
     * No-op implementation.
     * Required by {@link ComponentListener}, but not needed here.
     */
    public void componentMoved(ComponentEvent ce) {}

    /** 
     * No-op implementation.
     * Required by {@link ComponentListener}, but not needed here.
     */
    public void componentShown(ComponentEvent ce) {}

}
