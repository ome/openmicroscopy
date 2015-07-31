/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BirdEyeView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	
	/** Property indicating to the component is collapsed or expanded.*/
	static final String FULL_DISPLAY_PROPERTY = "fullDisplay";
	
	/** The width of the border. */
	static final int BORDER = 2;

	/** The width of the border x 5. */
	static final int BORDER_5 = 5*BORDER;
	
	/** The default fill color. */
	private static final Color FILL_COLOR = Color.LIGHT_GRAY;
	
	/** The default stroke color. */
	private static final Color STROKE_COLOR = Color.BLACK;
	
	/** The default color for the border. */
	private static final Color BORDER_COLOR = Color.LIGHT_GRAY;//Color.WHITE;
	
	/** The default selection color. */
	private static final Color SELECTION_COLOR = new Color(255, 0, 0, 100);
	
	/** The default selection color. */
	private static final Color SELECTION_COLOR_BORDER = Color.red;
	
	/** The default selection color. */
	private static final Color SELECTION_BLUE_COLOR = new Color(0, 0, 255, 100);
	
	/** The default selection color. */
	private static final Color SELECTION_BLUE_COLOR_BORDER = Color.blue;
	
	/** The processing image. */
	private BufferedImage pImage;
	
	/** Color of the selection rectangle. */
	private Color color;
	
	/** Color of the border of the selection rectangle. */
	private Color	colorBorder;
	
	/** The width of the rectangle. */
	private int w = 30; //to change
	
	/** The width of the rectangle. */
	private int h = 20;   //to change
	
	/** The X-coordinate of the top-left corner. */
	private int bx;
	
	/** The Y-coordinate of the top-left corner. */
	private int by;
	
	/** The X-coordinate of the top-left corner. */
	private int ax;
	
	/** The Y-coordinate of the top-left corner. */
	private int ay;
	
	/** Flag indicating the mouse is over the image. */
	private boolean release;
	
	/** Flag indicating the mouse is over the image. */
	private boolean bover;
	
	/** Flag indicating the mouse is locked. */
	private boolean locked = false;
	
	/** The X-coordinate of the top-left corner. */
	private int px;
	
	/** The Y-coordinate of the top-left corner. */
	private int py;
	
	/** 
	 * The difference of <code>bx</code>and the X-coordinate of the mouse 
	 * clicked. 
	 */
	private int bdifx = 0; 
	
	/** 
	 * The difference of <code>by</code>and the Y-coordinate of the mouse 
	 * clicked. 
	 */
	private int bdify = 0; 
	
	/** Flag indicating to display the full image or only the arrow. */
	private boolean fullDisplay;
	
	/** The length of the side of the arrow. */
	private int v = 4;
	
	/** The X-coordinate of the arrow. */
	private int xArrow = 2;
	
	/** The Y-coordinate of the arrow. */
	private int yArrow = 2;

	/** The area covered by the image. */
	private Rectangle imageRectangle;
	
	/** The area covered by the cross. */
	private Rectangle cross;
	
	/** The width of the canvas. */
	private int canvasWidth;
	
	/** The height of the canvas. */
	private int canvasHeight;
	
	/** The location of the mouse.*/
	private int mouseX, mouseY;
	
	/** One of the constants defined by this class.*/
	private int locationIndex;
	
	/** Indicates if the mouse pressed occurred on the cross or not.*/
    private boolean inCross = false;
    
	/**
	 * Returns <code>true</code> if the specified coordinates are contained
	 * in the selection, <code>false</code> otherwise.
	 * 
	 * @param x The X-coordinate of the mouse pressed.
	 * @param y The Y-coordinate of the mouse pressed.
	 * @return See above.
	 */
    private boolean inSelection(int x, int y)
    {
    	if (x < bx || x > (bx+w)) return false;
    	if (y < by || y > (by+h)) return false;
    	return true;
    }

	/**
	 * Returns <code>true</code> if the region is the full size of the image
	 * <code>false</code> otherwise.
	 * 
	 * @param r The region to handle.
	 * @return See above.
	 */
	private boolean isSameSelection(Rectangle r)
	{
		if (r.width == imageRectangle.width &&
			r.height == imageRectangle.height) return true;
		return (r.x == px && r.y == py);
		
	}
    
	/** Sets the location of the cross.*/
	private void setCrossLocation()
	{
		if (!fullDisplay) {
			cross.x = 0;
			cross.y = 0;
			return;
		}
		switch (locationIndex) {
			case ImageCanvas.BOTTOM_RIGHT:
				cross.x = canvasWidth-cross.width;
				cross.y = canvasHeight-cross.height;
				break;
			case ImageCanvas.TOP_LEFT:
				cross.x = 0;
				cross.y = 0;
		}
	}

	/**
	 * Returns <code>true</code> if the rectangle is in the image,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean inImage()
	{
		boolean b = true;
		if (bx < imageRectangle.x) {
			bx = imageRectangle.x;
			b = false;
		}
		if (by < imageRectangle.y) {
			by = imageRectangle.y;
			b = false;
		}
		if (!b) return b;
		if (bx+w > imageRectangle.width) {
			bx = imageRectangle.width-w;
			b = false;
		}
		if (by+h > imageRectangle.height) {
			by = imageRectangle.height-h;
			b = false;
		}
		return b;
	}
	

	/**
	 * Sets the cursor depending on the specific location.
	 * 
	 * @param x The X-coordinate of the mouse pressed.
	 * @param y The Y-coordinate of the mouse pressed.
	 */
	private void setCursor(int x, int y)
	{
		boolean b = inSelection(x, y);
		if (b) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param locationIndex One of the location constants defined by this class.
	 */
	BirdEyeViewComponent(int locationIndex)
	{
		fullDisplay = true;
		pImage = null;
		release = true;
		switch (locationIndex) {
			case ImageCanvas.TOP_LEFT:
			case ImageCanvas.BOTTOM_RIGHT:
				this.locationIndex = locationIndex;
				break;
			default:
				this.locationIndex = ImageCanvas.TOP_LEFT;
		}
		cross = new Rectangle(0, 0, BORDER_5, BORDER_5);
	}
	
	/** Creates a new instance. */
	BirdEyeViewComponent()
	{
		this(ImageCanvas.TOP_LEFT);
	}
	
	/**
	 * Returns the region used to select the part of the image to view.
	 * 
	 * @return See above.
	 */
	Rectangle getSelectionRegion()
	{
		return new Rectangle(bx, by, w, h);
	}

	/**
	 * Sets the location of the selection rectangle.
	 * 
	 * @param x The X-coordinate of the location.
	 * @param y The Y-coordinate of the location.
	 * @param w The width of the selection region.
	 * @param h The width of the selection region.
	 */
	void setSelection(int x, int y, int w, int h)
	{
		bx = x;
		by = y;
		if (w > imageRectangle.width) w = imageRectangle.width;
		if (h > imageRectangle.height) h = imageRectangle.height;
		this.w = w;
		this.h = h;
		if (bx < 0) {
			this.w += x;
			bx = 0;
		} else if (bx+w > imageRectangle.width) {
			this.w = imageRectangle.width-bx;
		}
		if (by < 0) {
			this.h += y;
			by = 0;
		} else if (by+h > imageRectangle.height) {
			this.h = imageRectangle.height-by;
		}
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
		setSize(w, h);
		canvasWidth = w;
		canvasHeight = h;
		setCrossLocation();
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
    		long count = 0;
    		long totalRed = 0, totalGreen = 0, totalBlue = 0;
    		Color c;
    		//determine the color of the lens
    		for (int i = 0 ; i < image.getWidth() ; i += 10) {
    			for (int j = 0 ; j < image.getHeight() ; j += 10) {
    				count++;
    				c = new Color(image.getRGB(i, j));
    				totalRed += c.getRed(); 
    				totalGreen += c.getGreen(); 
    				totalBlue += c.getBlue();
    			}
    		}
    		c = new Color((int) (totalRed/count), (int) (totalGreen/count),
    				(int) (totalBlue/count));
    		int result = UIUtilities.getColorRange(c);
    		if (result == UIUtilities.RED_COLOR)
    			setSelectionColor(SELECTION_BLUE_COLOR);
    		else setSelectionColor(SELECTION_COLOR);
			setCanvasSize(image.getWidth(), image.getHeight());
			imageRectangle = new Rectangle(0, 0, pImage.getWidth(),
					pImage.getHeight());
			if (w == 0) w = image.getWidth();
			if (h == 0) h = image.getHeight();
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
			if (color.equals(SELECTION_COLOR))
				colorBorder = SELECTION_COLOR_BORDER;
			else colorBorder = SELECTION_BLUE_COLOR_BORDER;
		}
	}
	
	/** 
	 * Returns the location of the bird eye view.
	 * 
	 * @return See above.
	 */
	int getLocationIndex() { return locationIndex; }
	
	/** 
	 * Sets the component up.
	 * 
	 * @param x The default value for the X-coordinate of the top left corner.
	 * @param y The default value for the Y-coordinate of the top left corner.
	 */
	void setup(int x, int y)
	{
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		setSelectionColor(SELECTION_COLOR);
		bx = x;
		by = x;
		fullDisplay = true;
		installListeners(true);
	}
	
	/**
	 * Adds or removes the listeners depending on the specified components.
	 * 
	 * @param add Pass <code>true</code> to attach the listeners,
	 * <code>false</code> otherwise.
	 */
	void installListeners(boolean add)
	{
		if (add) {
            // prevent adding the listeners multiple times
            if (!Arrays.asList(getMouseListeners()).contains(this)) {
                addMouseListener(this);
                addMouseMotionListener(this);
            }
		} else {
			removeMouseListener(this);
			removeMouseMotionListener(this);
		}
	}
	
	/**
	 * Returns the size of the image displayed in the component.
	 * 
	 * @return See above.
	 */
	Dimension getImageSize()
	{
		if (pImage == null) return null;
		return new Dimension(pImage.getWidth(), pImage.getHeight());
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
        setCrossLocation();
		if (!fullDisplay) {
			g2D.setColor(FILL_COLOR);
			g2D.fillRect(cross.x, cross.y, cross.width, cross.height);
			g2D.setColor(STROKE_COLOR);
			switch (locationIndex) {
				case ImageCanvas.BOTTOM_RIGHT:
					g2D.drawLine(xArrow, yArrow, BORDER_5, BORDER_5);
					g2D.drawLine(xArrow, yArrow, xArrow+v, yArrow);
					g2D.drawLine(xArrow, yArrow, xArrow, yArrow+v);
					break;
				case ImageCanvas.TOP_LEFT:
				default:
					g2D.drawLine(xArrow, yArrow, BORDER_5-xArrow,
							BORDER_5-yArrow);
					g2D.drawLine(BORDER_5-xArrow, BORDER_5-yArrow,
							BORDER_5-xArrow, BORDER_5-yArrow-v);
					g2D.drawLine(BORDER_5-xArrow, BORDER_5-yArrow,
							BORDER_5-xArrow-v, BORDER_5-yArrow);
			}
			return;
		}
		if (imageRectangle == null) {
			g2D.setColor(BORDER_COLOR);
		}
		setSize(canvasWidth, canvasHeight);
		g2D.drawImage(pImage, null, 0, 0);
		g2D.setColor(FILL_COLOR);
		g2D.fillRect(cross.x, cross.y, cross.width, cross.height);
		g2D.setColor(STROKE_COLOR);
		switch (locationIndex) {
			case ImageCanvas.BOTTOM_RIGHT:
				g2D.drawLine(canvasWidth-BORDER_5+xArrow,
						canvasHeight-BORDER_5+yArrow,
						canvasWidth-xArrow, canvasHeight-yArrow);
				g2D.drawLine(canvasWidth-xArrow-v, canvasHeight-yArrow,
						canvasWidth-xArrow, canvasHeight-yArrow);
				g2D.drawLine(canvasWidth-xArrow, canvasHeight-yArrow-v,
						canvasWidth-xArrow, canvasHeight-yArrow);
				break;
			case ImageCanvas.TOP_LEFT:
			default:
				
				g2D.drawLine(xArrow, yArrow, xArrow+v, yArrow);
				g2D.drawLine(xArrow, yArrow, xArrow, yArrow+v);
				g2D.drawLine(xArrow, yArrow, BORDER_5, BORDER_5);
		}

		g2D.setColor(color);
		g2D.fillRect(bx, by, w, h);
		if (colorBorder != null) {
			g2D.setColor(colorBorder);
			g2D.drawRect(bx, by, w, h);
			if (!release) g2D.drawRect(ax, ay, w, h);
		}
		g2D.setColor(BORDER_COLOR);
		g2D.drawRect(0, 0, canvasWidth, canvasHeight);
	}
    
    /**
     * Depending on mouse click location, shows or hide the bird eye view.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
	public void mousePressed(MouseEvent e)
	{
		px = ax;
		py = ay;
		mouseX = e.getX();
		mouseY = e.getY();
		inCross = false;
		if (cross.contains(mouseX, mouseY)) {
			inCross = true;
			boolean old = fullDisplay;
			fullDisplay = !fullDisplay;
			if (!fullDisplay) setSize(cross.width, cross.height);
			else setSize(canvasWidth, canvasHeight);
			firePropertyChange(FULL_DISPLAY_PROPERTY, old, fullDisplay);
			return;
		}
		if (!inSelection(mouseX, mouseY)) {
			bx = mouseX-w/2;
			if (bx < 0) bx = 0;
			by = mouseY-h/2;
			if (by < 0) by = 0;
		}
		if (bx < 0) bx = 0;
		if (by < 0) by = 0;
		fullDisplay = true;
		locked = bover;
		bdifx = mouseX-bx;
		bdify = mouseY-by;
		ax = bx;
		ay = by;
		release = false;
		setCursor(mouseX, mouseY);
	}

    /**
     * Fires a property to display the selection.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
	public void mouseReleased(MouseEvent e)
	{
		if (fullDisplay && !inCross) {
			locked = false;
			Rectangle r = getSelectionRegion();
			if (!isSameSelection(r))
				firePropertyChange(DISPLAY_REGION_PROPERTY, null, r);
		}
		mouseX = e.getX();
		mouseY = e.getY();
		bdifx = mouseX-bx;
		bdify = mouseY-by;
		ax = bx;
		ay = by;
		release = true;
		locked = false;
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
		locked = true;
		if (bx <= 0) bx = 1;
		if (by <= 0) by = 1;
		if (bx+w >= pImage.getWidth()) bx = pImage.getWidth()-w-1;
		if (by+h >= pImage.getHeight()) by = pImage.getHeight()-h-1;
		repaint();
	}
	
    /**
     * Sets the cursor depending on action.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */
	public void mouseMoved(MouseEvent e)
	{
		setCursor(e.getX(), e.getY());
	}
	
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
