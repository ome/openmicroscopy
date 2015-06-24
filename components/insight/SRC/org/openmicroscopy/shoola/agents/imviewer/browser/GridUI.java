/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GridUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

/** 
 * Hosts the UI components displaying the grid image.
 * Note that the layout manager of the viewport is set to <code>null</code>.
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
class GridUI 
	extends JScrollPane
{

	/** The canvas hosting the grid image. */
	private JComponent 		canvas;
	
	/** Reference to the model. */
	private BrowserModel	model;
	
	/** Reference to the view. */
	private BrowserUI		view;
	
	/** The UI component hosting the {@link GridCanvas}. */
    private JLayeredPane	layeredPane;

	/** Initializes the components composing the display. */
    private void initComponents()
    {
        layeredPane = new JLayeredPane();
        canvas = new GridBICanvas(model, view);
        //The image canvas is always at the bottom of the pile.
        //layeredPane.setLayout(new BorderLayout(0, 0));
        layeredPane.add(canvas, Integer.valueOf(0));
    }
    
	/** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	getViewport().setLayout(null);
    	getViewport().setBackground(model.getBackgroundColor());
        getViewport().add(layeredPane);
    }
    
    /** Creates a new instance. */
	GridUI() {}
	
	/**
	 * Links the components.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 */
	void initialize(BrowserModel model, BrowserUI view)
	{
		if (model == null) throw new NullPointerException("No model.");
		if (view == null) throw new NullPointerException("No view.");
		this.model = model;
		this.view = view;
		initComponents();
		buildGUI();
	}

	/** Sets the dimension of the UI components. */
	void setGridSize()
	{
		Dimension d = model.getGridSize();
		layeredPane.setPreferredSize(d);
        layeredPane.setSize(d);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
	}
	
	/**
     * Adds the component to the {@link #layeredPane}. The component will
     * be added to the top of the pile
     * 
     * @param c The component to add.
     */
    void addComponentToLayer(JComponent c)
    {
        layeredPane.add(c, Integer.valueOf(1));
    }
    
    /**
     * Removes the component from the {@link #layeredPane}.
     * 
     * @param c The component to remove.
     */
    void removeComponentFromLayer(JComponent c)
    {
        layeredPane.remove(c);
    }
    
	/** Determines the size of the canvas. */
	void paintImage() { repaint(); }
	
	/**
	 * Returns the grid image.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage()
	{ 
		if (canvas instanceof GridBICanvas) 
			return ((GridBICanvas) canvas).getGridImage();
		return null; 
	}
	
	/** Resets the size of the components when a new ratio is selected. */
	void setGridRatio()
	{
		setGridSize();
		getViewport().setViewPosition(new Point(-1, -1));
		canvas.repaint();
		setBounds(getBounds());
	}
	
	/**
	 * Returns the coordinate of the point w.r.t the grid image 
	 * coordinate system if the passed rectangle is contained in an image 
	 * composing the grid, <code>null</code> otherwise.
	 * 
	 * @param rect The rectangle to handle.
	 * @return See above.
	 */
	Point isOnImageInGrid(Rectangle rect)
	{
		if (canvas instanceof GridBICanvas) 
			return ((GridBICanvas) canvas).isOnImageInGrid(rect);
		return new Point(0, 0);
	}
	
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
		if (view.isAdjusting()) return;
		Rectangle r = getViewport().getViewRect();
		Dimension d = layeredPane.getPreferredSize();
		int xLoc = ((r.width-d.width)/2);
		int yLoc = ((r.height-d.height)/2);
		layeredPane.setBounds(xLoc, yLoc, d.width, d.height);
	}

}
