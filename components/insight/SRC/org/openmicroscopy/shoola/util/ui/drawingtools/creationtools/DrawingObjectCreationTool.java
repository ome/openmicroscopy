/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingObjectCreationTool 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.creationtools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.jhotdraw.draw.AbstractTool;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.util.ResourceBundleUtil;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;

/** 
 * A tool to create new drawing figures.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class DrawingObjectCreationTool
	extends AbstractTool
	implements DrawingCreationTool
{	
	
	/** Reset the tool to the select tool. */
	private boolean resetToSelect;
	
	 /**
     * Attributes to be applied to the created ConnectionFigure.
     * These attributes override the default attributes of the
     * DrawingEditor.
     */
    private Map<AttributeKey, Object> prototypeAttributes;
    
    /**
     * A localized name for this tool. The presentationName is displayed by the
     * UndoableEdit.
     */
    private String presentationName;
    
    /**
     * Threshold for which we create a larger shape of a minimal size.
     */
    private Dimension minimalSizeTreshold = new Dimension(2, 2);
    
    /**
     * We set the figure to this minimal size, if it is smaller than the
     * minimal size threshold.
     */
    private Dimension minimalSize = new Dimension(10, 10);
    
    /** The prototype for new figures. */
    private Figure prototype;
    
    /** The created figure. */
    protected Figure createdFigure;
    
    /**
     * Creates a new instance.
     * 
     * @param prototypeClassName The type of prototype to create.
     */
    public DrawingObjectCreationTool(String prototypeClassName)
    {
        this(prototypeClassName, null, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param prototypeClassName The type of prototype to create.
     * @param attributes The attributes to add.
     */
    public DrawingObjectCreationTool(String prototypeClassName, 
    		Map<AttributeKey, Object> attributes)
    {
        this(prototypeClassName, attributes, null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param prototypeClassName The type of prototype to create.
     * @param attributes The attributes to add.
     * @param name The name to display.
     */
    public DrawingObjectCreationTool(String prototypeClassName, 
    		Map<AttributeKey, Object> attributes, String name)
    {
        try {
            this.prototype = 
            	(Figure) Class.forName(prototypeClassName).newInstance();
        } catch (Exception e) {
            InternalError error = new InternalError(
            		"Unable to create Figure from "+prototypeClassName);
            error.initCause(e);
            throw error;
        }
        this.prototypeAttributes = attributes;
        if (name == null) {
            ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle(
            		"org.jhotdraw.draw.Labels");
            name = labels.getString("createFigure");
        }
        this.presentationName = name;
    }
    
    /** 
     * Creates a new instance with the specified prototype but without an
     * attribute set. The CreationTool clones this prototype each time a new
     *  Figure needs to be created. When a new Figure is created, the
     * CreationTool applies the default attributes from the DrawingEditor to it.
     *
     * @param prototype The prototype used to create a new Figure.
     */
    public DrawingObjectCreationTool(Figure prototype)
    {
        this(prototype, null, null);
    }
    
    /** Creates a new instance with the specified prototype but without an
     * attribute set. The CreationTool clones this prototype each time a new
     * Figure needs to be created. When a new Figure is created, the
     * CreationTool applies the default attributes from the DrawingEditor to it,
     * and then it applies the attributes to it, that have been supplied in
     * this constructor.
     *
     * @param prototype The prototype used to create a new Figure.
     * @param attributes The CreationTool applies these attributes to the
     * prototype after having applied the default attributes from the DrawingEditor.
     */
    public DrawingObjectCreationTool(Figure prototype, 
    		Map<AttributeKey, Object> attributes)
    {
        this(prototype, attributes, null);
    }
    
    /**
     * Creates a new instance with the specified prototype and attribute set.
     *
     * @param prototype The prototype used to create a new Figure.
     * @param attributes The CreationTool applies these attributes to the
     * prototype after having applied the default attributes from the DrawingEditor.
     * @param name The presentationName parameter is currently not used.
     */
    public DrawingObjectCreationTool(Figure prototype, 
    		Map<AttributeKey, Object> attributes, String name)
    {
        this.prototype = prototype;
        this.prototypeAttributes = attributes;
        if (name == null) {
            ResourceBundleUtil labels = 
            	ResourceBundleUtil.getLAFBundle("org.jhotdraw.draw.Labels");
            name = labels.getString("createFigure");
        }
        this.presentationName = name;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes The CreationTool applies these attributes to the
     * prototype after having applied the default attributes from the DrawingEditor.
     */
    public void setAttributes(Map<AttributeKey, Object> attributes)
    {
        prototypeAttributes = attributes;
    }
    
    /**
     * Returns the prototype.
     * 
     * @return See above.
     */
    public Figure getPrototype() { return prototype; }
    
    /**
     * Overridden to set the cursor and reset the figure.
     * @see AbstractTool#activate(DrawingEditor)
     */
    public void activate(DrawingEditor editor)
    {
        super.activate(editor);
        //getView().clearSelection();
        //getView().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    /**
     * Overridden to set the cursor and reset the figure.
     * @see AbstractTool#deactivate(DrawingEditor)
     */
    public void deactivate(DrawingEditor editor)
    {
        super.deactivate(editor);
        if (getView() != null) {
            getView().setCursor(Cursor.getDefaultCursor());
        }
        if (createdFigure != null) {
            if (createdFigure instanceof CompositeFigure) {
                ((CompositeFigure) createdFigure).layout();
            }
            createdFigure = null;
        }
    }
    
    /**
     * Handles the figure is not <code>null</code>.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent evt)
    {
        super.mousePressed(evt);
        getView().clearSelection();
        createdFigure = createFigure();
        Point2D.Double p = constrainPoint(viewToDrawing(anchor));
        anchor.x = evt.getX();
        anchor.y = evt.getY();
        createdFigure.setBounds(p, p);
        //work around since the font size is reset when the figure is added.
        Object s = createdFigure.getAttribute(MeasurementAttributes.FONT_SIZE);
        getDrawing().add(createdFigure);
        createdFigure.setAttribute(MeasurementAttributes.FONT_SIZE, s);
    }
    
    /**
     * Handles the figure is not <code>null</code>.
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent evt)
    {
        if (createdFigure != null) {
            Point2D.Double p = constrainPoint(new Point(evt.getX(), evt.getY()));
            createdFigure.willChange();
            
            createdFigure.setBounds(
                    constrainPoint(new Point(anchor.x, anchor.y)), p );
            createdFigure.changed();
        }
    }
    
    /**
     * Handles the figure is not <code>null</code>.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent evt)
    {
        if (createdFigure != null) {
            Rectangle2D.Double bounds = createdFigure.getBounds();
            if (bounds.width == 0 && bounds.height == 0) {
                getDrawing().remove(createdFigure);
                fireToolDone();
            } else {
                if (Math.abs(anchor.x - evt.getX()) < 
                		minimalSizeTreshold.width &&
                        Math.abs(anchor.y - evt.getY()) < 
                        minimalSizeTreshold.height) {
                    createdFigure.willChange();
                    createdFigure.setBounds(
                            constrainPoint(new Point(anchor.x, anchor.y)),
                            constrainPoint(new Point(
                            anchor.x + (int) Math.max(bounds.width, 
                            		minimalSize.width),
                            anchor.y + (int) Math.max(bounds.height, 
                            		minimalSize.height)
                            ))
                            );
                    createdFigure.changed();
                }
                getView().addToSelection(createdFigure);
                if (createdFigure instanceof CompositeFigure) {
                    ((CompositeFigure) createdFigure).layout();
                }
                final Figure addedFigure = createdFigure;
                final Drawing addedDrawing = getDrawing();
                getDrawing().fireUndoableEditHappened(
                		new AbstractUndoableEdit() {
                    public String getPresentationName() {
                        return presentationName;
                    }
                    public void undo() throws CannotUndoException {
                        super.undo();
                        addedDrawing.remove(addedFigure);
                    }
                    public void redo() throws CannotRedoException {
                        super.redo();
                        addedDrawing.add(addedFigure);
                    }
                });
                creationFinished(createdFigure);
            }
        } else {
            fireToolDone();
        }
    }
    
    /**
     * Creates a figure.
     * 
     * @return See above.
     */
    protected Figure createFigure()
    {
        Figure f = (Figure) prototype.clone();
        getEditor().applyDefaultAttributesTo(f);
        if (prototypeAttributes != null) {
            for (Map.Entry<AttributeKey, Object> 
            entry : prototypeAttributes.entrySet()) {
                f.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        return f;
    }
    
    /**
     * Returns the created figure.
     * 
     * @return See above.
     */
    protected Figure getCreatedFigure() { return createdFigure; }
    
    /**
     * Returns the added figure.
     * 
     * @return See above.
     */
    protected Figure getAddedFigure() { return createdFigure; }
    
    /**
     * This method allows subclasses to do perform additional user interactions
     * after the new figure has been created.
     * The implementation of this class just invokes fireToolDone.
     * 
     * @param createdFigure The newly created figure.
     */
    protected void creationFinished(Figure createdFigure) 
    {
        if (resetToSelect) fireToolDone();
    }

	/**
	 * Implemented as specified by the {@link DrawingCreationTool} I/F.
	 * @see DrawingCreationTool#isResetToSelect()
	 */
	public boolean isResetToSelect() { return resetToSelect; }
	
	/**
	 * Implemented as specified by the {@link DrawingCreationTool} I/F.
	 * @see DrawingCreationTool#setResetToSelect(boolean)
	 */
	public void setResetToSelect(boolean create)
	{
		resetToSelect = create;
	}
	
}


