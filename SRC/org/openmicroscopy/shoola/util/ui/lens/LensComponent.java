/*
 * org.openmicroscopy.shoola.util.ui.lens.lensComponent.java
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

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
	extends AbstractComponent
{

	/** Event fired when the zoomwindow is closed. */
	final static String		ZOOM_WINDOW_CLOSED_PROPERTY = "zoomWindowClosed";
    
	/** Default width of a lens */
	final static int		LENS_DEFAULT_WIDTH	= 50;
	
	/** Default height of a lens */
	final static int		LENS_DEFAULT_HEIGHT	= 50;
	
	/** Default magnification of lens. */
	final static float		DEFAULT_ZOOM = 2.0f;
	
	/** Reference to the lens object which will render onto the image canvas */
	private LensUI			lens;
	
	/** Menu objcet which hold the popup and menu items. */
	private	LensMenu 		menu;
	
	/** 
	 * Reference to the lensController which will modifiy the position and 
	 * properties of the lens and the zoomWindow. 
	 */
	private LensController	lensController;
	
	/** Shows the current zoomed image specified by the lens. */
	private ZoomWindow  	zoomWindow;
		
	/** Holds the properties of the lens, x,y, width height. */
	private LensModel		lensModel;
	
	/**
	 * Displays in pixels if <code>true</code>, in microns otherwise.
	 * 
	 * @param b See above.
	 */
	void setDisplayInPixels(boolean b)
	{
		zoomWindow.setDisplayInPixels(b);
		zoomWindow.setLensXY(lens.getX(), lens.getY());
		zoomWindow.setLensWidthHeight(lens.getWidth(), lens.getHeight());	
	}
	
	/**
	 * Sets the lens Size to a value described in LensAction. 
	 * 
	 * @param lensSize The size of the lens. 
	 */
	void setLensSize(int lensSize) 
	{
	     switch (lensSize) {
             case LensAction.LENSDEFAULTSIZE:
            	 lensController.setLensSize(LensComponent.LENS_DEFAULT_WIDTH, 
            			 LensComponent.LENS_DEFAULT_HEIGHT);
            	 break;
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
	 * Creates the lenscomponent which is the container for the lens 
	 * infrastructure.
	 * 
     * @param parent    parent JFrame of ZoomWindowUI. 
	 * @param planeImage Image being displayed by the viewer.
	 */
	public LensComponent(JFrame parent, BufferedImage planeImage)
	{
		lensModel = new LensModel(planeImage);
		zoomWindow = new ZoomWindow(parent, this);
		lens = new LensUI(this, LENS_DEFAULT_WIDTH, LENS_DEFAULT_HEIGHT);
		lensController = new LensController(lensModel, lens, zoomWindow);
		lensModel.setWidth(LENS_DEFAULT_WIDTH);
		lensModel.setHeight(LENS_DEFAULT_HEIGHT);
		lensModel.setImageZoomFactor(1.0f);
		lens.addController(lensController);
		lens.setLensColour(lensModel.getLensPreferredColour());
		zoomWindow.addController(lensController);
		menu = new LensMenu(this);
		lens.setPopupMenu(menu.getPopupMenu());
		zoomWindow.setMenu(menu.getMenubar());
	}
	
	/**
	 * Creates the lenscomponent which is the container for the lens 
	 * infrastructure.
	 * 
	 * @param parent parent JFrame of ZoomWindowUI. 
	 */
	public LensComponent(JFrame parent)
	{
		this(parent, null);
	}
	
	/**
	 * Sets the colour of the lens to better contrast with the 
	 * background of the image.
	 */
	public void setLensPreferredColour()
	{
		lens.setLensColour(lensModel.getLensPreferredColour());
	}
	
	/** Hides the lens and the control dialog. */
	public void zoomWindowClosed()
	{
		zoomWindow.setVisible(false);
		lens.setVisible(false);
	}
	
	/**
	 * Sets the mapping from pixel size to microns along the x and y axis. 
     * 
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	public void setXYPixelMicron(float  x, float y)
	{
		zoomWindow.setXYPixelMicron(x, y);
	}
	
	/**
	 * Sets the plane image of the lens to a new Image. 
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
	 * Sets the visiblity of the lens, and ZoomWindowUI.
	 * 
	 * @param makeVisible The value to set.
	 * 
	 */
	public void setVisible(boolean makeVisible)
	{
	    lens.setVisible(makeVisible);
		zoomWindow.setVisible(makeVisible);
	}
	
	/**
	 * Sets the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @param imageZoomFactor The amount of zooming that has occurred on the 
	 *                        image. 
	 */
	public void setImageZoomFactor(float imageZoomFactor)
	{
		lensModel.setImageZoomFactor(imageZoomFactor);
		lens.setImageZoomFactor();
	}
	
	/**
	 * Sets the zoomfactor for the lens. 
	 * 
	 * @param zoomFactor The magnification factor.
	 */
	public void setZoomFactor(float zoomFactor)
	{
		lensController.setZoomFactor(zoomFactor);
	}
	
	/**
	 * Sets the location of the lens on the canvas.
	 * 
	 * @param x The x-coordinate.
	 * @param y The y-coordinate. 
	 */
	public void setLensLocation(int x, int y)
	{
		lensController.setLensLocation(x, y);
	}
	
	/**
	 * Returns the zoomed image.
	 * 
	 * @return See above.
	 */
	public BufferedImage getZoomedImage() { return lensModel.getZoomedImage(); }
	
	/**
	 * Returns the lens UI. 
	 * 
	 * @return the lensUI. (a JPanel).
	 */
	public LensUI getLensUI() { return lens; }
	
	/**
	 * Returns <code>true</code> if the lens and zoomWindow are visible,
     * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isVisible()
	{
		return (lens.isVisible() && zoomWindow.isVisible());
	}

	/**
	 * Sets the location of the lens to point.
	 * 
	 * @param loc The value to set.
	 */
	public void setLensLocation(Point loc) { setLensLocation(loc.x, loc.y); }
	
	/** 
	 * Returns the scaled image size, takes into account the zoom factor of the 
	 * image viewer. 
	 * 
	 * @return size of lens, scaled by image zoom factor. 
	 */
	public Dimension getLensScaledSize()
	{ 
		return lensModel.getLensScaledSize();
	}
	
	/** 
	 * Gets the scaled image location, takes into account the zoom factor of the 
	 * image viewer. 
	 * 
	 * @return location of lens, scaled by image zoom factor. 
	 */
	public Point getLensScaledLocation()
	{
		return lensModel.getLensScaledLocation();
	}
	
	/**
	 * Returns the bounds of the scaled image size, takes into account the zoom 
	 * factor of the image viewer.
	 *  
	 * @return See above.
	 */
	public Rectangle getLensScaledBounds()
	{
		return lensModel.getLensScaledBounds();
	}
	
	/** 
	 * Gets the image location.
	 * 
	 * @return The location of lens. 
	 */
	public Point getLensLocation()
	{
		return lensModel.getLensLocation();
	}

	/**
	 * Sets the location of the zoomWindowUI. 
	 * 
	 * @param x x co-ordinate of the window location.
	 * @param y y co-ordinate of the window location.
	 */
	public void setZoomWindowLocation(int x, int y)
	{
		zoomWindow.setLocation(x, y);
	}
    
}
