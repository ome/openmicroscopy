/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ProjectionUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/** 
 * Hosts the UI components displaying the projected image.
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
 * @since 3.0-Beta4
 */
class ProjectionUI 
	extends JScrollPane
{

	/** The canvas hosting the projected image. */
	private JComponent 		canvas;
	
	/** Reference to the model. */
	private BrowserModel			model;
	
	/** Reference to the view. */
	private BrowserUI				view;
	
	/** The UI component hosting the {@link ProjectionCanvas}. */
    private JLayeredPane			layeredPane;
    
    /** Flag indicating if the experimenter uses the scrollbars. */
    private boolean adjusting;
    
	/** Initializes the components composing the display. */
    private void initComponents()
    {
        layeredPane = new JLayeredPane();
        canvas = new ProjectionBICanvas(model, view, this);
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
    

	/** Centers the image.*/
	private void center()
	{
		Rectangle r = getViewport().getViewRect();
		Dimension d = layeredPane.getPreferredSize();
		int xLoc = ((r.width-d.width)/2);
		int yLoc = ((r.height-d.height)/2);
		JScrollBar hBar = getHorizontalScrollBar();
		JScrollBar vBar = getVerticalScrollBar();
		if (hBar.isVisible()) xLoc = layeredPane.getX();
		if (vBar.isVisible()) yLoc = layeredPane.getY();
		layeredPane.setBounds(xLoc, yLoc, d.width, d.height);
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
		return (hBar.isVisible() || vBar.isVisible());
	}
	
    /** Creates a new instance. */
    ProjectionUI() {}
    
    /**
	 * Links the components.
	 * 
	 * @param model	Reference to the model. Mustn't be <code>null</code>.
	 * @param view	Reference to the view. Mustn't be <code>null</code>.
	 */
	void initialize(BrowserModel model, BrowserUI view)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		this.view = view;
		initComponents();
		buildGUI();
	}

    /** 
     * Returns the current size of the viewport. 
     * 
     * @return see above. 
     */
    Dimension getViewportSize() { return getViewport().getSize(); }

    /** Displays the zoomed image. 
     * 
     * @param resetLocation Pass <code>true</code> to reset the viewport location,
     * 						<code>false</code> otherwise.
     */
    void zoomImage(boolean resetLocation)
    {
    	int w, h;
    	if (model.getProjectedImage() == null) {
    	    double f = model.getZoomFactor();
    	    w = (int) (model.getMaxX()*f);
    	    h =	(int) (model.getMaxY()*f);
    	} else {
    	    model.createDisplayedProjectedImage();
    	    BufferedImage img = model.getDisplayedProjectedImage();
    	    if (img == null) {
    	        double f = model.getZoomFactor();
    	        w = (int) (model.getMaxX()*f);
    	        h =	(int) (model.getMaxY()*f);
    	    } else {
    	        w = img.getWidth();
    	        h = img.getHeight();
    	    }
    	}
    	setComponentsSize(w, h);
    	if (resetLocation)
    		getViewport().setViewPosition(new Point(-1, -1));
    	canvas.repaint();
    	setBounds(getBounds());
    }
    
    /**
     * Sets the size of the components because a layeredPane doesn't have a 
     * layout manager.
     * 
     * @param w The width to set.
     * @param h The height to set.
     */
	void setComponentsSize(int w, int h)
	{
		Dimension d = new Dimension(w, h);
        layeredPane.setPreferredSize(d);
        layeredPane.setSize(d);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        repaint();
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
		Rectangle r = getViewport().getViewRect();
		Dimension d = layeredPane.getPreferredSize();
		if (model.isBigImage()) {
			if (!(d.width < r.width && d.height < r.height))
				return;
		}
		if (!scrollbarsVisible() && adjusting) adjusting = false;
		JScrollBar hBar = getHorizontalScrollBar();
		JScrollBar vBar = getVerticalScrollBar();
		if (!(hBar.isVisible() && vBar.isVisible())) center();
	}

}
