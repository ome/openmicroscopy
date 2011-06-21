/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GLImageCanvas
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

//Third-party libraries
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

//Application-internal dependencies


/**
 * OpengGL canvas.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class GLImageCanvas 
	extends GLJPanel
	implements GLEventListener
{

	/** The font used. */
	static final int FONT = GLUT.BITMAP_HELVETICA_10;
	
	/** The default x location for scale bar is in bottom right.*/
	private static final float DEFAULT_X_BR = 0.99f;
	
	/** The default x location for scale bar is in bottom left.*/
	private static final float DEFAULT_X_BL = 0.01f;
	
	/** The default y location for scale bar.*/
	private static final float DEFAULT_Y = 0.98f;
	
	/** The thickness of scale bar.*/
	private static final float STROKE = 0.001f;
	
	/** The capabilities. */
	private static GLCapabilities CAPS;
	
	static {
		CAPS = new GLCapabilities();//GLProfile.get(GLProfile.GL2));
		CAPS.setAlphaBits(8);
		CAPS.setRedBits(8);
		CAPS.setGreenBits(8);
		CAPS.setBlueBits(8);
	}
	
	/** The Access to the OpenGL utility library. */
	protected GLU glu;
	
	/** Provides access to fond and stroke. */ 
	protected GLUT glut;
	
	/** Reference to the Model. */
	protected BrowserModel	model;
    
	/** Reference to the Model. */
	protected BrowserUI 	view;
	
	/** The texture. */
	protected Texture 		texture;
	
	/** The string to paint on top of the image. */
    protected String		paintedString;

    /** The file where to save the image. */
    protected boolean		savedFile;
    
    /** The image to save. */
    protected BufferedImage image;
    
    /**
     * Copies the frame to an array.
     * 
     * @param gl The graphics context.
     * @return See a
     */
    private int[] copyFrame(GL gl)
    { // copies the Frame to an integer array
		Dimension s = getSize();
		int w = s.width; // get the canvas' dimensions
		int h = s.height;
		// create a ByteBuffer to hold the image data
		ByteBuffer buffer = BufferUtil.newByteBuffer(w*h*3); 
		gl.glReadBuffer(GL.GL_BACK);
		gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
		gl.glReadPixels(0, 0, w, h, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);

		int[] pixels = new int[w*h];
		int p = w*h*3; 
		int q;   
		int i = 0;   
		int w3 = w*3;
		int r, g, b;
		for (int row = 0; row < h; row++) {
			p -= w3;
			q = p;
			for (int col = 0; col < w; col++) {
				r = buffer.get(q++);
				g = buffer.get(q++);
				b = buffer.get(q++);
				pixels[i++] = 0xFF000000 | ((r & 0x000000FF) << 16) | 
				((g & 0x000000FF) << 8) | (b & 0x000000FF);
			}
		}
		return pixels;
	}
    
    /**
     * Saves the displayed image.
     * 
     * @param gl The graphics context.
     */
    protected void saveDisplayedImage(GL gl)
    {
    	if (gl == null || savedFile) return;
    	Dimension s = getSize();
    	int w = s.width; 
    	int h = s.height;
    	image = new BufferedImage(w, h, 
    			BufferedImage.TYPE_INT_ARGB);
    	image.setRGB(0, 0, w, h, copyFrame(gl), 0, w);
    	savedFile = false;
    }
    
    /**
     * Draws the scale bar.
     * 
     * @param gl 	The drawing context.
     * @param width The width of the original image.
     * @param height The height of the original image
     */
    protected void drawScaleBar(GL gl, int width, int height)
    {
    	if (!model.isUnitBar()) return;
    	// Position scalebar in the bottom left of the viewport or
		// the image which ever is viewable. 
		Rectangle viewRect = view.getViewport().getViewRect();
    	float s = (float) (model.getOriginalUnitBarSize())/width;
    	if (s >= 1.0f) return;
		Color c = model.getUnitBarColor();
		//Display scale bar depending on size.
		//draw scale bar text
		gl.glColor3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
		float x1, x2, y1, y2;
        float xt = 0.0f;
		if (view.getBirdEyeViewLocationIndex() == ImageCanvas.BOTTOM_RIGHT) {
			x1 = (float) viewRect.x/width+DEFAULT_X_BL;
			if (x1 > (DEFAULT_X_BR-s)) x1 = DEFAULT_X_BR-s;
			x2 = x1+s;
			
			xt = 0.0f;
		} else {
			x1 = (float) viewRect.x/width+(float) (viewRect.width)/width
				-DEFAULT_X_BL-s;
			if (x1 > (DEFAULT_X_BR-s)) x1 = DEFAULT_X_BR-s;
			x2 = x1+s;
		}
		y1 = (float) viewRect.y/height
		+(float) (viewRect.height)/height-0.01f;
		if (y1 > DEFAULT_Y) y1 = DEFAULT_Y;
		y2 = y1+STROKE;
        if (model.getZoomFactor() > 0.25) {
        	String text = model.getUnitBarValue();
    		int length = 0;
    		if (text != null)
    			length = getFontMetrics(getFont()).stringWidth(text);
        	float t = (float) (length)/width;
        	float xText = x1+(s-t)/2+xt;
            float yText = y1-STROKE;
            gl.glRasterPos2f(xText, yText);
        	glut.glutBitmapString(FONT, text);
        }
        	
        
		//draw the scale bar.
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(x1, y1, 0.0f); 
		gl.glVertex3f(x2, y1, 0.0f); 
		gl.glVertex3f(x2, y2, 0.0f); 
		gl.glVertex3f(x1, y2, 0.0f); 
		gl.glEnd();
    }
    
    /** 
     * Subclasses should invoke the method to display the texture.
     * 
     * @param drawable  The drawing context.
     * @param data		The data to display.
     */
	protected void onDisplay(GLAutoDrawable drawable, TextureData data)
	{
		GL gl = drawable.getGL();
		// Clear The Screen And The Depth Buffer
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);	
		if (data == null) return;
		if (texture == null) {
			texture = TextureIO.newTexture(data);
		} else {
			texture.updateImage(data);

		}
		if (texture != null) {
			drawScaleBar(gl, data.getWidth(), data.getHeight());
			float x = 1;
			float y = 1;
			//image
			texture.enable();
			texture.bind();
			
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
					GL.GL_REPLACE);
			TextureCoords coords = new TextureCoords(0, 0, 1, 1);
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(coords.left(), coords.bottom());
			gl.glVertex3f(0, 0, 0);
			gl.glTexCoord2f(coords.right(), coords.bottom());
			gl.glVertex3d(x, 0, 0);
			gl.glTexCoord2f(coords.right(), coords.top());
			gl.glVertex3f(x, y, 0);
			gl.glTexCoord2f(coords.left(), coords.top());
			gl.glVertex3f(0, y, 0);
			gl.glEnd();
			saveDisplayedImage(gl);
			texture.disable();
			gl.glFlush();
		}
	}
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
	GLImageCanvas(BrowserModel model, BrowserUI view)
	{
		super(CAPS, null, null);
		if (model == null) throw new NullPointerException("No model.");
		if (view == null) throw new NullPointerException("No view.");
        this.model = model;	
        this.view = view;
        setOpaque(true);
        glu = new GLU();
        glut = new GLUT();
        addGLEventListener(this);
        paintedString = null;
	}
	
	/**
	 * Returns the image to save.
	 * 
	 * @return See above.
	 */
	BufferedImage getImageToSave() { return image; }
	
	/** Creates a buffered image. */
	void activeSave()
	{
		savedFile = true;
	}
	
    /**
	 * Sets the value of the selected z-section and time-point.
	 * 
	 * @param pressedZ	The selected z-section.
	 * @param pressedT	The selected time-point.
	 */
	void setPaintedString(int pressedZ, int pressedT)
	{
		if (pressedZ < 0 || pressedT < 0)  paintedString = null;
		else paintedString = "z="+pressedZ+", t="+pressedT;
		repaint();
	}
	
	/**
	 * Implemented as specified by the {@link GLEventListener} I/F.
	 * @see GLEventListener#init(GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		Color c = getBackground();
		if (c == null) c = Color.LIGHT_GRAY;
		float[] array = new float[4];
		array = c.getRGBColorComponents(array);
		gl.glClearColor(array[0], array[1], array[2], array[3]);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glScaled(1, -1, 1);
		glu.gluOrtho2D(0, 1, 0, 1);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * Implemented as specified by the {@link GLEventListener} I/F.
	 * @see GLEventListener#reshape(GLAutoDrawable, int, int, int, int)
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) 
	{
		 GL gl = drawable.getGL();
		 gl.glMatrixMode(GL.GL_PROJECTION);
		 gl.glLoadIdentity();
		 gl.glScaled(1, -1, 1);
		 glu.gluOrtho2D(0, 1, 0, 1);
		 gl.glMatrixMode(GL.GL_MODELVIEW);
		 gl.glLoadIdentity();
	}

	/**
	 * Required by the {@link GLEventListener} I/F but no-operation in our
	 * case.
	 * @see GLEventListener#display(GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable) {}

	/**
	 * Required by the {@link GLEventListener} I/F but no-operation in our
	 * case.
	 * @see GLEventListener#dispose(GLAutoDrawable)
	 */
	public void dispose(GLAutoDrawable drawable) {}

	/**
	 * Required by the {@link GLEventListener} I/F but no-operation in our
	 * case.
	 * @see GLEventListener#displayChanged(GLAutoDrawable, boolean, boolean)
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean a, boolean b) {}
	
}
