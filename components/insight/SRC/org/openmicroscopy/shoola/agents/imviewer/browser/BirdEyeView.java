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
import java.awt.Rectangle;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;

//Application-internal dependencies

/** 
 * Bird eye view using processing.
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
class BirdEyeView 
	extends PApplet
{

	/** Property indicating to render a region. */
	static final String RENDER_REGION_PROPERTY = "renderRegion";
	
	/** The width of the border. */
	static final int BORDER = 2;

	/** The width of the border x 5. */
	static final int BORDER_5 = 5*BORDER;
	
	/** The default fill color. */
	private static final int FILL_COLOR = Color.LIGHT_GRAY.getRGB();
	
	/** The processing image. */
	private PImage pImage;
	
	/** Color of the selection rectangle. */
	private int color;
	
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
	BirdEyeView()
	{
		init();
		fullDisplay = true;
		pImage = null;
		cross = new Rectangle(0, 0, BORDER_5, BORDER_5);
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
		draw();
	}
	
	/**
	 * Sets the size of the canvas.
	 *  
	 * @param w The width of the canvas.
	 * @param h The height of the canvas.
	 */
	void setCanvasSize(int w, int h)
	{
		size(w, h, P2D);
		canvasWidth = w;
		canvasHeight = h;
	}
	
	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		size(100, 100, P2D);
		hint(ENABLE_NATIVE_FONTS);
		color = color(0, 0, 255, 100); 
		noStroke();
		bx = BORDER;
		by = BORDER;
		fullDisplay = true;
	}
	
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (pImage == null) return;

		if (!fullDisplay) {
			fill(FILL_COLOR);
			rect(cross.x, cross.y, cross.width, cross.height);
			stroke(0);
			line(xArrow, yArrow, BORDER_5-xArrow, BORDER_5-yArrow);
			line(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow, 
					BORDER_5-yArrow-v);
			line(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow-v, 
					BORDER_5-yArrow);
			setSize(cross.width, cross.height);
			return;
		}
		if (imageRectangle == null) {
			imageRectangle = new Rectangle(BORDER, BORDER, pImage.width, 
					pImage.height);
		}
		setSize(canvasWidth, canvasHeight);
		stroke(255);
		rect(0, 0, canvasWidth, canvasHeight);

		image(pImage, BORDER, BORDER);
		
		fill(FILL_COLOR);
		rect(cross.x, cross.y, cross.width, cross.height);
		stroke(0);
		line(xArrow, yArrow, xArrow+v, yArrow);
		line(xArrow, yArrow, xArrow, yArrow+v);
		line(xArrow, yArrow, BORDER_5, BORDER_5);
		fill(color);
		stroke(color);	
		// Test if the cursor is over the box 
		bover = (mouseX > bx-w && mouseX < bx+w && 
				mouseY > by-h && mouseY < by+h);
		rect(bx, by, w, h);
		noFill();
	}
	
	/**
	 * Overridden to handle mouse pressed event.
	 * @see {@link PApplet#mousePressed()}
	 */
	public void mousePressed()
	{
		if (cross.contains(mouseX, mouseY)) {
			fullDisplay = !fullDisplay;
			draw();
			return;
		}
		fullDisplay = true;
		if (bover) { 
			locked = true; 
		} else {
			locked = false;
		}
		bdifx = mouseX-bx; 
		bdify = mouseY-by; 
	}

	/**
	 * Overridden to handle mouse pressed event.
	 * @see {@link PApplet#mouseDragged()}
	 */
	public	void mouseDragged()
	{
		if (!inImage()) 
			locked = false;
		if (locked) {
			bx = mouseX-bdifx; 
			by = mouseY-bdify; 
		}
		x = mouseX;
	}

	/**
	 * Overridden to handle mouse pressed event.
	 * @see {@link PApplet#mouseReleased()}
	 */
	public	void mouseReleased()
	{
		if (locked) {
			locked = false;
			Rectangle r = new Rectangle((int) bx, (int) by, w, h);
			firePropertyChange(RENDER_REGION_PROPERTY, null, r);
		} 
	}
	
}
