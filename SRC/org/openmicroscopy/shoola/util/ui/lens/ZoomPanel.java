/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports



//Third-party libraries
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

//Application-internal dependencies

/** 
 * ZoomPanel shows the zoomed image of the lens, in the centre of the 
 * ZoomWindowUI.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ZoomPanel
	extends GLJPanel
	implements GLEventListener
	//extends JPanel
{

	/** zoomed image from the lens, and magnified by the model. */
	//private BufferedImage 	zoomImage;
	
	/** The capabilities. */
	private static GLCapabilities CAPS;
	
	static {
		CAPS = new GLCapabilities();//GLProfile.get(GLProfile.GL2));
		CAPS.setAlphaBits(8);
	}
	
	/** The Access to the OpenGL utility library. */
	protected GLU 		glu;
	
	/** The texture. */
	protected Texture 	texture;
	
	/** Reference to the model. */
	private LensModel 	model;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model.
	 */
	ZoomPanel(LensModel model)
	{
		super(CAPS, null, null);
		this.model = model;
		setOpaque(true);
		glu = new GLU();
		addGLEventListener(this);
	}
	
	/**
	 * Sets the image shown on the zoomWindow.
	 * 
	 * @param img See above.
	 */
	/*
	void setZoomImage(BufferedImage img)
	{
		zoomImage = img;
		invalidate();
		repaint();
	}
	*/
    /**
     * Overridden to draw the zoomed image and the current position of the lens
     * on the canvas.
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
	/*
    public void paint(Graphics g)
    {
        int w = this.getWidth();
        int h = this.getHeight();
        
        if (zoomImage == null) return;
        g.setColor(CLEAR_COLOUR);
        g.fillRect(0, 0, w, h);
        int x = (w/2)-zoomImage.getWidth()/2;
        int y = (h/2)-zoomImage.getHeight()/2;
        g.drawImage(zoomImage, x, y, zoomImage.getWidth(), 
                                        zoomImage.getHeight(), null);
    }
*/
	
	/**
	 * Paints the image.
	 * @see GLEventListener#display(GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		// Clear The Screen And The Depth Buffer
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);	
		TextureData data = model.getImageAsTexture();
		if (data == null) return;
		if (texture == null) texture = TextureIO.newTexture(data);
		else texture.updateImage(data);
		texture.enable();
		texture.bind();
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		float w = (float) model.getImageWidth();
		float h =  (float) model.getImageHeight();
		float xStart = ((float) model.getX())/w;
		float xEnd = ((float) model.getX()+model.getWidth())/w;
		float yStart =((float) model.getY())/h;
		float yEnd = ((float) model.getY()+model.getHeight())/h;

		TextureCoords coords = new TextureCoords(xStart, yEnd, xEnd, yStart);
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(coords.left(), coords.bottom());
		gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(coords.right(), coords.bottom());
		gl.glVertex3d(1, 0, 0);
		gl.glTexCoord2f(coords.right(), coords.top());
		gl.glVertex3f(1, 1, 0);
		gl.glTexCoord2f(coords.left(), coords.top());
		gl.glVertex3f(0, 1, 0);
		gl.glEnd();
		texture.disable();
		gl.glFlush();
	}

	/**
	 * Implemented as specified by the {@link GLEventListener} I/F.
	 * @see GLEventListener#init(GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
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
		glu.gluOrtho2D(0, 1, 0, 1);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

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


