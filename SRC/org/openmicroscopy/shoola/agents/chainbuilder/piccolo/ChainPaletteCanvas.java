/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteCanvas;
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




package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java Imports
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceEvent;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;


//Third-party libraries
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ModulePaletteWindow;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ChainSelection;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.BufferedObject;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ContentComponent;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.PConstants;


/** 
 * A {@link PCanvas} to hold a library of analysis chains 
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

public class ChainPaletteCanvas extends PCanvas implements BufferedObject,
	   ContentComponent, DragGestureListener /*,  SelectionEventListener*/ {
	
	/***
	 * Vertical space betwen chains
	 * 
	 */
	private static float VGAP=20f;
	
	/** 
	 * Horizonal space betwen chains
	 * 
	 */
	private static float HGAP=40f;
	
	
	/**
	 * Scengraph layer for the canvas.
	 */
	private PLayer layer;
	
	/**
	 * Initial vertical position
	 */
	private float y=VGAP;
	
	/** 
	 * Initial horizontal position
	 */
	private float x=0;
	
	/**
	 * Height of the current row
	 */
	private float rowHeight =0;
	
	/**
	 * The currently selected chain
	 */
	private LayoutChainData draggingChain;
	
	
	/** The chains */
	private Collection chains;
	/** 
	 * Listener for drag events
	 */
	private DragSourceAdapter dragListener;
	
	/**
	 * Source for drag events
	 */
	private DragSource dragSource;
	
//	private PExecutionList executionList;

	public ChainPaletteCanvas() {
		super();
		
		//SelectionState.getState().addSelectionEventListener(this);
		setBackground(PConstants.CANVAS_BACKGROUND_COLOR);
		layer = getLayer();
		
		// make sure that rendering is always high quality
		
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		
		setPreferredSize(new Dimension(ModulePaletteWindow.SIDE,
			ModulePaletteWindow.SIDE));
		setMinimumSize(new Dimension(ModulePaletteWindow.SIDE,
			ModulePaletteWindow.SIDE));
		// install custom event handler
		
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		 
		
		// initialize data transfer
		dragListener = new DragSourceAdapter() {
				public void dragExit(DragSourceEvent dse) {
				}
			};
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
			DnDConstants.ACTION_COPY,this); 
						
		// setup tool tips.
		PCamera camera = getCamera();
		camera.addInputEventListener(new ModulePaletteToolTipHandler(camera));
	}
	
	
	public void setContents(Object obj) {
		chains = (Collection) obj;
	}
	
	public void layoutContents() {
		// the current chain
		
		LayoutChainData chain;
		
		Iterator iter = chains.iterator();
		
		int num = chains.size();
		
		// The display should be roughly square, 
		// in terms of the number of rows vs. # of columns
		int rowSize = (int) Math.floor(Math.sqrt(num));
		
		int count=0;
		// draw each of them.
		while (iter.hasNext()) {
			chain = (LayoutChainData) iter.next();
			drawChain(chain);
			count++;
			if (count == rowSize) {
				// move on to next row.
				count = 0;
				x = 0;
				y+= rowHeight;
				rowHeight = 0;
			}
		} 	
	}
	
	
	/**
	 * Draw a chain on the canvas. The chain is drawn at the current values
	 * of x and y.
	 * @param chain
	 */
	public  void drawChain(LayoutChainData chain) {
		
		
		float height = 0;
		
		ChainBox box = new ChainBox(chain);
		layer.addChild(box);
		box.moveToBack();
		box.setOffset(x,y);

		//	setup the chain widget
		
		height = (float) box.getHeight();
 		// set the row height if this is taller than others in the row.
		if (height+VGAP>rowHeight)
			rowHeight = height;
		
		//advance the horizontal position
		x+= box.getWidth();
	}
	

	public void completeInitialization() {
		addInputEventListener(new ChainPaletteEventHandler(this));
		scaleToSize();
	}
	
	/**
	 * Animate the view to center on the  contents of this canvas.
	 *
	 */
	public void scaleToSize() {
		getCamera().animateViewToCenterBounds(getBufferedBounds(),true,0);
	}
	
	public void animateToSize() {
		getCamera().animateViewToCenterBounds(getBufferedBounds(),true,
				PConstants.ANIMATION_DELAY);
	}

	/**
	 * 
	 * @return canvas bounds with appropriate buffers for centering
	 */	
	public PBounds getBufferedBounds() {
		PBounds b = layer.getFullBounds();
		return new PBounds(b.getX()-PConstants.BORDER,
			b.getY()-PConstants.BORDER,b.getWidth()+2*PConstants.BORDER,
			b.getHeight()+2*PConstants.BORDER); 
	}

	
	public void setDraggingChain(LayoutChainData chain) {
		draggingChain = chain;
	}
	
	public void clearDraggingChain() {
		//System.err.println("clear chain selection");
		draggingChain = null;
	}
	
	private boolean isChainDragging() { 
		return (draggingChain != null);
	} 
	
	/**
	 * Start a drag event for copying a chain to the chain canvas, 
	 * with a bit of a hack. Packaged up the ID of the chain as an integer
	 * and send it along.
	 * 
	 * @see PPaletteCanvas
	 */
	public void dragGestureRecognized(DragGestureEvent event) {
		if (isChainDragging()) {
			Integer id = new Integer(draggingChain.getID());
			System.err.println("dragging chain.."+id+", "+draggingChain.getName());
			ChainSelection c = new ChainSelection(id);
			
			dragSource.startDrag(event,DragSource.DefaultMoveDrop,c,dragListener);
		}
		else 
			System.err.println("dragging in chain library, but no selection.");
	}
	
       /*public void selectionChanged(SelectionEvent e) {
		SelectionState state = e.getSelectionState();
		LayoutChainData chain = state.getSelectedChain();
		if (chain != null) {
			PChainBox cb = findChainBox(chain);
			if (cb != null)
				zoomToChain(cb);
		}
		else if (state.getSelectedDataset() == null) {
			// zoom to root.
			animateToSize();
		}
	}
	
	public int getEventMask() {
		return SelectionEvent.SET_SELECTED_CHAIN|
			SelectionEvent.SET_SELECTED_DATASET;
	}*/
	
	private ChainBox findChainBox(LayoutChainData chain) {
		ChainBoxFilter filter = new ChainBoxFilter(chain);
		
		// 	should only be one.
		Collection boxes = layer.getAllNodes(filter,null);
		
		Iterator iter = boxes.iterator();

		// 	should always have exactly one. but be defensive.
		if (!iter.hasNext())
				return null;
		
		// if there is one, it will be a chain box (guranteed by ChainBoxFilter)
		ChainBox cb = (ChainBox) iter.next();
		return cb;
	}
	
	private void zoomToChain(ChainBox cb) {
		//System.err.println("zooming in on chain.");
		BufferedObject cBox = (BufferedObject) cb;				
		PBounds b = cBox.getBufferedBounds();
		PCamera camera = getCamera();
		camera.animateViewToCenterBounds(b,true,PConstants.ANIMATION_DELAY);
	}
	
	private class ChainBoxFilter implements PNodeFilter {
		
		private final LayoutChainData chain;
		
		ChainBoxFilter(LayoutChainData chain) {
			this.chain = chain;	
		}
		public boolean accept(PNode node) {
			if (!(node instanceof ChainBox))
				return false;
			return ((ChainBox) node).getChain() == chain;
		}
		
		public boolean acceptChildrenOf(PNode node) {
			return true;
		}
	}
	
	/*public void clearExecutionList() {
		if (executionList != null) {
			layer.removeChild(executionList);
			executionList = null;
		}
	}
	public void showExecutionList(PDatasetLabelText dl) {
		clearExecutionList();
		executionList = dl.getExecutionList();
		
		
		layer.addChild(executionList);
		
		PBounds b = dl.getGlobalFullBounds();
		executionList.setOffset(b.getX(),b.getY()+b.getHeight());
	}*/
}