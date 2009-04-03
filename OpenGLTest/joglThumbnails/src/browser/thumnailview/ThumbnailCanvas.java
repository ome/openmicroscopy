/*
* browser.ThumbnailCanvas
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package browser.thumnailview;


//Java imports
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;

import browser.thumnailview.model.ThumbnailModel;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;

//Third-party libraries

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ThumbnailCanvas
	extends GLJPanel
	implements GLEventListener
{
	private GLU glu = new GLU();
	private static GLCapabilities caps;
	private ThumbnailModel model;
	
	static 
	{
	    caps = new GLCapabilities();
	    caps.setAlphaBits(8);
	}
	
	public ThumbnailCanvas(ThumbnailModel model)
	{
		super(caps, null, null);
		this.model = model;
		addGLEventListener(this);
		setOpaque(true);
		createUI();
		
	}
	
	private void createUI()
	{
		
	}

	public void display(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		Map<Long, Texture> texture = model.getThumbnailTexture();
		Map<Long, Rectangle2D> position = model.getThumbnailPosition();
		Iterator<Long> thumbnail = texture.keySet().iterator();
		while(thumbnail.hasNext())
		{
			Long thumbnailId = thumbnail.next();
			renderThumbnail(gl, (Rectangle2D.Float) position.get(thumbnailId), 
							texture.get(thumbnailId));
		}
	}
	
	private void renderThumbnail(GL gl, Rectangle2D.Float position, Texture texture)
	{
		texture.enable();
		texture.bind();
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
				GL.GL_REPLACE);
		TextureCoords coords = texture.getImageTexCoords();
		coords = new TextureCoords(0, 0, 1, 1);
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(coords.left(), coords.bottom());
		gl.glVertex3f(position.x, position.y, 0);
		gl.glTexCoord2f(coords.right(), coords.bottom());
		gl.glVertex3d(position.x+position.width, position.y, 0);
		gl.glTexCoord2f(coords.right(), coords.top());
		gl.glVertex3f(position.x+position.width, position.y+position.height,  0);
		gl.glTexCoord2f(coords.left(), coords.top());
		gl.glVertex3f(position.x, position.y+position.height, 0);
		gl.glEnd();
		texture.disable();
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) 
	{
		
	}

	public void init(GLAutoDrawable drawable) 
	{
		drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	public void reshape(GLAutoDrawable drawable,int x, int y, int width,
			int height) 
	{
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0, 1, 0, 1);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	
}