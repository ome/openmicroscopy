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
import java.util.Vector;
import javax.swing.Timer;

//Third-party libraries
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;



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
	private ModuleView lastModuleEntered;
	
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
	 * The start of the a link in progress
	 */
	private Point2D.Float linkStart = new Point2D.Float();
	
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
	private Vector links = new Vector();
	
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
			//System.err.println("mouse entered last entered.."+
			if (linkState == NOT_LINKING) {
				ModuleView mod = lastParameterEntered.getModuleView();
				mod.setParamsHighlighted(false);
				// must turn on params for this parameter _after_
				// we turn off all params for the module, 
				// or else turnning off params for the module
				// will undo what had just bee turned on.
				lastParameterEntered.setParamsHighlighted(true);
				mod.setModulesHighlighted(true);	
			}
			e.setHandled(true);
		}
		else if (node instanceof ModuleView && linkState == NOT_LINKING) {
			ModuleView mod = (ModuleView) node;
			mod.setAllHighlights(true);
			e.setHandled(true);
		}
		else {
			super.mouseEntered(e);
		}
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
		//System.err.println("last parameter entered cleared");
		if (node instanceof FormalParameter) {
			FormalParameter param = (FormalParameter) node;
			if (linkState == NOT_LINKING) {	
				param.setParamsHighlighted(false);
				ModuleView mod = param.getModuleView();
				mod.setAllHighlights(false);
			}			
			e.setHandled(true);
		}
		else if (node instanceof ModuleView && linkState == NOT_LINKING) {
			ModuleView mod = (ModuleView) node;
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
		//System.err.println("mouse dragged..."+e.getPickedNode());
	//	System.err.println("CHAIN HANDLER:got a drag event in chain canvas");
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

	}
	
	
	public void actionPerformed(ActionEvent e) {
		//System.err.println("mouse single click");
		if (cachedEvent != null) 
			doMouseClicked(cachedEvent);
		cachedEvent = null;
		timer.stop();
	}
	
	public void mouseClicked(PInputEvent e) {
		if (timer.isRunning()) {
			//System.err.println("mouse double click");
			// this is effectively a double click.
			timer.stop();
			if (wasDoubleClick == true)
				wasDoubleClick  = false;
			else
				doMouseDoubleClicked(e);
		}
		else {
			timer.restart();
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
		// we only scale if we're not drawing a link.
		//System.err.println("mouse clicked..");
		//System.err.println("link state is "+linkState);
		if (linkState != NOT_LINKING) {
			if (linkState == LINKING_CANCELLATION) {
				//System.err.println("cancelled link. set to not linking");
				linkState = NOT_LINKING;
			}
			//System.err.println("linking...");
			e.setHandled(true);
			return;
		}
		
		if (postPopup == true) {
			//System.err.println("post popup");
			postPopup = false;
			e.setHandled(true);
			return;
		}
		
		PNode node = e.getPickedNode();
		//System.err.println("mouse clicked on .."+node);
		int mask = e.getModifiers() & allButtonMask;
		
		// if it's a buffered object that is not a chain.
		if (node instanceof BufferedObject && !(node instanceof ChainView)) {
			BufferedObject mod = (BufferedObject) node;
			PCamera camera = canvas.getCamera();
			if (mask == MouseEvent.BUTTON1_MASK && e.getClickCount()==1) {
				//System.err.println("zooming in on node...");
				PBounds b = mod.getBufferedBounds();
				camera.animateViewToCenterBounds(b,true,
					Constants.ANIMATION_DELAY);
			}
			else if (e.isControlDown() || (mask & MouseEvent.BUTTON3_MASK)==1) {
				//System.err.println("canvas right click..");
				evaluatePopup(e);					
			}
		}
		
		// otherwise, must be a camera, or a chain. either way, zoom in/out
		//if (! (node instanceof PCamera))
		//	return;
		
		if (e.isShiftDown()) {
			//System.err.println("zoomed with shhift..");
			PBounds b = canvas.getBufferedBounds();
			canvas.getCamera().animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);
			e.setHandled(true);
		}
		else {
			//System.err.println("zoomed witout shift...");
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
		//System.err.println("got a double click on "+node);
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
		e.setHandled(true);
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
		
		//System.err.println("mouse pressed on "+e.getPickedNode());
		if (wasDoubleClick == true) {
			//System.err.println("just came from double click..");
			wasDoubleClick = false;
			return;
		}
		
		if (e.isPopupTrigger()) {
			//System.err.println("mouse pressed..");
			evaluatePopup(e);
			return;
		}
		
		super.mousePressed(e);
		PNode node = e.getPickedNode();
		
		//System.err.println("mouse pressed on "+node+", state "+linkState);
		
		// clear off what was selected.
		if (selectedLink != null && linkState != LINK_CHANGING_POINT) {
		//	System.err.println("setting selected link to not be selected, in mousePressed");
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
			mousePressedLinkingParams(node,e);
		else if (linkState == LINKING_MODULES)
			mousePressedLinkingModules(node,e);
		else if (linkState == NOT_LINKING) 
			mousePressedNotLinking(node,e);		
		else if (linkState == LINK_CHANGING_POINT)
			mousePressedChangingPoint(node,e);
		else {
			//System.err.println("mouse pressed..setting link state to not linking..");
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
			//System.err.println("mousePressedLink. setting linkstate to not linking");
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
		//System.err.println("pressing on selection target..");
		selectionTarget = (LinkSelectionTarget) node;
		selectionTarget.getLink().setSelected(true);
		//System.err.println("mousePressedSelectionTarget.... setting link state to changing point");
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
	private void mousePressedLinkingParams(PNode node,PInputEvent e) {
		if (e.getClickCount() ==2) {
			cancelParamLink();
			//System.err.println("mouse pressed linking params. setting to linking cancellation");
			linkState = LINKING_CANCELLATION;
		}
		else if (lastParameterEntered == null) { // we're on canvas.
			Point2D pos = e.getPosition();
			link.setIntermediatePoint((float) pos.getX(),(float) pos.getY());
		}
		else if (lastParameterEntered != null)
			finishParamLink();
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
		//System.err.println("node is..."+node);
		//if (node instanceof ParamLink)
			//System.err.println("param link");
		//if (node instanceof ModuleLink)
			//System.err.println("module link");
		
		//System.err.println("caling mouse pressed linking modules..");
		if (count ==2) {
			//System.err.println("2 presses. node is "+node);
			if (node instanceof FormalParameter) {
				FormalParameter p = (FormalParameter) node;
				ModuleView mod = p.getModuleView();
				finishModuleLinks(mod);
			}
			else if (node instanceof ModuleView) {
				finishModuleLinks((ModuleView) node);
			}
			else
				cancelModuleLinks();
			wasDoubleClick = true;
		}
		else if (node instanceof PCamera){ // single click on camera
			//System.err.println("on camera");
			//when linking modules..
			//System.err.println("adding an intermediate point to modules link");
			
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
		//System.err.println("got mouse presssed not linking..");
		if (node instanceof FormalParameter) {
			if (lastParameterEntered == null) 
				mouseEntered(e);
			FormalParameter param = (FormalParameter) node;
			//System.err.println("starting a link from .."+param.getParameter().getName());
			if (param.canBeLinkOrigin())
				startParamLink(param);
		}
		else if (node instanceof LinkSelectionTarget) {
		//	System.err.println("pressiing on target..");
			selectionTarget  = (LinkSelectionTarget) node;
			//System.err.println("mouse pressed not linking. setting to link changing point");
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
			//System.err.println("clearing link selection target.. not_LINKING.");
			linkState = NOT_LINKING;
			if (selectionTarget != null) {
				Link link = selectionTarget.getLink();
				if (link != null)
					link.setSelected(false);
			}
		} 
		e.setHandled(true);
	}
	
	/***
	 * Start a new link between parameters
	 * @param param the origin of the new link
	 */
 	private void startParamLink(FormalParameter param) {
		////System.err.println("mouse pressing and starting link");
		linkOrigin = param;
		link = new ParamLink();
		linkLayer.addChild(link);
		link.setStartParam(linkOrigin);
		link.setPickable(false);
		//System.err.println("start param link. link state is LINKING_PARAMS");
		linkState = LINKING_PARAMS;			
 	}
		
	/**
	 * End the link curently in process at the link denoted by
	 * {@link lastParameterEntered}.
	 *
	 */
	private void finishParamLink() {
		if (lastParameterEntered.isLinkable() == true) {
			//System.err.println("finishing link");
			link.setEndParam(lastParameterEntered);
			link.setPickable(true);
			// add the {@link ModuleViewLink} between the modules
			linkLayer.completeLink(link);
			cleanUParamLink();
		}
		else {
			//////System.err.println("trying to finish link, but end point is not linkable");
			cancelParamLink();
		}
		//System.err.println("finishParamLink. state is NOT_LINKING");
		linkState = NOT_LINKING;
	}
	
	/**
	 * Cancel a lnk between parameters
	 *
	 */
	private void cancelParamLink() {
		//System.err.println("canceling link");
		link.removeFromParent();
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
		//	System.err.println("building module links on input side");
			startModuleLinks(inputs);
			moduleLinksStartedAsInputs = true;
		}
		else { 
		//	System.err.println("building module links on output side");
			startModuleLinks(outputs); 
			moduleLinksStartedAsInputs = false;
		}
		//System.err.println("start module links. link state is LINKING_MODULES");
		linkState = LINKING_MODULES; 
	}
	
	/**
	 * To start module links for a list of parameters, iterate over the list, 
	 * creating a new ParamLink for each, and taking care of other bookkeeping
	 * @param params
	 */
	private void startModuleLinks(Collection params) {
		//System.err.println("starting module link.. selected is "+selectedModule);
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
		//System.err.println("finish module links. state is not_linking");
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
		link.removeFromParent();
	}
	
	/**
	 * Cancel a set of module links in progress
	 *
	 */
	public void cancelModuleLinks() {
		//System.err.println("cancelling module links...");
		//System.err.println("selected module is..."+selectedModule);
		Iterator iter = links.iterator();
		while (iter.hasNext()) {
			ParamLink link = (ParamLink) iter.next();
			link.removeFromParent();
		}
		//System.err.println("cancelling modules links. link state is NOT_LINKING");
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
		selectedModule.setAllHighlights(false);
	}
	
	/** 
	 * A mouse-released event might indicate a popup event
	 */
	public void mouseReleased(PInputEvent e) {
		if (e.isPopupTrigger()) {
		//	System.err.println("mouse released");
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
		//System.err.println("popup. zooming out of "+n);
		if (n instanceof BufferedObject && (p == canvas.getLayer() 
				|| p instanceof ChainView)) {
			// if I'm on a module that's not in a chain or not. 
			// I should zoom to view of whole canvas
			PBounds b = canvas.getBufferedBounds();
			PCamera camera =canvas.getCamera();
			camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY);	
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
		//System.err.println("a key was pressed ");
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
	
}