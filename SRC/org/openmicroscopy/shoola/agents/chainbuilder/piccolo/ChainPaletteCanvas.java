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
import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ModulePaletteWindow;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ChainSelection;
import org.openmicroscopy.shoola.agents.events.DatasetEvent;
import org.openmicroscopy.shoola.agents.events.MouseOverChainExecutionEvent;
import org.openmicroscopy.shoola.agents.events.MouseOverDataset;
import org.openmicroscopy.shoola.agents.events.SelectChainExecutionEvent;
import org.openmicroscopy.shoola.agents.events.SelectDataset;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedCanvas;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.ButtonNode;
import org.openmicroscopy.shoola.util.ui.piccolo.ContentComponent;


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

public class ChainPaletteCanvas extends BufferedCanvas implements 
	   ContentComponent, DragGestureListener, AgentEventListener  {
	
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
	
	
	/** 
	 * Listener for drag events
	 */
	private DragSourceAdapter dragListener;
	
	/**
	 * Source for drag events
	 */
	private DragSource dragSource;
	
	/*** my event handler */
	private ChainPaletteEventHandler handler;
	
	/** paneevent handler*/
	private ChainPalettePanHandler panHandler;
	
	/** are we panning */
	private boolean panning = false;
	
	/** the data manager */
	private ChainDataManager dataManager;
	
	/** number of items in current row */
	private int count=0; 
	
	/** number of items to put in a row */
	private int rowSize;
	
	/** max width of a row */
	private float maxRowWidth = 0;
	
	/** the pan button */
	private ChainPalettePanButton panButton;
	
	/** the zoom button */
	private ChainPaletteZoomButton zoomButton;
	
	public ChainPaletteCanvas(ChainDataManager dataManager) {
		super();
		this.dataManager = dataManager;
		
		//SelectionState.getState().addSelectionEventListener(this);
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		layer = getLayer();
		layer.setPickable(false);
		
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
		removeInputEventListener(panHandler);
		 
		
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
		camera.addInputEventListener(new ChainbuilderToolTipHandler(camera));
		
		// build buttons
		IconFactory icons = dataManager.getIconFactory();
		Icon zoomIcon = icons.getIcon("zoomMode.png");
		Icon zoomIconDisabled = icons.getIcon("zoomMode-disabled.png");
		Icon panIcon = icons.getIcon("pan.png");
		Icon panIconDisabled = icons.getIcon("pan-disabled.png");
		
		
		Image panImage = ((ImageIcon) panIcon).getImage();
		Image panDisabledImage = ((ImageIcon) panIconDisabled).getImage();
		panButton = new ChainPalettePanButton(this,panImage,panDisabledImage);
		panButton.addToCanvas(this);
		panButton.setEnabled(true);
		
		Image zoomImage = ((ImageIcon) zoomIcon).getImage();
		Image zoomDisabledImage = ((ImageIcon) zoomIconDisabled).getImage();
		zoomButton = new ChainPaletteZoomButton(this,zoomImage,zoomDisabledImage);
		zoomButton.addToCanvas(this);
		zoomButton.setOffset(panButton.getWidth(),0);
		zoomButton.setEnabled(false);
		
		dataManager.getRegistry().getEventBus().register(this,new Class[] {
				MouseOverDataset.class, SelectDataset.class,
				MouseOverChainExecutionEvent.class,
				SelectChainExecutionEvent.class});
	}
	
	
	public void setContents(Object obj) {
	}
	
	public void layoutContents() {
		// the current chain
		
		LayoutChainData chain;
		PaletteChainView view;
		
		Collection chains = dataManager.getChains();
		ArrayList views = buildChainViews(chains);
		
		int num = views.size();
		
		// The display should be roughly square, 
		// in terms of the number of rows vs. # of columns
		rowSize = (int) Math.floor(Math.sqrt(num));
		
		count=0;
		// draw each of them.
		Iterator iter = views.iterator();
		while (iter.hasNext()) {
			view = (PaletteChainView) iter.next();
			ChainBox box = buildChain(view);
			if (box != null)  {
				placeChain(box);
			}
		}
		if (x > maxRowWidth) {
			maxRowWidth = x;
		}
		// fix up the last row.
		row.setHeight(rowHeight);
		
		rows.add(row);
		adjustSizes();
	}
	
	private ArrayList buildChainViews(Collection chains) {
		LayoutChainData chain;
		PaletteChainView view;
		
		ArrayList sortedChains = new ArrayList(chains);
		Collections.sort(sortedChains);
		Iterator iter = sortedChains.iterator();
		
		//build the chains
		ArrayList views = new ArrayList();
		while (iter.hasNext()) {
			chain = (LayoutChainData) iter.next();
			if (!chain.hasCycles()) {
				view = new PaletteChainView(chain,dataManager.getRegistry());
				views.add(view);
			}
		}
		//scaleAreas(views);
		// sort by areas.
		return views;
	}
	
	private void scaleAreas(ArrayList views) {
		PaletteChainView view;
		
		Collections.sort(views);
		/*
		// get smallest area
		PaletteChainView smallest= (PaletteChainView) views.get(0);
		double logSmallestArea = Math.log(smallest.getArea());
		double logArea;
		double ratio;
		double newArea;
		double scale;
		Iterator iter = views.iterator();
		
		
		iter = views.iterator();
		while (iter.hasNext()) {
			view = (PaletteChainView) iter.next();
			logArea = Math.log(view.getArea());
			ratio = 3*logArea/logSmallestArea;
			newArea = smallest.getArea()*ratio;
			scale = newArea/view.getArea();
			view.setScale(scale);
			System.err.println("\nchain..."+view.getChain().getName());
			System.err.println("chain width is "+view.getWidth());
			System.err.println(" scale is "+scale);
			double newWidth = scale*view.getWidth();
			System.err.println(" scaled width is "+newWidth);
		}*/
		//		 get largest area
		PaletteChainView largest= (PaletteChainView) views.get(views.size()-1);
		System.err.println("largest chain is "+largest.getChain().getName()+
				", area is "+largest.getArea());
		System.err.println("largest width is "+largest.getWidth()+","+largest.getHeight());
		
		double logLargestArea = Math.log(largest.getArea());
		double logArea;
		double ratio;
		double newArea;
		double scale;
		Iterator iter = views.iterator();
		
		
		iter = views.iterator();
		while (iter.hasNext()) {
			view = (PaletteChainView) iter.next();
			logArea = Math.log(view.getArea());
			ratio = logArea/logLargestArea;
			newArea = largest.getArea()*ratio;
			scale = newArea/view.getArea();
			view.setScale(scale);
			System.err.println("\nchain..."+view.getChain().getName());
			System.err.println("original area is "+view.getArea());
			System.err.println("chain width is "+view.getWidth());
			System.err.println(" scale is "+scale);
			double newWidth = scale*view.getWidth();
			System.err.println(" scaled width is "+newWidth);
			System.err.println("new area is "+newArea);
		}
	}
	
	// keep track of what was in row that was just finished.
	private ArrayList rows = new ArrayList();
	private RowInfo row = new RowInfo();
	
	private void placeChain(ChainBox box) {
			
		float height = 0;
		
		double width = box.getWidth();
		height = (float) box.getHeight();
 
		//	setup the chain widget
		
		// set the row height if this is taller than others in the row.
		if (height+VGAP>rowHeight)
			rowHeight = height;
		
		if (count >= rowSize && (x+width > maxRowWidth)) {
			// move on to next row.
			count = 0;
			if (x > maxRowWidth) {
				maxRowWidth = x;
			}
			x = 0;
			row.setHeight(rowHeight);
			rows.add(row);
			y+= rowHeight;
			rowHeight = height;
			row = new RowInfo();
		}
		
		box.setOffset(x,y);
		x+=box.getWidth();
		count++;
		row.addBox(box);
	}
	
	
	
	/*private ChainBox buildChain(LayoutChainData chain) {
		if (chain.getNodes().size() == 0) 
			return null;
		ChainBox box = new ChainBox(chain,dataManager.getRegistry());
		layer.addChild(box);
		box.moveToBack();
		return box;

	}*/
	
	
	private ChainBox buildChain(ChainView chainView) {
		if (chainView.getChain().getNodes().size() == 0) 
			return null;
		ChainBox box = new ChainBox(chainView,dataManager.getRegistry());
		layer.addChild(box);
		box.moveToBack();
		return box;
	}
	
	public void displayNewChain(LayoutChainData chain) {
		PaletteChainView chainView = new 
			PaletteChainView(chain,dataManager.getRegistry());
		ChainBox box = buildChain(chainView);
		if (box != null)  {
			placeChainInNewRow(box);
			// fix up last row
			adjustSizes();
			scaleToSize();
		}
		
	}
	
	/*
	 * new chains must be placed in their own rows- otherwise, 
	 * it's too difficult to figure.  
	 */
	private void placeChainInNewRow(ChainBox box) {
		float height = 0;
		
		float width = (float)box.getWidth();
		y+=rowHeight;
		rowHeight = (float) box.getHeight();
 
		x = 0;
		if (width > maxRowWidth) {
			maxRowWidth = width;
		}
		row = new RowInfo();
		row.setHeight(rowHeight);
		rows.add(row);
		
		box.setOffset(x,y);
		row.addBox(box);
	}
	
	private void adjustSizes() {
		Iterator iter = rows.iterator();
		while (iter.hasNext()) {
			RowInfo row = (RowInfo) iter.next();
			row.adjustSize(maxRowWidth);
		}
	}

	public void completeInitialization() {
		handler = new ChainPaletteEventHandler(this,dataManager.getRegistry()); 
		panHandler = new ChainPalettePanHandler(handler);
		addInputEventListener(handler);
	}
	
	public void setToZoom() {
		panning = false;
		removeInputEventListener(panHandler);
		addInputEventListener(handler);
		zoomButton.setEnabled(false);
		panButton.setEnabled(true);
	}
	
	public void setToPan() {
		panning = true;
		removeInputEventListener(handler);
		addInputEventListener(panHandler);
		zoomButton.setEnabled(true);
		panButton.setEnabled(false);
	}
	
	public void scaleToSize() {
		handler.animateToCanvasBounds();
	}

	public void scaleToResize() {
		handler.animateToLastBounds();
	}
	
	public void setDraggingChain(LayoutChainData chain) {
		draggingChain = chain;
	}
	
	public void clearDraggingChain() {
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
		if (panning == false && isChainDragging()) {
			Integer id = new Integer(draggingChain.getID());
			ChainSelection c = new ChainSelection(id);
			
			dragSource.startDrag(event,DragSource.DefaultMoveDrop,c,dragListener);
		}
	}
	
    
	
	private ChainBox findChainBox(AnalysisChainData chain) {
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
		camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);
	}
	
	public void eventFired(AgentEvent e) {
		if (e instanceof DatasetEvent) {
			DatasetEvent event = (DatasetEvent) e;
			DatasetData dataset = event.getDataset();
			selectDataset(dataset);
		}
		else if (e instanceof MouseOverChainExecutionEvent) {
			MouseOverChainExecutionEvent event = (MouseOverChainExecutionEvent) e;
			ChainExecutionData exec = event.getChainExecution();
			selectChainExecution(exec);
		}
		else if (e instanceof SelectChainExecutionEvent) {
			SelectChainExecutionEvent event = (SelectChainExecutionEvent) e;
			ChainExecutionData exec = event.getChainExecution();
			selectChainExecution(exec);
			if (exec != null) {
				ChainBox cb =  findChainBox(exec.getChain());
				if (cb != null)
					zoomToChain(cb);
				else 
					scaleToSize(); // show all
			}
			else {
				scaleToSize();
			}
		}
	}
	
	
	private void selectDataset(DatasetData d) {
		
		// for each of them, if they have an execution, set Highlighted, or not.
		Iterator iter = layer.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ChainBox) {
				ChainBox cb = (ChainBox) obj;
				LayoutChainData chain = cb.getChain();
				if (d == null) 
					cb.setHighlighted(false);
				else {
					boolean hasExecs =
						dataManager.chainHasExecutionsForDataset(chain.getID(),
								d.getID());
					if (hasExecs == true)
						cb.setHighlighted(true);
					else
						cb.setHighlighted(false);
				}
			}
		}					
	}
	

	
	private void selectChainExecution(ChainExecutionData exec) {
		AnalysisChainData selChain = null;
		if (exec != null) 
			selChain = exec.getChain();
	
		//for each of them, if they have an execution, set Highlighted, or not.
		Iterator iter = layer.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ChainBox) {
				ChainBox cb = (ChainBox) obj;
				LayoutChainData chain = cb.getChain();
				
				if (selChain != null && selChain.getID() == chain.getID())
					cb.setHighlighted(true);
				else
					cb.setHighlighted(false);
			}
		}
		
	}
		
    private class ChainBoxFilter implements PNodeFilter {
		
		private final AnalysisChainData chain;
		
		ChainBoxFilter(AnalysisChainData chain) {
			this.chain = chain;	
		}
		public boolean accept(PNode node) {
			if (!(node instanceof ChainBox))
				return false;
			return ((ChainBox) node).getChain().getID() == chain.getID();
		}
		
		public boolean acceptChildrenOf(PNode node) {
			return true;
		}
	}
    
    private class RowInfo {
    		
    		private ArrayList boxes = new ArrayList();
    		private float height;
    		private float width = 0;
    		
    		private RowInfo() {
    			
    		}
    		
    		private void addBox(ChainBox box) {
    			boxes.add(box);
    			width += box.getWidth();
    		}
    		
    		
    		private void setHeight(float height) {
    			this.height = height;
    		}
    		
    		private void adjustSize(float maxWidth) {
    			
    			// find difference between maxWidth and rowWidth;
    			float horizSpace = maxWidth -width;
    	
    			float padding = 0;
    			if (horizSpace > 0) {
    				padding = horizSpace/boxes.size();
    			}
    			float boxWidth;
    			Iterator iter = boxes.iterator();
    			ChainBox box;
    			float x = 0;
    			while (iter.hasNext()) {
    				box =(ChainBox) iter.next();
    				box.setHeight(height);
    				boxWidth = (float)box.getWidth();
    				if (padding > 0) {
    					boxWidth += padding;
    					box.setWidth(boxWidth);
    				}
    				Point2D pt  = box.getOffset();
    				box.setOffset(x,pt.getY());
    				box.centerChain();
    				x += boxWidth;
    			}
    			width = maxWidth;
    		}
    }
	
    private  class ChainPalettePanButton extends ButtonNode {
    	
	    	private ChainPaletteCanvas canvas;
	
	    	public ChainPalettePanButton(ChainPaletteCanvas canvas,Image enabled,
	    			Image disabled) {
	    		super(enabled,disabled);
	    		this.canvas = canvas;
	    	}
	    	
	    	public void doClick() {
	    		canvas.setToPan();
	    	}
	    	
	    	public void doPopup(){
	    		
	    	}
	    	
	    	public void setEnabled(boolean v) {
	    		super.setEnabled(v);
	    	}
    }
    
    private  class ChainPaletteZoomButton extends ButtonNode {
    	
	    	private ChainPaletteCanvas canvas;
	
	    	public ChainPaletteZoomButton(ChainPaletteCanvas canvas,Image enabled,
	    			Image disabled) {
	    		super(enabled,disabled);
	    		this.canvas = canvas;
	    	}
	    	
	    	public void doClick() {
	    		canvas.setToZoom();
	    	}
	    	
	    	public void doPopup(){
	    		
	    	}
	    	
	    	public void setEnabled(boolean v) {
	    		super.setEnabled(v);
	    	}
    }
}