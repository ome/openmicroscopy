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
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

//Third-party libraries
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.browser.layout.QuantumTreemap;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutions;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.ui.MainWindow;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.ContentComponent;

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
	ContentComponent {
	
	
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
	
	/** the main window in which this panel is being stored */
	private MainWindow mainWindow;


	
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
	
	/** the selected project and dataset */
	private BrowserProjectSummary selectedProject = null;
	private BrowserDatasetData selectedDataset = null;
	
	/**
	 * The event handler for this canvas
	 */
	private DatasetBrowserEventHandler eventHandler;
	
	private int width,height;
	
	private Registry registry;
	
	public DatasetBrowserCanvas(MainWindow mainWindow,Registry registry) {
		super();
		this.mainWindow = mainWindow;
		this.registry = registry;
		layer = getLayer();
		
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);

		//	remove handlers
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				/*if (selectedDataset != null)
					eventHandler.animateToNode(selectedDataset.getNode());
				else
					scaleToSize();*/
				if (eventHandler != null)
					eventHandler.animateToLastBounds();
			}
		});
		
	}
	
	
	public Dimension getPreferredSize() {
		height = getHeight();
		if (height == 0)
			height = Constants.BROWSER_SIDE;
		return new Dimension(getWidth(),height);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	} 
	
	
	public void setContents(Object allDatasets) {
		this.allDatasets = (Collection) allDatasets;
		// build nodes
		Iterator iter = this.allDatasets.iterator();
		BrowserDatasetData d; 
		while (iter.hasNext()) {
			d = (BrowserDatasetData) iter.next();
			d.setNode(new DatasetNode(d,this));
			
		}
	}
			
	
	/**
	 * Display a subset of the datasets. 
	 * @param datasets The subset of the dataset that should be displayed
	 */	
	private void displayDatasets(Collection datasets) {
		// reconcile this list of datasets against datasets in allDatasets
		Vector ds = new Vector();
		Iterator iter = datasets.iterator();
		while (iter.hasNext()) {
			BrowserDatasetData data = (BrowserDatasetData) iter.next();
			BrowserDatasetData old = findDatasetData(data.getID());
			if (old != null)
				ds.add(old);
		}
		doLayout(ds);
		eventHandler.animateToBounds(getBufferedBounds());
	
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
		// calculate aspect ratio
		double iar = getWidth()/getHeight();

		layer.removeAllChildren();
		// get stuff for layout
		int[] sizes = getSizes(datasets);
		Rectangle bounds = new Rectangle(0,0,getWidth(),getHeight());
		QuantumTreemap qt = new QuantumTreemap(sizes,iar,bounds);
		Rectangle[] rects = qt.quantumLayout();
		double xExtent = 0;
		double yExtent = 0;
		
		for (int i = 0; i < sizes.length; i++) {
			double rightExtent = rects[i].getX()+rects[i].getWidth();
			if (rightExtent > xExtent) 
				xExtent  = rightExtent;
			double bottom = rects[i].getY()+rects[i].getHeight();
			if (bottom > yExtent)
				yExtent = bottom;
		}
		
		// go through datasets and layout them out as such.
		Iterator iter = datasets.iterator();
		int i = 0;
		BrowserDatasetData data;
		DatasetNode node;
		Rectangle rect;
		while (iter.hasNext()) {
			data = (BrowserDatasetData) iter.next();
			node = data.getNode();
			layer.addChild(node);
			rect = rects[i++];
			double xOffset = (rect.getX()/xExtent)*bounds.getWidth();
			double yOffset = (rect.getY()/yExtent)*bounds.getHeight();
			node.setOffset(xOffset,yOffset);
			double xPortion = (rect.getWidth()/xExtent)*bounds.getWidth();
			double yPortion = (rect.getHeight()/yExtent)*bounds.getHeight();
			node.layoutImages(xPortion,yPortion);
			node.setHandler(eventHandler);
		}
	
	
	}
	
	private int[] getSizes(Collection datasets) {
		int[] szs = new int[datasets.size()];
		Iterator iter =datasets.iterator();
		BrowserDatasetData d;
		DatasetNode node;
		int i = 0;
		while (iter.hasNext()) {
			d = (BrowserDatasetData) iter.next();
			node = d.getNode();
			
			szs[i] = (int) node.getContentsArea();
			i++;
		}
		int[] res = new int[i];
		//		convert to array
		System.arraycopy(szs,0,res,0,i);
		return res;
	}
	
	
	
	
	/**
	 * Calculate the bounds necessary for appropriate zooming for this canvas
	 */	
	public PBounds getBufferedBounds() {
		PBounds b = layer.getFullBounds();
		return new PBounds(b.getX()-Constants.SMALL_BORDER,
			b.getY()-Constants.SMALL_BORDER,
			b.getWidth()+2*Constants.SMALL_BORDER,
			b.getHeight()+2*Constants.SMALL_BORDER); 
	}
	
	
	/**
	 * Displaying all of the datasets.
	 *
	 */	
	public void layoutContents() {
		
		eventHandler = new DatasetBrowserEventHandler(this,registry);
		// layout treemaps
	//	arrangeDisplay(allDatasets);
		doLayout(allDatasets);
	}
	
	public void completeInitialization() {
		
		// set up listeners
		addInputEventListener(eventHandler);  
		final PCamera camera = getCamera();
		camera.addInputEventListener(new DatasetBrowserToolTipHandler(camera));
	}
	
	public void scaleToSize() {
		eventHandler.animateToCanvasBounds();
	}
	
	/**
	 * Highlight all of the datasets in a collection. 
	 * @param p The project with datasets to be highlighted.
	 */
	public  void setRolloverProject(BrowserProjectSummary p) {
		
		Iterator iter = layer.getChildrenIterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof DatasetNode) {
				DatasetNode dNode = (DatasetNode) obj;
				BrowserDatasetData d = dNode.getDataset();
				if (p != null && p.hasDataset(d))
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
	public void setRolloverDataset(BrowserDatasetData rolled) {
		
		if (rolled == null && lastRolledOver == null)
			return;
		
		if (lastRolledOver != null) { 
			if (rolled == lastRolledOver.getDataset())
				return;
			lastRolledOver.setHighlighted(false);
			lastRolledOver = null;
		}
		
		if (rolled != null) {	
			lastRolledOver = rolled.getNode();
			lastRolledOver.setHighlighted(true);
		}
		mainWindow.setRolloverDataset(rolled);
	}
	
	public void setSelectedProject(BrowserProjectSummary proj) {
		if (proj == selectedProject) // no change
			return;
		selectedProject = proj;
		Collection datasetsToDisplay;
		// ok. what to do now?
		
		// cases:
	
		if (proj == null) {
			if (selectedDataset == null)
				//1) project is null and selected dataset is null we display all
				datasetsToDisplay = allDatasets;
			else {
				//2) project is null, selected dataset isn't. display it. 
				datasetsToDisplay = new Vector();
				datasetsToDisplay.add(selectedDataset);
			}
		}
		else { 
			if (selectedDataset == null) {
				// 3)project is not null. and selected is null display 
					//all for project
				datasetsToDisplay = proj.getDatasets();
			}
			else { // selected is not null
				if (!proj.hasDataset(selectedDataset)) {
					// 4) project is not null, selected is not null, and selected 
					// is not in project. display all for project and clear selected
					datasetsToDisplay = proj.getDatasets();
					selectedDataset = null;
				}
				else {
					// 5) project is not null and selected is not null and selected  
					// is in project display selected
					datasetsToDisplay = new Vector();
					datasetsToDisplay.add(selectedDataset);
				}
				
			}
		}
		displayDatasets(datasetsToDisplay);
	}
	
	
	public void setSelectedDataset(BrowserDatasetData dataset) {
		 
		// if they're the same, return
		// except for if it's null. then we might need to redraw
		if (dataset == selectedDataset && dataset != null)
			return;
		if (selectedDataset != null)
			selectedDataset.getNode().setHighlighted(false);
		selectedDataset  = dataset;
		
		// if a dataset is clicked on to be selected, it has already
		// told the canvas to draw to it.
		if (selectedDataset == null) {
			// selected is null display all
			displayDatasets(allDatasets);
		}
		mainWindow.setSelectedDataset(dataset);
	}
	
	public void respondToDatasetLoad(BrowserDatasetData dataset) {
		setRolloverDataset(dataset);
		eventHandler.resetZoomLevel();
		
		if (dataset != null && dataset.getNode() != null)
			eventHandler.animateToNode(dataset.getNode());
		setSelectedDataset(dataset);
	}
	
	public void selectAnalysisChain(AnalysisChainData chain) {
		ChainExecutions chainExecutions = mainWindow.getChainExecutions();
		if (chainExecutions == null)
			return;
		
		Iterator iter = layer.getChildrenIterator();
		DatasetNode n;
		while (iter.hasNext()) {
			n = (DatasetNode) iter.next();
			int id = n.getDataset().getID();
			if (chain == null)
				n.setHighlighted(false);
			else {
				int chainID = chain.getID();
				if (chainExecutions.chainHasExecutionsForDataset(chainID,id))
					n.setHighlighted(true);
				else
					n.setHighlighted(false);
			}
		}
		
	}
	
	public void mouseOverAnalysisChain(AnalysisChainData chain) {
		selectAnalysisChain(chain);
		
	}
	
	public void mouseOverChainExecution(ChainExecutionData exec) {
		Iterator iter = layer.getChildrenIterator();
		DatasetNode n;
		DatasetData dataset =null;
		if (exec != null)
			dataset = exec.getDataset();
		
		
		while (iter.hasNext()) {
			n = (DatasetNode) iter.next();
			int id = n.getDataset().getID();
			if (dataset != null && dataset.getID() ==id)
				n.setHighlighted(true);
			else
				n.setHighlighted(false);	
		}
	}
	
	public void selectChainExecution(ChainExecutionData exec) {
		if (exec == null) {
			setSelectedDataset(null);
			return;
		}
		DatasetData ds = exec.getDataset();
		int dsId = ds.getID();
		DatasetNode n = null;
		BrowserDatasetData browserDataset = findDatasetData(dsId);
		if (browserDataset != null)
			n = browserDataset.getNode();
		
		// make what I've found selected
		if (n != null) {
			// if it isn't displayed, display all
			if (n.getParent() != layer)
				doLayout(allDatasets);
			eventHandler.animateToNode(n);
		}
		setSelectedDataset(browserDataset);
	}
	
	private BrowserDatasetData findDatasetData(int dsId) {
		
		// go through some silliness to find the BrowserDatasetData
		// that corresponds to ds. must look at _all_ datasets - not
		// just those currently being displayed
		Iterator iter = allDatasets.iterator();
		BrowserDatasetData browserDataset = null;
		while (iter.hasNext()) {
			browserDataset = (BrowserDatasetData) iter.next();
			if (browserDataset.getID() == dsId) {
				return browserDataset;
			}
		}
		return null;
	}
 } 