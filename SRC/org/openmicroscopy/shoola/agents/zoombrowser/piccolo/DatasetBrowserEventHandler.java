/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.
 * 	DatasetBrowserEventHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * An event handler for the {@link DatasetBrowserCanvas}. Handle zooming into
 * and out of datasets, including multi-level zoom via
 * {@link ThumnailSelectionHalo} objects for zooming.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class DatasetBrowserEventHandler extends GenericZoomEventHandler {
	

	/**
	 * The canvas to which this is attached
	 */
	private DatasetBrowserCanvas canvas;
	
	/**
	 * How far down we have zoomed? Zooming a large browser canvas works on a 
	 * principal of varying zoom levels, rooughly based on exponentially 
	 * smaller steps: starting with zoom level 0 (the full screen), each 
	 * susbsequent zoom lvel zooms to a smaller subset - see 
	 * {@link DatasetImagesNode} for details on the zooming 
	 */
	private int zoomLevel = 0;
	
	public DatasetBrowserEventHandler(DatasetBrowserCanvas canvas) {
		super(canvas);
		this.canvas = canvas;	
	}

	/**
	 * What happens when I enter a node that is not a MouseableNode?
	 */	
	protected void defaultMouseEntered() {
		zoomLevel = 0;
	}

	
	/** What happens when I click on the background? */
	public  void handleBackgroundClick() {
		// click on background clears selected dataset
		canvas.setSelectedDataset(null);
		zoomLevel =0;	
		super.handleBackgroundClick();
	}
	
	/**
	 * @return
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * @param i
	 */
	public void setZoomLevel(int i) {
		zoomLevel = i;
	}
	
	public void resetZoomLevel() {
		setZoomLevel(0);
	}

}