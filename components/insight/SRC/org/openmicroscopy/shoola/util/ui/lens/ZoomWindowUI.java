/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomWindowUI.java
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies

/** 
 * The ZoomWindowUI is the dialog box used to display the zoomed image. It 
 * contains the zoomPanel which displays the zoomed image and the coordinates 
 * of the lens.  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
class ZoomWindowUI 
	extends	JScrollPane
	implements MouseMotionListener
{

    /** Flag indicating if the experimenter uses the scrollbars. */
    private boolean				adjusting;
    
	/** Panel holding the zoomed Image */
	private JComponent          canvas;

	/** Layered pane hosting the component. */
    private JLayeredPane        layeredPane;
 
    /** The lens model. */
    private LensModel 			model;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	 layeredPane = new JLayeredPane();
    	 canvas = new ZoomBIPanel(model);
    	 layeredPane.add(canvas, Integer.valueOf(0));
    	 getVerticalScrollBar().addMouseMotionListener(this);
         getHorizontalScrollBar().addMouseMotionListener(this);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	JViewport viewport = getViewport();
    	viewport.setLayout(null);
    	viewport.setBackground(model.getBackgroundColor());
    	viewport.add(layeredPane);
    }
    
	/**
	 * Returns <code>true</code> if the scrollbars are visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean scrollbarsVisible()
	{
		JScrollBar hBar = getHorizontalScrollBar();
		JScrollBar vBar = getVerticalScrollBar();
		if (hBar.isVisible()) return true;
		if (vBar.isVisible()) return true;
		return false;
	}
	
	/** 
     * Creates a new instance. 
     * 
	 * @param model The parent component of the control. 
	 * 				Mustn't be <code>null</code>.
	 */
	ZoomWindowUI(LensModel model)
	{
		this.model = model;
		initComponents();
		buildGUI();
	}

	/**
	 * Sets the size of the zoomWindowUI to scale with the zoomfactor. 
	 * 
	 * @param w width of zoomed image.
	 * @param h height of the zoomed image. 
	 */
	void setZoomUISize(int w, int h) 
	{
		Dimension d = new Dimension(w, h);
		layeredPane.setPreferredSize(d);
		layeredPane.setSize(d);
		canvas.setSize(d);
		canvas.setPreferredSize(d);
		getViewport().setViewPosition(new Point(-1, -1));
		canvas.repaint();
		setBounds(getBounds());
	}

	/** Updates the background color of the view port. */
	void updateBackgroundColor()
	{ 
		getViewport().setBackground(model.getBackgroundColor());
	}
	
	/**
	 * Sets the <code>adjusting</code> flag when the experimenter uses 
	 * the scrollbars.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) { adjusting = true; }
	
	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(Rectangle)
	 */
	public void setBounds(Rectangle r)
	{
		setBounds(r.x, r.y, r.width, r.height);
	}

	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(int, int, int, int)
	 */
	
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (!scrollbarsVisible() && adjusting) adjusting = false;
		if (adjusting) return;
		Rectangle r = getViewport().getViewRect();
		Dimension d = layeredPane.getPreferredSize();
		int xLoc = ((r.width-d.width)/2);
		int yLoc = ((r.height-d.height)/2);
		layeredPane.setBounds(xLoc, yLoc, d.width, d.height);
	}
	
	/**
	 * Required by the {@link MouseMotionListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}
	
}
