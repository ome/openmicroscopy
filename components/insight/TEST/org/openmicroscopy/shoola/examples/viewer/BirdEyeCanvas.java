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
	
	static final int BORDER = 0;

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
	float bdifx = 0.0f; 
	float bdify = 0.0f; 

	float x = 0;
	float y = 0;
	
	private Rectangle imageRectangle;
	
	private boolean inImage()
	{
		if (bx < imageRectangle.x) {
			bx = 2*BORDER;
			return false;
		}
		if (by < imageRectangle.y) {
			by = 2*BORDER;
			return false;
		}
		if (bx+w > imageRectangle.width-2) {
			bx = imageRectangle.width-w-2;
			return false;
		}
		if (by+h > imageRectangle.height-2) {
			by = imageRectangle.height-h-2;
			return false;
		}
		return true;
	}
	
	/** Creates a new instance. */
	BirdEyeCanvas()
	{
		init();
		pImage = null;
	}
	
	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		size(100, 100, P2D);
		hint(ENABLE_NATIVE_FONTS);
		color = color(255, 0, 0); 
		noStroke();
		bx = BORDER;
		by = BORDER;
	}
	
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (pImage == null) return;
		if (imageRectangle == null) {
			imageRectangle = new Rectangle(BORDER, BORDER, pImage.width, 
					pImage.height);
		}
		stroke(255);
		rect(0, 0, width, height);
		image(pImage, BORDER, BORDER);
		stroke(color);
		noFill();
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
	
	public void mousePressed() {
		if (bover) { 
			locked = true; 
		} else {
			locked = false;
		}
		bdifx = mouseX-bx; 
		bdify = mouseY-by; 
	}

	public	void mouseDragged() {
		if (!inImage()) locked = false;
		if (locked) {
			bx = mouseX-bdifx; 
			by = mouseY-bdify; 
		}
		x = mouseX;
		
	}

	public	void mouseReleased() {
		locked = false;
		Rectangle r = new Rectangle((int) bx, (int) by, w, h);
		firePropertyChange(RENDER_REGION_PROPERTY, null, r);
	}


}
