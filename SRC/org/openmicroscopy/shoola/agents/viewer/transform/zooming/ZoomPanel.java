/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomPanel
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;



//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

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
public class ZoomPanel
	extends JPanel
{
	
	/** Reference to the manager. */	
	private ImageInspectorManager	manager;
	
	/** Location of the image. */
	private int						x, y;
	
	/** Zoom level. */
	private double					magFactor;
	 
	public ZoomPanel(ImageInspectorManager manager)
	{
		this.manager = manager;
		setBackground(Viewer.BACKGROUND_COLOR); 
		magFactor = ImageInspectorManager.ZOOM_DEFAULT; 
	}
 
 	/** 
 	 * Paint the zoomed image. 
 	 * 
 	 * @param level		zoom level, value between 
 	 * 					{@link ImageInspectorManager#MIN_ZOOM_LEVEL} and
 	 * 					{@link ImageInspectorManager#MAX_ZOOM_LEVEL}.
 	 * @param x			x-coordinate.
 	 * @param y			y-coordinate.
 	 * 	
 	 */	
	public void paintImage(double level, int x, int y)
	{
		magFactor = level;
		this.x = x;
		this.y = y;
		repaint();
	} 

	/** Overrides the paintComponent. */
	public void paint(Graphics g)
	{
		super.paintComponent(g);
		BufferedImage image = manager.getBufferedImage();
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);

		g2D.setColor(Color.black);
		AffineTransform at = new AffineTransform();
		at.scale(magFactor, magFactor);
		BufferedImage bimg = new BufferedImage(image.getWidth(),
												image.getHeight(),
												BufferedImage.TYPE_INT_RGB);
		RescaleOp rop = new RescaleOp((float) magFactor, 0.0f, null);
		rop.filter(image, bimg);
		BufferedImageOp biop = new AffineTransformOp(at,
										AffineTransformOp.TYPE_BILINEAR);			
		g2D.drawImage(bimg, biop, x, y);
	}
	
}
