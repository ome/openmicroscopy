/*
 * org.openmicroscopy.shoola.examples.viewer.ProcessingOnlyCanvas 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;


//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

//Application-internal dependencies

/** 
 * This canvas is the processing canvas to demonstrate the use of processing 
 * for displaying images from OMERO. Using Processing see http://processing.org/
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
public class ProcessingOnlyCanvas 
	extends PApplet
	implements ImageCanvasInterface
{

	static final String PANNING_OFFSET_PROPERTY = "panningOffset";
	
	/** The minimum zooming value. */
	private static final float MIN_ZOOM = 0.25f;
	
	/** The minimum zooming value. */
	private static final float MAX_ZOOM = 3.0f;
	
	/** Default background color. */
	static int BACKGROUND = 255;
	
	/** The processing image. */
	private PImage pImage;
	
	/** Flag indicating to display the split view. */
	private boolean splitView;
	
	/** Flag indicating to display the split view. */
	private boolean scaleBarDisplay;
	
	/** The zoom factor. */
	private float zoomFactor = 1.0f;

	/** The control .*/
	private ControlPalette controlP5;
	
	private PVector mousePos;  // Stores the mouse position.
	                   // For pretty formatting of mouse coordinates.
	
	private PVector panPos;
	
	/**
	 * Increases or decreases the zooming factor.
	 * 
	 * @param delta The value to modify the factor with.
	 */
	private void mouseWheel(int delta) 
	{
		zoomFactor = zoomFactor - (float) delta/10.0f;
		if (zoomFactor < MIN_ZOOM)
			zoomFactor = MIN_ZOOM;
		if (zoomFactor > MAX_ZOOM)
			zoomFactor = MAX_ZOOM;
		redraw();
	}
	
	/** Attaches listeners to the component. */
	private void attachListeners()
	{
		addMouseWheelListener(new MouseWheelListener() 
		{ 
			public void mouseWheelMoved(MouseWheelEvent evt) 
			{ 
				mouseWheel(evt.getWheelRotation());
			}
		}); 
	}

	/** Draws the scale bar. */
	private void drawScaleBar()
	{
		if (!scaleBarDisplay) return;
		//draw scale bar
		int c = color(192, 192, 192);
		String v = "5";
		int scaleBar = 40; // width of the scale bar
		int right = 5;
		int bottom = 5;
		int x = pImage.width-scaleBar-right;
		int y = pImage.height-bottom;
		stroke(c);
		strokeCap(ROUND);
		strokeWeight(2.0f);
		line(x, y, x+scaleBar, y);
		//text
		int w = getFontMetrics(getFont()).stringWidth(v);
		x = pImage.width-(scaleBar+w)/2-right;
		fill(c);
		text(v, x, y-2);
	}
	
	/** Creates a new instance. */
	public ProcessingOnlyCanvas()
	{
		init();
		pImage = null;
		splitView = false;
		scaleBarDisplay = true;  
		//attachListeners();
	}

	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		setCanvasSize(ProcessingCanvas.DIMENSION);
		hint(ENABLE_NATIVE_FONTS);
		smooth();
		noStroke();
	}
	
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (pImage == null) return;
		background(BACKGROUND);
		//splitView code
		if (splitView) {
			scale(0.5f);
			int h = pImage.height;
			int w = pImage.height;
			tint(255, 0, 0);
			image(pImage, w, 0);
			tint(0, 255, 0);
			image(pImage, 0, h);
			tint(0, 0, 255);
			image(pImage, w, h);
			tint(255, 255, 255);
			image(pImage, 0, 0);
		}
		/*
		scale(zoomFactor); //zooming factor;
		
		
		image(pImage, 0, 0);
		drawScaleBar();
		*/

		// Do some drawing that can be zoomed and panned.  
		image(pImage, 0, 0);
		drawScaleBar();
		noStroke();
		
	}
	
	/**
	 * Implemented as specified by the {@link ImageCanvasInterface}.
	 * @see ImageCanvasInterface#setImage(BufferedImage)
	 */
	public void setImage(BufferedImage image)
	{
		pImage = new PImage(image);
		repaint();
	}

	/**
	 * Implemented as specified by the {@link ImageCanvasInterface}.
	 * @see ImageCanvasInterface#getCanvas()
	 */
	public Component getCanvas() { return this; }

	/**
	 * Implemented as specified by the {@link ImageCanvasInterface}.
	 * @see ImageCanvasInterface#setCanvasSize(Dimension)
	 */
	public void setCanvasSize(Dimension d)
	{
		size(d.width, d.height, P2D);
	}

}
