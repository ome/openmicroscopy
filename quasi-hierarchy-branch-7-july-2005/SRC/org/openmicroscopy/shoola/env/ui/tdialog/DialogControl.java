/*
 * org.openmicroscopy.shoola.env.ui.tdialog.WindowControl
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

package org.openmicroscopy.shoola.env.ui.tdialog;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Handles input events directed to the {@link TinyDialog} components and also 
 * observes the window's state so to update the UI upon state changes.
 * This class takes on the role of the window's Controller (as in MVC), but also
 * factors the Observer code out of the window's View.
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
class DialogControl
    implements 
    PropertyChangeListener, ActionListener, MouseListener, MouseMotionListener
{
    
    /** Action command ID. */
    static final int        SIZE = 0, CLOSE = 1;
    
    /** The window this controller is for. */
    private TinyDialog       model;
    
    /** The window's UI. */
    private TinyDialogUI     view;
   
    /** The mouse events controller. */
    private ScreenControl   sControl;
    
    /**
     * Creates a new instance to control a given {@link TinyDialog} and
     * notify its View.
     * 
     * @param model The window. Mustn't be <code>null</code>.
     * @param view The window's UI. Mustn't be <code>null</code>.
     */
    DialogControl(TinyDialog model, TinyDialogUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        sControl = new ScreenControl(this);
        model.addPropertyChangeListener(this);
        view.attachActionListener(this);
        view.attachMouseListener(this);
        view.attachMouseMotionListener(this);
    }

    /** Returns the <code>model</code>. */
    TinyDialog getModel() { return model; }
    
    /**
     * Monitors frame's state changes and updates the UI accordingly.
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propChanged = pce.getPropertyName();
        if (TinyDialog.TITLE_PROPERTY.equals(propChanged))
            view.updateTitleBar();
        else if (TinyDialog.CLOSED_PROPERTY.equals(propChanged))
            //Does it this way b/c we may need to override the closeWindow()
            //method.
            model.closeWindow();
        else if (TinyDialog.COLLAPSED_PROPERTY.equals(propChanged)) 
            //The size button has been pressed. The frame state changed
            //and we're getting notified.
            view.updateCollapsedState();
    }

    /** Reacts to input events in the UI. */
    public void actionPerformed(ActionEvent ae)
    {  
        try {
            int index = Integer.parseInt(ae.getActionCommand());
            switch (index) {
                case SIZE:  //The size button has been pressed.
                    if (model.isCollapsed()) model.setCollapsed(false);
                    else model.setCollapsed(true);
                    break;
                case CLOSE:
                    if (model.isClosed()) model.setClosed(false);
                    else model.setClosed(true);
                    break;
            }
        }  catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+ae.getActionCommand(), nfe); 
        }        
    }

    /** Forward event to the <code>Screen control</code>. */
    public void mousePressed(MouseEvent e) { sControl.mousePressed(e); }

    /** Forward event to the <code>Screen control</code>. */
    public void mouseReleased(MouseEvent e) { sControl.mouseReleased(e); }

    /** Forward event to the <code>Screen control</code>. */
    public void mouseDragged(MouseEvent e) { sControl.mouseDragged(e); }
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */  
    public void mouseClicked(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed in 
     * our case, no op implementation.
     */  
    public void mouseMoved(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */  
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */   
    public void mouseExited(MouseEvent e) {}
    
}

