/*
  * org.openmicroscopy.shoola.agents.chainbuilder.piccolo
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

//Java imports
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;

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
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainStructureErrors;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutLinkData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ChainFrame;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ChainFlavor;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ModuleFlavor;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.util.ui.Constants;



/** 
 * A {@link PCanvas} for building chains
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

public class ChainCreationCanvas extends PCanvas implements DropTargetListener {
	
	/** the side*/
	private static final int SIDE=200;
	
	/**
	 * The initial magnification of the  canvas
	 */
	private static float INIT_SCALE=0.6f;
	
	/** Data manager */
	private ChainDataManager manager;
	/**
	 * The layer for the canvas 
	 */
	private PLayer layer;
	
	
	/**
	 * The layer for the links. Links are stored in a different layer because 
	 * they must be drawn last if they are to avoid being obscured by modules. 
	 */
	private LinkLayer linkLayer;
	
	/**8
	 * The event handler for this canvas
	 */
	private ChainCreationEventHandler handler;
	
	/**
	 * DataTransfer bookkeeping
	 */
	private DropTarget dropTarget = null;
	
	/**
	 * The frame contaiing this canvas
	 */
	private ChainFrame frame;
	
	
	public ChainCreationCanvas(ChainFrame frame,ChainDataManager manager) {
		super();
		this.frame=  frame;
		this.manager = manager;
		layer = getLayer();
		
		// set rendering details.
		
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		
		setMinimumSize(new Dimension(SIDE,SIDE));
		setPreferredSize(new Dimension(SIDE,SIDE));
		// remove handlers
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		// set up link layer
		linkLayer = new LinkLayer();
		getCamera().addLayer(linkLayer);
		linkLayer.moveToFront();
		
		// event handler
		handler = new ChainCreationEventHandler(this,linkLayer);
		addInputEventListener(handler);
		
		// data transfer support
		dropTarget = new DropTarget(this,this);
		
		// set magnification
		final PCamera camera = getCamera();
   	    getCamera().setViewScale(INIT_SCALE);
	    
	    // setup tool tips.
		camera.addInputEventListener(new ChainToolTipHandler(camera));
		
		
	}
	
	
	public PBounds getBufferedBounds() {
		PBounds b = layer.getFullBounds();
		return new PBounds(b.getX(),b.getY(),b.getWidth()+4*Constants.BORDER,
		b.getHeight()+4*Constants.BORDER); 
	}
	

	/**
	 * Start a DataTransfer event disable the {@link ChainCreationEventHandler} while
	 * doing the transfer
	 */
	public void dragEnter(DropTargetDragEvent e) {
		removeInputEventListener(handler);
		e.acceptDrag (DnDConstants.ACTION_MOVE);
	
	}
	
	/**
	 * Accept a drop event. Create a chain if a chain was dropped,
	 * or a module if a module was dropped. Reinstate the event handler
	 */
	public void drop(DropTargetDropEvent e) {
		try {
			Transferable transferable =  e.getTransferable();
			if (transferable.isDataFlavorSupported(ModuleFlavor.moduleFlavor)) { 
				e.acceptDrop(DnDConstants.ACTION_MOVE);
				String i = (String)transferable.getTransferData(
						ModuleFlavor.moduleFlavor); 
				e.getDropTargetContext().dropComplete(true);
				int id = Integer.parseInt(i);
				ChainModuleData mod = (ChainModuleData)manager.getModule(id);
				Point2D loc = e.getLocation();
				createDroppedModule(mod,loc);
				addInputEventListener(handler);
			}
			else if (transferable.
					isDataFlavorSupported(ChainFlavor.chainFlavor)) {
				e.acceptDrop(DnDConstants.ACTION_MOVE);
				Integer i = (Integer)transferable.
					getTransferData(ChainFlavor.chainFlavor);
				e.getDropTargetContext().dropComplete(true);
				int id = i.intValue(); 
				Point2D loc = e.getLocation();
				LayoutChainData chain = manager.getChain(id);
				createDroppedChain(chain,loc);
				addInputEventListener(handler);			
			} 
		}
		catch(Exception exc ) {
			exc.printStackTrace();
			clearDrop(e);
		}
	}

	
	public void clearDrop(DropTargetDropEvent e) {
		e.rejectDrop();
		addInputEventListener(handler);
	}


	public void dragExit(DropTargetEvent e) {
	}
	
	public void dragOver(DropTargetDragEvent e) {
	}
	
	public void dropActionChanged(DropTargetDragEvent e) {
	}
	
	/**
	 * Create a dropped module
	 * @param mod the module to create
	 * @param location the poitn of drop.
	 */
	
	private void createDroppedModule(ChainModuleData mod,Point2D location) {
		
		// determine the corect point
		getCamera().localToView(location);
		
		//create the layer
		ModuleView mNode = new SingleModuleView(mod,
			(float) location.getX(), (float) location.getY());
		mod.addModuleNode(mNode);
		
		// add it to layer.
		layer.addChild(mNode);
		
		// put the module info back into the connection
		setSaveEnabled(true);
	}
	
	/**
	 * Create a dropped chain
	 * @param chain
	 * @param location
	 */
	public void createDroppedChain(LayoutChainData chain,Point2D location) {
		
		
		ChainStructureErrors  errors = chain.getStructureErrors();
		if (errors != null) 
			errors.display();
		
		getCamera().localToGlobal(location);
		float x = (float) location.getX();
		float y = (float) location.getY();
		ChainView p = new ChainView(chain);
		layer.addChild(p);
		p.setOffset(x,y);
		PBounds b = layer.getFullBounds();
		PBounds newb = new PBounds(b.getX()-Constants.BORDER,	
			b.getY()-Constants.BORDER,
			b.getWidth()+2*Constants.BORDER,
			b.getHeight()+2*Constants.BORDER);
		getCamera().animateViewToCenterBounds(newb,true,
			Constants.ANIMATION_DELAY);
		setSaveEnabled(true);
	}
	
	
	private void setSaveEnabled(boolean v) {
		if (frame != null)
			frame.setSaveEnabled(v);
	}
	
	/**
	 * The save status is ture (enabled) if there are any {@link ModuleView}
	 * objects on the {@link ChainCreationCanvas}
	 *
	 */
	public void updateSaveStatus() {
		boolean res  = false;
		
		Collection modules = findModules();
		if (modules.size() > 0)
			res = true;
		setSaveEnabled(res);
	}	
	
	/**
	 * To save a chain, ask the manager to create it,
	 * add the nodes and links, commit the transaction, and 
	 * updaet the library. 
	 * @param name
	 * @param desc
	 */
	public void save(String name,String desc) {	
		
		
		LayoutChainData newChain = new LayoutChainData();
		
		newChain.setName(name);
		newChain.setDescription(desc);
		
		buildNodes(newChain);
		buildLinks(newChain);
		manager.saveChain(newChain);
			
		
		String msg = new String("The new Chain \""+name+"\" was saved correctly.");
		JOptionPane.showMessageDialog(this,msg,"Save Complete",
				JOptionPane.INFORMATION_MESSAGE);
		newChain.layout();

		frame.updateChainPalette(newChain);
		manager.addChain(newChain);

	}
	
	public Collection findModules() {
		PNodeFilter filter = new PNodeFilter() {
			public boolean accept(PNode node) {
				return (node instanceof ModuleView);
			}
			public boolean acceptChildrenOf(PNode node) {
				return  true;
			}
		};
		return  layer.getAllNodes(filter,null);
		
	}
	
	private void buildNodes(LayoutChainData chain) {
		// ok. let's go over the nodes.
		Collection mods = findModules();
		Iterator iter = mods.iterator();
		Vector nodes = new Vector();
		while (iter.hasNext()) {
			SingleModuleView m = (SingleModuleView) iter.next();
			LayoutNodeData n = new LayoutNodeData();
			n.setModule(m.getModule());
			m.setNode(n);
			n.setChain(chain);
			nodes.add(n);
		}
		chain.setNodes(nodes);
	}
	
	public  Collection findLinks() {
		PNodeFilter filter = new PNodeFilter() {
			public boolean accept(PNode node) {
				return (node instanceof ParamLink);
			}
			public boolean acceptChildrenOf(PNode node) {
				return  true;
			}
		};
		
		Collection linkNodes = layer.getAllNodes(filter,null);
		// plus add in whatever is in linkLayer;
		linkNodes.addAll(linkLayer.links());
		return linkNodes;
	}
	
	private void buildLinks(LayoutChainData chain) {
		Vector links = new Vector();
		PNode node;
		ParamLink link;
		
		Collection linkNodes = findLinks();
		Iterator iter = linkNodes.iterator();
		while (iter.hasNext()) {
			node = (PNode) iter.next();
			if (node instanceof ParamLink) {
				link = (ParamLink) node; // add it somehow.
				System.err.println("found a link to save");
				// get from output
				FormalOutput output = link.getOutput();
				FormalOutputData fromOutput = (FormalOutputData) 
					output.getParameter();
				
				// to input
				FormalInput input = link.getInput();
				FormalInputData toInput = (FormalInputData) 
					input.getParameter();
				
				// what are these nodes?
				LayoutNodeData fromNode =  output.getModuleView().getNode();
				LayoutNodeData toNode = input.getModuleView().getNode();
				// somehow create a link
				LayoutLinkData linkData = new 
					LayoutLinkData(chain,fromNode,fromOutput,toNode,toInput);
				fromNode.addOutputLink(linkData);
				toNode.addInputLink(linkData);
				links.add(linkData);
			}
		}
		chain.setLinks(links);
	}
	
	public void setStatusLabel(String label) {
		frame.setStatusLabel(label);
	}

 }