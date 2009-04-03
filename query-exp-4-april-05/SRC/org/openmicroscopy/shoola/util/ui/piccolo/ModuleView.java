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




package org.openmicroscopy.shoola.util.ui.piccolo;


//Java imports
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.event.EventListenerList;


//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolo.util.PNodeFilter;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.CategoryBox;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalInput;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalOutput;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalParameter;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.LinkTarget;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleHandles;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleLinkTarget;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleNodeEventHandler;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.NodeEvent;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.NodeEventListener;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ParameterLabelNode;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ParameterNode;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ToolTipNode;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.util.ui.Constants;


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



public class ModuleView extends PNode implements SortableBufferedObject,
	MouseableNode, ToolTipNode  {
	
	/*
	 * 
	 * Some static constants for convenience.
	 */
	
	private static final float DEFAULT_ARC_WIDTH=10.0f;
	private static final float DEFAULT_ARC_HEIGHT=10.0f;
	private static final float NAME_LABEL_OFFSET=5.0f;
	private static final float NAME_SPACING=10.0f;
	public static final float PARAMETER_SPACING=3.0f;
	private static final float ALIGNMENT_BUFFER=30;
    private static final float NAME_MAG=1;
	private static final float LAST_ST_BUFFER=5f;
	private static final float BOTTOM_GAP=30;
	
	
	private static float maxParamWidth = 0f;
	private static float maxNameHeight = 0f;
	/**
	 *  The Rectangle with the bounds of the enclosing border
	 */
	
	protected PPath overview = new PPath();
	
	protected PPath detail = new PPath();
	
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
	
	private float noLabelHeight;
	
	/**
	 *  The node that will contain nodes for each of the formal parameters
	 */
	protected ParameterNode labelNodes;
	
	/**
	 * A node that holds the square rectangle {@link LinkTarget}s associated
	 * with each of the inputs and outputs.
	 * 
	 */
	private PNode linkTargets;
	
	/**
	 * The {@Plink LinkTarget}s for the modules as a whole
	 */
	private ModuleLinkTarget inputLinkTarget;
	private ModuleLinkTarget outputLinkTarget;
	
	/**
	 * Each {@link ModuleView} corresponds to a node in a chain, if it is part of
	 * an analysis chain
	 */
//	private Node node=null;
	
	
	/** the node in the analysis chain that is represented by this widget */
	private LayoutNodeData node;

	
	public LayoutNodeData getNode() {
		return node;
	}
	
	public void setNode(LayoutNodeData node) {
		this.node = node;
	}
	
	public ModuleView(float x,float y) {
		this();
		setOffset(x,y);
	}
	 
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public ModuleView() {
		super();
	
		
		// create the container node for the formal parameters
		labelNodes = new ParameterNode();
	}
	
	/** 
	 * get the name of the module. Will generally be implemented in subclasses. 
	 * Can't be abstract, as we don't really want to put a call in {@LayoutModule}
	 * @return
	 */
	public String getName() {
		return null;
	}
	
	protected ModuleLinkTarget getLinkTarget() {
		return new ModuleLinkTarget();
	}
	
	private void buildMagnifiedLabel() {
		// set up the magnified version of the module name
		zoomName = new PText(getName());
		zoomName.setGreekThreshold(0);
		zoomName.setFont(Constants.NAME_FONT);
		zoomName.setPickable(false);
		zoomName.setConstrainWidthToTextWidth(false); 
		
		overview.addChild(zoomName);	
		zoomName.setBounds(new PBounds(NAME_LABEL_OFFSET,NAME_LABEL_OFFSET,
				width-2*NAME_LABEL_OFFSET,height));
	
		zoomName.setVisible(true);
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
	private void addParameterLabels() {
		
		List inputs = getFormalInputs();
		List outputs = getFormalOutputs();
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
		int rows = inSize > outSize? inSize: outSize;
		
		Integer maxCard = getMaxIOCardinality();
		int max=0;
		
		if (maxCard != null) 
			max = maxCard.intValue();
		
		if (rows > max && maxCard != null)
			rows = max;
		
		ChainFormalInputData paramIn;
		ChainFormalOutputData paramOut;
		FormalInput fin;
		FormalOutput fout;
		ParameterLabelNode inp;
		ParameterLabelNode outp;
		
	
		Vector inSet  = new Vector();
		Vector outSet= new Vector();
		
	
		
		// for each row.
		for (int i = 0; i < rows; i++) {
			if (i < inSize) {
				if (i == max-1 && rows==max) {
					// if # of rows has been truncated and I'm at the last row.
					// add a text item.
					OrderableText text = new OrderableText();
					labelNodes.addChild(text);
					text.setFont(Constants.NAME_FONT);
					text.setVisible(true);
					inSet.add(text);
				}
				else {
					//as long as I have more inputs, create them, 
					// add them to label nodes, 
					// and store max width
					paramIn = (ChainFormalInputData) inputs.get(i);
					fin = getFormalInput(paramIn);
					labelNodes.addChild(fin);
					inSet.add(fin);
				}
			}
			if (i < outSize) {
				if (i == max-1 && rows == max) {
					// if # of rows has been truncated and I'm at the last row.
					// add a text item.
					OrderableText text = new OrderableText();
					labelNodes.addChild(text);
					text.setFont(Constants.NAME_FONT);
					text.setVisible(true);
					outSet.add(text);
				}
				else {
					paramOut = (ChainFormalOutputData) outputs.get(i);
					fout = getFormalOutput(paramOut);
					labelNodes.addChild(fout);
					outSet.add(fout);
				}
			}
		}
		
		
		
		// find horizontal starting point of the output parameters.
		// we want that to be the width -  the largest output,
		// with space for a margin left over.
		float outputColumnX = NAME_LABEL_OFFSET+ALIGNMENT_BUFFER;
					 		
		Collections.sort(inSet);
		Collections.sort(outSet);
	 	Object[] ins = inSet.toArray();
	 	Object[] outs = outSet.toArray();

	 	float paramHeight;
	 	if (inSet.size() > 0) 
	 		paramHeight = ((ParameterLabelNode) ins[0]).getLabelHeight();
	 	else if (outSet.size() >0 )
	 		paramHeight = ((ParameterLabelNode) outs[0]).getLabelHeight();
	 	else 
	 		paramHeight = 0;
	 	
	 	// add a bit of a buffer
	 	paramHeight += PARAMETER_SPACING;
		
		// place things at appropriate x,y.
		for (int i =0; i < rows; i++) {

			// get ith input 
			if (i <inSize) {
				inp = (ParameterLabelNode) ins[i];
				inp.setOffset(NAME_LABEL_OFFSET,height);
			}
			height += paramHeight;
			// get ith output
			if (i < outSize) {
				// we want to right-justify these. So, 
				// find difference bwtween the maximum output width
				// and the width of this one.
				outp = (ParameterLabelNode) outs[i];
				float rightJustifyGap = maxParamWidth-
					outp.getLabelWidth();
				// and then move right by that amount.
				float left = outputColumnX+rightJustifyGap;
				outp.setOffset(left,height);
			}
			height+=paramHeight;
		}
		// add a tiny bit to height to acconut for semantic type name of last
		// parameter
		height += LAST_ST_BUFFER;
		detail.addChild(labelNodes);
	}
	
	// max cardinality of ins and outs. null by default, but can over-ride
	protected Integer getMaxIOCardinality() {
		return null;
	}
	
	private void getParamWidth() {
		if (maxParamWidth == 0f) {
			ChainFormalInputData input = ChainFormalInputData.getLongestInput();
			ChainFormalOutputData output = ChainFormalOutputData.getLongestOutput();
			FormalInput inp = new FormalInput(this,input);
			FormalOutput outp = new FormalOutput(this,output);
			float inWidth = inp.getLabelWidth();
			float outWidth = outp.getLabelWidth();
			if (inWidth > outWidth)
				maxParamWidth = inWidth;
			else
				maxParamWidth = outWidth;
		}
	}
	
	protected List getFormalInputs() {
		return null;
	}
	
	protected List getFormalOutputs() {
		return null;
	}
	
	protected FormalInput getFormalInput(ChainFormalInputData paramIn) {
		return new FormalInput(this,paramIn);
	}
	
	protected FormalOutput getFormalOutput(ChainFormalOutputData paramOut) {
		return new FormalOutput(this,paramOut);
	}
	
	
	public void showDetails() {
		if (detail.getVisible()  != true) {
			if (detail.getParent() != this)
				addChild(detail);
			detail.setVisible(true);
			detail.setChildrenPickable(true);
			invalidateFullBounds();
			setBounds(detail.getBounds());
		}
		if (overview.getVisible()==true)
			overview.setVisible(false);
	}
	
	public void showOverview() {
		if (detail.getParent() == this && detail.getVisible() == true) {
			detail.setVisible(false);
			detail.setChildrenPickable(false);
		}
		overview.setVisible(true);
	    overview.setChildrenPickable(true);
		setBounds(overview.getBounds());
		
	}
	
		
	
	/** 
	 * Move module view to fron so it will get rendered on top of everything.
	 * To guarantee this, go all the way up the stack 
	 *
	 */
	protected void moveUp() {
		moveToFront();
		PNode n = getParent();
		while (n != null) {
			n.moveToFront();
			n = n.getParent();
		}
	}

	
	// can only link modules if the parameters are visible
	public boolean isLinkable() {
		return detail.getVisible();
	}
	
	/**
	 * Set the color of the module if it is highlighted.
	 * @param v true if the module is highlighted, else false
	 */
	public void setHighlighted(boolean v) {
		if (v == true) {
			overview.setStrokePaint(Constants.SINGLE_HIGHLIGHT_COLOR);
			detail.setStrokePaint(Constants.SINGLE_HIGHLIGHT_COLOR);
		}
		else {
			detail.setStrokePaint(null);
			overview.setStrokePaint(null); 
		}
		repaint();
	}
	
	/**
	 * Set the color indiciating that the module can be linked to from 
	 * the selected module
	 * @param v true if this module can be linked to from the current selection.
	 * 
	 */
	public void setLinkableHighlighted(boolean v) {
		if (v == true) {
			overview.setPaint(Constants.SELECTED_FILL);
			detail.setPaint(Constants.SELECTED_FILL);
		}
		else {
			overview.setPaint(Constants.DEFAULT_FILL);
			detail.setPaint(Constants.DEFAULT_FILL);
		}
		inputLinkTarget.setHighlighted(v);
		outputLinkTarget.setHighlighted(v);
		// do something with inputs and outputs.
		repaint();
	}
	
	public void setLinkTargetHighlighted(ModuleLinkTarget targ,boolean v) {
		Collection params;
		if (targ  == inputLinkTarget)
			params = getAllInputParameters();
		else //
			params = getOutputParameters();
			
		Iterator iter = params.iterator();
		FormalParameter p;
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			p.setParamsHighlighted(v);
		}
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
	}
	
	public void setModulesHighlighted(boolean v) {
		
	}
	
	/***
	 * Set the parameters associated with parameters of 
	 * this module to be highlighted.
	 * @param v true if the parameters should be highlighted, else false
	 */
	public void setParamsHighlighted(boolean v) {

		Iterator iter = labelNodes.getChildrenIterator();
		ParameterLabelNode p;
		
		while (iter.hasNext()) {
			p = (ParameterLabelNode) iter.next();
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
	
	
	//public double getWidth() {
	//	return noLabelRect.getBounds().getWidth();
	//}
	
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

	public ModuleLinkTarget getInputLinkTarget() {
		return inputLinkTarget;
	}
	
	public ModuleLinkTarget getOutputLinkTarget() {
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
	 * @return a sorted list of all of the input parameters that might be 
	 * link origins - those that both don't have incoming links and 
	 * can be link origins. These parameters are identified via
	 * 	a {@link PNodeFilter}
	 */
	public Set getInputParameters() {
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
	 * @return a sorted list of all of the input parameters that might be 
	 * don't have incoming linksThese parameters are identified via
	 * 	a {@link PNodeFilter}
	 */
	public Set getUnlinkedInputParameters() {
		PNodeFilter inputFilter = new PNodeFilter() {
			public boolean accept(PNode aNode) {
				// want only those things that are inputs.
				if (!(aNode instanceof FormalInput))
					return false;
				FormalInput inp = (FormalInput) aNode;
				// and can still be origins - don't have anything 
				// linked to them.
				return inp.isLinkable();
			}
			public boolean acceptChildrenOf(PNode aNode) {
				return true;
			}
		};
		return new TreeSet(labelNodes.getAllNodes(inputFilter,null));
	}

	protected void init() {
		
		//edu.umd.cs.piccolo.util.PDebug.debugFullBounds = true; 
		addChild(overview);
		detail.setVisible(false);
		overview.setPickable(false);
		detail.setPickable(false);
		
		//		 ok. so maxParamWidth is the max widht. 
		// width of whole thing will be maxParamWidth
		// + 2 namelabel offsets
		// + alignment buffer (so outputs go to right)
		getParamWidth();
		width = maxParamWidth+2*NAME_LABEL_OFFSET+ALIGNMENT_BUFFER;
		
		// create the name and position it.
		name = new PText(getName());
		name.setGreekThreshold(0);
		name.setFont(Constants.NAME_FONT);
		name.setPickable(false);
		//name.setScale(NAME_MAG);
	//	name.setTransparency(Constants.MODULE_TRANSPARENT);
		detail.addChild(name);
		
		// get height for largest name 
		height = getLargestNameHeight();
		name.setConstrainWidthToTextWidth(false);
		name.setBounds(new PBounds(NAME_LABEL_OFFSET,NAME_LABEL_OFFSET,
				width-2*NAME_LABEL_OFFSET,height));
		
		// calculate starting height for parameters.
		height = NAME_LABEL_OFFSET+ (float)name.getHeight();
	
		// build the node for the link targets
		linkTargets = new PNode();
		overview.addChild(linkTargets);	
		
		float linkTargetHeight = height;
		
		// add the input link target
		inputLinkTarget = getLinkTarget();
		linkTargets.addChild(inputLinkTarget);
		inputLinkTarget.setOffset(-inputLinkTarget.getHalfSize(),height);
		height+=NAME_SPACING;	
		
		buildMagnifiedLabel();
	
		// set width of the whole bounding rectangle

	    	noLabelHeight = height;
	 	// do the individual parameter labels.
		addParameterLabels();  
		

		RoundRectangle2D rect = 
			new RoundRectangle2D.Float(0f,0f,width,height,
					DEFAULT_ARC_WIDTH,DEFAULT_ARC_HEIGHT);
		detail.setPathTo(rect);
					
		overview.setPaint(Constants.DEFAULT_FILL);
		detail.setPaint(Constants.DEFAULT_FILL);
		overview.setStrokePaint(null);
		overview.setStroke(Constants.MODULE_STROKE);
		detail.setStrokePaint(null);
		detail.setStroke(Constants.MODULE_STROKE);
		
		// add the other target
		outputLinkTarget = getLinkTarget();
		linkTargets.addChild(outputLinkTarget);
		outputLinkTarget.setOffset(width-outputLinkTarget.getHalfSize(),
			linkTargetHeight);
	
		RoundRectangle2D noLabelRect = new RoundRectangle2D.Float(0f,0f, width,
				(float) (noLabelHeight+BOTTOM_GAP),
				DEFAULT_ARC_WIDTH,DEFAULT_ARC_HEIGHT);
		overview.setPathTo(noLabelRect);
		setBounds(overview.getBounds());
		invalidateFullBounds();
	}
	
	public float getBodyWidth() {
		return width;
	}
	
	private float getLargestNameHeight() {
		if (maxNameHeight == 0f) {
			name = new PText(getName());
			name.setFont(Constants.NAME_FONT);
			//name.setScale(NAME_MAG);
			name.setConstrainWidthToTextWidth(false);
			name.setWidth(width-2*NAME_LABEL_OFFSET);
			maxNameHeight = (float) name.getHeight();
		}
		return maxNameHeight;
	}
	/**
	 * 
	 * @return a sorted list of all of the output parameters. These parameters 
	 * are identified via a {@link PNodeFilter}
	 */
	public Set getOutputParameters() {
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
	
	/**
	 * 
	 * @return a sorted list of all of the input parameters. These parameters 
	 * are identified via a {@link PNodeFilter}
	 */
	public Set getAllInputParameters() {
		PNodeFilter inputFilter = new PNodeFilter() {
			public boolean accept(PNode aNode) {
				return (aNode instanceof FormalInput);
			}
			public boolean acceptChildrenOf(PNode aNode) {
				return true;
			}
		};
		return new TreeSet(labelNodes.getAllNodes(inputFilter,null));
	}
	
	
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
	
	public CategoryBox getCategoryBox() {
		BufferedObject b = getEnclosingBufferedNode();
		if (b instanceof CategoryBox)
			return (CategoryBox) b;
		else
			return null;
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		((ModuleNodeEventHandler) handler).animateToNode(this);
		((ModuleNodeEventHandler) handler).setLastEntered(this);
	}

	public void mouseDoubleClicked(GenericEventHandler handler,PInputEvent e) {
	}

	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		setAllHighlights(true);
		((ModuleNodeEventHandler) handler).setLastEntered(this);
		// make sure that the category box gets highlighted when
		// we enter the module
		// not necessary when we leave, because we might still be
		// in the same category box.
		CategoryBox cb = getCategoryBox();
		if (cb !=null)
			cb.mouseEntered(handler,e);
	}

	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
		setAllHighlights(false);
		((ModuleNodeEventHandler) handler).setLastEntered(null);
	}

	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		PNode p = getParent();
		if (p instanceof BufferedObject)  
			((ModuleNodeEventHandler) handler).animateToNode(p);	
	}
	
	public void setPickable(boolean b) {
		super.setPickable(b);
		super.setChildrenPickable(b);
	}
	
	/** 
	 *  will generally be overridden
	 */
	public ChainModuleData getModule() {
		return null;
	}
	
	public void remove() {
		
	}
	
	public PNode getToolTip() {
		String name = getName();
		//String desc = mod.getDescription();
		//only do a tool tip if we're showing the overview,
		// and we've got a name to show.
		if (detail.getVisible() == false && name.compareTo("") != 0) {
			PText pt = new PText(name);
			pt.setPickable(false);
			pt.setFont(Constants.TOOLTIP_FONT);
			PPath path  = new PPath();
			path.addChild(pt);
			pt.setOffset(0,0);
			pt.setFont(Constants.TOOLTIP_FONT);
			path.setBounds(path.getUnionOfChildrenBounds(null));
			path.setStrokePaint(Constants.TOOLTIP_BORDER_COLOR);
			path.setPaint(Constants.TOOLTIP_FILL_COLOR);
			path.setPickable(false);
			return path;
		}
		else
			return null;	
	}
	
	class OrderableText extends PText implements Comparable, ParameterLabelNode {
		
		public OrderableText() {
			super("More ...");
		}
		
		// alwyas comes after everything.
		public int compareTo(Object o) {
			return 1;
		}
		
		public float getLabelWidth() {
			return (float) getWidth();
		}
		
		public float getLabelHeight() {
			return (float) getHeight();
		}
		
		public void setOffset(float x,float y) {
			super.setOffset(x,y);
		}
		
		public void setParamsHighlighted(boolean v) {
			if (v == true) 
				setTextPaint(Constants.HIGHLIGHT_COLOR);
			else {
				setTextPaint(Constants.DEFAULT_TEXT_COLOR);
			}
		}
	}
}