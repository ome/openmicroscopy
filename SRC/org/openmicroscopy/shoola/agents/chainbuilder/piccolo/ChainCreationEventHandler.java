/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainCreationEventHandler
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.Timer;

//Third-party libraries
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.Link;



/** 
 * An event handler for the ChainCreationCanvas. This handler is responsible for 
 * (1) General panning of the scenegraph. (2) Dragging of the ModuleNodes
 * for positioning purposes. (3) Highlighting potential link targets upon
 * mouse over of a module input/output. (4) Click-drag interaction to support
 * linkage between inputs and outputs. (5) Zooming of the canvas. (6) Dragging 
 * internal points of a link, in order to change the actual path taken by the 
 * link.
 * 
 * In some cases we might prefer to do this via several small handlers. 
 * However, complexity and communication requirements make it better to do this 
 * all in one place. This is particularly true for goal (4), above, as 
 * a single handler allows us to easily communicate the identity of _both_ ends 
 * in a click-drag interaction.<p>
 * 
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */

public class ChainCreationEventHandler extends  PPanEventHandler 
	implements ActionListener {
	
	/**
	 * This handler can be viewed as a state machine, with differing
	 * behavior dependent on the current state. 
	 */
	/**
	 * The user is currently not in a state that involves creation of a link 
	 * between two sets of paramters or two sets of modules.
	 */
	private static final int NOT_LINKING=1;
	/**
	 * The user is creating a link between two parameters.
	 */
	private static final int LINKING_PARAMS=2;
	
	/**
	 * The user is creating links between all matching parameters on two 
	 * modules.
	 */
	private static final int LINKING_MODULES=3;
	
	/** 
	 * The user is cancelling a link creation that is in process.
	 * 
	 */
	private static final int LINKING_CANCELLATION=4;
	
	/**
	 * The user is modifying an internal point in a link.
	 */
	private static final int LINK_CHANGING_POINT=5;

	/**
	 * A link between two module inputs/outputs is being created.
	 * Semantically the same as links between all matching parameters 
	 *  (LINKING_MODULES), but occurs when things are zoomed out to show
	 * only the beween-module link targets, and not the formal parameter
	 * link targets
	 */
	private static final int LINKING_MODULE_TARGETS=6;
	/**
	 * An error message for multiple links 
	 */
	private static final String MULT_LINKS =
		"A Formal Input can only be connected to one Formal Output.";
	
	private static final String NO_CYCLES = 
		"Chains cannot contain cycles.";
	
	private static final String NO_INPUT_INPUT_LINKS =
		"Module inputs cannot be linked to each other.";
	
	private static final String NO_OUTPUT_OUTPUT_LINKS =
		"Module outputs cannot be linked to each other.";
	
	private static final String NO_SELF_MODULE_LINKS = 
		"A Module cannot be linked to itself.";
	
	private static final String NO_MULTIPLE_MODULE_LINKS = 
		"A link between these modules already exists.";
	
	private static final String TYPE_MISMATCH_ERROR = 
		"Two parameters can only be linked if they have the same Semantic Type.";
	
	private static final String NO_LEGAL_MODULE_LINKS =
		"There are no parameters in these modules that can be linked.";
	
	/**
	 * The distance between links when multiple links between two modules
	 * are created.
	 */
	private static final int SPACING=6;
	
	/**
	 * Initially, the user is NOT_LINKING
	 */
	private int linkState = NOT_LINKING;
	
	/**
	 * The last parameter node and last module node that we entered
	 */
	private FormalParameter lastParameterEntered;
	
	/**
	 * The {@link LinkSelectionTarget} that was selected to start the 
	 * process of modifying an internal point of a {@link PLink}
	 */
	private LinkSelectionTarget selectionTarget;
	
	/** 
	 * The {@link PLayer} holding the links.
	 */
	private LinkLayer linkLayer;
	
	
	/**
	 * The origin of a link that is being created
	 */
	private FormalParameter linkOrigin;
	
	/**
	 * The link that the user just clicked on to select
	 */ 
	private Link selectedLink = null;
	
	/**
	 * The link that is currently being created
	 */
	private ParamLink link;
	
	/**
	 * When multiple links are being created, a list of the links in progress
	 */
	private Collection links = new Vector();
	
	/**
	 * The currently selected module
	 */
	private ModuleView selectedModule;
	
	/**
	 * The parameters that are part of a link between modules.
	 */
	private Collection activeModuleLinkParams;
	
	/**
	 * True if linkage betwen modules started from the formal inputs from one
	 * of the modules.
	 */
	private boolean moduleLinksStartedAsInputs = false;
	
	/**
	 * A filter for the mouse events of interest
	 */
	protected int allButtonMask = MouseEvent.BUTTON1_MASK |
					MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK;
	
	/**
	 * The {@link PCanvas} of interest
	 */
	private ChainCreationCanvas canvas;
	
	/**
	 * This flag is set to be true immediately after a popup menu event, and
	 * is cleared immediately after that
	 */
	private boolean postPopup= false;
	
	private final Timer timer = new Timer(300,this);
	
	private PInputEvent cachedEvent;
	
	private boolean wasDoubleClick;
	
	private boolean postLinkCompletion = false;
	
	private ModuleLinkTarget moduleLinkOriginTarget = null;
	
	private ModuleLink moduleLink;
	
	public ChainCreationEventHandler(ChainCreationCanvas canvas,LinkLayer linkLayer) {
		super();
		
		this.canvas = canvas;
		this.linkLayer = linkLayer;
		PInputEventFilter filter =getEventFilter();
		filter.acceptEverything();
		setEventFilter(filter);
		canvas.getRoot().getDefaultInputManager().
			setKeyboardFocus(this);
		setAutopan(false);		
	}
	
	/**
	 * There are three cases for handling drag events:
	 * 1. If the drag event occurs on a module node, translate it. 
	 * 2. If we are modifying a link, and the drag event is on a {@link
	 *     LinkSelectionTarget}, translate that target
	 * 3. If we're not on a {@link FormalParameter}, call super.drag() and 
	 * 	   set the event to be handled.
	 * Otherwise, pass on the event.
	 */
	protected void drag(PInputEvent e) {
		PNode node = e.getPickedNode();
		
		if (node instanceof ModuleView) {
			if (linkState != LINKING_MODULES) {
				ModuleView mod = (ModuleView) node;
				Dimension2D delta = e.getDeltaRelativeTo(node);
				mod.translate(delta.getWidth(),delta.getHeight());
				e.setHandled(true);
			}
		}
		else if (linkState == LINK_CHANGING_POINT) {
			Dimension2D delta = e.getDeltaRelativeTo(node);
			if (node instanceof LinkSelectionTarget) {
				LinkSelectionTarget target = (LinkSelectionTarget) node;
				target.translate(delta.getWidth(),delta.getHeight());
			}
			e.setHandled(true);
		}
		else if (!(node instanceof FormalParameter) 
			&& linkState == NOT_LINKING){
			super.drag(e);
			e.setHandled(true);
		}
		setModulesDisplayMode();
		
	}
	
	/**
	 * If we've entered a parameter node, store it as the 
	 * "lastParameterEntered", to track the last parameter that the mouse 
	 * was in. If the handler's state is NOT_LINKING, this is a generic mouse
	 * over, so turn off all of the highlights for the module for that paraemter
	 * (this is needed to avoid leaving anything on from previous state),
	 * and turn on the highlighting for this parameter.
	 * 
	 * If we've entered a module andstate is NOT_LINKING, set the highlights
	 * for the module to be true
	 * 
	 * In general, we leave highlighting on while linking, in order to maintain
	 * the cues that the highlighting provides.
	 * 
	 * Otherwise, call the superclas shandler. 
	 
	 * 
	 */
	public void mouseEntered(PInputEvent e) {
		PNode node = e.getPickedNode();
		if (node instanceof FormalParameter) {
			lastParameterEntered = (FormalParameter) node;
			ChainBuilderAgent.debug("mouse entered last entered.."+node,3);
			if (linkState == NOT_LINKING) {
				// turn on params for this parameter 
				lastParameterEntered.setParamsHighlighted(true);
				// set all modules of this same type to be highlighted.
				ModuleView mod = lastParameterEntered.getModuleView();
				mod.setModulesHighlighted(true);	
			}
			e.setHandled(true);
		}
		else if (node instanceof ModuleLinkTarget && linkState == NOT_LINKING) {
			((ModuleLinkTarget) node).setParametersHighlighted(true);
			e.setHandled(true);
		}
		else if (node instanceof SingleModuleView && linkState == NOT_LINKING) {
			SingleModuleView mod = (SingleModuleView) node;
			mod.setAllHighlights(true);
			e.setHandled(true);
		}
		else if (node instanceof PCamera && 
				lastParameterEntered != null) {
				lastParameterEntered.setParamsHighlighted(false);
				ModuleView mod = lastParameterEntered.getModuleView();
				mod.setAllHighlights(false);
		}
		else
			super.mouseEntered(e);
	}
	
	/**
	 * When we leave a node, we clear "lastParameterEntered" and 
	 * turn off any highlighting.
	 * 
	 * As always, leave highlighting on if a link is being created. 
	 */
	public void mouseExited(PInputEvent e) {
		PNode node = e.getPickedNode();
		lastParameterEntered = null;
		ChainBuilderAgent.debug("last parameter entered cleared",3);
		if (node instanceof FormalParameter) {
			FormalParameter param = (FormalParameter) node;
			if (linkState == NOT_LINKING) {	
				param.setParamsHighlighted(false);
				ModuleView mod = param.getModuleView();
				mod.setAllHighlights(false);
			}			
			e.setHandled(true);
		}
		else if (node instanceof ModuleLinkTarget) {
			((ModuleLinkTarget) node).setParametersHighlighted(false);
			e.setHandled(true);
		}

		else if (node instanceof SingleModuleView) {
			SingleModuleView mod = (SingleModuleView) node;
			mod.setAllHighlights(false);
			e.setHandled(true);
		}
		else
			super.mouseExited(e);
	}
	
	
	/**
	 * mouseDragged() behavior is equivalent to mouseMoved() behavior.
	 */
	public void mouseDragged(PInputEvent e) {
		ChainBuilderAgent.debug("CHAIN HANDLER:got a drag event in chain canvas",3);
		ChainBuilderAgent.debug("mouse dragged..."+e.getPickedNode(),3);
		mouseMoved(e);
		super.mouseDragged(e);
	}
	
	/**
	 * Two behaviors if the mouse is moved:
	 * 1) if the state is linking parameters, add a point to the link in progress
	 * 2) if the state is LINKING_MODULES, add a point to all of the links that 
	 * 		are in progress.
	 */
	public void mouseMoved(PInputEvent e) {
		Point2D pos = e.getPosition();
		if (linkState == LINKING_PARAMS) {
			link.setEndCoords((float) pos.getX(),(float) pos.getY());
		}
		else if (linkState == LINKING_MODULES) {
			Iterator iter = links.iterator();
			ParamLink lnk;
			while (iter.hasNext()) {
				lnk = (ParamLink) iter.next();
				lnk.setEndCoords((float) pos.getX(),(float) pos.getY());
			}
		}
		else if (linkState == LINKING_MODULE_TARGETS) {
			ChainBuilderAgent.debug("setting end of module link..",3);
			moduleLink.setEndCoords((float) pos.getX(),(float) pos.getY());
		}

	}
	
	
	public void actionPerformed(ActionEvent e) {
		ChainBuilderAgent.debug("mouse single click",3);
		if (cachedEvent != null) 
			doMouseClicked(cachedEvent);
		cachedEvent = null;
		timer.stop();
	}
	
	public void mouseClicked(PInputEvent e) {
		ChainBuilderAgent.debug("got a mouse click",3);
		if (timer.isRunning()) {
			ChainBuilderAgent.debug("mouse double click",3);
			// this is effectively a double click.
			timer.stop();
			if (wasDoubleClick == true)
				wasDoubleClick  = false;
			else
				doMouseDoubleClicked(e);
		}
		else {
			timer.restart();
			ChainBuilderAgent.debug("caching click event .."+e,3);
			cachedEvent = e;
		}
	}
	
	/**
	 * Cases for mouse clicks:
	 * 
	 * 1) if the state is somethiung other than NOT_LINKING, ignore the event,
	 *  	possibly resetting state to NOT_LINKING
	 * 2) If {@link postPopup} is true, this click is the "residue" of a popup 
	 * 		mouse click. In this case, clear the flag and ignore the event.
	 * 3) Otherwise, zoom in one of three ways:
	 * 	a) If the shift key is down, center the canvas contents in the window
	 *  b) If the control key is down, or it's a right click, zoom out
	 *  c) Otherwise zoom in.
	 */
	public void doMouseClicked(PInputEvent e) {
		if (postLinkCompletion == true)
			return;
		// we only scale if we're not drawing a link.
		ChainBuilderAgent.debug("mouse clicked.."+e,3);
		ChainBuilderAgent.debug("link state is "+linkState,3);
		if (linkState != NOT_LINKING) {
			if (linkState == LINKING_CANCELLATION) {
				ChainBuilderAgent.debug("cancelled link. set to not linking",3);
				linkState = NOT_LINKING;
			}
			ChainBuilderAgent.debug("linking...",3);
			e.setHandled(true);
			return;
		}
		
		if (postPopup == true) {
			ChainBuilderAgent.debug("post popup",3);
			postPopup = false;
			e.setHandled(true);
			return;
		}
		
		PNode node = e.getPickedNode();
		ChainBuilderAgent.debug("mouse clicked on .."+node,3);
		int mask = e.getModifiers() & allButtonMask;
		
		// if it's a buffered object that is not a chain.
		if (node instanceof BufferedObject && !(node instanceof ChainView)) {
			BufferedObject mod = (BufferedObject) node;
			PCamera camera = canvas.getCamera();
			if (mask == MouseEvent.BUTTON1_MASK && e.getClickCount()==1) {
				ChainBuilderAgent.debug("zooming in on node..."+node,3);
				ChainBuilderAgent.debug("camera view scale was "+camera.getViewScale(),3);
				PBounds b = mod.getBufferedBounds();
				PActivity activity = camera.animateViewToCenterBounds(b,true,
							Constants.ANIMATION_DELAY);
				activity.setDelegate(new PActivityDelegate() {
					public void activityStarted(PActivity activity) {				
					}
					public void activityStepped(PActivity activity) {				
					}
					public void activityFinished(PActivity activity) {
						setModulesDisplayMode();
					}
				});
				
			}
			else if (e.isControlDown() || (mask & MouseEvent.BUTTON3_MASK)==1) {
				ChainBuilderAgent.debug("canvas right click..",3);
				evaluatePopup(e);					
			}
		}
		
		// otherwise, must be a camera, or a chain. either way, zoom in/out
		//if (! (node instanceof PCamera))
		//	return;
		else if (e.isShiftDown()) {
			ChainBuilderAgent.debug("zoomed with shhift..",3);
			PBounds b = canvas.getBufferedBounds();
			canvas.getCamera().animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);
			setModulesDisplayMode();
			e.setHandled(true);
		}
		else {
			ChainBuilderAgent.debug("zoomed witout shift...",3);
			double scaleFactor = Constants.SCALE_FACTOR;
			if (e.isControlDown() || ((mask & MouseEvent.BUTTON3_MASK)==1)) {
				//System.err.println("but wit control...");
				scaleFactor = 1/scaleFactor;
			}
			zoom(scaleFactor,e);
			e.setHandled(true);
		}  
	} 
	
	
	private void doMouseDoubleClicked(PInputEvent e) {
		PNode node = e.getPickedNode();
		ChainBuilderAgent.debug("got a double click on "+node,3);
		if (node instanceof ModuleView){
			selectedModule = (ModuleView) node;
			// only start links if it's linkable
			if (selectedModule.isLinkable())
				startModuleLinks(e);
		}
		else if (node instanceof FormalParameter) {
			selectedModule = ((FormalParameter) node).getModuleView();
			if (selectedModule.isLinkable())
				startModuleLinks(e);
		}		
		e.setHandled(true);
	}
	
	
	/***
	 * Adjust the magnification around the point of the {@link PInputEvent}.
	 * @param scale how much to zoom
	 * @param e the event leading to the zoom
	 */
	private void zoom(double scale,PInputEvent e) {
		PCamera camera=canvas.getCamera();
		double curScale = camera.getScale();
		curScale *= scale;
		Point2D pos = e.getPosition();
		camera.scaleViewAboutPoint(curScale,pos.getX(),pos.getY());
		setModulesDisplayMode();
		e.setHandled(true);
	}
	
	/****
	 * set the modules to make sure that they are showing detail or overview,
	 * according to scale.
	 */
	public void setModulesDisplayMode() {
		Collection mods = canvas.findModules();
		Iterator iter = mods.iterator();
		ModuleView mod; 
		PCamera camera=canvas.getCamera();
		double curScale = camera.getViewScale();
		boolean showingOverview = false;
		if (curScale< Constants.SCALE_THRESHOLD) {
			showingOverview = true;
		}
		ChainBuilderAgent.debug("current scale is "+curScale,3);
		ChainBuilderAgent.debug("showing overview..."+showingOverview,3);
		iter = mods.iterator(); 
		while (iter.hasNext()) {
			mod = (ModuleView)iter.next();
			if (showingOverview == true) {
				mod.showOverview();
			}
			else {
				mod.showDetails();
			}
		}
		if (showingOverview == true) 
			canvas.showModuleLinks();
		else 
			canvas.showParamLinks();
	}
	
	
	
	/***
	 * Several cases of what to do when we have a mouse pressed event
	 * 1) If it's a popup event, handle it and return
	 * 2) Clear out selectedLink, selectedModule, and selctionTarget if they
	 * 	are not null.
	 * 3) Call special purpose handlers based on the type of node set
	 * 4) Call handlers based on the current state. 
	 * 
	 * Thus, in the general case, two handlers may be callsed - one for the 
	 * type of node, and another for the current state
	 */
	public void mousePressed(PInputEvent e) {
		
		postLinkCompletion = false;
		ChainBuilderAgent.debug("mouse pressed on "+e.getPickedNode(),3);
		if (wasDoubleClick == true) {
			ChainBuilderAgent.debug("just came from double click..",3);
			wasDoubleClick = false;
			return;
		}
		
		if (e.isPopupTrigger()) {
			ChainBuilderAgent.debug("mouse pressed..",3);
			evaluatePopup(e);
			return;
		}
		
		super.mousePressed(e);
		PNode node = e.getPickedNode();		
		// clear off what was selected.
		if (selectedLink != null && linkState != LINK_CHANGING_POINT) {
			ChainBuilderAgent.debug("setting selected link to not be selected, in mousePressed",3);
			selectedLink.setSelected(false);
			selectedLink = null;
		}
		if (selectedModule != null && linkState != LINKING_MODULES) {
			selectedModule.removeHandles();
			selectedModule = null;
		}
		if (selectionTarget != null) {
			selectionTarget.getLink().setSelected(false);
			selectionTarget = null;
		}
		
		//first do things based on types of nodes
		// then do based on state
		if (node instanceof LinkSelectionTarget)
			mousePressedSelectionTarget(node);
		else if (node instanceof Link)
			mousePressedLink(node);
		else if (node instanceof ModuleView)
			mousePressedModule(node);

		if (linkState == LINKING_PARAMS)
			mousePressedLinkingParams(e);
		else if (linkState == LINKING_MODULES)
			mousePressedLinkingModules(node,e);
		else if (linkState == NOT_LINKING) 
			mousePressedNotLinking(node,e);		
		else if (linkState == LINK_CHANGING_POINT)
			mousePressedChangingPoint(node,e);
		else if (linkState == LINKING_MODULE_TARGETS)
			mousePressedLinkingModuleTargets(e);
		else {
			ChainBuilderAgent.debug("mouse pressed..setting link state to not linking..",3);
			linkState = NOT_LINKING;
		}
	}
	
	/**
	 * If the press is on a link, and I'm not already in the process of 
	 * changing a link, make this link the newly selected link
	 * @param node
	 */
	private void mousePressedLink(PNode node) {
		if (linkState != LINK_CHANGING_POINT) {
			selectedLink = (Link) node;
			selectedLink.setSelected(true);
			ChainBuilderAgent.debug("mouse pressed on a link when not changing. link state is NOT_LINKING",3);
			linkState = NOT_LINKING;
		}
	}


	/**
	 * If the mouse event is on a module, set it to be selected and add
	 * handles.
	 * @param node
	 */
	private void mousePressedModule(PNode node) {
		selectedModule = (ModuleView) node;
		selectedModule.addHandles();
	}
	
	/**
	 * If the press is on a {@link LinkSelectionTarget}, set the selection
	 * target and set the associated link to be selected.
	 * @param node the node that was pressed
	 */
	private void mousePressedSelectionTarget(PNode node) {
		ChainBuilderAgent.debug("pressing on selection target..",3);
		ChainBuilderAgent.debug("mousePressedSelectionTarget.... setting link state to changing point",3);
		selectionTarget = (LinkSelectionTarget) node;
		selectionTarget.getLink().setSelected(true);
		linkState = LINK_CHANGING_POINT;
	}
	
	/** 
	 * If the mouse was pressed while parameters were being linked, there are 
	 * three possibilities:
	 * 	1) if the press was  double click, the link should be cancelled.
	 *  2) if the mouse press occurred on the canvas, add a point to the link.
	 *  3) If the mouse is in a formal parameters, finish the link.
	 * @param node the link that for the pressed event
	 * @param e the pressed event
	 */
	private void mousePressedLinkingParams(PInputEvent e) {
		//System.err.println("mouse pressed linking params "+e);
		
		if (e.getClickCount() ==2) {
			cancelParamLink();
			ChainBuilderAgent.debug("mouse pressed linking params. setting to linking cancellation",3);
			linkState = LINKING_CANCELLATION;
			postLinkCompletion = true;
		}
		else if (lastParameterEntered == null) { // we're on canvas.
			Point2D pos = e.getPosition();
			link.setIntermediatePoint((float) pos.getX(),(float) pos.getY());
		}
		else if (lastParameterEntered != null) {
			finishParamLink();
			postLinkCompletion = true;
		}
		e.setHandled(true);
	}

	/** 
	 * If the mouse was pressed while parameters were being linked, there are 
	 * three possibilities:
	 * 	1) if the press was  double click, the link should be cancelled.
	 *  2) if the mouse press occurred on the canvas, add a point to the link.
	 *  3) If the mouse is in a formal parameters, finish the link.
	 * @param node the link that for the pressed event
	 * @param e the pressed event
	 */
	private void mousePressedLinkingModuleTargets(PInputEvent e) {
		ChainBuilderAgent.debug("mouse pressed linking module targets "+e,3);
		PNode n = e.getPickedNode();
		if (e.getClickCount() ==2) {
			cancelModuleTargetLink();
			ChainBuilderAgent.debug(
				"mouse pressed linking params. setting to linking cancellation",3);
			linkState = LINKING_CANCELLATION;
			postLinkCompletion = true;
		}
		else if (n instanceof ModuleView) {
			finishModuleTargetLink((ModuleView) n);
			postLinkCompletion = true;
		}
		else if (!(n instanceof ModuleLinkTarget)) { // we're on canvas.
			Point2D pos = e.getPosition();
			moduleLink.setIntermediatePoint((float) pos.getX(),(float) pos.getY());
		}
		else if (moduleLinkOriginTarget != null) {
			finishModuleTargetLink((ModuleLinkTarget) n);
			postLinkCompletion = true;
		}
		e.setHandled(true);
	}
	/**
	 * If the mouse is presed while modules are being linked,
	 * Start by checking the number of clicks. If there are two, 
	 * finish the links if the target node is either a formal parameter or a 
	 * module.
	 * Otherwise, if the click is on the {@link PCamera}, add a point to each
	 * of the links that are in progress.
	 * 
	 * @param node the target of the mouse press
	 * @param e the mouse press event
	 */
	private void mousePressedLinkingModules(PNode node,PInputEvent e) {
		int count = e.getClickCount();
		ChainBuilderAgent.debug("caling mouse pressed linking modules..",3);
		ChainBuilderAgent.debug("node is "+node,3);
		
		if (count ==2) {
			ChainBuilderAgent.debug("2 presses. node is "+node,3);
			cancelModuleLinks();
		}
		else	 if (node instanceof FormalParameter) {
			FormalParameter p = (FormalParameter) node;
			ModuleView mod = p.getModuleView();
			finishModuleLinks(mod);
		}
		else if (node instanceof ModuleView) {
			finishModuleLinks((ModuleView) node);
		}
		else if (node instanceof PCamera){ // single click on camera
			
			Iterator iter = links.iterator();
			Point2D pos = e.getPosition();
			ParamLink lnk;
			int size = links.size();
			float y = ((float) pos.getY()) - size/2*SPACING;
			while (iter.hasNext()) {
				lnk = (ParamLink) iter.next();
				lnk.setIntermediatePoint((float) pos.getX(),y);
				y += SPACING;
			}
		}
		postLinkCompletion = true;
		e.setHandled(true);
	}
	
	/**
	 * If the mouse is pressed while the state is NOT_LINKING, adjust 
	 * the highlights if needed, and start a link if appropriate. 
	 * If the target is a module and the user double-clicked, start module 
	 * links. These are the links that go between all possible inputs and 
	 * outputs for a pair of modules.
	 * 
	 * 
	 * Otherwise, if the target is a {@link LinkSelectionTarget}, set 
	 * the state to be LINK_CHANGING_POINT.
	 * 
	 * @param node
	 * @param e
	 */
	private void mousePressedNotLinking(PNode node,PInputEvent e) {
		ChainBuilderAgent.debug("got mouse presssed not linking..",3);
		if (node instanceof FormalParameter) {
			if (lastParameterEntered == null) 
				mouseEntered(e);
			FormalParameter param = (FormalParameter) node;
			ChainBuilderAgent.debug(
				"starting a link from .."+param.getParameter().getName(),3);
			if (param.canBeLinkOrigin())
				startParamLink(param);
			else
				canvas.setStatusLabel(MULT_LINKS);
		}
		else if (node instanceof ModuleLinkTarget) {
			ChainBuilderAgent.debug("pressing on module link target...",3);
			ModuleLinkTarget modLink = (ModuleLinkTarget) node;
			startModuleTargetLink(modLink);
		}
		else if (node instanceof LinkSelectionTarget) {
			ChainBuilderAgent.debug(
					"mouse pressed not linking. setting to link changing point",3);
			selectionTarget  = (LinkSelectionTarget) node;

			linkState = LINK_CHANGING_POINT;
		}
		e.setHandled(true);
	}
	
	/**
	 * If the mouse is pressed while the point is being changed, clear the 
	 * currently selected link if appropriate.
	 *  
	 * @param node
	 * @param e
	 */
	private void mousePressedChangingPoint(PNode node,PInputEvent e) {
		if (node instanceof PCamera)  {
			ChainBuilderAgent.debug(
					"clearing link selection target.. not_LINKING.",3);
			linkState = NOT_LINKING;
			if (selectionTarget != null) {
				Link link = selectionTarget.getLink();
				if (link != null)
					link.setSelected(false);
			}
		} 
		e.setHandled(true);
	}
	
	/**
	 * Start a link between two modules based on the module link target 
	 * - the target shown when zoomed out or compact view shown
	 * 
	 * @param modLink
	 */
	private void startModuleTargetLink(ModuleLinkTarget modLink) {
		
		moduleLinkOriginTarget = modLink;
		ChainBuilderAgent.debug(
			"starting mmodule target link..."+modLink+
			", link state is LINKING_MODULE_TARGETS",3);
		moduleLink = new ModuleLink(linkLayer,moduleLinkOriginTarget);
		moduleLink.setPickable(false);
		linkState = LINKING_MODULE_TARGETS;
	}
	
	private void finishModuleTargetLink(ModuleLinkTarget n) {
		//first, if input and output are the same, barf.
		if (n.getModuleView() == moduleLinkOriginTarget.getModuleView()) {
			showSelfLinkError();
			cancelModuleTargetLink();
			return;
		}
		else if (n.isInputLinkTarget() && 
					moduleLinkOriginTarget.isInputLinkTarget()) {
			canvas.setStatusLabel(NO_INPUT_INPUT_LINKS);
			cancelModuleTargetLink();
			return;
		}
		else if (n.isOutputLinkTarget() && 
					moduleLinkOriginTarget.isOutputLinkTarget()) {
		    canvas.setStatusLabel(NO_OUTPUT_OUTPUT_LINKS);
			cancelModuleTargetLink();
		    return;
		}
		else if (linkLayer.
				findModuleLink(n.getModuleView(),
						moduleLinkOriginTarget.getModuleView()) != null) {
			canvas.setStatusLabel(NO_MULTIPLE_MODULE_LINKS);
			cancelModuleTargetLink();
			return;
		}
		ChainBuilderAgent.debug("finishing module target link",3);
		
		// ok. now, create links and make sure we have no cycles.
		// get inputs & get outputs
		Collection  params1 = moduleLinkOriginTarget.getParameters();
		Collection params2 = n.getParameters();

		// set other end point of link.
		
		moduleLink.setTarget(n);
		if (finishModuleTargetLink(params1,params2) == false) {
			showNoLegalLinksBetwenModulesError();
			cancelModuleTargetLink();
			return;
		}		
		if (foundCycle() == true) {
			canvas.setStatusLabel(NO_CYCLES);
			cancelModuleTargetLink();
		}
		else {
			moduleLink.setPickable(true);
			cleanUpModuleTargetLink();
		}
		moduleLinkOriginTarget = null;
		ChainBuilderAgent.debug("ending finishModuleTargetLink ModuleLinkTarget. NOT_LINKING",3);
		linkState = NOT_LINKING;
	}
	
	
	private void finishModuleTargetLink(ModuleView mod) {
		//first, if input and output are the same, barf.
		if (mod == moduleLinkOriginTarget.getModuleView()) {
			showSelfLinkError();
			cancelModuleTargetLink();
			return;
		}
		else if (linkLayer.
				findModuleLink(mod,moduleLinkOriginTarget.getModuleView()) != null) {
			canvas.setStatusLabel(NO_MULTIPLE_MODULE_LINKS);
			cancelModuleTargetLink();
			return;
		}
		ChainBuilderAgent.debug("finishing module target link",3);
		// ok. now, create links and make sure we have no cycles.
		// get inputs & get outputs
		Collection  params1 = moduleLinkOriginTarget.getParameters();
		Collection params2;
		// get the paramters that the module link origin target can link to
		// and the target in the module
		if (moduleLinkOriginTarget.isInputLinkTarget()) {
			params2 = mod.getOutputParameters();
			//params2 and set target are the outputs of mod
			moduleLink.setTarget(mod.getOutputLinkTarget());
		}
		else {
			// params2 and set target are the inputs of mod
			params2 = mod.getUnlinkedInputParameters();
			moduleLink.setTarget(mod.getInputLinkTarget());
		}

		
		
		if (finishModuleTargetLink(params1,params2) == false) {
			showNoLegalLinksBetwenModulesError();
			cancelModuleTargetLink();
			return;
		}		
		if (foundCycle() == true) {
			canvas.setStatusLabel(NO_CYCLES);
			cancelModuleTargetLink();
		}
		else {
			moduleLink.setPickable(true);
			cleanUpModuleTargetLink();
		}
		moduleLinkOriginTarget = null;
		ChainBuilderAgent.debug(
				"ending finsihModuleTargetLink ModuleView. NOT_LINKING",3);
		
		linkState = NOT_LINKING;
	}
	
	private boolean finishModuleTargetLink(Collection params1,Collection params2) {
		// foreach thing in params1, do something for everything in params2
		Iterator iter = params1.iterator();
		boolean res = false;
		while (iter.hasNext()) {
			FormalParameter p = (FormalParameter) iter.next();
			// if it's an input with something coming in, don't do this link.
			if (p instanceof FormalInput && !p.isLinkable())
				continue;
			if (finishAModuleTargetLink(p,params2) == true)
				res = true;
		}
		return res;
	}
	
	private boolean finishAModuleTargetLink(FormalParameter p,Collection params2) {
		Iterator iter = params2.iterator();
		while (iter.hasNext()) {
			FormalParameter p2 = (FormalParameter) iter.next();
			if (p2 instanceof FormalInput && ! p2.isLinkable())
				continue;
			if (p2.getSemanticType() == p.getSemanticType()) {
	
				// create the link
				ParamLink link = new ParamLink();
				linkLayer.addChild(link);
				link.setStartParam(p);
				link.setEndParam(p2);
				return true;
			}
		}
		return false;
	}
	private void cleanUpModuleTargetLink() {
		moduleLinkOriginTarget= null;
	}
	
	private void cancelModuleTargetLink() {
		moduleLink.remove();
		moduleLink =null;
		cleanUpModuleTargetLink();
		ChainBuilderAgent.debug("cancel up module target link NOT_LINKING",3);
		linkState = NOT_LINKING;
	}
	
	/***
	 * Start a new link between parameters
	 * @param param the origin of the new link
	 */
 	private void startParamLink(FormalParameter param) {
 		ChainBuilderAgent.debug("mouse pressing and starting link",3);
 		ChainBuilderAgent.debug("start param link. link state is LINKING_PARAMS",3);
		linkOrigin = param;
		link = new ParamLink();
		linkLayer.addChild(link);
		link.setStartParam(linkOrigin);
		link.setPickable(false);
		linkState = LINKING_PARAMS;			
 	}
		
	/**
	 * End the link curently in process at the link denoted by
	 * {@link lastParameterEntered}.
	 *
	 */
	private void finishParamLink() {
		if (lastParameterEntered.getModuleView() == linkOrigin.getModuleView()) {
			showSelfLinkError();
			cancelParamLink();
		}
		else if (lastParameterEntered.getClass() == linkOrigin.getClass()) {
			showParameterInputOutputConflictError(linkOrigin);
			cancelParamLink();
		}
		else if (lastParameterEntered.isLinkable() == true) {
			
			if (linkOrigin.sameTypeAs(lastParameterEntered) ==false) {
				showParamterTypeMisMatchError();
				cancelParamLink();
			}
			else {
				ChainBuilderAgent.debug("finishing link",3);
				link.setEndParam(lastParameterEntered);
				if (foundCycle() ==false) {
					link.setPickable(true);
					// 	add the {@link ModuleViewLink} between the modules
					linkLayer.completeLink(link);
					cleanUParamLink();
				}
				else {
					canvas.setStatusLabel(NO_CYCLES);
					cancelParamLink();
				}
			}
		}
		else {
			canvas.setStatusLabel(MULT_LINKS);
			cancelParamLink();
		}
		ChainBuilderAgent.debug("finishParamLink. state is NOT_LINKING",3);
		linkState = NOT_LINKING;
	}
	
	/**
	 * Check for a cycle and let us know if we've found it.
	 *
	 * Do this by checking modules views and param links 
	 * and progressively removing modules that have no inputs,
	 * and all corresponding outputs,
	 * until nothing left (ok) or we have none with no inputs - cycles
	 */
	
	private boolean foundCycle() {
		Vector modules = new Vector(canvas.findModules());
		Vector links = new Vector(canvas.findLinks());
		
		ListIterator iter;
	    boolean foundOne = false;
	    while (modules.size() > 0) {
	    		foundOne = false;
		    iter = modules.listIterator();
		    	while (iter.hasNext()) {
		    		ModuleView module = (ModuleView) iter.next();
		    		boolean inLinks = hasInLinks(module,links);
		    		if (inLinks == false) {
		    			iter.remove();	
		    			Vector outLinks = getOutLinks(module,links);
		    			if (outLinks != null) {
		    				links.removeAll(outLinks);
		    			}
		    			foundOne = true;
		    			break;
		    		}
		    	}
		
			// made it through list
			if (foundOne == false) {
				return true;
			}
	    } 
	    	return false;
	}
			    	
	

	private boolean hasInLinks(ModuleView module,Collection links) {
		Iterator iter = links.iterator();
		ParamLink link;
		while (iter.hasNext()) {
			link = (ParamLink) iter.next();
			if ( link.getInput() != null &&
					link.getInput().getModuleView() == module)
				return true;
		}
		return false;
	}
	
	private Vector getOutLinks(ModuleView module,Collection links) {
		Iterator iter = links.iterator();
		ParamLink link;
		Vector res = null;
		while (iter.hasNext()) {
			link = (ParamLink) iter.next();
			if (link.getOutput() != null &&
				link.getOutput().getModuleView() == module) {
				if (res == null)
					res = new Vector();
				res.add(link);
			}
		}
		return res;	
	}

	
	/**
	 * Cancel a lnk between parameters
	 *
	 */
	private void cancelParamLink() {
		ChainBuilderAgent.debug("canceling link",3);
		if (link != null)
			link.remove();
		link =null;
		cleanUParamLink();
	}
	
	/***
	 * Final cleanup of a parameter link. Called after {@link finishParamLink()}
	 * and {@link cancelParamLink()}
	 * 
	 *
	 */
	private void cleanUParamLink() {
		linkOrigin.setParamsHighlighted(false);
		linkOrigin = null;
		lastParameterEntered = null;
	}
	
	/**
	 * Start all of the links between two modules. Identify which side the event
	 * was on (input and output), get the list of parameters associated with
	 * the start event, set moduleLinksStartedAsInputs to be true if appropriate,
	 * adn call {@link startModuleLinks}
 	 * @param e the mouse event that starts the links.
	 */
	private void startModuleLinks(PInputEvent e) {

		if (selectedModule == null)
			return;
		Point2D pos = e.getPosition();
		boolean isInput = selectedModule.isOnInputSide(pos);
		Collection inputs = selectedModule.getInputParameters();
		Collection outputs = selectedModule.getOutputParameters();
		if (isInput == true  || outputs.size() == 0) {
			ChainBuilderAgent.debug("building module links on input side",3);
			startModuleLinks(inputs);
			moduleLinksStartedAsInputs = true;
		}
		else { 
			ChainBuilderAgent.debug("building module links on output side",3);
			startModuleLinks(outputs); 
			moduleLinksStartedAsInputs = false;
		}
		ChainBuilderAgent.debug("start module links. link state is LINKING_MODULES",3);
		linkState = LINKING_MODULES; 
	}
	
	/**
	 * To start module links for a list of parameters, iterate over the list, 
	 * creating a new ParamLink for each, and taking care of other bookkeeping
	 * @param params
	 */
	private void startModuleLinks(Collection params) {
		ChainBuilderAgent.debug("starting module link.. selected is "+selectedModule,3);
		activeModuleLinkParams = params;
	
		Iterator iter = params.iterator();
		while (iter.hasNext()) {
			FormalParameter param = (FormalParameter) iter.next();
			ParamLink link = new ParamLink();
			link.setPickable(false);
			link.setChildrenPickable(false);
			linkLayer.addChild(link);
			link.setStartParam(param);
			link.setPickable(false);
			links.add(link);
		}
	}
	
	/** 
	 * to finis the links between modules, get the corresponding parameters for 
	 * the current module, and call finishModuleLinks on that list.
	 * @param mod
	 */
	public void finishModuleLinks(ModuleView mod) {
		Collection c;
		
		// if I started as inputs, get outputs of this node.
		if (moduleLinksStartedAsInputs == true)
		 	c = mod.getOutputParameters();
		else 
		 	c = mod.getInputParameters();
		finishModuleLinks(c);
		links = new Vector();
		ChainBuilderAgent.debug("finish module links. state is not_linking",3);
		mod.setAllHighlights(false);
		linkState = NOT_LINKING;
	}
	
	/**
	 * Iterate through the list of end parameters and finish off each of the 
	 * links
	 * 
	 * @param targets the end points of the links in progress.
	 */
	public void finishModuleLinks(Collection targets) {
		// ok, for each thing in the initial params, finish 
		//this link against targets
		Iterator iter = links.iterator();
		while (iter.hasNext()) {
			ParamLink lnk = (ParamLink) iter.next();
			finishAModuleLink(lnk,targets);
		}
		// ok, check cycle
		if (foundCycle()== true) {
			canvas.setStatusLabel(NO_CYCLES);
			cancelModuleLinks();
		}
	}	
	
	/**
	 * To finish a module link, find the items in the target list that has the
	 * right semnatic type and complete the link. If there is no match, 
	 * remove the link
	 * @param link the link to be completed
	 * @param targets the list of potential endpoints
	 */
	public void finishAModuleLink(ParamLink link,Collection targets) {
		FormalParameter start = link.getStartParam();
		SemanticTypeData startType = start.getSemanticType();
		
		Iterator iter = targets.iterator();
		FormalParameter p;
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			SemanticTypeData type = p.getSemanticType();
			if (startType == type) {
				// finish it 
				link.setEndParam(p);
				link.setPickable(true);
				linkLayer.completeLink(link);
				start.setParamsHighlighted(false);
				return;
			}
		}
		// no matches. remove it.
		start.setParamsHighlighted(false);
		link.remove();
	}
	
	/**
	 * Cancel a set of module links in progress
	 *
	 */
	public void cancelModuleLinks() {
		ChainBuilderAgent.debug("cancelling module links...",3);
		ChainBuilderAgent.debug("selected module is..."+selectedModule,3);
		Iterator iter = links.iterator();
		while (iter.hasNext()) {
			ParamLink link = (ParamLink) iter.next();
			link.remove();
		}
		ChainBuilderAgent.debug(
				"cancelling modules links. link state is NOT_LINKING",3);
		linkState = NOT_LINKING;
		cleanUpModuleLink();
	}
	
	/**
	 * Bookkeeping clean-up after module links are finished or cancelled
	 *
	 */
	public void cleanUpModuleLink() {
		Iterator iter = activeModuleLinkParams.iterator();
		while (iter.hasNext()) {
			FormalParameter origin = (FormalParameter) iter.next();
			origin.setParamsHighlighted(false);
		}
		links = new Vector();
		if (selectedModule != null)
			selectedModule.setAllHighlights(false);
	}
	
	/** 
	 * A mouse-released event might indicate a popup event
	 */
	public void mouseReleased(PInputEvent e) {
		if (e.isPopupTrigger()) {
			ChainBuilderAgent.debug("mouse released",3);
			evaluatePopup(e);
		}
	}
	
	/**
	 * When a popup event occurs, call {@link zoom()} to zoom out one step.
	 * Also, set the {@link postPopup} flag to be true. This is needed to make 
	 * sure that any mouse clicks that also get executed do not get processed:
	 * they are artifactual and should be ignored.
	 *  
	 * @param e
	 */
	private void evaluatePopup(PInputEvent e) {
		postPopup=true;
		PNode n = e.getPickedNode();
		PNode p = n.getParent();
		ChainBuilderAgent.debug("popup. zooming out of "+n,3);
		if (n instanceof BufferedObject && (p == canvas.getLayer() 
				|| p instanceof ChainView)) {
			// if I'm on a module that's not in a chain or not. 
			// I should zoom to view of whole canvas
			PBounds b = canvas.getBufferedBounds();
			PCamera camera =canvas.getCamera();
			ChainBuilderAgent.debug("zooming to canvas bounds..",3);
			PActivity act = 
				camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);	
			PActivityDelegate delegate = new PActivityDelegate() {
				public void activityStarted(PActivity activity) {
				}
				public void activityStepped(PActivity activity) {
				}
				public void activityFinished(PActivity activity) {
					setModulesDisplayMode();
				}
			};
			act.setDelegate(delegate);
			
		}
		else {
			double scaleFactor = 1/Constants.SCALE_FACTOR;
			zoom(scaleFactor,e); 	
		}
		e.setHandled(true);
	}
	
	/** 
	 * If the user presses back-space or delete, delete the selected module 
	 * and/or link.
	 */
	public void keyPressed(PInputEvent e) {
		ChainBuilderAgent.debug("a key was pressed ",3);
		int key = e.getKeyCode();
		if (key != KeyEvent.VK_DELETE && key != KeyEvent.VK_BACK_SPACE)
			return;
			
		if (selectedLink != null) {
			selectedLink.remove();
			selectedLink = null;
		}
		else if (selectedModule != null) {
			selectedModule.remove();
			selectedModule = null;
		}
		canvas.updateSaveStatus();
	}
	
	private void showParameterInputOutputConflictError(FormalParameter param) {
		if (param instanceof FormalInput) {
			canvas.setStatusLabel(NO_INPUT_INPUT_LINKS);
		}
		else
			canvas.setStatusLabel(NO_OUTPUT_OUTPUT_LINKS);
	}
	
	private void showSelfLinkError() {
		canvas.setStatusLabel(NO_SELF_MODULE_LINKS);
	}
	
	private void showParamterTypeMisMatchError() {
		canvas.setStatusLabel(TYPE_MISMATCH_ERROR);
	}
	
	private void showNoLegalLinksBetwenModulesError() {
		canvas.setStatusLabel(NO_LEGAL_MODULE_LINKS);
	}	
}