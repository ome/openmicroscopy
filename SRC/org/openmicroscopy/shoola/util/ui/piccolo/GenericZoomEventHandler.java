/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.GenericZoomEventHandler
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
 
package org.openmicroscopy.shoola.util.ui.piccolo;

//Java imports
import java.awt.event.MouseEvent;

import org.openmicroscopy.shoola.util.ui.Constants;


//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies

/** 
 * An event handler superclass for zoomable {@link PCanvas} surfaces.
 * 
 *   
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class GenericZoomEventHandler extends  GenericEventHandler {
	
	/**
	 * The Canvas for which we are handling events
	 */
	protected BufferedObject canvas;
	
	/**
	 * A Mask to select for left button events
	 */
	protected int allButtonMask = MouseEvent.BUTTON1_MASK;
	
	private PBounds lastBounds;
	
	
	public GenericZoomEventHandler(BufferedObject canvas) {
		super();
		PInputEventFilter filter = new PInputEventFilter();
		filter.acceptEverything();
		setEventFilter(filter);
		this.canvas = canvas;		
	}
	
	
	/**
	 * If the mouse is single-clicked on a buffered node (either a 
	 * {@link CategoryBox}, or a {@link ModuleView}, zoom to center it.
	 * If the mouse is clicked on the layer or on the {@link PCamera},
	 * zoom to center the entire canvas.
	 * 
	 * If we right click, zoom out to the parent of where we clicked.
	 */
	public void doMouseClicked(PInputEvent e) {
		PNode node = e.getPickedNode();
		int mask = e.getModifiers() & allButtonMask;
		
	//	if (isPostPopup(e))
	//		return;
			
		if (mask == MouseEvent.BUTTON1_MASK && e.getClickCount() == 1) {
			
			if (node instanceof MouseableNode) {
				((MouseableNode) node).mouseClicked(this);
			}
			else if (node instanceof BufferedObject) {
				animateToNode(node);
				
			}
			else if (isBackgroundClick(node)) {
				handleBackgroundClick();
			}
		} 
		else if (e.isControlDown() || (mask & MouseEvent.BUTTON3_MASK)==1) {
			handlePopup(e);
		}
		e.setHandled(true); 
	}
		
	/**
	 * Specific code for handling a background click
	 */
	public void handleBackgroundClick() {
		animateToCanvasBounds();
	}
	

	
	/***
	 * Handle the popup, or zoom to the parent of the current node 
	 */
	public void handlePopup(PInputEvent e) {
		postPopup = true;
		PNode node = e.getPickedNode();
		if (node instanceof MouseableNode) {
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

	public  void animateToBounds(PBounds b) {
		PCamera camera = ((PCanvas) canvas).getCamera();
		camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);
		lastBounds = b;
	}
	
	public void animateToCanvasBounds() {
		animateToBounds(canvas.getBufferedBounds());
	}
	
	public void animateToLastBounds() {
		if (lastBounds != null)
			animateToBounds(lastBounds);
		else
			animateToCanvasBounds();
	}
	
	public void animateToNode(PNode node) {
		if (node instanceof BufferedObject) {
			animateToBufferedObject((BufferedObject) node);
		}
	}
	
	public void animateToBufferedObject(BufferedObject node) {
		animateToBounds(node.getBufferedBounds());
	}
	
	protected boolean isBackgroundClick(PNode node) {
		return (node instanceof PCamera || 
			node == ((PCanvas) canvas).getLayer());
	}
	
	public PCamera getCamera() {
		return ((PCanvas) canvas).getCamera();
	}
 }
