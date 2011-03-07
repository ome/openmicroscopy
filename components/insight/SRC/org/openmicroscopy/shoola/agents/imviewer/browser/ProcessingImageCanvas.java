/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ProcessingImageCanvas 
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
import java.awt.Dimension;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PImage;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;

/** 
 * Displays the image using Processing.
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
class ProcessingImageCanvas 
	extends PApplet
{

	/** The default size. */
	//static final Dimension DIMENSION = new Dimension(640, 360);
	
	/** The constants determining the graphics context. */
	private String pConstants;
	
	/** The processing image. */
	private PImage pImage;
	
	/** Flag indicating support for <code>OpenGL</code>. */
	private boolean openGL;
	
	/** Reference to the Model. */
	protected BrowserModel	model;
    
	/** Reference to the Model. */
	protected BrowserUI 	view;
	
	/** Draws the image using <code>OpenGL</code> support. */
	private void drawOpenGL()
	{
		beginShape();
		texture(pImage);
		vertex(0, 0, 0, 0);
		vertex(pImage.width, 0, pImage.width, 0);
		vertex(pImage.width, pImage.height, pImage.width, pImage.height);
		vertex(0, pImage.height, 0, pImage.height);
		endShape();
	}
	
	/** Draws the image using <code>OpenGL</code> support. */
	private void drawGeneral()
	{
		if (model.getDisplayedImage() == null) return;
		pImage = new PImage(model.getDisplayedImage());
		image(pImage, 0, 0);
		drawScaleBar();
		noStroke();
	}
	
	/** Draws the scale bar. */
	private void drawScaleBar()
	{
		if (!(model.isUnitBar())) return;
		String v = model.getUnitBarValue(); 
		int c = model.getUnitBarColor().getRGB();
		int scaleBar = (int) (model.getUnitBarSize());
		int right = 10;
		int bottom = 10;
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
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
	ProcessingImageCanvas(BrowserModel model, BrowserUI view)
	{
		if (model == null) throw new NullPointerException("No model.");
    	if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
		openGL = ImViewerAgent.hasOpenGLSupport();
		pConstants = P2D;
		if (openGL) pConstants = P3D;
		init();
	}
	
	/**
	 * Sets the size and the graphics constants.
	 * 
	 * @param d The value to set.
	 */
	void setCanvasSize(Dimension d)
	{
		size(d.width, d.height, pConstants);
	}
	
	/**
	 * Sets up the component.
	 * Overridden from @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		//setCanvasSize(DIMENSION);
		pImage = null;
		hint(ENABLE_NATIVE_FONTS);
		smooth();
		noStroke();
	}
	
	/**
	 * Draws the image.
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		background(model.getBackgroundColor().getRGB());
		if (openGL) drawOpenGL();
		else drawGeneral();
	}

}
