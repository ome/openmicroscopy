/*
 * org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D;

//Java imports
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.DrawingCanvasMng;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas.ImagesCanvas;


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
public class Viewer3DManager
{

	private Viewer3D 		view;
	
	/** Current image displayed. */
	private BufferedImage	XYimage, XZimage, ZYimage;
	
	private int 			xMax, yMax;
	
	private ImagesCanvas	canvas;
	
	private DrawingCanvas 	drawing;
	
	/** coordinates of the top-left corner of the XY-image. */
	private int				xMain, yMain;
	
	public Viewer3DManager(Viewer3D view)
	{
		this.view = view;
		attachListener();
	}
	
	/** Attach a window listener to the dialog. */
	private void attachListener()
	{
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { view.onClosing(); }
		});
	}
		
	public BufferedImage getXYimage() { return XYimage; }
	
	public BufferedImage getXZimage() { return XZimage; }

	public BufferedImage getZYimage() { return ZYimage; }

	void setImagesCanvas(ImagesCanvas canvas) { this.canvas = canvas; }
	
	void setDrawingCanvas(DrawingCanvas drawing) { this.drawing = drawing; }
	
	/**
	 * Display the 3 images in the canvas for the first time.
	 * 
	 * @param XYimage	XYImage to display.
	 * @param XZimage	XZImage to display.
	 * @param ZYimage	ZYImage to display.
	 */
	void setImages(BufferedImage XYimage, BufferedImage XZimage, 
					BufferedImage ZYimage)
	{
		this.XYimage = XYimage;
		this.XZimage = XZimage;
		this.ZYimage = ZYimage;
		xMax =  XZimage.getWidth()+XYimage.getWidth()+2*Viewer3D.SPACE;
		yMax = XZimage.getHeight()+XYimage.getHeight()+2*Viewer3D.SPACE;
		//set the size of the panel.
		Dimension d = new Dimension(xMax, yMax);
		canvas.setBounds(0, 0, xMax, yMax);
		drawing.setPreferredSize(d);
		drawing.setSize(d);		
		drawing.setBounds(0, 0, xMax, yMax);
		canvas.setPreferredSize(d);
		canvas.paintImages(XYimage.getWidth(), ZYimage.getWidth(),
									XYimage.getHeight());
		canvas.revalidate();
		setWindowSize(d);
	} 
	
	/** The XZImage and ZYImage to display. */
	void setImages(BufferedImage XZimage, BufferedImage ZYimage)
	{
		this.XZimage = XZimage;
		this.ZYimage = ZYimage;
		canvas.repaint();	
	}
	
	/** The XZImage, ZYImage and XYImage to display. */ 
	void resetImages(BufferedImage XYimage, BufferedImage XZimage, 
					BufferedImage ZYimage)	
	{
		this.XYimage = XYimage;
		this.XZimage = XZimage;
		this.ZYimage = ZYimage;	
		canvas.repaint();
	}
	
	/** X-coordinate of the origin of the frame. */
	public int getXOrigin() { return canvas.getXOrigin(); }
	
	/** Y-ccordinate of the origin of the frame. */
	public int getYOrigin() { return canvas.getYOrigin(); }
	
	/**
	 * A point has been selected on the XYImage. a new XZImage and ZYImage
	 * will be rendered.
	 * 
	 * @param x		x-coordinate of the point selected.
	 * @param y		y-coordinate of the point selected.
	 */
	public void onPlaneSelected(int x, int y)
	{
		view.onPlaneSelected(x-xMain, y-yMain);
	}
	
	/**
	 * A point has been selected on the XZImage. a new XZImage, ZYImage and 
	 * XYImage will be rendered.
	 * 
	 * @param x		x-coordinate of the point selected.
	 * @param y		y-coordinate of the point selected.
	 */
	public void onXZPlaneSelected(int x, int z)
	{
		view.onPlaneSelected(z-getYOrigin(), 
							x-getXOrigin()-Viewer3D.SPACE-ZYimage.getWidth(),
							Viewer3D.XZ);
	}
	
	/**
	 * A point has been selected on the ZYImage. a new XZImage, ZYImage and 
	 * XYImage will be rendered.
	 * 
	 * @param x		x-coordinate of the point selected.
	 * @param y		y-coordinate of the point selected.
	 */
	public void onZYPlaneSelected(int z, int y)
	{
		view.onPlaneSelected(z-getXOrigin(), 
							y-Viewer3D.SPACE-XZimage.getHeight()-getYOrigin(),
							Viewer3D.ZY);
	}
	
	/** 
	 * Set the drawing area. 
	 * 
	 * @param x	x-coordinate of origin of the frame.
	 * @param y y-coordinate of origin of the frame.
	 */
	public void setDrawingArea(int x, int y)
	{
		JLayeredPane contents = view.contents;
		JPanel back = view.backPanel;
		Dimension d = new Dimension(x+xMax, y+yMax);
		contents.setPreferredSize(d);
		contents.setSize(d);
		back.setPreferredSize(d);
		back.setSize(d);
		DrawingCanvasMng dm = drawing.getManager();
		xMain = x+ZYimage.getWidth()+Viewer3D.SPACE;
		yMain = y+XZimage.getHeight()+Viewer3D.SPACE;
		drawing.setDrawingDimension(XYimage.getWidth(), XYimage.getHeight(), 
									ZYimage.getWidth(), xMain, yMain);
	
		dm.setDrawingAreaXY(xMain, yMain, XYimage.getWidth(), 
							XYimage.getHeight());					
		dm.setDrawingAreaXZ(xMain, y, XYimage.getWidth(), XZimage.getHeight());
		dm.setDrawingAreaZY(x, yMain, ZYimage.getWidth(), ZYimage.getHeight());	
	}
	
	/** Set the size of the window. */
	private void setWindowSize(Dimension d)
	{
		int w = d.width;
		int h = d.height;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 7*(screenSize.width/10);
		int height = 7*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;	
		view.setSize(w, h);
	}
	
}
