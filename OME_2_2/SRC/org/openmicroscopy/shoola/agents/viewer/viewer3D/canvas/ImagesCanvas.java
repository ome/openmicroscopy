/*
 * org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.ImageCanvas
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas;

//Java imports
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3D;

/** 
 * 
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
public class ImagesCanvas
	extends JPanel
{
	
	private Viewer3DManager		manager;
	
	private Viewer3D			view;

	private static final int	LENGTH = 20;

	/** Space between the images. */
	private static final int	ORIGIN = 5;
	
	private static final int	ARROW = 3;
	
	private static final int	LENGTH_SQRT = (int) (LENGTH*(Math.sqrt(2.0))/2);
	
	/** Coordinate of the origin. */
	private int					xOrigin, yOrigin;
	
	/** width of the ZYimage+Viewer3D.SPACE+XYimage */
	private int					imagesWidth;
	
	/** height of the XZimage+Viewer3D.SPACE+XYimage */
	private int 				imagesHeight;
	
	private int					space;
	
	public ImagesCanvas(Viewer3D view, Viewer3DManager manager)
	{
		this.view = view;
		this.manager = manager;
		setBackground(Viewer3D.BACKGROUND_COLOR); 
		xOrigin = Viewer3D.SPACE;
		yOrigin = Viewer3D.SPACE;
	}

	public int getXOrigin() { return xOrigin; }
	
	public int getYOrigin() { return yOrigin; }
	
	/** Display the 3 buffered Images. */
	public void paintImages(int XYimageWidth, int ZYimageWidth, 
							int XYimageHeight)
	{
		space = Viewer3D.SPACE+ZYimageWidth;
		imagesWidth = XYimageWidth+Viewer3D.SPACE+ZYimageWidth;
		imagesHeight = XYimageHeight+Viewer3D.SPACE+ZYimageWidth;
		repaint();
	}

	/** Overrides the paintComponent. */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);

		g2D.setColor(Color.black);
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight()/4;
		setLocation();
		manager.setDrawingArea(xOrigin, yOrigin);
		paintFrame(g2D, hFont);
		paintXYFrame(g2D, hFont);
		paintXZFrame(g2D, hFont);
		paintZYFrame(g2D, hFont);
		g2D.drawImage(manager.getXZimage(), null, xOrigin+space, yOrigin);
		g2D.drawImage(manager.getZYimage(), null, xOrigin, yOrigin+space);
		g2D.drawImage(manager.getXYimage(), null, xOrigin+space, yOrigin+space);
	}

	/** Paint the frame. */
	private void paintFrame(Graphics2D g2D, int hFont)
	{
		//x-axis
		g2D.drawLine(xOrigin, yOrigin, xOrigin+LENGTH, yOrigin);
		g2D.drawLine(xOrigin+LENGTH-ARROW, yOrigin-ARROW, xOrigin+LENGTH, 
					yOrigin);
		g2D.drawLine(xOrigin+LENGTH-ARROW, yOrigin+ARROW, xOrigin+LENGTH, 
					yOrigin);
		//y-axis
		g2D.drawLine(xOrigin, yOrigin, xOrigin, yOrigin+LENGTH);
		g2D.drawLine(xOrigin-ARROW, yOrigin+LENGTH-ARROW, xOrigin, 
					yOrigin+LENGTH);
		g2D.drawLine(xOrigin+ARROW, yOrigin+LENGTH-ARROW, xOrigin, 
					yOrigin+LENGTH);
		//z-axis
		g2D.drawLine(xOrigin, yOrigin, xOrigin-LENGTH_SQRT, 
						yOrigin+LENGTH_SQRT);
		g2D.drawLine(xOrigin-LENGTH_SQRT, yOrigin+LENGTH_SQRT-5, 
					xOrigin-LENGTH_SQRT, yOrigin+LENGTH_SQRT);
		g2D.drawLine(xOrigin-LENGTH_SQRT+5, yOrigin+LENGTH_SQRT, 
					xOrigin-LENGTH_SQRT, yOrigin+LENGTH_SQRT);
		g2D.drawString("o", xOrigin-hFont, yOrigin-hFont);
		g2D.drawString("x", xOrigin+LENGTH/2, yOrigin-hFont);
		g2D.drawString("y", xOrigin+2*hFont, yOrigin+LENGTH-hFont);	
		g2D.drawString("z", xOrigin-LENGTH/2-hFont, yOrigin+LENGTH/4);
	}
	
	/** Paint the XZ-frame. */
	private void paintXZFrame(Graphics2D g2D, int hFont)
	{
		//x-axis
		int x1 = xOrigin+space-ORIGIN;
		int y1 = yOrigin-ORIGIN;
		g2D.drawLine(x1, y1, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1-ARROW, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1+ARROW, x1+LENGTH, y1);
		//z-axis
		g2D.drawLine(x1, y1, x1, y1+LENGTH);
		g2D.drawLine(x1-ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);
		g2D.drawLine(x1+ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);	
		//name axis
		g2D.drawString("o", x1-hFont, y1-hFont);
		g2D.drawString("x", x1+LENGTH/2, y1-hFont);
		g2D.drawString("z", x1-2*hFont, y1+LENGTH-hFont);	
	}
	

	/** Paint the XY-frame. */
	private void paintXYFrame(Graphics2D g2D, int hFont)
	{
		//x-axis
		int x1 = space+xOrigin-ORIGIN;
		int y1 = space+yOrigin-ORIGIN;
		g2D.drawLine(x1, y1, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1-ARROW, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1+ARROW, x1+LENGTH, y1);
		//y-axis
		g2D.drawLine(x1, y1, x1, y1+LENGTH);
		g2D.drawLine(x1-ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);
		g2D.drawLine(x1+ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);	
		//
		g2D.drawString("o", x1-hFont, y1-hFont);
		g2D.drawString("x", x1+LENGTH/2, y1-hFont);
		g2D.drawString("y", x1-2*hFont, y1+LENGTH-hFont);	
	}
	

	/** Paint the ZY-frame. */
	private void paintZYFrame(Graphics2D g2D, int hFont)
	{
		int x1 = xOrigin-ORIGIN;
		int y1 = space+yOrigin-ORIGIN;
		//x-axis
		g2D.drawLine(x1, y1, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1-ARROW, x1+LENGTH, y1);
		g2D.drawLine(x1-ARROW+LENGTH, y1+ARROW, x1+LENGTH, y1);
		//y-axis
		g2D.drawLine(x1, y1, x1, y1+LENGTH);
		g2D.drawLine(x1-ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);
		g2D.drawLine(x1+ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);	
		//
		g2D.drawString("o", x1-hFont, y1-hFont);
		g2D.drawString("z", x1+LENGTH/2, y1-hFont);
		g2D.drawString("y", x1-2*hFont, y1+LENGTH-hFont);	
	}

	/** Set the location of the origin. */
	private void setLocation()
	{
		Rectangle r = view.getScrollPane().getViewportBorderBounds();
		xOrigin = (int) (r.width-imagesWidth)/2;
		yOrigin = (int) (r.height-imagesHeight)/2;
		if (xOrigin < Viewer3D.SPACE) xOrigin = Viewer3D.SPACE;
		if (yOrigin < Viewer3D.SPACE) yOrigin = Viewer3D.SPACE;
	}		
	
}
