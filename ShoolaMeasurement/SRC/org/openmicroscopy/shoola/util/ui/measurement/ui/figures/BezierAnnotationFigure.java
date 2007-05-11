/*
 * org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureBezierTextFigure 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.figures;


//Java imports
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import static org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys.BASIC_TEXT;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.Layouter;
import org.jhotdraw.draw.LocatorLayouter;
import org.jhotdraw.draw.RelativeLocator;
import org.jhotdraw.draw.TextFigure;
import org.jhotdraw.util.ReversedList;
import static org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys.BASIC_TEXT;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BezierAnnotationFigure 
			extends 	MeasureBezierFigure
			implements 	CompositeFigure 
{
	
	private Layouter layouter;
    private ArrayList<Figure> children = new ArrayList();

    private Rectangle2D.Double drawBounds;

	/**
	 * This is used to perform faster drawing and hit testing.
	 */
		
	private TextFigure  text;
	   
	 private ChildHandler childHandler = new ChildHandler(this);
	    private class ChildHandler implements FigureListener, UndoableEditListener 
	    {
	        private BezierAnnotationFigure owner;
	        private ChildHandler(BezierAnnotationFigure owner) 
	        {
	            this.owner = owner;
	        }
	        public void figureRequestRemove(FigureEvent e) 
	        {
	            owner.remove(e.getFigure());
	        }
	        
	        public void figureRemoved(FigureEvent evt) 
	        {
	        }
	        
	        public void figureChanged(FigureEvent e) 
	        {
	            if (! owner.isChanging()) 
	            {
	                owner.willChange();
	                owner.fireFigureChanged(e);
	                owner.changed();
	            }
	        }
	        
	        public void figureAdded(FigureEvent e) 
	        {
	        }
	        
	        public void figureAttributeChanged(FigureEvent e) 
	        {
	           	if(e.getAttribute()==TEXT)
	        		owner.getROIShape().setAnnotation(BASIC_TEXT, e.getNewValue());
	        }
	        
	        public void figureAreaInvalidated(FigureEvent e) 
	        {
	            if (! owner.isChanging()) 
	            {
	                owner.fireAreaInvalidated(e.getInvalidatedArea());
	            }
	        }
	        
	        public void undoableEditHappened(UndoableEditEvent e) 
	        {
	            owner.fireUndoableEditHappened(e.getEdit());
	        }
	    };
	
	/** Creates a new instance. */
	public BezierAnnotationFigure()
	{
		super();
		addText();
	}
	
	public BezierAnnotationFigure(boolean closed) 
	{
		super(closed);
		addText();
	}
	
	private void addText()
	{
		text = new TextFigure();
		text.setEditable(true);
		text.setText("Text");
		RelativeLocator d = new RelativeLocator();
		
		d.locate(this, text);
		text.setAttribute(LocatorLayouter.LAYOUT_LOCATOR, d);
		this.setLayouter(new LocatorLayouter());
		this.add(text);
	}
	
	 /**
     * Draw the figure. This method is delegated to the encapsulated presentation figure.
     */
    public void draw(Graphics2D g) 
    {
        super.draw(g);
        
        for (Figure child : children) 
        {
            if (child.isVisible()) 
            {
                child.draw(g);
            }
        }
    }
    
    // SHAPE AND BOUNDS
    /**
     * Transforms the figure.
     */
    public void basicTransform(AffineTransform tx) 
    {
        super.basicTransform(tx);
        for (Figure f : children) 
        {
            f.basicTransform(tx);
        }
        invalidateBounds();
    }
    
    public void basicSetBounds(Point2D.Double anchor, Point2D.Double lead) 
    {
        super.basicSetBounds(anchor, lead);
        invalidate();
    }
    
    public Rectangle2D.Double getBounds() 
    {
        return super.getBounds();
    }
    
    public Rectangle2D.Double getDrawingArea() 
    {
        if (drawBounds == null) 
        {
            drawBounds = super.getDrawingArea();
            for (Figure child : getChildrenFrontToBack()) 
            {
                if (child.isVisible()) 
                {
                    Rectangle2D.Double childBounds = child.getDrawingArea();
                    if (! childBounds.isEmpty()) 
                    {
                        drawBounds.add(childBounds);
                    }
                }
            }
        }
        return (Rectangle2D.Double) drawBounds.clone();
    }
    
    public boolean contains(Point2D.Double p) 
    {
        if (getDrawingArea().contains(p)) 
        {
            for (Figure child : getChildrenFrontToBack()) 
            {
                if (child.isVisible() && child.contains(p)) 
                	return true;
            }
            return super.contains(p);
        }
        return false;
    }
    
    protected void invalidateBounds() 
    {
        drawBounds = null;
    }
    
    // ATTRIBUTES
    /**
     * Sets an attribute of the figure.
     * AttributeKey name and semantics are defined by the class implementing
     * the figure interface.
     */
    public void setAttribute(AttributeKey key, Object newValue) 
    {
        willChange();
        super.setAttribute(key, newValue);
        if (isAttributeEnabled(key)) 
        {
            if (children != null) 
            {
                for (Figure child : children) 
                {
                    child.setAttribute(key, newValue);
                }
            }
        }
        changed();
    }
    
    public Object getAttribute(AttributeKey key)
    {
    	if(childKey(key))
    	{
    		return text.getAttribute(key);
    	}
    	return super.getAttribute(key);
    }

    public Map<AttributeKey, Object> getAttributes()
    {
    	Map<AttributeKey, Object> attributes;
    	attributes = super.getAttributes();
    	attributes.put(AttributeKeys.TEXT, getAttribute(AttributeKeys.TEXT));
    	return attributes;
    }
    
    public boolean childKey(AttributeKey key)
    {
    	if(key.getKey().equals(AttributeKeys.TEXT.getKey()))
    	    return true;
    	return false;
    }
    
    // EDITING
    public Figure findFigureInside(Point2D.Double p) 
    {
        if (getDrawingArea().contains(p)) 
        {
            Figure found = null;
            for (Figure child : getChildrenFrontToBack()) 
            {
                if (child.isVisible()) 
                {
                    found = child.findFigureInside(p);
                    if (found != null) 
                    {
                        return found;
                    }
                }
            }
        }
        return null;
    }
    
    // COMPOSITE FIGURES
    public java.util.List<Figure> getChildren() 
    {
        return Collections.unmodifiableList(children);
    }
    
    public int getChildCount() 
    {
        return children.size();
    }
    
    public Figure getChild(int index) 
    {
        return children.get(index);
    }
    
    public void set(int index, Figure child) 
    {
        children.set(index, child);
    }
    
    /**
     * Returns an iterator to iterate in
     * Z-order front to back over the children.
     */
    public java.util.List<Figure> getChildrenFrontToBack() 
    {
        return children ==  null ?
            new LinkedList<Figure>() :
            new ReversedList<Figure>(children);
    }
    
    public void add(Figure figure) 
    {
        basicAdd(figure);
        if (getDrawing() != null) 
        {
            figure.addNotify(getDrawing());
        }
    }
    
    public void add(int index, Figure figure) 
    {
        basicAdd(index, figure);
        if (getDrawing() != null) 
        {
            figure.addNotify(getDrawing());
        }
    }
    
    public void basicAdd(Figure figure) 
    {
        basicAdd(children.size(), figure);
    }
    
    public void basicAdd(int index, Figure figure) 
    {
        children.add(index, figure);
        figure.addFigureListener(childHandler);
        figure.addUndoableEditListener(childHandler);
        invalidate();
    }
    
    public boolean remove(final Figure figure) 
    {
        int index = children.indexOf(figure);
        if (index == -1) 
        {
            return false;
        } 
        else 
        {
            willChange();
            basicRemoveChild(index);
            if (getDrawing() != null) 
            {
                figure.removeNotify(getDrawing());
            }
            changed();
            return true;
        }
    }
    
    public Figure removeChild(int index) 
    {
        willChange();
        Figure figure = basicRemoveChild(index);
        if (getDrawing() != null) 
        {
            figure.removeNotify(getDrawing());
        }
        changed();
        return figure;
    }
    
    public boolean basicRemove(final Figure figure) 
    {
        int index = children.indexOf(figure);
        if (index == -1) 
        {
            return false;
        } 
        else 
        {
            basicRemoveChild(index);
            return true;
        }
    }
    
    public Figure basicRemoveChild(int index) 
    {
        Figure figure = children.remove(index);
        figure.removeFigureListener(childHandler);
        figure.removeUndoableEditListener(childHandler);
        
        return figure;
    }
    
    public void removeAllChildren() 
    {
        willChange();
        while (children.size() > 0) 
        {
            Figure figure = basicRemoveChild(children.size() - 1);
            if (getDrawing() != null) {
                figure.removeNotify(getDrawing());
            }
        }
        changed();
    }
    public void basicRemoveAllChildren() 
    {
        while (children.size() > 0) 
        {
            basicRemoveChild(children.size() - 1);
        }
    }
    
    // LAYOUT
    /**
     * Get a Layouter object which encapsulated a layout
     * algorithm for this figure. Typically, a Layouter
     * accesses the child components of this figure and arranges
     * their graphical presentation.
     *
     *
     * @return layout strategy used by this figure
     */
    public Layouter getLayouter() 
    {
        return layouter;
    }
    
    public void setLayouter(Layouter newLayouter) 
    {
        this.layouter = newLayouter;
    }
    
    /**
     * A layout algorithm is used to define how the child components
     * should be laid out in relation to each other. The task for
     * layouting the child components for presentation is delegated
     * to a Layouter which can be plugged in at runtime.
     */
    public void layout() 
    {
        if (getLayouter() != null) 
        {
            Rectangle2D.Double bounds = getBounds();
            Point2D.Double p = new Point2D.Double(bounds.x, bounds.y);
            Rectangle2D.Double r = getLayouter().layout(
                    this, p, p
                    );
            invalidateBounds();
        }
    }
    
