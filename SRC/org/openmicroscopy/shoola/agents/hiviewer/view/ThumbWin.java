/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.ThumbWin
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.twindow.TinyWindow;
import org.openmicroscopy.shoola.env.data.model.DataObject;

/** 
 * A floating window to display a thumbnail at its maximum scaling size.
 * It makes a {@link ThumbWinPopupMenu popup} menu available that lets
 * the users view the image, annotate it, etc.
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
class ThumbWin
    extends TinyWindow
    implements MouseListener
{

    /** The Image object the thumbnail is for. */
    private DataObject   dataObject;
    
    /** The point at which the last popup event occurred. */
    private Point        popupPoint;
    
    
    /**
     * Creates a new instance.
     * 
     * @param parent The parent frame.  Mustn't be <code>null</code>.
     * @param fullScaleThumb The thumbnail.  Mustn't be <code>null</code>.
     * @param image The Image object the thumbnail is for.
     *              Mustn't be <code>null</code>.
     */
    ThumbWin(JFrame parent, BufferedImage fullScaleThumb, DataObject image)
    {
        super(parent, fullScaleThumb);
        if (image == null) throw new NullPointerException("No image.");
        dataObject = image;
        uiDelegate.attachMouseListener(this);
        addMouseListener(this);
    }
    
    /**
     * The Image object the thumbnail is for.
     * 
     * @return See above.
     */
    DataObject getDataObject() { return dataObject; }
    
    /**
     * The point at which the last popup event occurred.
     * 
     * @return See above.
     */
    Point getPopupPoint() { return popupPoint; }

    /**
     * Intercepts popup triggers on this window.
     * If the mouse event is a popup trigger, then we register the popup point
     * and display the popup menu.
     */
    public void mouseReleased(MouseEvent me)
    {
        if (me.isPopupTrigger()) {
            popupPoint = me.getPoint();
            ThumbWinPopupMenu.showMenuFor(this);
        }
    }
    
    /** Hides the menu when a mousePressed event occurs. */
    public void mousePressed(MouseEvent me) { ThumbWinPopupMenu.hideMenu(); }
    
    /**
     * No-op implementation.
     */
    public void mouseClicked(MouseEvent me) {}

    /**
     * No-op implementation.
     */
    public void mouseEntered(MouseEvent me) {}

    /**
     * No-op implementation.
     */
    public void mouseExited(MouseEvent me) {}
    
}
