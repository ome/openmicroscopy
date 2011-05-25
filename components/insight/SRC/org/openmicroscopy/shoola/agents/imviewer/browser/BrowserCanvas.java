/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.util.Map;

//Third-party libraries
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.Region;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

/** 
 * UI component where the image is painted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class BrowserCanvas
    extends GLImageCanvas
{
    
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    BrowserCanvas(BrowserModel model, BrowserUI view)
    {
        super(model, view);
    }

    /**
     * Paints the image.
     * @see GLImageCanvas#display(GLAutoDrawable)
     */
    public void display(GLAutoDrawable drawable) 
	{
    	if (model.isBigImage()) {
    		GL gl = drawable.getGL();
    		// Clear The Screen And The Depth Buffer
    		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
    				GL.GL_REPLACE);
    		Map<Integer, Tile> tiles = model.getTiles();
        	int rows = model.getRows();
        	int columns = model.getColumns();
        	Tile tile;
        	int index;
        	Object img;
            Region r;
            TextureCoords coords = new TextureCoords(0f, 0f, 1f, 1f);
			Color c = ImageCanvas.BACKGROUND;
    		float xStart, yStart, xEnd = 0, yEnd;
    		drawScaleBar(gl, model.getTiledImageSizeX(), 
    				model.getTiledImageSizeY());
        	for (int i = 0; i < rows; i++) {
    			for (int j = 0; j < columns; j++) {
    				index = i*columns+j;
    				tile = tiles.get(index);
    				r = tile.getRegion();
    				img = tile.getImage();
    				xStart = (float) r.getX()/(r.getWidth()*columns);
    				xEnd = 
    					(float) (r.getX()+r.getWidth())/(r.getWidth()*columns);
    				yStart = (float) r.getY()/(r.getHeight()*rows);
    				yEnd = 
    					(float) (r.getY()+r.getHeight())/(r.getHeight()*rows);
    				if (img != null) {
    					if (texture == null) 
    						texture = TextureIO.newTexture((TextureData) img);
    					else texture.updateImage((TextureData) img);
    					texture.enable();
    					texture.bind();
    					gl.glBegin(GL.GL_QUADS);
    					gl.glTexCoord2f(coords.left(), coords.bottom());
    					gl.glVertex3f(xStart, yStart, 0);
    					gl.glTexCoord2f(coords.right(), coords.bottom());
    					gl.glVertex3d(xEnd, yStart, 0);
    					gl.glTexCoord2f(coords.right(), coords.top());
    					gl.glVertex3f(xEnd, yEnd, 0);
    					gl.glTexCoord2f(coords.left(), coords.top());
    					gl.glVertex3f(xStart, yEnd, 0);
    					gl.glEnd();
    					
    				} else { //draw the grid.
    					gl.glColor3f(c.getRed()/255f, c.getGreen()/255f,
    							c.getBlue()/255f);
    					gl.glBegin(GL.GL_LINES);
    					gl.glVertex3f(xStart, yStart, 0.0f); 
    					gl.glVertex3f(xEnd, yStart, 0.0f); 
    					
    					gl.glVertex3f(xStart, yStart, 0.0f); 
    					gl.glVertex3f(xStart, yEnd, 0.0f);
    					
    					gl.glVertex3f(xStart, yEnd, 0.0f); 
    					gl.glVertex3f(xEnd, yEnd, 0.0f);
    					
    					gl.glVertex3f(xEnd, yStart, 0.0f); 
    					gl.glVertex3f(xEnd, yEnd, 0.0f);
    					gl.glEnd();
    				}
    			}
    		}
        	if (texture != null) texture.disable();
        	
        	gl.glFlush();
    	} else {//image
    		onDisplay(drawable, model.getRenderedImageAsTexture());
    	}
	}
    
}
