/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GridCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

//Application-internal dependencies

/** 
 * Paints the main image and the split channels.
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
class GridCanvas 
	extends GLImageCanvas//ImageCanvas
{

	/** The horizontal gap between image. */
	private static final float HGAP = 0.5f;
	
	/** The horizontal gap between image if 3 columns are used. */
	private static final float HGAP_HIGH = 0.33f;
	
	/** The zoom factor. */
	private static final float ZOOM = 0.25f;

	/**
	 * Paints the grid image.
	 * 
	 * @param gl The graphics context.
	 */
	private void paintGrid(GL gl)
	{
		List<GridImage> list = model.getGridImages();
		Iterator<GridImage> i = list.iterator();
		GridImage img;
		
		//paint image.
		TextureCoords coords = new TextureCoords(0f, 0f, 1f, 1f);
		float hGap = HGAP;
		float vGap = 1.0f/model.getGridRow();
		if (model.getMaxC() > 3) hGap = HGAP_HIGH;
		float xStart = 0;
		float yStart = 0;
		float xEnd = hGap;
		float yEnd = vGap;
		int col = 0;
		int columns = model.getGridColumn();
		TextureData data;
		while (i.hasNext()) {
			img = (GridImage) i.next();
			if (img.isActive()) {
				data = img.getTextureData();
				if (data != null) {
					if (texture == null) texture = TextureIO.newTexture(data);
					else texture.updateImage(data);
					
					gl.glBegin(GL.GL_QUADS);
					gl.glScaled(ZOOM, -1, 1);
					gl.glTexCoord2f(coords.left(), coords.bottom());
					gl.glVertex3f(xStart, yStart, 0);
					gl.glTexCoord2f(coords.right(), coords.bottom());
					gl.glVertex3d(xEnd, yStart, 0);
					gl.glTexCoord2f(coords.right(), coords.top());
					gl.glVertex3f(xEnd, yEnd, 0);
					gl.glTexCoord2f(coords.left(), coords.top());
					gl.glVertex3f(xStart, yEnd, 0);
					gl.glEnd();
					//texture.disable();
				}
			}
			col++;
			if (col == columns) {
				col = 0;
				xStart = 0;
				xEnd = hGap;
				yStart += vGap;
				yEnd += vGap;
			} else {
				xStart = hGap;
				xEnd += hGap;
			}
		}
	}
	
	/**
	 * Paints the text
	 * 
	 * @param gl The graphics context.
	 */
	private void paintText(GL gl)
	{
		int columns = model.getGridColumn();
		int col = 0;
		//write the text.
		Color c = model.getUnitBarColor();
		//draw scale bar text
		gl.glColor3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
		List<GridImage> list = model.getGridImages();
		Iterator<GridImage> i = list.iterator();
		GridImage img;
		
		float start = 0.01f;//0.04f;
		float y = 0.03f;
		float x = start;
		float hGap = HGAP;
		float vGap = 1.0f/model.getGridRow();
		if (model.getMaxC() > 3) hGap = HGAP_HIGH;
		while (i.hasNext()) {
			img = (GridImage) i.next();
			if (img.isActive()) {
				gl.glRasterPos2f(x, y);
				glut.glutBitmapString(FONT, img.getLabel());
			}
			col++;
			if (col == columns) {
				col = 0;
				x = start;
				y += vGap;
			} else x += hGap;
		}
	}
	
	/**
	 * Paints the grid image if it is an RGB image.
	 * 
	 * @param gl The graphics context.
	 */
	private void paintGridAsRGB(GL gl)
	{
		List<GridImage> list = model.getGridImages();
		Iterator<GridImage> i = list.iterator();
		GridImage img;
		
		//paint image.
		TextureCoords coords = new TextureCoords(0f, 0f, 1f, 1f);
		boolean[] rgb;
		float hGap = HGAP;
		float vGap = 1.0f/model.getGridRow();
		if (model.getMaxC() > 3) hGap = HGAP_HIGH;
		float xStart = 0;
		float yStart = 0;
		float xEnd = hGap;
		float yEnd = vGap;
		int col = 0;
		int columns = model.getGridColumn();
		Color c;
		int v = 0;
		while (i.hasNext()) {
			img = (GridImage) i.next();
			if (img.isActive()) {
				rgb = img.getRGB();
				gl.glColorMask(rgb[0], rgb[1], rgb[2], true);
				gl.glBegin(GL.GL_QUADS);
				gl.glScaled(ZOOM, -1, 1);
				gl.glTexCoord2f(coords.left(), coords.bottom());
				gl.glVertex3f(xStart, yStart, 0);
				gl.glTexCoord2f(coords.right(), coords.bottom());
				gl.glVertex3d(xEnd, yStart, 0);
				gl.glTexCoord2f(coords.right(), coords.top());
				gl.glVertex3f(xEnd, yEnd, 0);
				gl.glTexCoord2f(coords.left(), coords.top());
				gl.glVertex3f(xStart, yEnd, 0);
				gl.glEnd();
			}
			col++;
			if (col == columns) {
				col = 0;
				xStart = 0;
				xEnd = hGap;
				yStart += vGap;
				yEnd += vGap;
			} else {
				xStart = hGap;
				xEnd += hGap;
			}
		}
	}
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * 
     */
	GridCanvas(BrowserModel model, BrowserUI view)
	{
		super(model, view); 
	}
	
	/**
	 * Creates and returns an image representing the grid.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage()
	{
		return null;
	}

	/**
	 * Returns the location of the point of the top-left corner of the 
	 * the rectangle with respect to the image coordinate system if the 
	 * passed rectangle is contained in an image composing the grid.
	 * Returns <code>null</code> otherwise.
	 * 
	 * @param r The rectangle to handle.
	 * @return See above.
	 */
	Point isOnImageInGrid(Rectangle r)
	{
		List images = model.getSplitImages();
    	if (images == null) return null; 
    	SplitImage combined = null;
    	int n = images.size();
    	if (n <= 3) n = 4;
    	if (n > 4 && n%2 != 0) {
    		combined = (SplitImage) images.get(images.size()-1);
    		images.remove(images.size()-1);
    	}
    	n = (int) Math.floor(Math.sqrt(n));
        Iterator channels = images.iterator();
        BufferedImage image;
        int x = 0, y = 0;
        SplitImage channel;
        BufferedImage original = model.getCombinedImage();
    	int w = original.getWidth(), h = original.getHeight();
    	Rectangle imageRectangle;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (!channels.hasNext()) break; //Done
                channel = (SplitImage) channels.next();
                image = channel.getImage();
                x = j*(w+BrowserModel.GAP);
                if (image != null) {
                	imageRectangle = new Rectangle (x, y, w, h); 
                	if (imageRectangle.contains(r)) {
                		return new Point(r.x-imageRectangle.x, 
                						r.y-imageRectangle.y);
                		
                	}
                } 
            }
            x = 0;
            y = (i+1)*(h+BrowserModel.GAP);
        }   
        if (combined != null) {
        	image = combined.getImage();
        	y = 0;
        	x = n*(w+BrowserModel.GAP);
        	if (image != null) {
        		imageRectangle = new Rectangle (x, y, w, h); 
        		if (imageRectangle.contains(r)) {
            		return new Point(r.x-imageRectangle.x, 
            						r.y-imageRectangle.y);
            		
            	}
        	} return null;
        }
        return null;
	}
	
    /**
     * Paints the grid.
     * @see GLImageCanvas#display(GLAutoDrawable)
     */
    public void display(GLAutoDrawable drawable) 
	{
    	TextureData data = model.getRenderedImageAsTexture();
    	GL gl = drawable.getGL();
		// Clear The Screen And The Depth Buffer
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);	
		if (data == null) return;
    	//paint the text
    	if (model.isTextVisible() && 
    			model.getGridRatio() > ZOOM) paintText(gl);
    	// pain the grid.
		
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		if (texture == null) texture = TextureIO.newTexture(data);
		else texture.updateImage(data);
		texture.enable();
		texture.bind();
    	if (model.isRenderedImageRGB() && model.isModelRGB()) { 
    		//texture = TextureIO.newTexture(data);
    		//texture.enable();
    		//texture.bind();
    		//paintGridAsRGB(gl);
    		paintGrid(gl);
    	} else {
    		//texture = null;
    		paintGrid(gl);
    	}
    	float vGap = 1.0f/model.getGridRow();
    	//paint the 
    	float xStart = HGAP;
    	float yStart = 0;
    	float xEnd = 2*HGAP;
    	float yEnd = vGap;
    	switch (model.getMaxC()) {
			case 2:
				xStart = 0;
				xEnd = HGAP;
				yEnd += vGap;
				yStart += vGap;
				break;
			case 3:
				yStart = vGap;
				yEnd += vGap;
				break;
			default:
				xStart = 2*HGAP_HIGH;
				xEnd = 1.0f;
		}
    	if (texture == null) {
    		texture = TextureIO.newTexture(data);
    		//texture.enable();
    		//texture.bind();
    	} else texture.updateImage(data);
    	TextureCoords coords = new TextureCoords(0f, 0f, 1f, 1f);
    	gl.glColorMask(true, true, true, true);
    	gl.glBegin(GL.GL_QUADS);
		gl.glScaled(ZOOM, -1, 1);
		gl.glTexCoord2f(coords.left(), coords.bottom());
		gl.glVertex3f(xStart, yStart, 0);
		gl.glTexCoord2f(coords.right(), coords.bottom());
		gl.glVertex3d(xEnd, yStart, 0);
		gl.glTexCoord2f(coords.right(), coords.top());
		gl.glVertex3f(xEnd, yEnd, 0);
		gl.glTexCoord2f(coords.left(), coords.top());
		gl.glVertex3f(xStart, yEnd, 0);
		gl.glEnd();
		
		saveDisplayedImage(gl);
		texture.disable();
    	gl.glFlush();
	}
    
}
