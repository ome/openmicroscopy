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


//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

//Third-party libraries
import org.jhotdraw.draw.AbstractTool;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.util.ResourceBundleUtil;

//Application-internal dependencies

/** 
 * A tool to create new drawing figures.
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
public class DrawingObjectCreationTool
	extends AbstractTool
	implements DrawingCreationTool
{	
	/** Reset the tool to the select tool. */
	private boolean resetToSelect = false;
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
     * Treshold for which we create a larger shape of a minimal size.
     */
    private Dimension minimalSizeTreshold = new Dimension(2,2);
    /**
     * We set the figure to this minimal size, if it is smaller than the
     * minimal size treshold.
     */
    private Dimension minimalSize = new Dimension(10,10);
    /**
     * The prototype for new figures.
     */
    private Figure prototype;
    /**
     * The created figure.
     */
    protected Figure createdFigure;
    
    /** Creates a new instance. */
    public DrawingObjectCreationTool(String prototypeClassName) {
        this(prototypeClassName, null, null);
    }
    public DrawingObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes) {
        this(prototypeClassName, attributes, null);
    }
    public DrawingObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes, String name) {
        try {
            this.prototype = (Figure) Class.forName(prototypeClassName).newInstance();
        } catch (Exception e) {
            InternalError error = new InternalError("Unable to create Figure from "+prototypeClassName);
            error.initCause(e);
            throw error;
        }
        this.prototypeAttributes = attributes;
        if (name == null) {
            ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.draw.Labels");
            name = labels.getString("createFigure");
        }
        this.presentationName = name;
    }
    /** Creates a new instance with the specified prototype but without an
     * attribute set. The CreationTool clones this prototype each time a new
     *  Figure needs to be created. When a new Figure is created, the
     * CreationTool applies the default attributes from the DrawingEditor to it.
     *
     * @param prototype The prototype used to create a new Figure.
     */
    public DrawingObjectCreationTool(Figure prototype) {
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
    public DrawingObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes) {
        this(prototype, attributes, null);
    }
    /**
     * Creates a new instance with the specified prototype and attribute set.
     *
     * @param prototype The prototype used to create a new Figure.
     * @param attributes The CreationTool applies these attributes to the
     * prototype after having applied the default attributes from the DrawingEditor.
     * @param presentationName The presentationName parameter is currently not used.
     */
    public DrawingObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes, String name) {
        this.prototype = prototype;
        this.prototypeAttributes = attributes;
        if (name == null) {
            ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.draw.Labels");
            name = labels.getString("createFigure");
        }
        this.presentationName = name;
    }
    
    public Figure getPrototype() {
        return prototype;
    }
    
    public void activate(DrawingEditor editor) {
        super.activate(editor);
        //getView().clearSelection();
        //getView().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    public void deactivate(DrawingEditor editor) {
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
    
    
    public void mousePressed(MouseEvent evt) {
        super.mousePressed(evt);
        getView().clearSelection();
        createdFigure = createFigure();
        Point2D.Double p = constrainPoint(viewToDrawing(anchor));
        anchor.x = evt.getX();
        anchor.y = evt.getY();
        createdFigure.setBounds(p, p);
        getDrawing().add(createdFigure);
    }
    
    public void mouseDragged(MouseEvent evt) {
        if (createdFigure != null) {
            Point2D.Double p = constrainPoint(new Point(evt.getX(), evt.getY()));
            createdFigure.willChange();
            createdFigure.setBounds(
                    constrainPoint(new Point(anchor.x, anchor.y)),
                    p
                    );
            createdFigure.changed();
        }
    }
    public void mouseReleased(MouseEvent evt) {
        if (createdFigure != null) {
            Rectangle2D.Double bounds = createdFigure.getBounds();
            if (bounds.width == 0 && bounds.height == 0) {
                getDrawing().remove(createdFigure);
                fireToolDone();
            } else {
                if (Math.abs(anchor.x - evt.getX()) < minimalSizeTreshold.width &&
                        Math.abs(anchor.y - evt.getY()) < minimalSizeTreshold.height) {
                    createdFigure.willChange();
                    createdFigure.setBounds(
                            constrainPoint(new Point(anchor.x, anchor.y)),
                            constrainPoint(new Point(
                            anchor.x + (int) Math.max(bounds.width, minimalSize.width),
                            anchor.y + (int) Math.max(bounds.height, minimalSize.height)
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
                getDrawing().fireUndoableEditHappened(new AbstractUndoableEdit() {
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
    protected Figure createFigure() {
        Figure f = (Figure) prototype.clone();
        getEditor().applyDefaultAttributesTo(f);
        if (prototypeAttributes != null) {
            for (Map.Entry<AttributeKey, Object> entry : prototypeAttributes.entrySet()) {
                f.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        return f;
    }
    
    protected Figure getCreatedFigure() {
        return createdFigure;
    }
    protected Figure getAddedFigure() {
        return createdFigure;
    }
    
    /**
     * This method allows subclasses to do perform additonal user interactions
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


