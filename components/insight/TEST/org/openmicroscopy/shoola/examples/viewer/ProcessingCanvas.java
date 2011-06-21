/*
 * org.openmicroscopy.shoola.examples.viewer.ProcessingCanvas
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
 *----------------------------------------------------------------------------*/

package org.openmicroscopy.shoola.examples.viewer;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;

//Application-internal dependencies

/** 
 * This canvas is the processing canvas to demonstrate the use of processing 
 * for displaying images from OMERO. Using OpengGL and Processing, see
 * http://processing.org/
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
public class ProcessingCanvas 
	extends PApplet
	implements ImageCanvasInterface
{

	/** The default size. */
	static final Dimension DIMENSION = new Dimension(640, 360);
	
	/** The processing image. */
	private PImage pImage;
	
	/** The zoom factor. */
	private float zoomFactor = 2;

	/** Flag indicating to use a circle lens or not. */
	private boolean circleLens = false;
	 
	/**
	 * Creates a cylinder lens.
	 * 
	 * @param w
	 * @param sides
	 * @return
	 */
	private float[][] cylinder(float w, int sides)
	{
	  float angle;
	  float[][] pts = new float[sides+1][sides+1];
	 
	  //get the x and z position on a circle for all the sides
	  for (int i = 0; i < pts.length; i++) {
		  angle = TWO_PI/(sides)*i;
		  pts[i][0] = sin(angle)*w;
		  pts[i][1] = cos(angle)*w;
	  }
	  return pts;
	}

	/**
	 * Increases or decreases the zooming factor.
	 * 
	 * @param delta The value to modify the factor with.
	 */
	private void mouseWheel(int delta) 
	{
		zoomFactor = zoomFactor-(float)delta/10.0f;
		if (zoomFactor < 1.5)
			zoomFactor = 1.5f;
		if (zoomFactor > 5)
			zoomFactor = 5;
	}
	
	/** Creates a new instance. */
	public ProcessingCanvas()
	{
		init();
		pImage = null;
	}

	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		addMouseWheelListener(new java.awt.event.MouseWheelListener() 
		{ 
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) 
			{ 
				mouseWheel(evt.getWheelRotation());
			}
		}); 

		setCanvasSize(DIMENSION);
		noStroke();
	}
	
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		background(0);
		float lensSizeX = 40;
		float lensSizeY = 40;
		if (pImage == null) return;
		float[][] circle = cylinder(1, 20);
		float ZF = lensSizeX/zoomFactor;
		beginShape();
		texture(pImage);
		vertex(0, 0, 0, 0);
		vertex(pImage.width, 0, pImage.width, 0);
		vertex(pImage.width, pImage.height, pImage.width, pImage.height);
		vertex(0, pImage.height, 0, pImage.height);
		endShape();

		if (circleLens)
		{
			pushMatrix();
			translate(mouseX, mouseY);
			scale(lensSizeX, lensSizeY);
			beginShape(TRIANGLE_FAN);
			texture(pImage);
			for (int i = 0; i < circle.length; i++)
				vertex(circle[i][0], circle[i][1],circle[i][0]*ZF+mouseX,
						circle[i][1]*ZF+mouseY);
			endShape();
			popMatrix();
		}
		else
		{
			beginShape();
			texture(pImage);
			vertex(mouseX-lensSizeX, mouseY-lensSizeY, mouseX-ZF, mouseY-ZF);
			vertex(mouseX+lensSizeX, mouseY-lensSizeY, mouseX+ZF, mouseY-ZF);
			vertex(mouseX+lensSizeX, mouseY+lensSizeY, mouseX+ZF, mouseY+ZF);
			vertex(mouseX-lensSizeX, mouseY+lensSizeY, mouseX-ZF, mouseY+ZF);
			endShape();
		}
	}

    /**
     * Implemented as specified by the {@link ImageCanvasInterface} I/F.
     * @see ImageCanvasInterface#getCanvas()
     */
	public Component getCanvas()
	{
		return this;
	}

	/**
	 * Implemented as specified by the {@link ImageCanvasInterface} I/F.
	 * @see ImageCanvasInterface#setCanvasSize(Dimension)
	 */
	public void setCanvasSize(Dimension d)
	{
		size(d.width, d.height, P3D);
	}

	/**
	 * Implemented as specified by the {@link ImageCanvasInterface} I/F.
	 * @see ImageCanvasInterface#setImage(BufferedImage)
	 */
	public void setImage(BufferedImage image)
	{
		pImage = new PImage(image);
		repaint();
	}

}
