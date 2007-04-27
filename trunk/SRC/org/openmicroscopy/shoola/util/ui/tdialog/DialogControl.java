/*
 * org.openmicroscopy.shoola.util.ui.tdialog.WindowControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.tdialog;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.geom.Factory;

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
 * (<b>Internal version:</b> $Revision: 4695 $ $Date: 2006-12-15 17:08:05 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
class DialogControl
    implements 
    PropertyChangeListener, ActionListener, MouseListener, MouseMotionListener,
    MouseWheelListener
{
    
    /** Action command ID to modify the size of the component. */
    static final int        SIZE = 0;
    
    /** Action command ID to modify the close of the component. */
    static final int		CLOSE = 1;
    
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
     * @param model	The window. Mustn't be <code>null</code>.
     * @param view 	The window's UI. Mustn't be <code>null</code>.
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

    /** 
     * Returns the <code>Model</code>.
     * 
     * @return See above.
     */
    TinyDialog getModel() { return model; }
    
    /**
     * Monitors frame's state changes and updates the UI accordingly.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
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

    /** 
     * Reacts to input events in the UI. 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {  
        try {
            int index = Integer.parseInt(ae.getActionCommand());
            switch (index) {
                case SIZE:  //The size button has been pressed.
                    model.setCollapsed(!model.isCollapsed());
                    break;
                case CLOSE:
                    model.setClosed(!model.isClosed());
                    break;
            }
        }  catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+ae.getActionCommand(), nfe); 
        }        
    }

    /** 
     * Modifies the magnification factor depending on the number of "clicks"
     * the mouse wheel was rotated.
     * 
     * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
     */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		float zoomFactor = model.getZoomFactor();
		zoomFactor -= 0.1f*e.getWheelRotation();
		zoomFactor = Math.round(zoomFactor*10)/10.0f;
		if (zoomFactor < TinyDialog.MINIMUM_ZOOM)
			zoomFactor = TinyDialog.MINIMUM_ZOOM;
		if (zoomFactor > TinyDialog.MAXIMUM_ZOOM)
			zoomFactor = TinyDialog.MAXIMUM_ZOOM;
		model.setZoomFactor(zoomFactor);
		BufferedImage img = Factory.magnifyImage(zoomFactor, 
										model.getOriginalImage());
		view.setImage(img);
	}
	
    /** 
     * Forward event to the <code>Screen control</code>. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) { sControl.mousePressed(e); }

    /** 
     * Forward event to the <code>Screen control</code>. 
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e) { sControl.mouseReleased(e); }

    /** 
     * Forward event to the <code>Screen control</code>. 
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent e) { sControl.mouseDragged(e); }
    
    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed in 
     * our case, no op implementation.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */  
    public void mouseMoved(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */  
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     * @see MouseListener#mouseEntered(MouseEvent)
     */  
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     *  @see MouseListener#mouseExited(MouseEvent)
     */   
    public void mouseExited(MouseEvent e) {}
    
}

