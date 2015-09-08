/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.SelectionVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.RowSelectionVisitor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourObject;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;
import omero.gateway.model.ImageData;

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
 * @since OME3.0
 */
class BrowserControl
	implements MouseListener, ImageDisplayVisitor, PropertyChangeListener,
	MouseMotionListener
{
    
    //TODO: Implement scroll listener.  When the currently selected node is 
    //scrolled out of the parent's viewport then it has to be deselected. 
    
    /** Indicates to navigate to the left */
    private static final int DIRECTION_LEFT = 0;
    
    /** Indicates to navigate to the right */
    private static final int DIRECTION_RIGHT = 1;

    /** Indicates to navigate upwards */
    private static final int DIRECTION_UP = 2;

    /** Indicates to navigate downwards */
    private static final int DIRECTION_DOWN = 3;
    
    /** The Model controlled by this Controller. */
    private BrowserModel    model;
    
    /** The View controlled by this Controller.*/
    private RootDisplay     view;
        
    /** The selected cell, only used when displaying Plate. */
    private CellDisplay		selectedCell;

    /** Flag indicating if it is a right-click.*/
    private boolean			rightClickButton;
    
    /** Flag indicating if it is a right-click.*/
    private boolean			rightClickPad;
    
    /** Indicates if the <code>control</code> key is down. */
    private boolean			ctrl;
    
    /** Flag indicating if the left mouse button is pressed. */
    private boolean			leftMouseButton;
    
    /** The location of the mouse pressed.*/
    private Point anchor = new Point();
    
    /** The source of the mouse pressed.*/
    private Component source;
    
    /** The rectangle used to select multiple nodes.*/
    private Rectangle selection = new Rectangle();
    
    /** Flag indicating if the <code>Shift</code> is down or not.*/
    private boolean shiftDown;
    
    /** Component used to select several images.*/
    private GlassPane glassPane;
    
    /** Flag indicating that the {@link #glassPane} has already been added.*/
    private boolean added;
    
    /** Flag indicating that the multi-selection is on.*/
    private boolean dragging;
    
    /** The listener to add to the nodes.*/
    private KeyAdapter keyListener;
    
    /** Handles the multi-selection using key.*/
    private void handleKeySelection()
    {
		SelectionVisitor visitor = new SelectionVisitor(null, true);
		view.accept(visitor);
		model.setSelectedDisplays(visitor.getSelected());
    }
    
    /**
     * Handles the multi-selection.
     * 
     * @param install Pass <code>true</code> to install the glass pane, 
     *                <code>false</code> otherwise.
     */
    private SelectionVisitor handleMultiSelection(boolean install)
    {
    	SelectionVisitor visitor = new SelectionVisitor(selection, !install);
		view.accept(visitor);
		JLayeredPane pane = (JLayeredPane) view.getInternalDesktop();
		
    	if (install) {
    		if (glassPane == null) {
    			glassPane = new GlassPane();
    			
    		}
    		glassPane.setSelection(selection);
    		if (!added) {
    			glassPane.setSize(view.getSize());
    			pane.add(glassPane, Integer.valueOf(1));
    			added = true;
    		}
    	} else {
    		if (glassPane != null) {
    			pane.remove(glassPane);
    		}
    		added = false;
    	}
    	pane.repaint();
    	return visitor;
    }
    
    /**
     * Reacts to mouse pressed and mouse release event.
     * 
     * @param me        The event to handle.
     * @param released  Pass <code>true</code> if the method is invoked when
     *                  the mouse is released, <code>false</code> otherwise.
     */
    private void onClick(MouseEvent me, boolean released)
    {
    	if (me.getClickCount() == 1) {
    		ImageDisplay d = findParentDisplay(me.getSource());
    		//if (d == view) return;
        	d.moveToFront();
        	handleSelection(d, me);
        	me = SwingUtilities.convertMouseEvent((Component) me.getSource(),
        			me, view);
        	Point p = me.getPoint();
        	model.setPopupPoint(p, false);
    		if ((me.isPopupTrigger() && !released) ||
            		(me.isPopupTrigger() && released &&
            				!UIUtilities.isMacOS()) ||
            				(UIUtilities.isMacOS() &&
            						SwingUtilities.isLeftMouseButton(me)
            						&& me.isControlDown())) {
    			model.setPopupPoint(p, true);
    		}
    	} else if (me.getClickCount() == 2 && !(me.isMetaDown()
        		|| me.isControlDown() || me.isShiftDown())) {
    		Object src = me.getSource();
            ImageDisplay d = findParentDisplay(src);
            if (d == view) return;
            if (d instanceof ImageNode && !(d.getTitleBar() == src)
                && isSelectionValid(d)) {
            	model.viewDisplay(d, false);
            }
    	}
    }
    
    /**
     * Handles the selection.
     * 
     * @param d The selected node.
     * @param me The mouse event to handle.
     */
    private void handleSelection(ImageDisplay d, MouseEvent me)
    {
    	boolean shiftDown = me.isShiftDown();
    	Point p = me.getPoint();
    	ImageDisplay previousDisplay = model.getLastSelectedDisplay();
    	
    	if (((rightClickButton && !ctrl) || rightClickPad)
        		&& model.isMultiSelection()) {
        		//setFoundNode(nodes);
        		return;
        }
    	if (((ctrl || shiftDown) && leftMouseButton)) { //multi selection
    		ImageDisplay previous = model.getLastSelectedDisplay();
    		if (previous == null) {
    			model.setSelectedDisplay(d, true, true);
    			return;
    		}
    		Object object = previous.getHierarchyObject();
    		Class<?> ref = object.getClass();
    		if (!ref.equals(d.getHierarchyObject().getClass())) return;

    		Collection<ImageDisplay> nodes = model.getSelectedDisplays();
    		Iterator<ImageDisplay> i = nodes.iterator();
    		ImageDisplay node;
    		if (nodes.size() == 1) {
    			while (i.hasNext()) {
					node = i.next();
					if (node.equals(d)) return;
				}
    		}
    		
    		boolean remove = false;
    		while (i.hasNext()) {
    			node = i.next();
				if (node.equals(d)) {
					remove = true;
					break;
				}
			}
    		if (remove) model.removeSelectedDisplay(d);
    		else model.setSelectedDisplay(d, true, true);
    	} else {
    		if (isSelectionValid(d)) {
    			if (d instanceof CellDisplay && !(d.equals(previousDisplay))) {
    				setSelectedCell(p, (CellDisplay) d);
    			} else {
    				boolean b = model.isMultiSelection();
    				if (rightClickButton && b)
    					return;
    				if (b || !(d.equals(previousDisplay)))
    					model.setSelectedDisplay(d, false, true);
    			}
    		}
    	}
    }
    
    /**
     * Handles the selection.
     * 
     * @param d The selected node.
     * @param multiSelection Passed <code>true</code> if multiple selection is 
     * 						 on, <code>false</code> otherwise.
     * @param p The point where the mouse is clicked.
     */
    /*
    private void handleSelection(ImageDisplay d, 
    		boolean multiSelection, Point p)
    {
    	ImageDisplay previousDisplay = model.getLastSelectedDisplay();
		if (multiSelection) { //multi selection
    		ImageDisplay previous = model.getLastSelectedDisplay();
    		if (previous == null) {
    			model.setSelectedDisplay(d, true, true);
    			return;
    		}
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
    		if (isSelectionValid(d)) {
    			if (d instanceof CellDisplay && !(d.equals(previousDisplay))) {
    				setSelectedCell(p, (CellDisplay) d);
    			} else {
    				boolean b = model.isMultiSelection();
    				if (b || !(d.equals(previousDisplay)))
    					model.setSelectedDisplay(d, false, true);
    			}
    		}
    	}
    }
    */
    
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
    	node.addPropertyChangeListener(this);
    	node.addListenerToComponents(this);
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
    BrowserControl(final BrowserModel model, RootDisplay view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        model.addPropertyChangeListener(
        		Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY, this);
        model.addPropertyChangeListener(Browser.ROLL_OVER_PROPERTY, this);
        this.model = model;
        this.view = view;
        view.getInternalDesktop().addMouseMotionListener(this);
		view.getInternalDesktop().setCursor(Cursor.getDefaultCursor());
		keyListener = new KeyAdapter() {
		    
			/** 
			 * Selects all the nodes if <code>Ctrl-A</code> or
			 * <code>Cmd-A</code> is pressed.
			 * @see KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(KeyEvent e)
			{
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_A:
                                    if ((UIUtilities.isMacOS() && e.isMetaDown())
                                            || (!UIUtilities.isMacOS() && e.isControlDown())) {
                                        handleKeySelection();
                                    }
                                    break;
                                case KeyEvent.VK_UP:
                                    navigate(DIRECTION_UP, e.isShiftDown());
                                    break;
                                case KeyEvent.VK_DOWN:
                                    navigate(DIRECTION_DOWN, e.isShiftDown());
                                    break;
                                case KeyEvent.VK_LEFT:
                                    navigate(DIRECTION_LEFT, e.isShiftDown());
                                    break;
                                case KeyEvent.VK_RIGHT:
                                    navigate(DIRECTION_RIGHT, e.isShiftDown());
                                    break;
                            }
			}
			
		};
    }
    
    /**
     * Moves the current selection to the right, left, up or down.
     * 
     * @param direction
     *            The direction to move to 
     * @param multiSel
     *            Pass <code>true</code> to add the selection 
     *            to a multiple selection
     */
    public void navigate(int direction, boolean multiSel) {
        ImageDisplay current = model.getLastSelectedDisplay();
        if (current == null)
            return;
        
        Rectangle b = current.getBounds();
        
        int x = b.x;
        int y = b.y;

        // choose a point which lies 50% of the ImageDisplay width/height 
        // to the left/right/above/under the current ImageDisplay;
        // (this will work if the gap between the ImageDisplays is not 
        //  too big)
        switch (direction) {
            case DIRECTION_LEFT:
                x = b.x - (int) (b.width * 0.5);
                break;
            case DIRECTION_RIGHT:
                x = b.x + (int) (b.width * 1.5);
                break;
            case DIRECTION_UP:
                y = b.y - (int) (b.height * 0.5);
                break;
            case DIRECTION_DOWN:
                y = b.y + (int) (b.height * 1.5);
                break;
        }

        model.setSelectedDisplay(new Point(x, y), multiSel);
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
    	anchor = me.getPoint();
    	Component s = ((Component) me.getSource());
    	s.requestFocus();
    	s.removeKeyListener(keyListener);
    	s.addKeyListener(keyListener);
    	shiftDown = me.isShiftDown();
		if (dragging) return;
		Collection<ImageDisplay> l = model.getSelectedDisplays();
		if (source == null) {
			if (l.size() == 0) source = (JComponent) me.getSource();
			else source = (JComponent) ((List<ImageDisplay>) l).get(0);
		}
		if (shiftDown && l.size() > 0)
			return;
    	rightClickPad = UIUtilities.isMacOS() && 
    	SwingUtilities.isLeftMouseButton(me) && me.isControlDown();
    	rightClickButton = SwingUtilities.isRightMouseButton(me);
    	ctrl = me.isControlDown();
    	if (UIUtilities.isMacOS()) ctrl = me.isMetaDown();
    	leftMouseButton = SwingUtilities.isLeftMouseButton(me);
    	if (!UIUtilities.isWindowsOS()) 
    		onClick(me, false); 
    }
    
    /**
     * Tells the model that either a pop-up point or a thumbnail selection
     * was detected.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) 
    {
    	if (dragging) {
    		SelectionVisitor visitor = handleMultiSelection(false);
    		model.setSelectedDisplays(visitor.getSelected());
    		dragging = false;
    		return;
    	}
    	Collection<ImageDisplay> l = model.getSelectedDisplays();
    	if (shiftDown && l.size() >= 1) {
    		if (source == null) source = (JComponent) ((List<ImageDisplay>) l).get(0);
    		ImageDisplay display = findParentDisplay((Component) me.getSource());
    		Rectangle rS = display.getBounds();
    		display = findParentDisplay(source);
    		Rectangle rAnchor =  display.getBounds();
			RowSelectionVisitor visitor = new RowSelectionVisitor(rS, rAnchor, true);
			view.accept(visitor);
			shiftDown = me.isShiftDown();
			if (!shiftDown) source = null;
			final List<ImageDisplay> selectedDisplays = new ArrayList<ImageDisplay>();
			final List<ImageNode> visibleNodes = model.getVisibleImageNodes();
			final Colors colors = Colors.getInstance();
			for (final ImageDisplay node : visitor.getSelected())
			    if (visibleNodes.contains(node))
			        selectedDisplays.add(node);
			    else
			        node.setHighlight(colors.getDeselectedHighLight(node));
			model.setSelectedDisplays(selectedDisplays);
			return;
		}
    	source = null;
    	leftMouseButton = SwingUtilities.isLeftMouseButton(me);
    	if (UIUtilities.isWindowsOS()) onClick(me, true);
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
    }
    
    /**
     * Highlights the nodes if the <code>SHIFT</code> key is down.
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
	public void mouseDragged(MouseEvent e)
	{
		if (e.getSource() != view.getInternalDesktop()) return;
		dragging = true;
		Point p = e.getPoint();
		if (p == null) p = new Point();
		selection.width = Math.abs(p.x-anchor.x);
		selection.height = Math.abs(p.y-anchor.y);
		if (anchor.x < p.x) selection.x = anchor.x;
		else selection.x = p.x;
		if (anchor.y < p.y) selection.y = anchor.y;
		else selection.y = p.y;
		handleMultiSelection(true);
	}

    /**
     * Required by the {@link MouseListener} I/F but no-operation implementation
     * in our case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {}

	/**
     * Required by the {@link MouseMotionListener} I/F but no-operation
     * implementation in our case.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */
	public void mouseMoved(MouseEvent e) {}

}
