/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleView;
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
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.event.EventListenerList;


//Third-party libraries
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolo.util.PNodeFilter;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;
import org.openmicroscopy.shoola.util.ui.piccolo.SortableBufferedObject;


/** 
 * A Piccolo widget for an OME analysis  module. This widget will consist of a 
 * rounded rectangle, which is a border. This node will have two children:
 * a node with the name of the Module, and a second child which will itself
 * have multiple children - one for each input and output of the module. These 
 * children will be instances of FormalInput and FormalOutput (or appropriate
 * subclasses thereof).  
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */



public class ModuleView extends PPath implements SortableBufferedObject,
	MouseableNode {
	
	/*
	 * 
	 * Some static constants for convenience.
	 */
	
	private static final float DEFAULT_WIDTH=80;
	private static final float DEFAULT_HEIGHT=50;
	private static final float DEFAULT_ARC_WIDTH=10.0f;
	private static final float DEFAULT_ARC_HEIGHT=10.0f;
	private static final float NAME_LABEL_OFFSET=5.0f;
	private static final float NAME_SPACING=15.0f;
	public static final float PARAMETER_SPACING=3.0f;
	private static final float HORIZONTAL_GAP =50.0f;
    private static final float NAME_MAG=1;
	private static final float ZOOM_MAG=2;
	
		
	/**
	 *  The Rectangle with the bounds of the enclosing border
	 */
	private RoundRectangle2D rect;
	
	/**
	 * The node contiaining the module name
	 */
	private PText name;
	
	/**
	 * A version of the module name suitable for semantic zooming
	 */
	private PText zoomName;
	 
	/**
	 * Dimensions of the ModuleView
	 */
	private float height;
	private float width=0;
	
	/**
	 * The Width of the name node.
	 */
	private float nameWidth=0;
	
	/**
	 *  The node that will contain nodes for each of the formal parameters
	 */
	private ParameterNode labelNodes;
	
	/**
	 * A node that holds the square rectangle {@link LinkTarget}s associated
	 * with each of the inputs and outputs.
	 * 
	 */
	private PNode linkTargets;
	
	/**
	 * The {@Plink LinkTarget}s for the modules as a whole
	 */
	private LinkTarget inputLinkTarget;
	private LinkTarget outputLinkTarget;
	
	/**
	 * Each {@link ModuleView} corresponds to a node in a chain, if it is part of
	 * an analysis chain
	 */
//	private Node node=null;
	
	/**
	 * The OME object corresponding to the analysis module that this object
	 * represents
	 */
	private ChainModuleData module;
	
	/** the node in the analysis chain that is represented by this widget */
	private LayoutNodeData node;
	
	public ModuleView() {
	}
	
	public LayoutNodeData getNode() {
		return node;
	}
	
	public void setNode(LayoutNodeData node) {
		this.node = node;
	}
	
	public ModuleView(ChainModuleData module,float x,float y) {
		this(module);
		setOffset(x,y);
	}
	 
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public ModuleView(ChainModuleData module) {
		super();
	
		this.module = module;
		
		// create the container node for the formal parameters
		labelNodes = new ParameterNode();
		addChild(labelNodes);
		
		// create the name and position it.
		name = new PText(module.getName());
		name.setGreekThreshold(0);
		name.setFont(Constants.NAME_FONT);
		name.setPickable(false);
		name.setScale(NAME_MAG);
		addChild(name);
		
		name.setOffset(NAME_LABEL_OFFSET,NAME_LABEL_OFFSET);
		PBounds nameBounds = name.getGlobalFullBounds();
		
		// calculate starting height for parameters.
		height = NAME_LABEL_OFFSET+((float) nameBounds.getHeight());
	
		// build the node for the link targets
		linkTargets = new PNode();
		addChild(linkTargets);	
		
		float linkTargetHeight = height;
		
		// add the input link target
		inputLinkTarget = getLinkTarget();
		linkTargets.addChild(inputLinkTarget);
		inputLinkTarget.setOffset(-Constants.LINK_TARGET_HALF_SIZE,height);
				
		nameWidth = (float) nameBounds.getWidth();
		
		// do the individual parameter labels.
		addParameterLabels(module);  
		
		// set width of the whole bounding rectangle
	    width = NAME_LABEL_OFFSET*2+width-Constants.LINK_TARGET_HALF_SIZE;
		
		// create bounding rectangle, set it to be this node's path,
		// and finish other parameters.
		rect = 
			new RoundRectangle2D.Float(0f,0f,width,height,
					DEFAULT_ARC_WIDTH,DEFAULT_ARC_HEIGHT);
					
		setPathTo(rect);
		setPaint(Constants.DEFAULT_FILL);
		setStrokePaint(Constants.DEFAULT_COLOR);
		setStroke(Constants.MODULE_STROKE);
		
		// add the other target
		outputLinkTarget = getLinkTarget();
		linkTargets.addChild(outputLinkTarget);
		outputLinkTarget.setOffset(width-Constants.LINK_TARGET_HALF_SIZE,
			linkTargetHeight);
	
		buildMagnifiedLabel();
	}
	
	protected LinkTarget getLinkTarget() {
		return new LinkTarget();
	}
	
	private void buildMagnifiedLabel() {
		// set up the magnified version of the module name
		zoomName = new PText(module.getName());
		zoomName.setGreekThreshold(0);
		zoomName.setFont(Constants.NAME_FONT);
		zoomName.setPickable(false);
		zoomName.setConstrainWidthToTextWidth(false); 
		
		double zwidth;
		double zheight;
		float scale = ZOOM_MAG;
		zoomName.setScale(scale);
		addChild(zoomName);	
		
		zwidth = (width-2*NAME_LABEL_OFFSET)/ZOOM_MAG;
		zheight = (height-2*NAME_LABEL_OFFSET)/ZOOM_MAG;
		
		
	
		zoomName.setBounds(new PBounds(NAME_LABEL_OFFSET,NAME_LABEL_OFFSET,
				zwidth,zheight));
		float zoomHeight = (float) zoomName.getHeight();
		float zoomWidth = (float) zoomName.getWidth();
		
		// scale the text to fit in the box.
		
		float heightScale=1.0f;
		float widthScale=1.0f;
		if (zoomHeight >= zheight) 
			heightScale = (float)(zheight/zoomHeight);
		if (zoomWidth >= zwidth) 
			widthScale = (float)(zwidth/zoomWidth);
		if (widthScale < heightScale)
			scale = widthScale;
		else
			scale = heightScale;
		zoomName.scale(scale);
		
		
	
		zoomName.setVisible(false);
	}
	
	/** 
	 * Input and output parameters will be displayed in rows - 
	 * with the inputs on the left and the outputs on the right. Each 
	 * row will contain at most one input and one output. Whichever set
	 * (input or output) is larger will have some entries without matching 
	 * counterparts.<p>
	 * 
	 * This procedure positions the parameter nodes and calculates the size
	 * of the bounding rectangle that will be needed to hold all of the 
	 * parameters<p>
	 *
	 * @param module the module in question
	 */
	private void addParameterLabels(ChainModuleData module) {
		
		List inputs = module.getFormalInputs();
		List outputs = module.getFormalOutputs();
		int inSize = 0;
		int outSize = 0;
		if (inputs != null)
			inSize = inputs.size();
		if (outputs != null) 
			outSize = outputs.size();
		// each row will contain one input and one output.
		// if # of each is not equal, we'll have one or more rows of input
		// only or output only.
		// # of rows is max of input and output
		int 	rows = inSize > outSize? inSize: outSize;
		
		ChainFormalInputData paramIn;
		ChainFormalOutputData paramOut;
		FormalInput inp;
		FormalOutput outp;
		
		// Store them in {@link TreeSet} objects, so things will be sorted,
		// based on the id numbers of the semantic types of the associated 
		// parameters   See FormalParameter for details
		TreeSet inSet  = new TreeSet();
		TreeSet outSet= new TreeSet();
		
		// get input nodes and find max input width
		float maxInputWidth =0;
		float maxOutputWidth =0;
		
		// for each row.
		for (int i = 0; i < rows; i++) {
			if (i < inSize) {
				// as long as I have more inputs, create them, 
				// add them to label nodes, 
				// and store max width
				paramIn = (ChainFormalInputData) inputs.get(i);
				inp = getFormalInput(paramIn);
				labelNodes.addChild(inp);
				inSet.add(inp);
				if (inp.getLabelWidth() > maxInputWidth)
					maxInputWidth = inp.getLabelWidth();
			}
			if (i < outSize) {
				paramOut = (ChainFormalOutputData) outputs.get(i);
				outp = getFormalOutput(paramOut);
				labelNodes.addChild(outp);
				outSet.add(outp);
				if (outp.getLabelWidth() > maxOutputWidth)
					maxOutputWidth = outp.getLabelWidth();
			}
		}
		
		// find maximum width of the whole thing.
		width = maxInputWidth+maxOutputWidth+HORIZONTAL_GAP;
		if (nameWidth > width)
			width = nameWidth;
		
		// find horizontal starting point of the output parameters.
		//float outputColumnX=NAME_LABEL_OFFSET+maxInputWidth+HORIZONTAL_GAP;
		float outputColumnX = width-maxOutputWidth;
		
		
		//height of first one
		height+=NAME_SPACING;
		float inHeight = 0;
		float outHeight=0;
			 		
	 	Object[] ins = inSet.toArray();
	 	Object[] outs = outSet.toArray();
	
		// place things at appropriate x,y.
		for (int i =0; i < rows; i++) {
			inHeight = outHeight =0;
			// get ith input 
			if (i <inSize) {
				inp = (FormalInput) ins[i];
				inp.setOffset(NAME_LABEL_OFFSET,height);
				inHeight = (float) inp.getFullBoundsReference().getHeight();	
			}
			// get ith output
			if (i < outSize) {
				// we want to right-justify these. So, 
				// find difference bwtween the maximum output width
				// and the width of this one.
				outp = (FormalOutput) outs[i];
				float rightJustifyGap = maxOutputWidth-
					outp.getLabelWidth();
				// and then move right by that amount.
				outp.setOffset(outputColumnX+rightJustifyGap,height);
				outHeight = (float) outp.getFullBoundsReference().getHeight();
			}
			if (inHeight > outHeight )
				height +=inHeight;
			else
				height+=outHeight; 
		}
	}
	
	protected FormalInput getFormalInput(ChainFormalInputData paramIn) {
		return new FormalInput(this,paramIn);
	}
	
	protected FormalOutput getFormalOutput(ChainFormalOutputData paramOut) {
		return new FormalOutput(this,paramOut);
	}
	
	/**
	 * Paint the node in the given context. This method does some 
	 * simple semantic zooming. If the scale factor is below the threshold - 
	 * the user has zoomed out - don't show the individual parameters and the 
	 * link targets - just show the larger module name. Otherwise, 
	 * show all of the details.
	 * 
	 */
	public void paint(PPaintContext aPaintContext) {
		double s = aPaintContext.getScale();
	
		if (s <= Constants.SCALE_THRESHOLD) {
			labelNodes.setVisible(false);
			labelNodes.setPickable(false);
			name.setVisible(false);
			zoomName.setVisible(true);
			linkTargets.setVisible(true);
		}
		else {
			linkTargets.setVisible(false);
			name.setVisible(true);
			labelNodes.setVisible(true);
			labelNodes.setPickable(true);
			zoomName.setVisible(false);
		} 
		super.paint(aPaintContext);
	} 
	
	// can only link modules if the parameters are visible
	public boolean isLinkable() {
		return labelNodes.getVisible();
	}
	
	/**
	 * Set the color of the module if it is highlighted.
	 * @param v true if the module is highlighted, else false
	 */
	public void setHighlighted(boolean v) {
		if (v == true)
			setStrokePaint(Constants.SINGLE_HIGHLIGHT_COLOR);
		else
			setStrokePaint(Constants.DEFAULT_COLOR);
		repaint();
	}
	
	/**
	 * Set the color indiciating that the module can be linked to from 
	 * the selected module
	 * @param v true if this module can be linked to from the current selection.
	 * 
	 */
	public void setLinkableHighlighted(boolean v) {
		if (v == true)
			setPaint(Constants.SELECTED_FILL);
		else
			setPaint(Constants.DEFAULT_FILL);
		repaint();
	}
	
	/**
	 * 
	 * @return the OME Module associated with this graphical display
	 */
	public ChainModuleData getModule() {
		return module;
	}
	
	
	/**
	 * to remove a {@link ModuleView}, remove all of its links,
	 * remove this widget from the list of widgets for the corresponding OME 
	 * Module, and remove this widget from the scenegraph
	 *
	 */
	public void remove() {
		// iterate over children of labelNodes
		Iterator iter = labelNodes.getChildrenIterator();
		
		FormalParameter p;
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			p.removeLinks();
		}
		module.removeModuleNode(this);
		removeFromParent();
	}
	
	
	/***
	 * Some code for managing listeners and events
	 */
	
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
	 * translate - call super class and then notify all who are interested 
	 * in receiving events from this node.
	 */
	
	public void translate(double dx,double dy) {
		super.translate(dx,dy);
		fireStateChanged();
	}

	public void setAllHighlights(boolean v) {
		setParamsHighlighted(v);
		setModulesHighlighted(v);
	}
	
	/**
	 * Set all of the {@link ModuleView} objects with the same OME {@link Module} 
	 * as this one to have the same highlighted state.
	 * @param v true if the modules should be highlighted, else false
	 */
	public void setModulesHighlighted(boolean v) {
		
		module.setModulesHighlighted(v);
	}
	
	/***
	 * Set the parameters associated with this module to be highlighted.
	 * @param v true if the parameters should be highlighted, else false
	 */
	public void setParamsHighlighted(boolean v) {

		Iterator iter = labelNodes.getChildrenIterator();
		FormalParameter p;
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			p.setParamsHighlighted(v);
		}
	}	

	public PBounds getBufferedBounds() {
		PBounds b = getGlobalFullBounds();
		return new PBounds(b.getX()-Constants.BORDER,
			b.getY()-Constants.BORDER,
			b.getWidth()+2*Constants.BORDER,
			b.getHeight()+2*Constants.BORDER);
	}
	
	// handles
	public void addHandles() {
		addChild(new ModuleHandles(PBoundsLocator.createNorthEastLocator(this)));
		addChild(new ModuleHandles(PBoundsLocator.createNorthWestLocator(this)));
		addChild(new ModuleHandles(PBoundsLocator.createSouthEastLocator(this)));
		addChild(new ModuleHandles(PBoundsLocator.createSouthWestLocator(this)));
	}
	
	public void removeHandles() {
		ArrayList handles = new ArrayList();
		Iterator i = getChildrenIterator();
		while (i.hasNext()) {
			PNode each = (PNode) i.next();
			if (each instanceof ModuleHandles) 
				handles.add(each);
		}
		removeChildren(handles);
	}
	
	/*** TODO  -FIX THESE ***/
	/**
	 * @param out The object wrapping an OME {@link FormalOutput} for this 
	 * 	module
	 * @return The {@link FormalOutput} node for that {@link FormalOutput} 
	 */
	public FormalOutput getFormalOutputNode(ChainFormalOutputData out) {
		return (FormalOutput) getMatchingParameterNode(ChainFormalOutputData.class,out);
	}
	
	/**
	 * @param in The object wrapping an OME {@link FormalInput} for this 
	 * 	module
	 * @return The {@link FormalInput} node for that {@link FormalInput} 
	 */
	public FormalInput getFormalInputNode(ChainFormalInputData in) {
		return (FormalInput) getMatchingParameterNode(ChainFormalInputData.class,in);
	}	
	
	/**
	 * 
	 * @param clazz The class ({@link FormalOutput} or {@link FormalInput} 
	 * 		desired
	 * @param target The {@link FormalParameter} object to be mached
	 * @return The corresponding {@link FormalParmeter}
	 */
	private FormalParameter getMatchingParameterNode(final Class clazz,
		FormalParameterData target) {
		
		if (labelNodes == null) 
			return null;
		
		Iterator iter = labelNodes.getChildrenIterator();
		FormalParameter p;
		FormalParameterData param;
		
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			param = p.getParameter();
			Class pClass = param.getClass();
			if (pClass ==  clazz) {
				if (target.getID() == param.getID())
					return p;
			}
		}
		// should never reach here
		return null;
	}

	public LinkTarget getInputLinkTarget() {
		return inputLinkTarget;
	}
	
	public LinkTarget getOutputLinkTarget() {
		return outputLinkTarget;
	}
	
	/**
	 * A position is no the input side if it's to the left of the horizontal
	 * midpoint. Used to determine which side the user clicked on when creating 
	 * bulk links between modules
	 * @param pos A location on the module.
	 * @return True if thhe location is on the left half, else false
	 */
	public boolean isOnInputSide(Point2D pos) {
		boolean res = false;
		globalToLocal(pos);
		float posX = (float)pos.getX();
		PBounds b = getFullBoundsReference();
		float mid = (float) (b.getWidth()/2);
		if (posX < mid)
			res = true;
		return res;
	}
	
	/**
	 * 
	 * @return a sorted list of all of the input parameters that don't already
	 * have incoming links. These parameters are identified via
	 * 	a {@link PNodeFilter}
	 */
	public TreeSet getInputParameters() {
		PNodeFilter inputFilter = new PNodeFilter() {
			public boolean accept(PNode aNode) {
				// want only those things that are inputs.
				if (!(aNode instanceof FormalInput))
					return false;
				FormalInput inp = (FormalInput) aNode;
				// and can still be origins - don't have anything 
				// linked to them.
				return inp.canBeLinkOrigin();
			}
			public boolean acceptChildrenOf(PNode aNode) {
				return true;
			}
		};
		return new TreeSet(labelNodes.getAllNodes(inputFilter,null));
	}
	
	/**
	 * 
	 * @return a sorted list of all of the output parameters. These parameters 
	 * are identified via a {@link PNodeFilter}
	 */
	public TreeSet getOutputParameters() {
		PNodeFilter outputFilter = new PNodeFilter() {
			public boolean accept(PNode aNode) {
				return (aNode instanceof FormalOutput);
			}
			public boolean acceptChildrenOf(PNode aNode) {
				return true;
			}
		};
		return new TreeSet(labelNodes.getAllNodes(outputFilter,null));
	}
	
	/*public void setNode(Node node) {
		this.node = node;
	}
	
	public Node getNode() {
		return node;
	}*/
	
	public double getX() {
		return getFullBounds().getX();
	}
	
	public double getY() {
		return getFullBounds().getY();
	}
	
	public int compareTo(Object o) {
		if (o instanceof SortableBufferedObject) {
			SortableBufferedObject node = (SortableBufferedObject) o;
			double myArea = getHeight()*getWidth();
			PBounds bounds = node.getBufferedBounds();
			double nodeArea = bounds.getHeight()*bounds.getWidth();
			int res =(int) (myArea-nodeArea);
			return res;
		}
		else
			return -1;
	}
	
	public BufferedObject getEnclosingBufferedNode() {
		
		PNode p = getParent();
		while (p != null) {
			if (p instanceof BufferedObject)
				return (BufferedObject) p;
			p = p.getParent();
		}
		return null;
	}
	
	public void mouseClicked(GenericEventHandler handler) {
		
		((ModuleNodeEventHandler) handler).animateToNode(this);
		((ModuleNodeEventHandler) handler).setLastEntered(this);
	}

	public void mouseDoubleClicked(GenericEventHandler handler) {
	}

	public void mouseEntered(GenericEventHandler handler) {
		setAllHighlights(true);
		((ModuleNodeEventHandler) handler).setLastEntered(this);
	}

	public void mouseExited(GenericEventHandler handler) {
		setAllHighlights(false);
		((ModuleNodeEventHandler) handler).setLastEntered(null);
	}

	public void mousePopup(GenericEventHandler handler) {
		PNode p = getParent();
		if (p instanceof BufferedObject)  
			((ModuleNodeEventHandler) handler).animateToNode(p);	
	}
}