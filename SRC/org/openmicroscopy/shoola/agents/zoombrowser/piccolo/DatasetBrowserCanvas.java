/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.DatasetBrowserCanvas
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
import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Third-party libraries
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.events.SelectionEvent;
import 
	org.openmicroscopy.shoola.agents.zoombrowser.events.SelectionEventListener;
import org.openmicroscopy.shoola.agents.zoombrowser.SelectionState;

/** 
 * A {@link PCanvas} for viewing images in datasets, with datasets laid out
 * in a treemap 
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class DatasetBrowserCanvas extends PCanvas implements BufferedObject, 
	SelectionEventListener, ContentComponent {
	
	private static final int BROWSER_SIDE=400;
	
	/** The agent of which we are a part */
	//private ZoomBrowserAgent agent;
	/**
	 * The initial magnification of the  canvas
	 */
	private static float INIT_SCALE=1.0f;
	
	/**
	 * The layer for the canvas. 
	 */
	private PLayer layer;
	
	/**
	 * horizontal separation between datasets
	 */
	
	private static float HGAP=5;

	/**
	 * Coordinates used in layout
	 */
	private double x,y;

	
	
	/**
	 * Collection of all current datasets
	 */	
	private Collection allDatasets;

	/**
	 * A {@link PExecutionList} that may show the lists of executions for a
	 * dataset.
	 * 
	 */	
