/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import omero.gateway.model.DataObject;

/** 
 * A floating window to display a thumbnail at its maximum scaling size.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ThumbnailWindow    
	extends TinyDialog
    implements MouseListener, PropertyChangeListener
{

    /** The selected {@link ImageDisplay} node. */
    private ImageDisplay    node;
    
    /** The Image object the thumbnail is for. */
    private DataObject      dataObject;
    
    /** The point at which the last popup event occurred. */
    private Point           popupPoint;
    
    /** The parent frame of this window. */
    private JFrame          parentFrame;
    /**
     * Intercepts popup triggers on this window.
     * If the mouse event is a popup trigger, then we register the popup point
     * and display the popup menu.
     * 
     * @param me The mouse click event.
     */
    private void onClick(MouseEvent me)
    {
        if (me.isPopupTrigger()) {
            popupPoint = me.getPoint();
           // ThumbWinPopupMenu.showMenuFor(this);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent            The parent frame. Mustn't be <code>null</code>.
     * @param fullScaleThumb    The thumbnail. Mustn't be <code>null</code>.
     * @param image             The Image object the thumbnail is for.
     *                          Mustn't be <code>null</code>.
     * @param node              The node hosting the display. 
     *                          Mustn't be <code>null</code>.
     */
    ThumbnailWindow(JFrame parent, BufferedImage fullScaleThumb,
    		DataObject image, ImageDisplay node)
    {
        super(parent, fullScaleThumb);
        if (image == null) throw new IllegalArgumentException("No image.");
        if (node == null)
            throw new IllegalArgumentException("No node.");
        dataObject = image;
        parentFrame = parent;
        this.node = node;
        uiDelegate.attachMouseListener(this);
        addMouseListener(this);
        addPropertyChangeListener(TinyDialog.CLOSED_PROPERTY, this);
    }
    
    /**
     * Sets the new data object.
     * 
     * @param image The Image object the thumbnail is for.
     *              Mustn't be <code>null</code>.
     */
    void setDataObject(DataObject image)
    {
    	if (image == null) throw new IllegalArgumentException("No image.");
    	dataObject = image;
    	validate();
    	repaint();
    }

    /**
     * The Image object the thumbnail is for.
     * 
     * @return See above.
     */
    DataObject getDataObject() { return dataObject; }
    
    /**
     * Returns the selected node.
     * 
     * @return See above.
     */
    ImageDisplay getSelectedNode() { return node; }
    
    /**
     * The point at which the last popup event occurred.
     * 
     * @return See above.
     */
    Point getPopupPoint() { return popupPoint; }

    /**
     * Returns the parent frame of this window.
     * 
     * @return See above.
     */
    JFrame getParentFrame() { return parentFrame; }
    
    /**
     * Intercepts popup triggers on this window.
     * If the mouse event is a popup trigger, then we register the popup point
     * and display the popup menu.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    { 
        onClick(me); 
        if (me.getClickCount() == 2) {
           //ViewCmd cmd = new ViewCmd((ImageData) node.getHierarchyObject());
           // cmd.execute();
        }      
    }
    
    /** 
     * Hides the menu when a mousePressed event occurs.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    { 
        //ThumbWinPopupMenu.hideMenu();
        onClick(me); //needed for Mac
    }
    
    /** 
     * Hides the menu when the window is closed.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        //ThumbWinPopupMenu.hideMenu();
    }
    
    /**
     * Required by the {@link MouseListener} I/F but no-op implementation in our
     * case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {}

    /**
     * Required by the {@link MouseListener} I/F but no-op implementation in our
     * case.
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me) {}

    /**
     * Required by the {@link MouseListener} I/F but no-op implementation in our
     * case.
     * @see MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me) {}
    
}
