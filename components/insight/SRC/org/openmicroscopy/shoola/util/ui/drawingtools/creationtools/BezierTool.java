/*
* org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.BezierTool
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.creationtools;


//Java imports
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;


//Third-party libraries
import org.jhotdraw.draw.AbstractTool;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.Bezier;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.util.ResourceBundleUtil;

import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.BezierTextFigure;


/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BezierTool
	extends AbstractTool 
{
    /**
     * Set this to true to turn on debugging output on System.out.
     */
    private final static boolean DEBUG = false;
    
    private final boolean clickOnly = true;
    private final double error = 1;
    private Boolean finishWhenMouseReleased;
    protected Map<AttributeKey, Object> attributes;
    /**
     * The prototype for new figures.
     */
    private BezierTextFigure prototype;
    /**
     * The created figure.
     */
    protected BezierTextFigure createdFigure;
    
    private int nodeCountBeforeDrag;
    /**
     * A localized name for this tool. The presentationName is displayed by the
     * UndoableEdit.
     */
    private String presentationName;
    
    /** Creates a new instance. */
    public BezierTool(BezierTextFigure prototype) {
        this(prototype, null);
    }
    /** Creates a new instance. */
    public BezierTool(BezierTextFigure prototype, Map attributes) {
        this(prototype, attributes, null);
    }
    public BezierTool(BezierTextFigure prototype, Map attributes, String name) {
        this.prototype = prototype;
        this.attributes = attributes;
        if (name == null) {
            ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.draw.Labels");
            name = labels.getString("createFigure");
        }
        this.presentationName = name;
    }

    public String getPresentationName() {
        return presentationName;
    }
    
    public void activate(DrawingEditor editor) {
        super.activate(editor);
        getView().clearSelection();
        getView().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    public void deactivate(DrawingEditor editor) {
        super.deactivate(editor);
        getView().setCursor(Cursor.getDefaultCursor());
        if (createdFigure != null) {
           
            finishCreation(createdFigure);
            createdFigure = null;
        }
    }
    
    public void mousePressed(MouseEvent evt) {
        if (DEBUG) System.out.println("BezierTool.mousePressed "+evt);
        super.mousePressed(evt);
        if (createdFigure == null) {
            finishWhenMouseReleased = null;
            
            createdFigure = createFigure();
            createdFigure.addNode(new BezierPath.Node(
                    getView().getConstrainer().constrainPoint(
                    getView().viewToDrawing(anchor)
                    )));
          //work around since the font size is reset when the figure is added.
            Object s = createdFigure.getAttribute(MeasurementAttributes.FONT_SIZE);
            getDrawing().add(createdFigure);
            createdFigure.setAttribute(MeasurementAttributes.FONT_SIZE, s);
            
            nodeCountBeforeDrag = createdFigure.getNodeCount();
        } else {
            if (evt.getClickCount() == 1) {
                addPointToFigure(getView().getConstrainer().constrainPoint(
                        getView().viewToDrawing(anchor)));
            }
            nodeCountBeforeDrag = createdFigure.getNodeCount();
        }
    }
    
    protected BezierTextFigure createFigure() {
    	BezierTextFigure f = (BezierTextFigure) prototype.clone();
        getEditor().applyDefaultAttributesTo(f);
        if (attributes != null) {
            for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
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
    
    protected void addPointToFigure(Point2D.Double newPoint) {
        int pointCount = createdFigure.getNodeCount();
        
        createdFigure.willChange();
        if (pointCount < 2) {
            createdFigure.addNode(new BezierPath.Node(newPoint));
        } else {
            Point2D.Double endPoint = createdFigure.getEndPoint();
            Point2D.Double secondLastPoint = (pointCount <= 1) ?
                endPoint :
                createdFigure.getPoint(pointCount - 2, 0);
            if (newPoint.equals(endPoint)) {
                // nothing to do
            } else if (pointCount > 1 && Geom.lineContainsPoint(newPoint.x, newPoint.y, secondLastPoint.x, secondLastPoint.y, endPoint.x, endPoint.y, 0.9f / getView().getScaleFactor())) {
                createdFigure.setPoint(pointCount - 1, 0, newPoint);
            } else {
                createdFigure.addNode(new BezierPath.Node(newPoint));
            }
        }
        createdFigure.changed();
    }
    
    public void mouseClicked(MouseEvent evt) {
        if (createdFigure != null) {
            switch (evt.getClickCount()) {
                case 1 :
                    if (createdFigure.getNodeCount() > 2) {
                        Rectangle r = new Rectangle(getView().drawingToView(createdFigure.getStartPoint()));
                        r.grow(2,2);
                        if (r.contains(evt.getX(), evt.getY())) {
                            createdFigure.setClosed(true);
                           
                            finishCreation(createdFigure);
                            createdFigure = null;
                            fireToolDone();
                        }
                    }
                    break;
                case 2 :
                    finishWhenMouseReleased = null;
                   
                    finishCreation(createdFigure);
                    /*
                    getView().addToSelection(createdFigure);
                     */
                    createdFigure = null;
                    fireToolDone();
                    break;
            }
        }
    }
    
    protected void fireUndoEvent(Figure createdFigure) {
        final Figure addedFigure = createdFigure;
        final Drawing addedDrawing = getDrawing();
        final DrawingView addedView = getView();
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
                addedView.clearSelection();
                addedDrawing.add(addedFigure);
                addedView.addToSelection(addedFigure);
            }
        });
    }
    public void mouseReleased(MouseEvent evt) {
    	if (!clickOnly)
    	{
    	if (DEBUG) System.out.println("BezierTool.mouseReleased "+evt);
        if (finishWhenMouseReleased == Boolean.TRUE) {
            if (createdFigure.getNodeCount() > 2) {
                BezierPath fittedPath = Bezier.fitBezierCurve(createdFigure.getBezierPath(), error);
                createdFigure.willChange();
                createdFigure.setBezierPath(fittedPath);
                createdFigure.changed();
               
                finishCreation(createdFigure);
                createdFigure = null;
                finishWhenMouseReleased = null;
                fireToolDone();
                return;
            }
        } else if (finishWhenMouseReleased == null) {
            finishWhenMouseReleased = Boolean.FALSE;
        }
        
        if (createdFigure.getNodeCount() > nodeCountBeforeDrag + 1) {
            createdFigure.willChange();
            BezierPath fittedPath = new BezierPath();
            for (int i=nodeCountBeforeDrag, n = createdFigure.getNodeCount(); i < n; i++) {
                fittedPath.add(createdFigure.getNode(nodeCountBeforeDrag));
                createdFigure.removeNode(nodeCountBeforeDrag);
            }
            fittedPath = Bezier.fitBezierCurve(fittedPath, error);
            for (BezierPath.Node node : fittedPath) {
                createdFigure.addNode(node);
            }
            nodeCountBeforeDrag = createdFigure.getNodeCount();
            createdFigure.changed();
        }
    	}
    }
    
    protected void finishCreation(BezierTextFigure createdFigure) {
        getView().addToSelection(createdFigure);
        fireUndoEvent(createdFigure);
    }
    
    public void mouseDragged(MouseEvent evt) {
    	if (createdFigure != null) {
            if (finishWhenMouseReleased == null) {
               finishWhenMouseReleased = Boolean.TRUE;
           }
            
            if(!clickOnly)
        	{
        	   int x = evt.getX();
           int y = evt.getY();
            
            addPointToFigure(getView().viewToDrawing(new Point(x, y)));
        }}
    }
    public void mouseMoved(MouseEvent evt) {
        /*
        if (createdFigure != null) {
        }*/
    }
}
