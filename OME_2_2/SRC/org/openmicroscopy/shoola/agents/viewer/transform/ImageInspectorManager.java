/*
 * org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager
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

package org.openmicroscopy.shoola.agents.viewer.transform;

//Java imports
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
import org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomBar;
import org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomMenu;

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
public class ImageInspectorManager
{

	/** Width and height of the current image. */
	private int						imageWidth, imageHeight;
	
	/** Canvas. */
	private ImageCanvas				canvas;
	
	/** Current zooming level. */
	private double					curZoomLevel;
	
    /** Reference to the view. */
    private ImageInspector          view;
    
    private ViewerCtrl              control;
    
	public ImageInspectorManager(ImageInspector view, ViewerCtrl control, 
                                double magFactor)
	{
		this.view = view;
        this.control = control;
		curZoomLevel = magFactor;
		attachListener();
	}
	
	/** Attach a window listener to the dialog. */
	private void attachListener()
	{
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { onClosing(); }
		});
	}
	
	/** Zoom in or out. */
	public void setZoomLevel(double level)
	{
		if (curZoomLevel != level) {
			zoom(level);
			curZoomLevel = level;
		}
	}
	
	public int getImageWidth() { return imageWidth; }
	
	public int getImageHeight() { return imageHeight; }
	
    void setCanvas(ImageCanvas canvas) { this.canvas = canvas; }
    
	/** 
	 * Get the with and height of the bufferedimage
	 * 
	 * @param image	bufferedImage to zoom in or out.
	 */
	void setImageDimension(int width, int height)
	{
		imageWidth = width;
		imageHeight = height;	
	}

	/** 
	 * Zoom in or out according to the level.
	 * 
	 * @param level	value between MIN_ZOOM_LEVEL and MAX_ZOOM_LEVEL.
	 */
	private void zoom(double level)
	{
		ZoomBar zoomBar = view.toolBar.getZoomBar();
		zoomBar.getManager().setText(level);
		ZoomMenu zoomMenu = view.menuBar.getZoomMenu();
		zoomMenu.getManager().setItemSelected(level);
		int w = (int) (imageWidth*level)+2*ViewerUIF.START;
        int h = (int) (imageHeight*level)+2*ViewerUIF.START;
        control.setSizePaintedComponents(new Dimension(w, h));
        canvas.paintImage(level, w, h);	
	}
 
    /** Handle windowClosing event. */
    private void onClosing()
    {
        control.setMagFactor(curZoomLevel);
        view.dispose();
    }
    
}
