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

import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericZoomEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;

//Java imports
import java.awt.geom.Point2D;

//Third-party libraries
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.ui.ThumbnailPopupMenu;
import org.openmicroscopy.shoola.env.config.Registry;

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
	
	private ThumbnailPopupMenu popup;
	
	public DatasetBrowserEventHandler(DatasetBrowserCanvas canvas,
			Registry registry) {
		super(canvas);
		this.canvas = canvas;
		popup =  new ThumbnailPopupMenu(canvas,registry);
	}
	
	
	
	/**
	 * What happens when I enter a node that is not a MouseableNode?
	 */	
	protected void defaultMouseEntered() {
		zoomLevel = 0;
	}

	
	// handle a popup.
	public void handlePopup(PInputEvent e) {
		postPopup = true;
		PNode node = e.getPickedNode();
		if (node instanceof Thumbnail) {
			Thumbnail  thumb = (Thumbnail) node;
			boolean canZoomOut = (zoomLevel != 0);
			popup.popup(thumb,canZoomOut,e.getCanvasPosition());
		}
		else if (node instanceof MouseableNode) {
			((MouseableNode) node).mousePopup(this);
		}
		else {
			PNode p = node.getParent();
			if (p instanceof BufferedObject) {
				animateToNode(p);		
			} else if (isBackgroundClick(node) || isBackgroundClick(p)) {
				handleBackgroundClick();
			}
		}
		e.setHandled(true);
	}
	
	/** 
	 * for mouse exit calls, do not pass the exit along if the node
	 * being exited is a datasetnode, but I am still within the bounds of the
	 * node. In this case, i am still inside of the node, but I've entered
	 * one of its children, so i don't want to say I've left.
	 */
	
	public void mouseExited(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof DatasetNode) {
			PBounds b = n.getBounds();
			Point2D pickedPos = e.getPositionRelativeTo(n);
			if (!b.contains(pickedPos))
				super.mouseExited(e);
		}
		else 
			super.mouseExited(e);
		e.setHandled(true);
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