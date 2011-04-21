/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BirdEyeView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Bird eye view using <code>JPanel</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class BirdEyeViewComponent 
	extends JPanel
	implements MouseListener, MouseMotionListener
{

	/** Property indicating to render a region. */
	static final String DISPLAY_REGION_PROPERTY = "displayRegion";
	
	/** The width of the border. */
	static final int BORDER = 2;

	/** The width of the border x 5. */
	static final int BORDER_5 = 5*BORDER;
	
	/** The default fill color. */
	private static final Color FILL_COLOR = Color.LIGHT_GRAY;
	
	/** The default stroke color. */
	private static final Color STROKE_COLOR = Color.BLACK;
	
	/** The default selection color. */
	private static final Color SELECTION_COLOR = Color.RED;
	
	/** The processing image. */
	private BufferedImage pImage;
	
	/** Color of the selection rectangle. */
	private Color color;
	
	/** The width of the rectangle. */
	private int w = 30; //to change
	
	/** The width of the rectangle. */
	private int h = 20;   //to change
	
	/** The X-coordinate of the top-left corner. */
	private float bx;
	
	/** The Y-coordinate of the top-left corner. */
	private float by;
	
	/** Flag indicating the mouse is over the image. */
	private boolean bover;
	
	/** Flag indicating the mouse is locked. */
	private boolean locked = false;
	
	/** 
	 * The difference of <code>bx</code>and the X-coordinate of the mouse 
	 * clicked. 
	 */
	private float bdifx = 0.0f; 
	
	/** 
	 * The difference of <code>by</code>and the Y-coordinate of the mouse 
	 * clicked. 
	 */
	private float bdify = 0.0f; 

	/** The X-location of the mouse. */
	private float x = 0;
	
	/** The Y-location of the mouse. */
	private float y = 0;
	
	/** Flag indicating to display the full image or only the arrow. */
	private boolean fullDisplay;
	
	/** The length of the side of the arrow. */
	private int v = 4;
	
	/** The X-coordinate of the arrow. */
	private int xArrow = 2;
	
	/** The Y-coordinate of the arrow. */
	private int yArrow = 2;
	
	/** The weight of the stroke. */
	private int strokeWeight = 1;
	
	/** The area covered by the image. */
	private Rectangle imageRectangle;
	
	/** The area covered by the cross. */
	private Rectangle cross;
	
	/** The width of the canvas. */
	private int canvasWidth;
	
	/** The height of the canvas. */
	private int canvasHeight;
	
	private int mouseX, mouseY;
	
	/**
	 * Returns <code>true</code> if the rectangle is image, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	private boolean inImage()
	{
		if (bx-strokeWeight < imageRectangle.x) {
			bx = BORDER+strokeWeight;
			return false;
		}
		if (by-strokeWeight < imageRectangle.y) {
			by = BORDER+strokeWeight;
			return false;
		}
		if (bx+w-strokeWeight > imageRectangle.width) {
			bx = imageRectangle.width-w+strokeWeight;
			return false;
		}
		if (by+h-strokeWeight > imageRectangle.height) {
			by = imageRectangle.height-h+strokeWeight;
			return false;
		}
		return true;
	}
	
	/** Creates a new instance. */
	BirdEyeViewComponent()
	{
		//init();
		fullDisplay = true;
		pImage = null;
		cross = new Rectangle(0, 0, BORDER_5, BORDER_5);
	}
	
	/**
	 * Returns the region used to select the part of the image to view.
	 * 
	 * @return See above.
	 */
	Rectangle getSelectionRegion()
	{
		return new Rectangle((int) bx, (int) by, w, h);
	}
	
	/**
	 * Sets the location of the selection rectangle.
	 * 
	 * @param x The X-coordinate of the location.
	 * @param y The Y-coordinate of the location.
	 */
	void setSelection(float x, float y)
	{
		bx = x;
		by = y;
		inImage();
		repaint();
	}
	
	/**
	 * Sets the size of the canvas.
	 *  
	 * @param w The width of the canvas.
	 * @param h The height of the canvas.
	 */
	void setCanvasSize(int w, int h)
	{
		//size(w, h, P2D);
		setSize(w, h);
		canvasWidth = w;
		canvasHeight = h;
		setSize(w, h);
	}
	
	/** 
	 * Adds an image to the display.
	 * 
	 * @param image The image to display.
	 */
	void setImage(BufferedImage image)
	{
		pImage = image;
		if (image != null) {
			setCanvasSize(image.getWidth(), image.getHeight());
		}
		repaint();
	}
	
	/** 
	 * Sets the selection color.
	 * 
	 * @param color The value to set.
	 */
	void setSelectionColor(Color color)
	{
		if (color != null) {
			this.color = color;
		}
	}
	
	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		//size(100, 100, P2D);
		setSize(100, 100);
		//hint(ENABLE_NATIVE_FONTS);
		color = SELECTION_COLOR; 
		//noStroke();
		bx = BORDER;
		by = BORDER;
		fullDisplay = true;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
		if (pImage == null) return;
		Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
		if (!fullDisplay) {
			g2D.setColor(FILL_COLOR);
			g2D.fillRect(cross.x, cross.y, cross.width, cross.height);
			g2D.setColor(STROKE_COLOR);

			g2D.drawLine(xArrow, yArrow, BORDER_5-xArrow, BORDER_5-yArrow);
			g2D.drawLine(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow, 
					BORDER_5-yArrow-v);
			g2D.drawLine(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow-v, 
					BORDER_5-yArrow);
			setSize(cross.width, cross.height);
			return;
		}
		if (imageRectangle == null) {
			imageRectangle = new Rectangle(BORDER, BORDER, pImage.getWidth(), 
					pImage.getHeight());
		}
		setSize(canvasWidth, canvasHeight);
		//stroke(255);
		g2D.drawRect(0, 0, canvasWidth, canvasHeight);

		g2D.drawImage(pImage, null, BORDER, BORDER);
		
		g2D.setColor(FILL_COLOR);
		g2D.fillRect(cross.x, cross.y, cross.width, cross.height);
		g2D.setColor(STROKE_COLOR);
		g2D.setColor(Color.BLACK);
		g2D.drawLine(xArrow, yArrow, xArrow+v, yArrow);
		g2D.drawLine(xArrow, yArrow, xArrow, yArrow+v);
		g2D.drawLine(xArrow, yArrow, BORDER_5, BORDER_5);
		g2D.setColor(color);
		//stroke(color);	
		// Test if the cursor is over the box 
		bover = (mouseX > bx-w && mouseX < bx+w && 
				mouseY > by-h && mouseY < by+h);
		g2D.drawRect((int) bx, (int) by, (int) w, (int) h);
		//noFill();
	}

    /**
     * Depending on mouse click location, shows or hide the bird eye view.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
	public void mousePressed(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		if (cross.contains(mouseX, mouseY)) {
			fullDisplay = !fullDisplay;
			repaint();
			return;
		}
		fullDisplay = true;
		locked = bover;
		bdifx = mouseX-bx; 
		bdify = mouseY-by; 
	}

    /**
     * Fires a property to display the selection.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
	public void mouseReleased(MouseEvent e)
	{
		if (locked) {
			locked = false;
			Rectangle r = new Rectangle((int) bx, (int) by, w, h);
			firePropertyChange(DISPLAY_REGION_PROPERTY, null, r);
		} 
	}

    /**
     * Sets the location of the mouse when dragging the selection.
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
	public void mouseDragged(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		if (!inImage()) 
			locked = false;
		if (locked) {
			bx = mouseX-bdifx; 
			by = mouseY-bdify; 
		}
		x = mouseX;
		repaint();
	}

    /**
     * Required by the {@link MouseMotionListener} I/F but no-operation
     * implementation in our case.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */
	public void mouseMoved(MouseEvent e) {}
	
    /**
     * Required by the {@link MouseListener} I/F but no-operation implementation
     * in our case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
	public void mouseClicked(MouseEvent e) {}

    /**
     * Required by the {@link MouseListener} I/F but no-operation implementation
     * in our case.
     * @see MouseListener#mouseEntered(MouseEvent)
     */
	public void mouseEntered(MouseEvent e) {}

    /**
     * Required by the {@link MouseListener} I/F but no-operation implementation
     * in our case.
     * @see MouseListener#mouseExited(MouseEvent)
     */
	public void mouseExited(MouseEvent e) {}
	
}
