/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util.proj;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries

//Application-internal dependencies

/** 
 * Component hosting the canvas. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class ProjectionUI 
	extends JScrollPane
{

	/** The canvas displaying the image. */
	private ProjectionCanvas	   canvas;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param background
	 */
	ProjectionUI(Color background)
	{
		canvas = new ProjectionCanvas(background);
		JViewport port= getViewport();
		port.setLayout(null);
		port.setBackground(background);
		port.add(canvas);
	}
	
	/**
	 * Sets the projected image.
	 * 
	 * @param image The image to set.
	 */
	void setProjectedImage(BufferedImage image)
	{
		if (image == null) return;
		canvas.setImage(image);
		Dimension d = new Dimension(image.getWidth(), image.getHeight());
        canvas.setPreferredSize(d);
        canvas.setSize(d);
	}
	
	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(Rectangle)
	 */
	public void setBounds(Rectangle r)
	{
		setBounds(r.x, r.y, r.width, r.height);
	}
	
	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		Rectangle r = getViewport().getViewRect();
		Dimension d = canvas.getPreferredSize();
		int xLoc = ((r.width-d.width)/2);
		int yLoc = ((r.height-d.height)/2);
		canvas.setBounds(xLoc, yLoc, d.width, d.height);
	}
	
}