//	private PExecutionList executionList;

	/**
	 * Some internal state variables for treemap calculation
	 */
	private double totalArea = 0;
	private double scaleFactor;
	private double screenArea;
	private Vector strips;
	private double screenHeight = 0;
	private double screenWidth =0;
	
	/**
	 * The last dataset that we moused over
	 */

	private DatasetNode lastRolledOver = null;
	
	/**
	 * The event handler for this canvas
	 */
	private DatasetBrowserEventHandler eventHandler;
	
	public DatasetBrowserCanvas() {
		super();
		layer = getLayer();
		
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setBackground(PConstants.CANVAS_BACKGROUND_COLOR);
		setPreferredSize(new Dimension(PConstants.BROWSER_SIDE,
			PConstants.BROWSER_SIDE));
		
		//	remove handlers
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		
		
		
	}
	
	
	public void setContents(Collection allDatasets) {
		this.allDatasets = allDatasets;
	}
	
	
	/**
	 * Return the area for the dataset's node, creating the node and adding it 
	 * to the layer if needed
	 * @param d the dataset in question. This "area" is really a proxy for the
	 * size of the dataset.
	 * @return "area" on screen that the dataset will occur. 
	 */
	private  double getArea(BrowserDatasetSummary d) {
		if (d == null) 
			return 0;
		DatasetNode node;
		node = d.getNode();
		if (node == null)
			node = new DatasetNode(d);
		if (node == null)
			return 0;
		node.clearWidths();
		return node.getContentsArea();
	}
		
	
	/**
	 * Display a subset of the datasets. 
	 * @param datasets The subset of the dataset that should be displayed
	 */	
	private void displayDatasets(Collection datasets) {
			
		doLayout(datasets);
		eventHandler.animateToBounds(getBufferedBounds());
	
	}
	
	/**
	 * Calculate the treemap layout for some datasets
	 * @param datasets the collection of datasets to be laid out.
	 * 
	 */
	private void arrangeDisplay(Collection datasets) {
		// initialize and find clear out the layer
		layer.removeAllChildren();
		if (datasets == null) {
			return;
		}
		// calculate the total area, adding nodes to the layer
		// as we go.		
		totalArea = 0;
		Iterator iter = datasets.iterator();
		
		while (iter.hasNext()) {
			BrowserDatasetSummary d = (BrowserDatasetSummary) iter.next();
			double area = getArea(d);
			totalArea += area;
		}
		
		
		screenHeight = getHeight();
		screenWidth = getWidth();
		screenArea = screenHeight*screenWidth;

		// scale the width and height by the total area		
		scaleFactor = Math.sqrt(screenArea/totalArea);
		screenHeight /= scaleFactor;
		screenWidth /= scaleFactor;
		// build the treemap.
		strips = doTreeMap(datasets);
	}

	/**
	 * Given a layout that has been calculated, position the datasets 
	 * (or some subset) according to that layout. If not all datasets are 
	 * being displayed, just skip over them. This results in a layout that 
	 * guarantees the relative ordering of datasets (ie, the datasets are not
	 * reordered when some are not displayed) but possibly loses some of the 
	 * aesthetics of the treemap.
	 * 
	 * @param datasets the collection of datasets to be laid out.
	 * @param layoutDatasets
	 */
	private void doLayout(Collection datasets) {
		if (datasets == null)
			return;
		layer.setScale(1.0);
		x = HGAP;
		y = 0;

		// strips is a vector of vectors. Each element in strips is a vector
		// that contains items from a given strip, as calculated by 
		//arrangeDisplay()
		

		Iterator iter = strips.iterator();
		double maxHeight = 0;
		while (iter.hasNext()) {
			// For each strip...
			
			Vector strip = (Vector)iter.next();
			Iterator iter2 = strip.iterator();
			maxHeight = 0;
			// iterate over items in the strip
			while (iter2.hasNext()) {
				
				// for each dataset
				DatasetNode node = (DatasetNode) iter2.next();
				
				if (containsDataset(datasets,node.getDataset())) {
					// position it if it's being displayed
					if (node.getParent() != layer)
						layer.addChild(node);
					node.setOffset(x,y);
					
					//move to next horizontal position. Adjust height of row.
					x+= node.getGlobalFullBounds().getWidth();
					double height = node.getGlobalFullBounds().getHeight();
					if (height > maxHeight)
						maxHeight = height;
				}
				else {
					// not showing this node. remove it.
					// this way, it's not included in bounds.
					if (node.getParent() == layer)
						layer.removeChild(node);
				}	 
			}
			x =HGAP;
			y +=maxHeight;
		}
	}
	
	private boolean containsDataset(Collection datasets,
		BrowserDatasetSummary dataset) {
	
		int id= dataset.getID();
		Iterator iter = datasets.iterator();
		BrowserDatasetSummary d;
		
		while (iter.hasNext()) {
			d = (BrowserDatasetSummary) iter.next();
			if (d.getID()==id)
				return true;			
		}
		return false;
	}
	// some private vars used to compute the treemap.
	private double oldAspectRatio =0;
	private double newAspectRatio = 0;
	private double stripHeight = 0;
	private double oldHeight = 0;

	/**
	 * build the treemap of the datasets
	 * @param datasets the datasets to be included
	 * @return a vector of vectors, each containing datasets to be put
	 * in a given row
	 */
	private Vector doTreeMap(Collection datasets) {
	
		oldAspectRatio = 0;
		newAspectRatio = 0;
		Vector strips = new Vector();
		Vector strip = new Vector();
		
		Iterator iter = datasets.iterator();
		BrowserDatasetSummary d = null;
		
		DatasetNode node=null;
		
		while (iter.hasNext())  {
			d = (BrowserDatasetSummary) iter.next();
			node = d.getNode();
			
			// place node in the current strip
			strip.add(node);
		
			// calc,update stats.
			getTreemapStripHeight(strip);
			
			// if it doesn't fit
			if (strip.size()>1 &&  newAspectRatio > oldAspectRatio) {
				// back it out of the row
				strip.remove(node);
				Iterator iter2 = strip.iterator();
				
				// revert width,height of items in that row
				while (iter2.hasNext()) {
					DatasetNode ds = (DatasetNode) iter2.next();
					ds.revertWidth();
					ds.setHeight(oldHeight);
				}
				//add strip to strips vector
				strips.add(strip);
				// move on to next.
				strip = new Vector();
				newAspectRatio = oldAspectRatio = 0;
				strip.add(node);
				oldHeight = 0;
				getTreemapStripHeight(strip);
			}
			// otherwise, keep what I've calculated.
			oldAspectRatio = newAspectRatio;
		}
		// set height of nodes in last strip
		iter = strip.iterator();
		while (iter.hasNext()) {
			DatasetNode ds  =(DatasetNode) iter.next();
			ds.setHeight(stripHeight);
		}
		strips.add(strip);
		// rescale
		
		
		// Now that the widths and heights are all set,
		// layout the datasets and set the handlers.
		iter = strips.iterator();
		while (iter.hasNext()) {
			Vector v = (Vector) iter.next();
			Iterator iter2= v.iterator();
			while (iter2.hasNext()) {
				DatasetNode p = (DatasetNode) iter2.next();
				p.scaleArea(scaleFactor);
				p.layoutImages();
				p.setHandler(eventHandler);
			}
		}
		return strips;
	}
	
	/**
	 * Calculate the height of a treemap strip
	 * @param strip
	 */
	private void  getTreemapStripHeight(Vector strip) {
		
		double stripArea =0;
		Iterator iter = strip.iterator();
		// get total area of strip
		while (iter.hasNext()) {
			DatasetNode node = (DatasetNode) iter.next();
			double area = node.getContentsArea();
			stripArea += area;
		}
		
		// save previous height
		oldHeight = stripHeight;
		
		stripHeight = stripArea/screenWidth;
		// get width of each and update ratios;
		double width;
		int i =0;
		newAspectRatio = 0;
		iter = strip.iterator();
		while (iter.hasNext()) {
			DatasetNode node = (DatasetNode) iter.next();
			width = node.getContentsArea()/stripHeight;
			node.setWidth(width);
			if (width > stripHeight) 
				newAspectRatio += width/stripHeight;
			else 
				newAspectRatio += stripHeight/width;
			i++;
		}
		newAspectRatio = newAspectRatio/i;
	}
	
	/**
	 * Calculate the bounds necessary for appropriate zooming for this canvas
	 */	
	public PBounds getBufferedBounds() {
		PBounds b = layer.getFullBounds();
		return new PBounds(b.getX()-PConstants.SMALL_BORDER,
			b.getY()-PConstants.SMALL_BORDER,
			b.getWidth()+2*PConstants.SMALL_BORDER,
			b.getHeight()+2*PConstants.SMALL_BORDER); 
	}
	
	
	/**
	 * Displaying all of the datasets.
	 *
	 */	
	public void layoutContents() {
		
		eventHandler = new DatasetBrowserEventHandler(this);
		// layout treemaps
		arrangeDisplay(allDatasets);
		doLayout(allDatasets);
	}
	
	public void completeInitialization() {
		
		// set up listeners
		addInputEventListener(eventHandler);  
		final PCamera camera = getCamera();
		camera.addInputEventListener(new DatasetBrowserToolTipHandler(camera));
		
		// center view
		eventHandler.animateToBounds(getBufferedBounds());
		//listen for selection events
		SelectionState.getState().addSelectionEventListener(this);
	}
	
	/**
	 * Handler for selection events
	 */
	public void selectionChanged(SelectionEvent e) {
		Collection sets = null;
		SelectionState state = e.getSelectionState();
		
		if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_PROJECT)) {
			//when we roll over a project, highlight the project's datasets
			BrowserProjectSummary rollover = state.getRolloverProject();
			if (rollover != null)
				highlightDatasets(rollover);
		}
		else if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_DATASET)) {
			// highlight a dataset when we roll over it.
			BrowserDatasetSummary rolled = state.getRolloverDataset();
			highlightDataset(rolled);	 
		}
		else if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_CHAIN)) {
			//when we roll over a chain, we highlight the datasets with 
			// executions.
		/*	CChain chain = state.getRolloverChain();
			if (chain != null)
				sets = chain.getDatasetsWithExecutions();
			highlightDatasets(sets); */	 
		}
		
		else if (e.isEventOfType(SelectionEvent.SET_SELECTED_CHAIN)) {
			// show only those that are for current project
			// and have executions for this chain
			
			List executed = state.getExecutedDatasets();
			Collection datasets;
			if (sets != null) {
				Collections.sort(executed);
				datasets = executed;
			}
			else
				datasets = allDatasets;
			displayDatasets(datasets);
		}
		else if (e.isEventOfType(SelectionEvent.SET_SELECTED_PROJECT)
			|| e.isEventOfType(SelectionEvent.SET_SELECTED_DATASET)) {
			// if we select a project or dataset, update their state.
			updateProjectDatasetSelection(state);	
		}
	  
	}
	
	/**
	 * Highlight selected datasets and display as needed.
	 * @param state
	 */
	private void updateProjectDatasetSelection(SelectionState state) {
		BrowserDatasetSummary selected = state.getSelectedDataset();
		
		// if there is no selected project, don't higlight any datasets
		if (state.getSelectedProject() == null)
			clearHighlightDatasets(); 
			
		if (selected == null){
			// display the active datasets, or all of them (if none active).
			Collection selections = state.getActiveDatasets();
			if (selections != null && selections.size() > 0) {
				displayDatasets(selections);
			}
			else 
				displayDatasets(allDatasets);
		}
	}

	/**
	 * This Browser is interested in most SelectionState events
	 */
	public int getEventMask() {
		return SelectionEvent.SET_SELECTED_DATASET | 
			SelectionEvent.SET_SELECTED_PROJECT | 
			SelectionEvent.SET_ROLLOVER_PROJECT |
			SelectionEvent.SET_ROLLOVER_DATASET | 
			SelectionEvent.SET_ROLLOVER_CHAIN |
			SelectionEvent.SET_SELECTED_CHAIN;
	}

	/**
	 * Clear the list of executions for the active chain
	 *
	 */	
	/*public void clearExecutionList() {
		if (executionList != null) {
			if (executionList.getParent() == layer)
				layer.removeChild(executionList);
			executionList = null;
		}
	}*/

	/**
	 * Show a list of executions for the given {@link PChainLabelText}. The 
	 * {@link PChainLabelText} is a text node indicating that a chain has
	 * been executed against this dataset. The executionList will show the 
	 * executions for this dataset,chain pair.
	 * 
	 * @param cl The chain label 
	 * 
	 */	
	/*public void showExecutionList(PChainLabelText cl) {
		clearExecutionList();

		executionList = cl.getExecutionList();
		
		
		layer.addChild(executionList);
		
		PBounds b = cl.getGlobalFullBounds();
		executionList.setOffset(b.getX(),b.getY()+b.getHeight());
	} */
	

	/**
	 * Highlight all of the datasets in a collection. 
	 * @param p The project with datasets to be highlighted.
	 */
	private void highlightDatasets(BrowserProjectSummary p) {
		
		// we must look at all datasets, as we want to turn off the 
		// highlighting in those that are not in the collection c.
		Iterator iter = layer.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof DatasetNode) {
				DatasetNode dNode = (DatasetNode) obj;
				BrowserDatasetSummary d = dNode.getDataset();
				if (p.hasDataset(d))
					dNode.setHighlighted(true);
				else
					dNode.setHighlighted(false);
			}
		}
	}

	private void clearHighlightDatasets() {
		Iterator iter = layer.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof DatasetNode) {
				DatasetNode dNode = (DatasetNode) obj;
				dNode.setHighlighted(false);	
			}
		}
	}
	
	/**
	 * To highlight a dataset, clear the previous highlighted dataset
	 *  and set the new one
	 * 
	 * @param rolled the dataset to be highlighted.
	 */	
	private void highlightDataset(BrowserDatasetSummary rolled) {
		
		if (rolled == null && lastRolledOver == null)
			return;
		
		if (lastRolledOver != null) { 
			if (rolled == lastRolledOver.getDataset())
				return;
			lastRolledOver.setSelected(false);
			lastRolledOver = null;
		}
		
		if (rolled != null) {	
			lastRolledOver = rolled.getNode();
			lastRolledOver.setSelected(true);
		}		
	}
 }