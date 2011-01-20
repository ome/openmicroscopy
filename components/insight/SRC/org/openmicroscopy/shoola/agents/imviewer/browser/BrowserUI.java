/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserUI
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries
import com.sun.opengl.util.texture.TextureData;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;

/** 
 * Hosts the UI components displaying the rendered image.
 * Note that the layout manager of the viewport is set to <code>null</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class BrowserUI
    extends JScrollPane
    implements MouseMotionListener
{
    
    /**
     * The Layered pane hosting the {@link BrowserCanvas} and any other 
     * UI components added on top of it.
     */
    private JLayeredPane        	layeredPane;

    /** The canvas hosting the image. */
    private JComponent  			canvas;
    
    /** Reference to the Model. */
    private BrowserModel        	model;
    
    /** Reference to the Control. */
    private BrowserControl      	controller;
    
    /** Listens to the mouse moves on the Image canvas. */
    private ImageCanvasListener		canvasListener;
    
    /** Components related to the view while settings the bounds. */
    private Map<Integer, JComponent> siblings;

    /** Flag indicating if the experimenter uses the scrollbars. */
    private boolean					adjusting;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        layeredPane = new JLayeredPane();
        if (ImViewerAgent.hasOpenGLSupport()) {
        	canvas = new BrowserCanvas(model, this);
        } else {
        	 canvas = new BrowserBICanvas(model, this);
        }
       
        //The image canvas is always at the bottom of the pile.
        layeredPane.add(canvas, Integer.valueOf(0));
       
        canvasListener = new ImageCanvasListener(this, model, canvas);
        canvasListener.setHandleKeyDown(true);
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
	
    /** Creates a new instance. */
    BrowserUI()
    {
    	siblings = new HashMap<Integer, JComponent>();
    }
    
    /**
     * Links this View to its Controller and Model
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     */
    void initialize(BrowserControl controller, BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) throw new NullPointerException("No control.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }

	/**
	 * Saves the image to the passed file.
	 * 
	 * @param file		The file where to save the image.
	 * @param format	The format to use.
	 */
	void activeFileSave(File file, String format)
	{
		if (canvas instanceof BrowserCanvas) {
			((BrowserCanvas) canvas).activeSave(file, format);
			canvas.repaint();
		}
	}
	
    /** 
     * Sets the component related to this component when the bounds of 
     * the view are reset.
     * 
     * @param index 	The index corresponding to the passed component. 
     * @param sibling 	The value to set.
     */
    void setSibling(int index, JComponent sibling)
    { 
    	siblings.put(index, sibling);
    }
    
    /**
     * Adds the component to the {@link #layeredPane}. The component will
     * be added to the top of the pile
     * 
     * @param c 	The component to add.
     */
    void addComponentToLayer(JComponent c)
    {
    	Component[] components = layeredPane.getComponents();
    	for (int i = 0; i < components.length; i++) {
			if (components[i] == c) return;
		}
    	layeredPane.add(c, Integer.valueOf(1));
    }
    
    /**
     * Removes the component from the {@link #layeredPane}.
     * 
     * @param c 	The component to remove.
     */
    void removeComponentFromLayer(JComponent c)
    {
    	layeredPane.remove(c);
    }

    /**
     * Creates the displayed image and paints it.
     * This method should be called straight after setting the 
     * rendered image.
     */
    void paintMainImage()
    {
    	if (canvas instanceof BrowserCanvas) {
    		TextureData img = model.getRenderedImageAsTexture();
        	if (img == null) return;
        	double zoom = model.getZoomFactor();
        	int w = (int) (img.getWidth()*zoom);
        	int h = (int) (img.getHeight()*zoom);
        	canvasListener.setAreaSize(w, h);
        	canvas.repaint();
    	} else {
    		if (model.getRenderedImage() == null) return;
    		model.createDisplayedImage();
    		BufferedImage img = model.getDisplayedImage();
    		if (img == null) return;
    		canvasListener.setAreaSize(img.getWidth(), img.getHeight());
    		canvas.repaint();
    	}
    }
    
    /** Displays the zoomed image. */
    void zoomImage()
    {
    	if (canvas instanceof BrowserCanvas) {
    		TextureData img = model.getRenderedImageAsTexture();
        	if (img == null) return;
        	double zoom = model.getZoomFactor();
        	int w = (int) (img.getWidth()*zoom);
        	int h = (int) (img.getHeight()*zoom);
        	setComponentsSize(w, h);
        	canvasListener.setAreaSize(img.getWidth(), img.getHeight());
        	
    	} else {
    		if (model.getRenderedImage() == null) return;
    		model.createDisplayedImage();
    		BufferedImage img = model.getDisplayedImage();
    		if (img == null) return;
    		setComponentsSize(img.getWidth(), img.getHeight());
    		canvasListener.setAreaSize(img.getWidth(), img.getHeight());
    		getViewport().setViewPosition(new Point(-1, -1));
    		canvas.repaint();
    		setBounds(getBounds());
    	}
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
    }
    
    /** 
     * Returns the current size of the viewport. 
     * 
     * @return see above. 
     */
    Dimension getViewportSize() { return getViewport().getSize(); }

	/**
	 * Scrolls to the location.
	 * 
	 * @param bounds 			The bounds of the node.
	 * @param blockIncrement	Pass <code>true</code> to consider block
	 * 							increment, <code>false</code> otherwise.
	 * 						
	 */
	void scrollTo(Rectangle bounds, boolean blockIncrement)
	{
		Rectangle viewRect = getViewport().getViewRect();
		JScrollBar hBar = getHorizontalScrollBar();
		JScrollBar vBar = getVerticalScrollBar();
		int x = 0;
		int y = 0;
		if (!viewRect.contains(bounds)) {
			int deltaX = viewRect.x-bounds.x;
			int deltaY = viewRect.y-bounds.y;
			if (deltaX < 0 && blockIncrement)
				x = hBar.getValue()+hBar.getBlockIncrement();
			else {
				int w = viewRect.width-bounds.width;
				if (w < 0) w = -w;
				x = bounds.x-w/2;
			}
			if (deltaY < 0 && blockIncrement)
				y = vBar.getValue()+vBar.getBlockIncrement();
			else {
				int h = viewRect.height-bounds.height;
				if (h < 0) h = -h;
				y = bounds.y-h/2;
			}
			
        } else {
        	//lens not centered
        	if (blockIncrement) return;
        	int w = viewRect.width-bounds.width;
			if (w < 0) w = -w;
			x = bounds.x-w/2;
			int h = viewRect.height-bounds.height;
			if (h < 0) h = -h;
			y = bounds.y-h/2;
        }
		vBar.setValue(y);
		hBar.setValue(x);
	}
	
	/**
	 * Sets the value of the horizontal and vertical scrollBars.
	 * 
	 * @param vValue	The value to set for the vertical scrollBar.
	 * @param hValue	The value to set for the horizontal scrollBar.
	 */
	void scrollTo(int vValue, int hValue)
	{
		//Rectangle viewRect = getViewport().getViewRect();
		JScrollBar vBar = getVerticalScrollBar();
		JScrollBar hBar = getHorizontalScrollBar();
		hBar.setValue(hBar.getValue()+hValue);
		vBar.setValue(vBar.getValue()+vValue);
	}

	/** Clears the grid images. */
	void clearGridImages() { model.clearGridImages(); }
	
	/**
	 * Returns <code>true</code> if the user is adjusting the window,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAdjusting() { return adjusting; }
	
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
		JComponent sibling = siblings.get(model.getSelectedIndex());
		if (sibling != null) 
			sibling.setBounds(sibling.getBounds());
		layeredPane.setBounds(xLoc, yLoc, d.width, d.height);
	}
	
	/**
	 * Required by the {@link MouseMotionListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}

}
