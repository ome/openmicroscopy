/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomWindow.java
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
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

//Third-party libraries

//Application-internal dependencies

/** 
 *	ZoomWindow is the component of the zoomWindowUI showing the zoomed verison
 *	of the lens.  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
class ZoomWindow
{
	
	/** The UI which displays the zoomed image. */
	private ZoomWindowUI 	zoomWindowUI;
	
	/** Controller which manipulates the lens and zoomWindow images. */
	private LensController	lensController;
	
	/** Parent component of the lens and ZoomWindow. */
	private LensComponent   lensComponent;
	
	/**
	 * Constructor of the zoomWindow. This creates an instance of the 
	 * ZoomWindowUI(JDialog).
	 * 
	 * @param parent JFrame parent window.  
	 * @param lensComponent The parent component of the ZoomWindow.
	 *
	 */
	ZoomWindow(JFrame parent, LensComponent lensComponent)
	{
		this.lensComponent = lensComponent;
		zoomWindowUI = new ZoomWindowUI(parent, lensComponent);
	}
	
	/**
	 * Creates a  new  instance.
	 * 
	 * @param lensComponent The parent component of the ZoomWindow.
	 * 						Mustn't be <code>null</code>.
	 */
	ZoomWindow(LensComponent lensComponent)
	{
		if (lensComponent == null)
			throw new IllegalArgumentException("No parent.");
		this.lensComponent = lensComponent;
		zoomWindowUI = new ZoomWindowUI(null, lensComponent);
	}
	
	/**
	 * Adds controller to the zoomWindow.
	 * 
	 * @param lensController 	Reference to the control. 
	 * 							Mustn't be <code>null</code>.
	 */
	void addController(LensController lensController)
	{
		if (lensController == null)
			throw new IllegalArgumentException("No control.");
		this.lensController = lensController;
	}

	/**
	 * Displays in pixels if <code>true</code> or in microns otherwise.
	 * 
	 * @param b see above.
	 */
	void setDisplayInPixels(boolean b)
	{
		zoomWindowUI.setDisplayInPixels(b);
	}

	/** 
	 * Adds menu to the zoomWindowUI. 
	 * 
	 * @param menu menubar.
	 */
	void setMenu(JMenuBar menu) { zoomWindowUI.setJMenuBar(menu); }
	
	/**
	 * Sets the mapping from pixel size to microns along the x and y axis. 
     * 
	 * @param x mapping in x axis.
	 * @param y mapping in y axis.
	 */
	void setXYPixelMicron(float x, float y) 
	{
		zoomWindowUI.setXYPixelMicron(x, y);
	}

	/** Repaints the zoomwindowUI after and update.  */
	void repaint() { zoomWindowUI.repaint(); }
	
	/**
	 * Sets the visiblity of the zoomWindowUI.
	 * 
	 * @param makeVisible see above.
	 */
	void setVisible(boolean makeVisible)
	{
		zoomWindowUI.setVisible(makeVisible);
	}
    
	/**
	 * Sets the zoomImage to be the bufferedImage.
	 * 
	 * @param zoomImage See above.
	 */
	void setZoomImage(BufferedImage zoomImage)
	{
		zoomWindowUI.setZoomImage(zoomImage);
	}
	
	/**
	 * Sets the X,Y co-ordinates of the lens on the ZoomWindowUI.
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensXY(int x, int y) { zoomWindowUI.setLensXY(x, y); }
	
	/**
	 * Sets the w,h size of the lens on the ZoomWindowUI.
	 * 
	 * @param w See above.
	 * @param h See above.
	 */
	void setLensWidthHeight(int w, int h)
	{
		zoomWindowUI.setLensWidthHeight(w, h);
	}

	/** 
     * Sets the zoomFactor of the lens.
	 * 
	 * @param zoomFactor See above.
	 */
	void setLensZoomFactor(float zoomFactor)
	{
		zoomWindowUI.setLensZoomFactor(zoomFactor);
	}

	/**
	 * Sets the size of the zoomWindowUI to scale with the zoomfactor. 
	 * 
	 * @param w width of zoomed image.
	 * @param h height of the zoomed image. 
	 */
	void setZoomUISize(float w, float h) 
	{
		zoomWindowUI.setZoomedImageSize((int) w, (int) h);
	} 
	
	/**
	 * Returns the UI of the zoomWindow. 
	 * 
	 * @return zoomWindowUI.
	 */
	ZoomWindowUI getUI() { return zoomWindowUI; }
	
	/**
	 * Returns <code>true</code> if the zoomWindowUI is visible,
     * <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	boolean isVisible() { return zoomWindowUI.isVisible(); }

	/**
	 * Sets the location of the zoomWindowUI. 
	 * 
	 * @param x x co-ordinate of the window location.
	 * @param y y co-ordinate of the window location.
	 */
	void setLocation(int x, int y) { zoomWindowUI.setLocation(x, y); }
	
}
