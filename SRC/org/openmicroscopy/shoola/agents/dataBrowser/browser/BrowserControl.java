/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.browser;



//Java imports
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourObject;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;

import pojos.ImageData;

/** 
 * Handles input events originating from the {@link Browser}'s View.
 * That is, from the {@link RootDisplay} containing all the visualization
 * trees. 
 * This class takes on the role of the browser's Controller (as in MVC).
 *
 * @see BrowserModel
 * @see RootDisplay
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class BrowserControl     
	implements MouseListener, ImageDisplayVisitor, PropertyChangeListener
{
    
    //TODO: Implement scroll listener.  When the currently selected node is 
    //scrolled out of the parent's viewport then it has to be deselected. 
    
    /** The Model controlled by this Controller. */
    private BrowserModel    model;
    
    /** The View controlled by this Controller.*/
    private RootDisplay     view;
    
    /** Flag to indicate that a popupTrigger event occurred. */
    private boolean         popupTrigger;
    
    /** The selected cell, only used when displaying Plate. */
    private CellDisplay		selectedCell;
    
    /**
     * Brings up the color picker to set the color of the node.
     * 
     * @param p 	The location of the dialog.
     * @param node	The selected node.
     */
    private void setSelectedCell(Point p, CellDisplay node)
    {
    	selectedCell = node;
    	SwingUtilities.convertPointToScreen(p, node);
    	ColourPicker picker = new ColourPicker(
    			DataBrowserAgent.getRegistry().getTaskBar().getFrame(), 
    			node.getHighlight(), true);
    	picker.setColorDescription(node.getDescription());
    	picker.addPropertyChangeListener(this);
    	picker.setLocation(p);
    	picker.setVisible(true);
    }
    
    /**
     * Finds the first {@link ImageDisplay} in <code>x</code>'s containment
     * hierarchy.
     * 
     * @param x A component.
     * @return The parent {@link ImageDisplay} or <code>null</code> if none
     *         was found.
     */
    private ImageDisplay findParentDisplay(Object x)
    {
        while (true) {
            if (x instanceof ImageDisplay) return (ImageDisplay) x;
            if (x instanceof JComponent) x = ((JComponent) x).getParent();
            else break;
        }
        return null;
    }

    /**
     * Attaches the listeners to the specified node.
     * 
     * @param node The node to handle.
     */
    private void attachListeners(ImageNode node)
    {
    	if (node == null) return;
    	node.addMouseListenerToComponents(this);
    	node.addPropertyChangeListener(this);
    }

    /**
     * Creates a new Controller for the specified <code>model</code> and
     * <code>view</code>.
     * You need to call the {@link #initialize() initialize} method after
     * creation to complete the MVC set up.
     * 
     * @param model The Model.
     * @param view The View.
     */
    BrowserControl(BrowserModel model, RootDisplay view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        model.addPropertyChangeListener(
        		Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY,
                                        this);
        model.addPropertyChangeListener(Browser.ROLL_OVER_PROPERTY,
                                            this);
        this.model = model;
        this.view = view;
        popupTrigger = false;
    }
    
    /**
     * Subscribes for mouse events notification with each node in the
     * various visualization trees.
     */
    void initialize() { model.accept(this); }

    /**
     * Checks if the passed image has pixels set related to it.
     * Returns <code>true</code> if some pixels set are linked, 
     * <code>false</code> otherwise.
     * 
     * @param node The node to handle.
     * @return See above.
     */
    boolean isSelectionValid(ImageDisplay node)
    {
    	if (!(node instanceof ImageNode)) return true;
    	Object ho = node.getHierarchyObject();
    	if (!(ho instanceof ImageData)) return true;
    	ImageData img = (ImageData) ho;
		try {
			img.getDefaultPixels();
			return true;
		} catch (Exception e) {
			/*
			UserNotifier un = 
				DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Image Not valid", "The selected image is not valid");
			node.setHighlight(
					Colors.getInstance().getDeselectedHighLight(node));
					*/
			return false;
		}
    }

    /**
     * Registers this object as mouse listeners with each node.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) 
    { 
    	attachListeners(node);
    	if (node instanceof WellSampleNode) {
    		WellSampleNode sample = (WellSampleNode) node;
    		WellImageSet well = sample.getParentWell();
    		List<WellSampleNode> samples = well.getWellSamples();
    		Iterator<WellSampleNode> i = samples.iterator();
    		WellSampleNode wsn;
    		while (i.hasNext()) {
    			wsn = i.next();
    			if (wsn != node) attachListeners(wsn);
			}
    	}
    }

    /**
     * Registers this object as mouse listeners with each node.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) 
    {
        node.getTitleBar().addMouseListener(this);
        node.getInternalDesktop().addMouseListener(this);
        node.addPropertyChangeListener(this);
        //node.addPropertyChangeListener(ImageDisplay.END_MOVING_PROPERTY, this);
    }
    
    /** 
     * Listens to the property event fired by {@link Browser} and 
     * {@link ImageDisplay}.
     * Necessary for clarity.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */ 
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (ImageNode.CLASSIFY_NODE_PROPERTY.equals(name)) {
            model.setNodeForProperty(Browser.CLASSIFIED_NODE_PROPERTY, 
                        evt.getNewValue());
        } else if (ImageDisplay.ANNOTATE_NODE_PROPERTY.equals(name)) {
            model.setNodeForProperty(Browser.ANNOTATED_NODE_PROPERTY, 
                                    evt.getNewValue());
        } else if (Browser.ROLL_OVER_PROPERTY.equals(name)) {
        	RollOverNode n = (RollOverNode) evt.getNewValue();
        	ImageNode img = null;
        	if (n != null) img = n.getNode();
            view.setTitle(model.currentPathString(img));
        } else if (ImageNode.PIN_THUMBNAIL_PROPERTY.equals(name)) {
        	ImageNode node = (ImageNode) evt.getNewValue();
        	model.setThumbSelected(true, node);
        } else if (ImageDisplay.END_MOVING_PROPERTY.equals(name)) {
        	//ImageDisplay node = model.getLastSelectedDisplay();
        	/*
        	ImageDisplay node = (ImageDisplay) evt.getNewValue();
        	int layoutIndex = model.getSelectedLayout();
        	Layout layout = LayoutFactory.createLayout(layoutIndex, sorter);
        	view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            node.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
        	view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        	*/
        } else if (CellDisplay.DESCRIPTOR_PROPERTY.equals(name)) {
        	CellDisplay node = (CellDisplay) evt.getNewValue();
        	setSelectedCell(node.getLocation(), node);
        } else if (ColourPicker.COLOUR_PROPERTY.equals(name)) {
        	ColourObject co = (ColourObject) evt.getNewValue();
        	if (selectedCell == null) return;
        	selectedCell.setHighlight(co.getColor());
        	selectedCell.setDescription(co.getDescription());
        	model.setSelectedCell(selectedCell);
        }
    }
    
    /**
     * Sets the currently selected display.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {
    	ImageDisplay d = findParentDisplay(me.getSource());
    	d.moveToFront();
    	ImageDisplay previousDisplay = model.getLastSelectedDisplay();
    	
    	boolean macOS = UIUtilities.isMacOS();
    	
    	boolean b = (me.isMetaDown() ||
    			me.isShiftDown());//me.isShiftDown();
    
    	if (!macOS) b = b || me.isControlDown();
    	
    	Point p = me.getPoint();
    	if (me.isPopupTrigger() && b &&
    		previousDisplay != null && previousDisplay.getBounds().contains(p))
    	{
    		model.setPopupPoint(p, true);
    		return;
    	}
    	if (me.isPopupTrigger() ||
    			(SwingUtilities.isRightMouseButton(me) && !macOS) ||
        		(me.isControlDown() && SwingUtilities.isLeftMouseButton(me) &&
        				macOS)) {
        		model.setPopupPoint(p, true);
        		popupTrigger = true;
        		return;
        }
    	
    	if (b) { //multi selection
    		ImageDisplay previous = model.getLastSelectedDisplay();
    		if (previous == null) return;
    		Object object = previous.getHierarchyObject();
    		Class ref = object.getClass();
    		if (!ref.equals(d.getHierarchyObject().getClass())) return;
    		
    		Collection nodes = model.getSelectedDisplays();
    		Iterator i = nodes.iterator();
    		ImageDisplay node;
    		boolean remove = false;
    		while (i.hasNext()) {
    			node = (ImageDisplay) i.next();
				if (node.equals(d)) {
					remove = true;
					break;
				}
			}
    		if (remove) model.removeSelectedDisplay(d);
    		else model.setSelectedDisplay(d, true, true);
    	} else {
    		if (!(d.equals(previousDisplay)) && isSelectionValid(d)) {
    		//if (isSelectionValid(d)) {
    			if (d instanceof CellDisplay) {
    				setSelectedCell(me.getPoint(), (CellDisplay) d);
    			} else model.setSelectedDisplay(d, false, true);
    		}
    	}
    }

    /**
     * Tells the model that either a popup point or a thumbnail selection
     * was detected.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) 
    {
    	/*
        if (me.isPopupTrigger() || SwingUtilities.isRightMouseButton(me) ||
    			(me.isControlDown() && SwingUtilities.isLeftMouseButton(me)))
                model.setPopupPoint(me.getPoint(), true);
        else {
            Object src = me.getSource();
            ImageDisplay d = findParentDisplay(src);
            if (d instanceof ImageNode && !(d.getTitleBar() == src) 
                && me.getClickCount() == 2 && isSelectionValid(d)) {
            	model.viewDisplay(d);
            }   
        }
        popupTrigger = false; 
        */
    	int count = me.getClickCount();
    	/*
    	if (count == 1) {
    		System.err.println(me.isPopupTrigger() +" "+SwingUtilities.isRightMouseButton(me));
    		if (UIUtilities.isMacOS()) {
    			if (me.isPopupTrigger() || //SwingUtilities.isRightMouseButton(me) ||
    	    			(me.isControlDown() && SwingUtilities.isLeftMouseButton(me)))
    	                model.setPopupPoint(me.getPoint(), true);
    		}
    	}
    	*/
    	if (count == 2) {
    		if (UIUtilities.isMacOS()) {
        		if (!me.isControlDown()) {
        			Object src = me.getSource();
                    ImageDisplay d = findParentDisplay(src);
                    if (d instanceof ImageNode && !(d.getTitleBar() == src) 
                        && isSelectionValid(d)) {
                    	model.viewDisplay(d);
                    }   
        		} 
        	} else {
        		Object src = me.getSource();
                ImageDisplay d = findParentDisplay(src);
                if (d instanceof ImageNode && !(d.getTitleBar() == src) 
                    && isSelectionValid(d)) {
                	model.viewDisplay(d);
                }   
        	}
    	}
    }

    /**
     * Sets the node which has to be zoomed when the roll over flag
     * is turned on. Note that the {@link ImageNode}s are the only nodes
     * considered.
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me)
    {
    	boolean mo = model.isMouseOver();
    	boolean ro = model.isRollOver();
    	if (!mo && !ro) return;
        Object src = me.getSource();
        ImageDisplay d = findParentDisplay(src);
        if (mo) {
        	if (d instanceof RootDisplay) {
            	ImageDisplay lastSelected = model.getLastSelectedDisplay();
            	if (lastSelected != null) {
            		view.setTitle(model.currentPathString(lastSelected));
            	} else lastSelected = null;
            	model.setNodeForProperty(Browser.MOUSE_OVER_PROPERTY, 
    					lastSelected);
            	return;
            }
            if (!(d instanceof RootDisplay))
                view.setTitle(model.currentPathString(d));
            model.setNodeForProperty(Browser.MOUSE_OVER_PROPERTY, d);
        }
        if (!ro) return;
        if (d instanceof ImageNode && !(d.getTitleBar() == src)) {
        	ImageNode img = (ImageNode) d;
        	RollOverNode n = new RollOverNode(img, img.getLocationOnScreen());
            model.setRollOverNode(n);
        } else model.setRollOverNode(null);
    }

    /**
     * Displays the name of the selected node if any when the mouse exited.
     * @see MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me)
    {
    	model.setRollOverNode(null);
    	//if (model.isRollOver()) return;
        //ImageDisplay d = model.getLastSelectedDisplay();
        //if (d != null) view.setTitle(model.currentPathString(d));
        //else view.setTitle("");
        //model.setNodeForProperty(Browser.SELECTED_DISPLAY_PROPERTY, d);
    }
    
    /**
     * Required by the {@link MouseListener} I/F but no-op implementation
     * in our case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {}
   
}
