/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainBox;
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


//Third-party libraries
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.agents.events.MouseOverAnalysisChain;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericBox;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;
import org.openmicroscopy.shoola.util.ui.piccolo.PConstants;


/** 
 * A subclass of {@link PCategoryBox} that is used to provide a colored 
 * background for {@link PChain} widgets in the {@link PChainLibraryCannvas}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class ChainBox extends GenericBox implements MouseableNode 
		/*,SelectionEventListener*/ {
	

	/**
	 * The size of an orthogonal side of the lock icon
	 */
	public static final int SIZE_LENGTH=50;
	
	public static final double MAX_NAME_SCALE=6;
	

	/**
	 * 
	 * The ID of the chain being stored
	 */
	private int chainID=0;
	
	private LayoutChainData chain;
	
	
	private static final float VGAP=10;
	private static final float HGAP=20;
	private static final float FUDGE=3;
	
	private PText name;
	
	private float height;
	private float width;
	float x =0;
	float y = 0;
	
	private PLayer chainLayer;
	private LinkLayer PLinkLayer;
	
	/** the chain being represented */
	private ChainView chainView;
	
	/** The OME registry */
	private Registry registry;
	
	public ChainBox(LayoutChainData chain,Registry registry) {
		super();
		this.chain = chain;
		this.registry = registry;
		chainID = chain.getID();
		/*SelectionState selectionState = SelectionState.getState();
		selectionState.addSelectionEventListener(this); */
		
		
		chainLayer = new PLayer();
		addChild(chainLayer);
		
	
		// add name
		name = new PText(chain.getName());
		name.setFont(PConstants.LABEL_FONT);
		name.setPickable(false);
		name.setScale(MAX_NAME_SCALE);
		chainLayer.addChild(name);
		name.setOffset(HGAP,VGAP*3);
		
		double width = name.getGlobalFullBounds().getWidth();
		//		 one VGAP below + 3 above
		y = (float) (name.getGlobalFullBounds().getHeight()+VGAP*3); 
		
		
		// add ower name
		PText owner = new PText(chain.getOwner());
		owner.setFont(PConstants.LABEL_FONT);
		owner.setPickable(false);
	
		
		chainLayer.addChild(owner);
		owner.setOffset(x+HGAP,y+VGAP);
		y += owner.getHeight()+VGAP;
		
		// build the chain..
		chainView = new ChainView(chain,false);
		
		chainLayer.addChild(chainView);
		chainView.setOffset(HGAP*2,y);
		y += chainView.getHeight()+VGAP; 

		//		 find width. use it in layout of datasets/executions..
		if (chainView.getWidth() > width)
			width = chainView.getWidth();
		
		
		
		// if executions, add them here...
		/*Collection datasets = chain.getDatasetsWithExecutions();
		if (datasets.size() > 0) {
			// add indication of datasets
			PText datasetLabel = new PText("Datasets: ");
			datasetLabel.setFont(PConstants.LABEL_FONT);
			datasetLabel.setOffset(x+HGAP,y);
			datasetLabel.setPickable(false);
			datasetLabel.setScale(PConstants.FIELD_LABEL_SCALE);
			chainLayer.addChild(datasetLabel);
			PBounds dlbounds = datasetLabel.getGlobalFullBounds();
			//y+=dlbounds.getHeight()+VGAP;
			double datasetsWidth = width - (dlbounds.getWidth()+2*HGAP);
			
			// add individual datasets
			PDatasetLabels datasetLabels = new 
				PDatasetLabels(datasets,datasetsWidth);
			
			// adjust size
			chainLayer.addChild(datasetLabels);
			double ratio = PConstants.ITEM_LABEL_SCALE/
				PConstants.FIELD_LABEL_SCALE;
			y += (1-ratio)*dlbounds.getHeight()-VGAP-FUDGE;
			datasetLabels.setOffset(x+dlbounds.getWidth()+2*HGAP,y);
			PBounds b2 = datasetLabels.getGlobalFullBounds();
			double datasetHeight = dlbounds.getHeight();
			if (b2.getHeight() > datasetHeight)
				datasetHeight = b2.getHeight();
			y+= datasetHeight+VGAP;
			// add indications of executions
			
			
			/// add the individual labels;
		} */
		
		setExtent(width+HGAP*2,y);
	}
	
	/**
	 * 
	 * @return the ID of the chain stored in the box
	 */
	public int getChainID() {
		return chain.getID();
	}
	
	/**
	 * @return the chain stored in the box
	 * 
	 */
	public LayoutChainData getChain() {
		return chain;
	}
	
	public ChainView getChainView() {
		return chainView;
	}
	
	public void setExtent(double width,double height) {
		super.setExtent(width,height);
		// add a triangle in the corner.
		if (chain.getIsLocked()) {
			addLockedIndicator();
			//PBounds b = getFullBoundsReference();
			
		}
	}
	
	private void addLockedIndicator() {
		PBounds b = getFullBoundsReference();
		PText locked = new PText("Locked");
		locked.setFont(PConstants.LABEL_FONT);
		locked.setPaint(PConstants.LOCKED_COLOR);
		locked.setScale(2);
		chainLayer.addChild(locked);
		PBounds lockedBounds = locked.getGlobalFullBounds();
		float x = (float) (b.getX()+b.getWidth()-lockedBounds.getWidth()-HGAP);
		locked.setOffset(x,b.getY()+VGAP);
	}
			
	
	public void setSelected(boolean v) {
		if (v == true)
			setPaint(PConstants.EXECUTED_COLOR);
		else
			setPaint(null);
		repaint();
	}
	
	/*public void selectionChanged(SelectionEvent e) {
		SelectionState selectionState = e.getSelectionState();
		if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_CHAIN)) {
			boolean selected = selectionState.getRolloverChain() == chain; 
			setHighlighted(selected);
			
		}
		else {
			Collection activeDatasets = selectionState.getActiveDatasets();
			boolean selected = 
				chain.hasExecutionsInSelectedDatasets(activeDatasets);
			setSelected(selected);
		}
	}
	
	public int getEventMask() {
		return SelectionEvent.SET_SELECTED_DATASET |
			SelectionEvent.SET_SELECTED_PROJECT |
			SelectionEvent.SET_ROLLOVER_CHAIN;
	}*/

	public void mouseClicked(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).animateToNode(this);
		((ChainPaletteEventHandler) handler).setLastEntered(chainView);
		SelectAnalysisChain event = new SelectAnalysisChain(chainView.getChain());
		registry.getEventBus().post(event);
	}

	public void mouseDoubleClicked(GenericEventHandler handler) {
	}

	public void mouseEntered(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastHighlighted(this);
		setHighlighted(true);
		MouseOverAnalysisChain event = 
			new MouseOverAnalysisChain(chainView.getChain());
		registry.getEventBus().post(event);
	}

	public void mouseExited(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastHighlighted(null);
		setHighlighted(false);
		MouseOverAnalysisChain event = 
			new MouseOverAnalysisChain(null);
		registry.getEventBus().post(event);
	}

	public void mousePopup(GenericEventHandler handler) {
		PNode p = getParent();
		if (p instanceof BufferedObject)  
			((ModuleNodeEventHandler) handler).animateToNode(p);	
	}	
		
}