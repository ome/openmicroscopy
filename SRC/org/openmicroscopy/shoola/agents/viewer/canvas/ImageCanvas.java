/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.viewer.canvas;

//Java imports
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;

/** 
 * Canvas to display the selected buffered 2D-image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImageCanvas
	extends JPanel
{

	/** The BufferedImage to display. */	
	private BufferedImage 		image;
	
	/** Width and height. */
	private int					imageWidth, imageHeight;
	
	/** Coordinates of the top-left corner of he image. */
	private int					x, y;
	
	private ViewerUIF view;
	
	public ImageCanvas(ViewerUIF view)
	{
		this.view = view;
		setBackground(Viewer.BACKGROUND_COLOR); 
		setDoubleBuffered(true);
	}
 
	/** 
	 * Paint the image. 
	 * 
	 * @param image		buffered image to display.
	 * 	
	 */	
	public void paintImage(BufferedImage image)
	{
		this.image = image;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		repaint();
	} 
	
	/** Overrides the paintComponent. */
	public void paint(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		setLocation();
		paintXYFrame(g2D);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);

		g2D.setColor(Color.black);
		g2D.drawImage(image, null, x+ViewerUIF.START, y+ViewerUIF.START);
	}

	/** Paint the frame. */
	private void paintXYFrame(Graphics2D g2D)
	{
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight()/4;
		//x-axis
		int x1 = ViewerUIF.START-ViewerUIF.ORIGIN+x;
		int y1 = ViewerUIF.START-ViewerUIF.ORIGIN+y;
		g2D.drawLine(x1, y1, x1+ViewerUIF.LENGTH, y1);
		g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1-ViewerUIF.ARROW, 
					x1+ViewerUIF.LENGTH, y1);
		g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1+ViewerUIF.ARROW, 
					x1+ViewerUIF.LENGTH, y1);
		//y-axis
		g2D.drawLine(x1, y1, x1, ViewerUIF.LENGTH+y1);
		g2D.drawLine(x1-ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
					x1, ViewerUIF.LENGTH+y1);
		g2D.drawLine(x1+ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
					x1, ViewerUIF.LENGTH+y1);	
		//name
		g2D.drawString("o", x1-hFont, y1-hFont);
		g2D.drawString("x", x1+ViewerUIF.LENGTH/2, y1-hFont);
		g2D.drawString("y", x1-2*hFont, y1+ViewerUIF.LENGTH-hFont);	
	}
	
	/** Set the coordinates of the top-left corner of the image. */
	private void setLocation()
	{
		Rectangle r = view.getScrollPane().getViewportBorderBounds();
		x = (int) (r.width-imageWidth)/2;
		y = (int) (r.height-imageHeight)/2;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
	}
	
}
