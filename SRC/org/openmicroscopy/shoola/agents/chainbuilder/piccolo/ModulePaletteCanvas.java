/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModulePaletteCanvas
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
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ModulesData;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ModuleSelection;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ModulePaletteWindow;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ModuleTreeNode;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.ContentComponent;
import org.openmicroscopy.shoola.util.ui.piccolo.SortableBufferedObject;


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

public class ModulePaletteCanvas extends PCanvas implements DragGestureListener,
	BufferedObject, ContentComponent {
	
	/**
	 * Some screen layout parameters
	 */
	private static final float HGAP=10f;
	private static final float TOP=20f;
	private static final float LEFT=20f;
	private static final float VGAP=10f;
	private static final float NAME_INSET=20;
	

	/**
	 * The initial magnification of the  canvas
	 */
	private static float INIT_SCALE=1.0f;
	
	/**
	 * The layer for the canvas. 
	 */
	private PLayer layer;
	
	
	/** The main window that this is part of */
	private ModulePaletteWindow main;
	
	/** The data describing the modules */
	private ModulesData modData;
	
	/** The {@link JTree} node containing the category hierarhcy */
	private ModuleTreeNode treeNode;
	
	/** The selected module node */
	private ModuleView selected;
	
	/** The event handler */
	private ModuleNodeEventHandler handler;

    /** support for dragging of modules off of the canvas */
    private DragSourceAdapter dragListener;
    private DragSource dragSource;
    
  
		
	public ModulePaletteCanvas(ModulePaletteWindow main) {
		
		super();
		this.main = main;
		layer = getLayer();
		
		setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		setPreferredSize(new Dimension(ModulePaletteWindow.SIDE,
			ModulePaletteWindow.SIDE));
		setMinimumSize(new Dimension(ModulePaletteWindow.SIDE,
					ModulePaletteWindow.SIDE));
		//	remove handlers
		removeInputEventListener(getZoomEventHandler());
		removeInputEventListener(getPanEventHandler());
		handler = new ModulePaletteEventHandler(this);
		addInputEventListener(handler);

		// configure data transfer
		dragListener = new DragSourceAdapter() {
			public void dragExit(DragSourceEvent dse) {
			}
		};
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY,this);

			//	set up tooltips.
		 final PCamera camera = getCamera();
		 camera.addInputEventListener(new ModulePaletteToolTipHandler(camera));
	}
	
	
	public void setContents(Object obj) {
		modData = (ModulesData) obj;
	}
	
	/**
	 * Displaying all of the modules
	 *
	 */	
	public void layoutContents() {
		
		treeNode = new ModuleTreeNode();
		layer.setVisible(false);
		
		// do root categories.
		Iterator iter = modData.rootCategoriesIterator();
		while (iter.hasNext()) {
			ModuleCategoryData cat = (ModuleCategoryData) iter.next();
			displayModulesByCategory(layer,cat,treeNode);
		}
		
		if (modData.getUncategorizedCount() == 0)
			return;
			
		// no category type for uncategorized
		CategoryBox box =decorateCategory(layer,null);
		displayCategoryName(box,ModuleTreeNode.UNCAT_NAME);
		iter = modData.uncategorizedModulesIterator();
		ModuleTreeNode uncatNode = new ModuleTreeNode(ModuleTreeNode.UNCAT_NAME);
		treeNode.add(uncatNode);
		while (iter.hasNext()) {
			ChainModuleData mod  = (ChainModuleData) iter.next();
			displayModule(box,mod,uncatNode);
		}
		
		arrangeChildren(layer);
		layer.setVisible(true);
	}
	
	private CategoryBox decorateCategory(PNode parent,ModuleCategoryData category) {
		CategoryBox box  = new CategoryBox(category);
		parent.addChild(box);
		return box;
	}
	
	public void completeInitialization() {
	}
	
	public void scaleToResize() {
		handler.animateToLastBounds();
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
	
	/*
	 * Add the modules that are children of this category, 
	 * and recursively display the modules in subcategories, all the while 
	 * doing no layout on the results. After all of the modules in the category
	 * are displayed, call {@link arrangeChildren()} to layout the results.
	 * 
	 * @param parent the parent node that will old all of the modules
	 * @param cat the {@link ModuleCategory} that the nodes fit in.
	 * @param treeParent the {@link ModuleTreeNode} for the containing category
	 */
	public void displayModulesByCategory(PNode parent,ModuleCategoryData cat,
			ModuleTreeNode treeParent) {
		// display all modules for this category
		List mods = cat.getModules();
		Iterator iter  = mods.iterator();

		//decorate the category with a box.		
		CategoryBox box = decorateCategory(parent,cat);
		displayCategoryName(box,cat.getName());
		ModuleTreeNode catNode = new ModuleTreeNode(cat); // was .getName(),cat.getID());
		treeParent.add(catNode);

		// display the module in the box.
		while (iter.hasNext()) {
			ChainModuleData mod = (ChainModuleData) iter.next();
			displayModule(box,mod,catNode);
		}

		// recursively iterate over children categories.
		List children = cat.getChildCategories();
		iter = children.iterator();
		while (iter.hasNext()) {
			ModuleCategoryData child = (ModuleCategoryData) iter.next();
			displayModulesByCategory(box,child,catNode);
		}	
	}
	
	/**
	 * Display the name of the category in a {@link CategoryBox}
	 * @param box
	 * @param name
	 */
	private void displayCategoryName(CategoryBox box,String name) {
		if (name.compareTo("") !=0) {// if there is a name
			PText nameText = new PText(name);
			nameText.setFont(Constants.LARGE_NAME_FONT);
			nameText.setPickable(false);
			nameText.setGreekThreshold(0);
			box.addLabel(nameText);
			nameText.setScale(2);
			nameText.moveToFront();
			//System.err.println("translating name label to "+nameX+","+y);
			nameText.setOffset(Constants.CATEGORY_LABEL_OFFSET_X,
				Constants.CATEGORY_LABEL_OFFSET_Y);
		}
	}
	
		
	/** 
	 * Create a node for each module, add it to the canvas,
	 * and update the list of widgets for that module.
	 * 
	 * @param box    The parent of this module
	 * @param module The module to be displayed.
	 * @param catNode The tree node for the containing category
	 */	
	private void displayModule(CategoryBox box,ChainModuleData mod,
		ModuleTreeNode catNode) {

		ModuleView mNode = new SingleModuleView(mod);
		mod.addModuleNode(mNode);
		box.addChild(mNode);
		mNode.setOffset(0,0);

		ModuleTreeNode modNode = new ModuleTreeNode(mod); // was .getName(),mod.getID());
		catNode.add(modNode);
	}	

	public ModuleTreeNode getModuleTreeNode() {
		return treeNode;
	}
	
	/*
	 * arrangeChildren() does a pseudo-treemap layout, attempting
	 * to put things in boxes such that we don't get absurdly bad aspect
	 * ratios. This isn't as nice or as efficient as treemap, but it's 
	 * simpler.
	 * 
	 */
	private void arrangeChildren(PNode node) {

		float width = 0;
	
		List children = node.getChildrenReference();
	
		Iterator iter = children.iterator();
		float y =TOP;
		Vector curStrip = new Vector();
		SortableBufferedObject box;
		float x = LEFT;
		Object obj=null;
		PBounds b;
		double childrenCountRoot = Math.sqrt(node.getChildrenCount());
	
		// if the node is a categorybox, skip over the label	
		if (node instanceof CategoryBox) {
			CategoryBox catBox = (CategoryBox) node;
			y += catBox.getLabelHeight()+VGAP;
		}
		for (; ; ) {
			// get next item if i need  it.
			if (obj == null) {
				if (iter.hasNext()) {
					obj = iter.next();
					if (!(obj instanceof SortableBufferedObject)) {
						obj = null;
						continue;
					}
				}
				else
					break;
			}
		
			//add the next element in the list to a vector
			box = (SortableBufferedObject) obj;
			curStrip.add(box);
		
			// place the items in the current strip.
			Point2D pt = placeChildren(curStrip,y);
		
			if (pt.getX() > width)
				width = (float)pt.getX();
			if (curStrip.size()>childrenCountRoot )  {
				// remove the last item from the strip.
				curStrip.remove(box);
				// do rest of strip without that last box
				pt = placeChildren(curStrip,y);
				//create a new strip and add curent box to it.
				curStrip.clear();
				// move onto the next line.
				y= (float)pt.getY();
			
				width =0;
			}	
			else { // it fits. move on.
				obj = null;
			}
		}
		if (curStrip.size() > 0) {
			// place what's left over
			placeChildren(curStrip,y);
		}
		if (node instanceof CategoryBox) {
			// adjust the size of the category box
			b = new PBounds();
			b = node.getUnionOfChildrenBounds(b);
			((CategoryBox) node).setExtent(b.getWidth()+2*HGAP,
					b.getHeight()+4*VGAP);
		}
	}

	/**
	 * Place the nodes in a list in a row
	 * @param parent the parent node
	 * @param v a vector containing the nodes to place
	 * @param y the y-coordinate for the upper-left corner of each node.
	 * @return a point that contains the width of the row and 
	 * 		the y coordinate of the bottom of the row
	 */
	private Point2D placeChildren(Vector v,float y) {
		float x = LEFT;
		SortableBufferedObject node;
		Iterator iter = v.iterator();
		float maxHeight = 0;
		float childHeight = 0;
		float childWidth =0;
	
		// iterate through, placing nodes as need be.	
		while (iter.hasNext()) {
			node = (SortableBufferedObject) iter.next();
			if (node instanceof CategoryBox)
				arrangeChildren((PNode) node);
			node.setOffset(x,y);
		
			PBounds b = ((PNode) node).getBounds();
			childHeight = (float) b.getHeight();
			childWidth = (float) b.getWidth();
		

			x += childWidth+HGAP;
			// track the height of the row
			if (childHeight > maxHeight)
				maxHeight = childHeight;
		} 
		
		// return a point that indicates how wide the row is and the 
		// y-coordinate of the bottom.
		return new Point2D.Float(x-(LEFT),y+maxHeight);
	}	
	
	public void setSelectedForDrag(ModuleView module) {
		selected = module;
	}
	
	public void highlightModule(ChainModuleData module) {
		handler.highlightModules(module); 	
		
		Collection result = layer.getAllNodes();
		Iterator iter = result.iterator();
		while (iter.hasNext()) {
			PNode node = (PNode) iter.next();
			if (node instanceof ModuleView) {
				ModuleView mod = (ModuleView) node;
				if (mod.getModule() == module) {
					//	zoom in to it. 
					BufferedObject cBox = (BufferedObject) node;				
					PBounds b = cBox.getBufferedBounds();
					PCamera camera = getCamera();
					camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY); 
					return;
				}
			}
		}
	}
	
	public void unhighlightModules() {
		handler.unhighlightModules();
	}
	
	public void unhighlightModules(ChainModuleData mod) {
		handler.unhighlightModules(mod);
	}
	public void highlightCategory(ModuleCategoryData category) {
		Collection result = layer.getAllNodes();
		Iterator iter = result.iterator();
		while (iter.hasNext()) {
			PNode node = (PNode) iter.next();
			if (node instanceof CategoryBox) {
				CategoryBox cb = (CategoryBox)node;
				if (cb.isSameCategory(category) == true) {
						//	zoom in to it. 
					BufferedObject cBox = (BufferedObject) node;				
					PBounds b = cBox.getBufferedBounds();
					PCamera camera = getCamera();
					camera.animateViewToCenterBounds(b,true,Constants.ANIMATION_DELAY); 
					return;
				}
			} 
		}
	}

	/**
	 * this is a bit of hackery - because we don't want to go through the 
	 * pain of making RemoteModules serializable, we package up the id of the
	 * module as a StringSelection and use it as the instance of Transferable
	 * needed to do the drag and drop. The receiver of the drop can unpackage it
	 * and identfy the module via the connection object.
	 * 
	 * @see java.awt.dnd.DragGestureListener#
	 * 	dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent event) {
		if (selected != null) {
			selected.setModulesHighlighted(false);
			ChainModuleData mod = selected.getModule();
			if (mod != null) {
				int id = mod.getID();
				ModuleSelection text = new ModuleSelection(id);
				dragSource.startDrag(event,DragSource.DefaultMoveDrop,
						text,dragListener);
			}
		}
	}
	 
 }