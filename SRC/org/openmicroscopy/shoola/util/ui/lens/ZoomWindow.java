/*
 * ZoomWindow.java
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
public class ZoomWindow
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
	 * @param lensComponent The parent component of the ZoomWindow.
	 *
	 */
	ZoomWindow(LensComponent lensComponent)
	{
		this.lensComponent = lensComponent;
		zoomWindowUI = new ZoomWindowUI(lensComponent);
	}
	
	/**
	 * Add controller to the zoomWindow.
	 * 
	 * @param lensController
	 */
	void addController(LensController lensController)
	{
		this.lensController = lensController;
	}
	
	void repaint()
	{
		zoomWindowUI.repaint();
	}
	
	/**
	 * Set the visiblity of the zoomWindowUI.
	 * 
	 * @param makeVisible see above.
	 */
	void setVisible(boolean makeVisible)
	{
		zoomWindowUI.setVisible(makeVisible);
	}
	/**
	 * Set the zoomImage to be the bufferedImage.
	 * 
	 * @param zoomImage See above.
	 */
	void setZoomImage(BufferedImage zoomImage)
	{
		zoomWindowUI.setZoomImage(zoomImage);
	}
	
	/**
	 * Set the X,Y co-ordinates of the lens on the ZoomWindowUI.
	 * 
	 * @param x See above.
	 * @param y See above.
	 */
	void setLensXY(int x, int y)
	{
		zoomWindowUI.setLensXY(x, y);
	}
	
	/**
	 * Set the w,h size of the lens on the ZoomWindowUI.
	 * 
	 * @param w See above.
	 * @param h See above.
	 */
	void setLensWidthHeight(int w, int h)
	{
		zoomWindowUI.setLensWidthHeight(w, h);
	}

	/** Set the zoomFactor of the lens.
	 * 
	 * @param zoomFactor See above.
	 */
	void setLensZoomFactor(float zoomFactor)
	{
		zoomWindowUI.setLensZoomFactor(zoomFactor);
	}

	
	/**
	 * Set the size of the zoomWindowUI to scale with the zoomfactor. 
	 * 
	 * @param w width of zoomed image.
	 * @param h height of the zoomed image. 
	 */
	void setZoomUISize(float w, float h) 
	{
		zoomWindowUI.setZoomedImageSize((int) w, (int) h);
	} 
	
	/**
	 * Return the UI of the zoomWindow. 
	 * 
	 * @return zoomWindowUI.
	 */
	ZoomWindowUI getUI()
	{
		return zoomWindowUI;
	}
	
	/**
	 * Is the zoomWindowUI visible.
	 *  
	 * @return see above.
	 */
	boolean isVisible()
	{
		return zoomWindowUI.isVisible();
	}
}
