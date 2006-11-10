/*
 * org.openmicroscopy.shoola.util.ui.lens.lensComponent.java
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries
//Application-internal dependencies
/** 
 * The Lens Component is the main component of the lens accessable from outside
 * of the lens Package. 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
public class LensComponent 
{

	/** Default width of a lens */
	final static int		LENS_DEFAULT_WIDTH	= 50;
	
	/** Default height of a lens */
	final static int		LENS_DEFAULT_HEIGHT	= 50;
	
	/** Default magnification of lens. */
	final static float		DEFAULT_ZOOM = 2.0f;
	
	/** Refeence to the lens object which will render onto the image canvas */
	private LensUI			lens;
	
	/** 
	 * Reference to the lensController which will modifiy the position and 
	 * properties of the lens and the zoomWindow. 
	 */
	private LensController	lensController;
	
	/** Shows the current zoomed image specified by the lens. */
	private ZoomWindow  	zoomWindow;
	
	/** The image on the canvas */
	private BufferedImage 	planeImage;
	
	/** Holds the properties of the lens, x,y, width height. */
	private LensModel		lensModel;
	
	
	/**
	 * Create the lenscomponent which is the container for the lens 
	 * infrastructure.
	 * 
	 * @param planeImage Image being displayed by the viewer.
	 */
	public LensComponent(BufferedImage planeImage)
	{
		this.planeImage = planeImage;
		lensModel = new LensModel(planeImage);
		zoomWindow = new ZoomWindow(this);
		lens = new LensUI(this, LENS_DEFAULT_WIDTH, LENS_DEFAULT_HEIGHT);
		lensController = new LensController(lensModel , lens, zoomWindow);
		lensModel.setWidth(LENS_DEFAULT_WIDTH);
		lensModel.setHeight(LENS_DEFAULT_HEIGHT);
		lens.addController(lensController);
		zoomWindow.addController(lensController);
	}
	
	/**
	 * Create the lenscomponent which is the container for the lens 
	 * infrastructure.
	 * 
	 * @param planeImage Image being displayed by the viewer.
	 */
	public LensComponent()
	{
		this.planeImage = null;
		lensModel = new LensModel(null);
		zoomWindow = new ZoomWindow(this);
		lens = new LensUI(this, LENS_DEFAULT_WIDTH, LENS_DEFAULT_HEIGHT);
		lensController = new LensController(lensModel , lens, zoomWindow);
		lensModel.setWidth(LENS_DEFAULT_WIDTH);
		lensModel.setHeight(LENS_DEFAULT_HEIGHT);
		lens.addController(lensController);
		zoomWindow.addController(lensController);
	}

	/**
	 * Display in pixels if <code>true</code> or in microns otherwise.
	 * 
	 * @param b see above.
	 */
	void setDisplayInPixels(boolean b)
	{
		zoomWindow.setDisplayInPixels(b);
		zoomWindow.setLensXY(lens.getX(), lens.getY());
		zoomWindow.setLensWidthHeight(lens.getWidth(), lens.getHeight());
	}

	/**
	 * Set the mapping from pixel size to microns along the x and y axis. 
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	public void setXYPixelMicron(float  x, float y)
	{
		zoomWindow.setXYPixelMicron(x, y);
	}
	
	/**
	 * Set the plane image of the lens to a new Image. 
	 * 
	 * @param img new Image.
	 */
	public void setPlaneImage(BufferedImage img)
	{
		lensModel.setPlaneImage(img);
		zoomWindow.setZoomImage(lensModel.getZoomedImage());
		zoomWindow.repaint();
	}
	
	/**
	 * Set the visiblity of the lens, and ZoomWindowUI.
	 * 
	 * @param makeVisible see above.
	 * 
	 */
	public void setVisible(boolean makeVisible)
	{
			lens.setVisible(makeVisible);
			zoomWindow.setVisible(makeVisible);
	}
	/**
	 * Set the zoomfactor for the lens. 
	 * 
	 * @param zoomFactor
	 */
	public void setZoomFactor(float zoomFactor)
	{
		lensController.setZoomFactor(zoomFactor);
	}
	
	/**
	 * Set the location of the lens on the canvas.
	 * 
	 * @param x see above.
	 * @param y see above. 
	 */
	public void setLensLocation(int x, int y)
	{
		lensController.setLensLocation(x, y);
	}

	/**
	 * Set the lens Size to a value described in LensAction. 
	 * 
	 * @param lensSize from lensAction. 
	 */
	public void setLensSize(int lensSize) 
	{
	     switch (lensSize) 
	     {
         case LensAction.LENSDEFAULTSIZE:
        	 lensController.setLensSize(LensComponent.LENS_DEFAULT_WIDTH, 
        			 LensComponent.LENS_DEFAULT_HEIGHT);
         case LensAction.LENS40x40:
        	 lensController.setLensSize(40, 40);
        	 break;
         case LensAction.LENS50x50:
        	 lensController.setLensSize(50, 50);
        	 break;
         case LensAction.LENS60x60:
        	 lensController.setLensSize(60, 60);
        	 break;
         case LensAction.LENS70x70:
        	 lensController.setLensSize(70, 70);
        	 break;
         case LensAction.LENS80x80:
        	 lensController.setLensSize(80, 80);
        	 break;
         case LensAction.LENS90x90:
        	 lensController.setLensSize(90, 90);
        	 break;
         case LensAction.LENS100x100:
        	 lensController.setLensSize(100, 100);
        	 break;
         case LensAction.LENS120x120:
        	 lensController.setLensSize(120, 120);
        	 break;
         case LensAction.LENS150x150:
        	 lensController.setLensSize(150, 150);
        	 break;
         default:
             throw new IllegalArgumentException("Index not supported.");
	     }
	}
	
	/**
	 * Return the zoomed image.
	 * 
	 * @return see above.
	 */
	public BufferedImage getZoomedImage()
	{
		return lensModel.getZoomedImage();
	}
	
	/**
	 * Get the lens UI. 
	 * 
	 * @return the lensUI. (a JPanel);
	 */
	public LensUI getLensUI()
	{
		return lens;
	}
	
	/**
	 * Are the lens and zoomWindow visible.
	 * 
	 * @return see above.
	 */
	public boolean isVisible()
	{
		return (lens.isVisible() && zoomWindow.isVisible());
	}
	
}
