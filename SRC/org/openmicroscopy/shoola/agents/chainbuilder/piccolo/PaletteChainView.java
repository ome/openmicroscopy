/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.PaletteChainView
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
import java.util.Collection;
//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
//import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutionsByModuleID;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;



/** 
 * A view of chains to be used on a {@link ChainPaletteCanvas}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class PaletteChainView extends ChainView {
	
	private Registry registry;
 
	private ChainCompoundModuleView compoundView;
	
	private boolean showingFull = false;
	
	private ChainExecutionsByModuleID executions;
	
	public PaletteChainView(LayoutChainData chain,ChainDataManager dataManager) {
		super(chain);
		this.registry = dataManager.getRegistry();
		this.executions = dataManager.getChainExecutionsByChainID(chain.getID());
		drawChain();
		setPickable(false);
	}	
	
	public void setPickable(boolean b) {
		//super.setPickable(b);
		setChildrenPickable(b);
		// links are always not pickable..
		super.setPickable(false);
		linkLayer.setPickable(false);
	}
	

	protected LinkLayer createLinkLayer() {
		return new PaletteChainLinkLayer();
	}
	
	protected ModuleView getModuleView(ChainModuleData mod) {
		Collection mexes = null;
		if (executions != null)
			mexes = executions.getMexes(mod);
		PaletteModuleView moduleView = 
			new PaletteModuleView(mod,mexes);
		//find the execution here..
		return moduleView;
	}
	

	
	protected ParamLink getParamLink(FormalInput inputPNode,
			FormalOutput outputPNode) {
		ParamLink pLink = super.getParamLink(inputPNode,outputPNode);
		pLink.setPickable(false);
		return pLink;
	}
	
	
	// we don't use module links in palette view
	protected ModuleLink getModuleLink(LinkLayer linkLayer,ParamLink newLinkNode) {
		return null; 
	}
			
	//	 if this chain is in a chainbox - which would then be the grandparent
	// return a chain box that is the enclosing grandparent
	private ChainBox getParentChainBox() {
		PNode parent = getParent();
		if (parent == null)
			return null;
		parent = parent.getParent();
		if (parent == null)
			return null;
		if (parent instanceof ChainBox)
			return ((ChainBox) parent);
		else 
			return null;
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		ChainPaletteEventHandler chainHandler = (ChainPaletteEventHandler) handler;
		chainHandler.setLastEntered(this);
		SelectAnalysisChain event = new SelectAnalysisChain(getChain());
		registry.getEventBus().post(event);
		chainHandler.zoomIn(e);
	}
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		ChainPaletteEventHandler chainHandler = (ChainPaletteEventHandler) handler;
		chainHandler.zoomOut(e);
	}	
	
	public PNode getToolTip() {
		String name = getChain().getName();
		//String desc = mod.getDescription();
		if (name.compareTo("") != 0) {
			PText pt = new PText(name);
			pt.setPickable(false);
			pt.setFont(Constants.TOOLTIP_FONT);
			PPath node = new PPath();
			node.addChild(pt);
			pt.setOffset(0,0);
			pt.setFont(Constants.TOOLTIP_FONT);
			
			node.setBounds(node.getUnionOfChildrenBounds(null));
			node.setStrokePaint(Constants.TOOLTIP_BORDER_COLOR);
			node.setPaint(Constants.TOOLTIP_FILL_COLOR);
			node.setPickable(false);
			return node;
		}
		else 
			return null;
	}
}
