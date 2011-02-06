/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ProjectionCanvas 
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



//Third-party libraries
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies

/** 
 * Paints the projected image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ProjectionCanvas 
	extends GLImageCanvas
{
	
    /** The mouse listener. */
    private MouseAdapter	listener;
    
    /** The text displayed when no projection preview. */
    private String          text;
    
    /** Attaches the listener. */
    private void attachListener()
    {
    	if (listener != null) return;
    	listener = new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				model.projectionPreview();
				text = ProjectionBICanvas.CREATION_TEXT;
		    	removeMouseListener(listener);
		    	repaint();
			}
		};
		addMouseListener(listener);
    }
    
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
	ProjectionCanvas(BrowserModel model, BrowserUI view)
	{
		super(model, view);
		text = ProjectionBICanvas.DEFAULT_TEXT;
	}

    /**
     * Paints the image.
     * 
     */
    public void display(GLAutoDrawable drawable) 
	{
    	TextureData data = model.getProjectedImageAsTexture();
    	if (data == null) {
    		GL gl = drawable.getGL();
    		// Clear The Screen And The Depth Buffer
    		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);	
    		Color c = ProjectionBICanvas.BACKGROUND_COLOR;
    		float[] array = new float[4];
    		array = c.getRGBColorComponents(array);
    		gl.glClearColor(array[0], array[1], array[2], array[3]);
    		c = ProjectionBICanvas.TEXT_COLOR;
    		gl.glColor3f(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
            gl.glRasterPos2f(0.25f, 0.5f);
        	glut.glutBitmapString(FONT, text);
        	attachListener();
    		return;
    	}
    	onDisplay(drawable, data);
	}
    
}
