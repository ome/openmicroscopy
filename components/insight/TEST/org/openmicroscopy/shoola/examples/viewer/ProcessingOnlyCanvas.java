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
import java.awt.image.BufferedImage;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;

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

	/** The processing image. */
	private PImage pImage;
	
	/** Creates a new instance. */
	public ProcessingOnlyCanvas()
	{
		init();
		pImage = null;
	}
	
	/**
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		setCanvasSize(ProcessingCanvas.DIMENSION);
		noStroke();
	}
	
	/**
	 * Overridden 
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (pImage == null) return;
		image(pImage, 0, 0);
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
