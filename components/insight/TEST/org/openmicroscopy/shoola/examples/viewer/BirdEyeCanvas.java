/*
 * org.openmicroscopy.shoola.examples.viewer.BirdEyeCanvas 
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
package org.openmicroscopy.shoola.examples.viewer;


//Java imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;

//Application-internal dependencies

/** 
 * The Bird eye view.
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
class BirdEyeCanvas 
	extends PApplet
{
	
	/** The region to render. */
	static final String RENDER_REGION_PROPERTY = "renderRegion";
	
	static final int BORDER = 2;

	static final int BORDER_5 = 5*BORDER;
	
	/** The processing image. */
	private PImage pImage;
	
	/** Color of the selection rectangle. */
	private int color;
	
	/** The width of the rectangle. */
	private int w = 30;
	
	/** The width of the rectangle. */
	private int h = 20;
	
	float bx;
	float by;
	boolean bover = false;
	boolean locked = false;
	boolean canvasMove = false;
	boolean canvasLocked = false;
	float bdifx = 0.0f; 
	float bdify = 0.0f; 

	float x = 0;
	float y = 0;
	
	private int canvasWidth;
	
	private int canvasHeight;
	
	private int strokeWeight = 1;
	
	private Rectangle imageRectangle;
	
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
	
	private Rectangle cross = new Rectangle(0, 0, BORDER_5, BORDER_5);
	
	/** Creates a new instance. */
	BirdEyeCanvas()
	{
		init();
		pImage = null;
	}
	
	void setSelection(float x, float y)
	{
		bx = x;
		by = y;
		draw();
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
	
	int v = 4;
	int xArrow = 2;
	int yArrow = 2;
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (pImage == null) return;

		if (!fullDisplay) {
			fill(192, 192, 192);
			rect(cross.x, cross.y, cross.width, cross.height);
			stroke(0);
			line(xArrow, yArrow, BORDER_5-xArrow, BORDER_5-yArrow);
			line(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow, BORDER_5-yArrow-v);
			line(BORDER_5-xArrow, BORDER_5-yArrow, BORDER_5-xArrow-v, BORDER_5-yArrow);
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
		
		fill(192, 192, 192);
		rect(cross.x, cross.y, cross.width, cross.height);
		stroke(0);
		line(xArrow, yArrow, xArrow+v, yArrow);
		line(xArrow, yArrow, xArrow, yArrow+v);
		line(xArrow, yArrow, BORDER_5, BORDER_5);
		fill(color);
		stroke(color);
		
		
		//strokeWeight(1.5f);
		// Test if the cursor is over the box 
		if (mouseX > bx-w && mouseX < bx+w && 
				mouseY > by-h && mouseY < by+h) {
			bover = true;  
		} else {
			bover = false;
		}
		rect(bx, by, w, h);
		if (mouseY < BORDER) {
			canvasMove = true;
		}
		noFill();
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
	 * 
	 * @param image
	 */
	void setImage(BufferedImage image)
	{
		pImage = new PImage(image);
		repaint();
	}
	
	private boolean fullDisplay = true;
	
	private boolean onControl()
	{
		return cross.contains(mouseX, mouseY);
	}
	
	public void mousePressed()
	{
		if (onControl()) {
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

	public	void mouseDragged() {
		if (canvasLocked) {
			
		} else {
			if (!inImage()) locked = false;
			if (locked) {
				bx = mouseX-bdifx; 
				by = mouseY-bdify; 
				
			}
			x = mouseX;
		}
	}

	public	void mouseReleased() {
		if (locked) {
			locked = false;
			Rectangle r = new Rectangle((int) bx, (int) by, w, h);
			firePropertyChange(RENDER_REGION_PROPERTY, null, r);
		} 
	}

}