// EVENT HANDLING
    
    public void invalidate() 
    {
        super.invalidate();
        invalidateBounds();
    }
    
    public void validate() 
    {
        super.validate();
        layout();
    }
    
    public void addNotify(Drawing drawing) 
    {
        for (Figure child : new LinkedList<Figure>(children)) 
        {
            child.addNotify(drawing);
        }
        super.addNotify(drawing);
    }
    
    public void removeNotify(Drawing drawing) 
    {
        for (Figure child : new LinkedList<Figure>(children)) 
        {
            child.removeNotify(drawing);
        }
        super.removeNotify(drawing);
    }
    
    public BezierAnnotationFigure clone() 
    {
    	BezierAnnotationFigure that = (BezierAnnotationFigure) super.clone();
        that.childHandler = new ChildHandler(that);
        that.children = new ArrayList<Figure>();
        for (Figure thisChild : this.children) 
        {
            Figure thatChild = (Figure) thisChild.clone();
            that.children.add(thatChild);
            thatChild.addFigureListener(that.childHandler);
            thatChild.addUndoableEditListener(that.childHandler);
        }
        return that;
    }
    
    public void remap(HashMap<Figure,Figure> oldToNew) 
    {
        super.remap(oldToNew);
        for (Figure child : children) 
        {
            child.remap(oldToNew);
        }
    }
    
}
