/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalParameter
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
import java.awt.geom.Point2D;
import javax.swing.event.EventListenerList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;

/** 
 * Nodes for displaying module inputs and outputs. Currently, all
 * module parameters are displayed as text, with decorations (color) to
 * indicate change of state. For example, the node will be painted in 
 * HIGHLIGHT_COLOR when it is a candidate for a link with another actively
 * selected parameter.<p>
 * 
 * Generally, there will be two subclasses of this class - one for inputs
 * and one for outputs - in other words, {@link PFormalInputs} and 
 * {@link PformalOutputs}.
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


public abstract class FormalParameter extends PNode implements 
	NodeEventListener, Comparable, MouseableNode, ParameterLabelNode, ToolTipNode {
	
	/**
	 * Some generic display parameters
	 */
	public static final int TYPE_NODE_VERTICAL_OFFSET=12;
	//public static final float TYPE_NODE_DEFAULT_SCALE=0.5f;
	
	private static final float HEIGHT_PORTION=0.8f;
	
	/**
	 * The OME FormalParameter object that the node represents.
	 */
	protected FormalParameterData param;
	
	/**
	 * Connection to the OME database
	 */
	
	/**
	 * The module containing this parameter
	 */
	protected ModuleView node;
	
	/**
	 * A list of {@link FormalParameter} object that this one is linked to
	 */
	protected  Vector linkedTo = new Vector();
	
	/**
	 * A list of {@link PLinks} involving this parameter
	 */
	protected Vector links = new Vector();
	 
	 /**
	  * True if a link to this parameter can be added
	  */
	protected boolean linkable = false;
	
	/**
	 * The name of the parameter
	 */
	protected PText textNode;
	
	/**
	 * The semantic type of the parameter
	 */
	protected PText typeNode;
	
	/**
	 * The {@link LinkTarget} associated with this parameter
	 */
	protected LinkTarget target = null;
	
	/**
	 * A node containing the textual labels for the name 
	 * and semantic type
	 */
	protected PNode labelNode;
	
	private FormalParameterMouseDelegate delegate = null;
	
	
	public FormalParameter(ModuleView node,FormalParameterData param) {
		this(node,param,null);
	}
	/**
	 * 
	 * @param node The ModuleView containing this parameter
	 * @param param The OME Formal Parameter
	 */
	public FormalParameter(ModuleView node,FormalParameterData param,
			FormalParameterMouseDelegate delegate) {
		super();
		this.param = param;
		this.node = node;
		if (delegate == null) 
			this.delegate = new FormalParameterMouseDelegate();
		else
			this.delegate = delegate;
		
		this.delegate.setParam(this);
		
		setChildrenPickable(false);
		labelNode = new PNode();
		addChild(labelNode);
		
		textNode = new PText(param.getName());
		textNode.setFont(Constants.NAME_FONT);
		textNode.setTextPaint(Constants.DEFAULT_TEXT_COLOR);
		//textNode.setGreekThreshold(0);
		labelNode.addChild(textNode);
		
		
		// add a semantic type label only if the type is not null
		SemanticTypeData type = param.getSemanticType();
		if (type != null) {
			typeNode = new PText(type.getName());
		//	typeNode.setGreekThreshold(0);
			labelNode.addChild(typeNode);
			typeNode.setTextPaint(Constants.DEFAULT_TEXT_COLOR);
			typeNode.setFont(Constants.ST_FONT);
		}						
		
		// this formal parameter will listen to any changes that happen to
		// the node.
		node.addNodeEventListener(this);
		//setTransparency(Constants.MODULE_TRANSPARENT);
		
		
	}

	/**
	 * Add a {@link LinkTarget}
	 *
	 */	
	protected void addTarget() {
		target = new LinkTarget();
		addChild(target);
		target.setPickable(false);
		setTargetPosition();
	}
	
	protected void setTargetPosition() {
		PBounds b = labelNode.getFullBoundsReference();
		float x = getLinkTargetX();
		float y = (float) b.getY()+Constants.LINK_TARGET_BUFFER;
		if (target == null)
			addTarget();
		target.setOffset(x,y);
	}
	
	/**
	 * For inputs, the target is to the left of the text node.
	 * For outptuts, it is on the right. 
	 * @return The x-coordinate of the link target.
	 */
	protected abstract float getLinkTargetX();
	
	public String getName() {
		return param.getName();
	}
	
	public ModuleView getModuleView() {
		return node;
	}
	
	public ChainModuleData getModule() {
		return getModuleView().getModule();
	}
	
	/**
	 * A module parameter is said to be linkable if (1) it has the same
	 * semantic type as a currently selected parameter, and (2) it's position 
	 * (input vs. output) corresponds appropriately to that of the current 
	 * selection. Inputs can only link to outputs, and vice-versa.<p>
	 * 
	 * @param v true if the parameter is linkable.
	 */
	public void setLinkable(boolean v) {
		linkable = v;
		setHighlighted(v);
		getModuleView().setLinkableHighlighted(v);
		repaint();
	}
	
	public void setHighlighted(boolean v) {
		
		if (v == true) {
			if (typeNode != null)
				typeNode.setTextPaint(Constants.HIGHLIGHT_COLOR);
			textNode.setTextPaint(Constants.HIGHLIGHT_COLOR);
		}
		else {
			if (typeNode != null)
				typeNode.setTextPaint(Constants.DEFAULT_TEXT_COLOR);
			textNode.setTextPaint(Constants.DEFAULT_TEXT_COLOR);
		}
		target.setHighlighted(v);
	}
	
	public boolean isLinkable() {
		return linkable;
	}
	
	
	public FormalParameterData getParameter() {
		return param;
	}
	
	public SemanticTypeData getSemanticType() {
		return param.getSemanticType();
	}
	
	
	/**
	 * Get a list of parameters that have the same semantic type
	 * as this one, but in the opposite position. If this is an input(output),
	 * get all of the outputs(inputs) with the same semantic type.<p>
	 * 
	 * @return A list of FormalParameters of the appropriate type and 
	 * 	corresponding position.
	 */
	public abstract List getCorresponding();

	/**
	 * Notify objects that are interested in case of changes to the appropriate 
	 * node
	 */
	public void nodeChanged(NodeEvent e) {
		
		// if I'm listening to a node, and it's a parent, pass 
		// it along to whomever is listening to me.
		PNode n = e.getNode();
		if (isDescendentOf(n)) {
			fireStateChanged();
		}
	}
	
	
	private EventListenerList listenerList =
		new EventListenerList();
	
	public void addNodeEventListener(NodeEventListener nel) {
		listenerList.add(NodeEventListener.class,nel);
	}

	public void removeNodeEventListener(NodeEventListener nel) {
		listenerList.remove(NodeEventListener.class,nel);
	}
		
	public void fireStateChanged() {
		Object[] listeners  = listenerList.getListenerList();
		for (int i = listeners.length-2; i >=0; i -=2) {
			if (listeners[i]==NodeEventListener.class) {
				((NodeEventListener)listeners[i+1]).nodeChanged(
					new NodeEvent(this));
			}
		}
	}

	/**
	 * Update the state when this paramter is linked to another - 
	 *  add the other parameter, and the {@link Link} to the appropriate 
	 *  lists, and indicate that the {@link LinkTarget} is linked 
	 * @param param
	 * @param link
	 */
	public void setLinkedTo(FormalParameter param,ParamLink link) {
		linkedTo.add(param);
		target.setLinked(true);
		links.add(link);
	}
	
	/**
	 * Inverse of {@link setLinkedTo()}
	 * @param param
	 */
	public void clearLinkedTo(FormalParameter param) {
		linkedTo.remove(param);
		if (linkedTo.isEmpty())
			target.setLinked(false);
		target.setSelected(false);
	}
	
	/**
	 * This parameter is linked to another if the other is in this
	 * parameter's list of parameters that it is linked to
	 * @param param
	 * @return true if this parameter is linked to param, else false.
	 */
	public boolean isLinkedTo(FormalParameter param) {
		return (linkedTo.indexOf(param)!=-1);
 	}
 	
 	
 	
 	public void removeLinks() {
 		ParamLink link;
 		Iterator iter = links.iterator();
 		while (iter.hasNext()) {
 			link = (ParamLink)iter.next();
 			link.remove();
 		}
 		links = new Vector();
 		linkedTo = new Vector();
 	}
 	
	/** 
	 * To highlight link targets for a given FormalParameter, get
	 * the list of "corresponding" ModuleParameters (ie, inputs if this is 
	 * an output, and outputs if this is the input) of the same type, 
	 * and set each of those to be linkable<p>
	 *
	 * @param v
	 */
		
	public void setParamsHighlighted(boolean v) {
			
		setHighlighted(v);
		List list = getCorresponding();
	
		if (list == null)
			return;
		
		
	 	ChainModuleData source = getModule();
		
		FormalParameter p;
		Iterator iter = list.iterator();
		
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			
			if (v == true) {// when making things linkable
				// only make it linkable if we're not linked already
				// and we're not in the same module.
				if (!isLinkedTo(p) && source != null &&
						p.getModule() != null && source != p.getModule())
						p.setLinkable(v);
			}
			else // always want to clear linkable 
				p.setLinkable(v);		
		}
	}

	/**
	 * Set the bounds to include the {@link LinkTarget}
	 *
	 */
	public void updateBounds() {
		PBounds b = labelNode.getFullBounds();
		b.add(target.getFullBounds());
 		setBounds(new PBounds(b.getX(),b.getY(),b.getWidth(),
			b.getHeight()+ModuleView.PARAMETER_SPACING)); 
	}

	/**
	 * 
	 * By default, a parameter can be the origin of a link, but only if it is 
	 * visible - ie, not hidden due to semantic zoom. So, check the parent, 
	 * which is the node that holds this.
	 * 	
	 */
	public boolean canBeLinkOrigin() {
		if (getParent() == null)
			return false;
		return getParent().getVisible();
	}
	
	public float getLabelWidth() {
		PBounds b = labelNode.getFullBoundsReference();
		return (float) labelNode.getFullBoundsReference().getWidth();	
	}
	
	public float getLabelHeight() {
		PBounds b = labelNode.getFullBoundsReference();
		return (float) labelNode.getFullBoundsReference().getHeight()
			*HEIGHT_PORTION;
	}
	
	public Point2D getLinkCenter() {
		PBounds b = target.getFullBoundsReference();
		float x = (float) (b.getX()+b.getWidth()/2);
		float y = (float) (b.getY()+b.getHeight()/2);
		Point2D.Float result = new Point2D.Float(x,y);
		localToGlobal(result);
		return result;
	}
	
	public LinkTarget getLinkTarget() {
		return target;
	}
	
	/**
	 * {@link FormalParameter} instances are placed on a {@link ModuleView}
	 * ordered by semantic type ID. This procedure implementes the 
	 * {@link Comparable} interface, so we can do the necessary sorting.  
	 */
	public int compareTo(Object o) {
		if (!(o instanceof FormalParameter))
			return -1;
		FormalParameter other =(FormalParameter) o;
		
		// defaults
		int myID=-1;
		int otherID =-1;
		
		SemanticTypeData myType = getSemanticType();
		if (myType != null)
			myID = myType.getID();
		SemanticTypeData otherType = other.getSemanticType();
		if (otherType != null)
			otherID = otherType.getID();
		int diff =  myID-otherID;
		
		// if they're different, return this result
		if (diff != 0)
			return diff;
			
		// else, semantic types are the same, order by Ids.
		myID  = getParameter().getID();
		otherID = other.getParameter().getID();

		return (myID-otherID);
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		delegate.mouseClicked(handler,e);
	}

	public void mouseDoubleClicked(GenericEventHandler handler,PInputEvent e) {
	}

	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		if (delegate != null)
			delegate.mouseEntered(handler,e);
	}

	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
		if (delegate != null)
			delegate.mouseExited(handler,e);
	}

	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		delegate.mousePopup(handler,e);
	}
	
	public boolean sameTypeAs(FormalParameter p) {
		SemanticTypeData mySt = getSemanticType();
		SemanticTypeData otherSt = p.getSemanticType();
		if (mySt == null && otherSt ==null)
			return true;
		// if not both null, it's no good if either is null
		else if (mySt == null || otherSt == null)
			return false;
		int myID = mySt.getID();
		int otherID = otherSt.getID();
		return (myID==otherID);
	}
	
	public void setOffset(float x,float y) {
		super.setOffset((double) x,(double) y);
	}
	
	public PNode getToolTip() {
		PPath node = new PPath();
		
		FormalParameterData fp = getParameter();
		PText p = new PText(fp.getName());
		node.addChild(p);
		double y=0;
		p.setOffset(0,y);
		p.setFont(Constants.TOOLTIP_FONT);
		p.setPickable(false);
		y += p.getHeight();
		SemanticTypeData st = fp.getSemanticType();
		if (st != null) {
			p = new PText("Type: "+st.getName());
			p.setPickable(false);
			node.addChild(p);
			p.setFont(Constants.TOOLTIP_FONT);
			p.setOffset(0,y);
			y+=p.getHeight();	
		}
		node.setBounds(node.getUnionOfChildrenBounds(null));
		node.setStrokePaint(Constants.TOOLTIP_BORDER_COLOR);
		node.setPaint(Constants.TOOLTIP_FILL_COLOR);
		node.setPickable(false);
		return node;	
	}
}