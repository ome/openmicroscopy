/*
 * org.openmicroscopy.shoola.agents.measurement.util.MeasurementBezierTool 
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
package org.openmicroscopy.shoola.agents.measurement.util;

//Java imports
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Map;

//Third-party libraries
import org.jhotdraw.draw.AbstractTool;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.geom.Bezier;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.undo.CompositeEdit;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

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
public class MeasurementBezierTool
	extends AbstractTool
	implements MeasureCreationTool
{	
	private boolean resetToSelect = false;
	 private Boolean finishWhenMouseReleased;
	  protected Map<AttributeKey, Object> attributes;
	    /**
	     * The prototype for new figures.
	     */
	    private MeasureBezierFigure prototype;
	    /**
	     * The created figure.
	     */
	    protected MeasureBezierFigure createdFigure;
	    
	    private CompositeEdit creationEdit;
	    
	    private int nodeCountBeforeDrag;
	    
	    /** Creates a new instance. */
	    public MeasurementBezierTool(MeasureBezierFigure prototype) {
	        this(prototype, null);
	    }
	    /** Creates a new instance. */
	    public MeasurementBezierTool(MeasureBezierFigure prototype, Map attributes) {
	        this.prototype = prototype;
	        this.attributes = attributes;
	    }
	    /*
	    public void draw(Graphics2D g) {
	        if (createdFigure != null
	        && mouseLocation != null) {
	            Point2D.Double p = createdFigure.getEndPoint();
	            g.setColor(Color.black);
	            g.setStroke(new BasicStroke());
	            g.drawLine(p.x, p.y, mouseLocation.x, mouseLocation.y);
	        }
	     
	    }
	     */
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
	            getDrawing().fireUndoableEditHappened(creationEdit);
	        }
	    }
	    
	    public void mousePressed(MouseEvent evt) {
	        super.mousePressed(evt);
	        if (createdFigure == null) {
	            finishWhenMouseReleased = null;
	            
	            creationEdit = new CompositeEdit("Figur erstellen");
	            getDrawing().fireUndoableEditHappened(creationEdit);
	            createdFigure = createFigure();
	            createdFigure.addNode(new BezierPath.Node(
	                    getView().getConstrainer().constrainPoint(
	                    getView().viewToDrawing(anchor)
	                    )));
	            getDrawing().add(createdFigure);
	            nodeCountBeforeDrag = createdFigure.getNodeCount();
	        } else {
	            if (evt.getClickCount() == 1) {
	                addPointToFigure(getView().getConstrainer().constrainPoint(
	                        getView().viewToDrawing(anchor)));
	            }
	            nodeCountBeforeDrag = createdFigure.getNodeCount();
	        }
	    }
	    
	    protected MeasureBezierFigure createFigure() {
	    	MeasureBezierFigure f = (MeasureBezierFigure) prototype.clone();
	        getEditor().applyDefaultAttributesTo(f);
	        if (attributes != null) {
	            for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
	                f.setAttribute(entry.getKey(), entry.getValue());
	            }
	        }
	        return f;
	    }
	    
	    protected ROIFigure getCreatedFigure() {
	        return createdFigure;
	    }
	    protected ROIFigure getAddedFigure() {
	        return createdFigure;
	    }
	    
	    protected void addPointToFigure(Point2D.Double newPoint) {
	        int pointCount = createdFigure.getNodeCount();
	        
	        createdFigure.willChange();
	        if (pointCount < 2) {
	            createdFigure.basicAddNode(new BezierPath.Node(newPoint));
	        } else {
	            Point2D.Double endPoint = createdFigure.getEndPoint();
	            Point2D.Double secondLastPoint = (pointCount <= 1) ?
	                endPoint :
	                createdFigure.getPoint(pointCount - 2, 0);
	            if (newPoint.equals(endPoint)) {
	                // nothing to do
	            } else if (pointCount > 1 && Geom.lineContainsPoint(newPoint.x, newPoint.y, secondLastPoint.x, secondLastPoint.y, endPoint.x, endPoint.y, 0.9f / getView().getScaleFactor())) {
	                createdFigure.basicSetPoint(pointCount - 1, 0, newPoint);
	            } else {
	                createdFigure.basicAddNode(new BezierPath.Node(newPoint));
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
	                            // getView().addToSelection(createdFigure);
	                            finishCreation(createdFigure);
	                            getDrawing().fireUndoableEditHappened(creationEdit);
	                            createdFigure = null;
	        	                if(resetToSelect)
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
	                    getDrawing().fireUndoableEditHappened(creationEdit);
	                    createdFigure = null;
		                if(resetToSelect)
		                	fireToolDone();
	 	                break;
	            }
	        }
	    }
	    public void mouseReleased(MouseEvent evt) {
	        if (finishWhenMouseReleased == Boolean.TRUE) {
	            if (createdFigure.getNodeCount() > 2) {
	                BezierPath fittedPath = Bezier.fitBezierCurve(createdFigure.getBezierPath(), 1);
	                createdFigure.willChange();
	                createdFigure.basicSetBezierPath(fittedPath);
	                createdFigure.changed();
	                finishCreation(createdFigure);
	                createdFigure = null;
	                getDrawing().fireUndoableEditHappened(creationEdit);
	                finishWhenMouseReleased = null;
	                if(resetToSelect)
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
	                createdFigure.measureBasicRemoveNode(nodeCountBeforeDrag);
	            }
	            fittedPath = Bezier.fitBezierCurve(fittedPath, 1);
	            for (BezierPath.Node node : fittedPath) {
	                createdFigure.basicAddNode(node);
	            }
	            nodeCountBeforeDrag = createdFigure.getNodeCount();
	            createdFigure.changed();
	        }
	        
	    }
	    
	    protected void finishCreation(MeasureBezierFigure createdFigure) {
	        getView().addToSelection(createdFigure);
	    }
	    
	    public void mouseDragged(MouseEvent evt) {
	        if (createdFigure != null) {
	            if (finishWhenMouseReleased == null) {
	                finishWhenMouseReleased = Boolean.TRUE;
	            }
	            
	            int x = evt.getX();
	            int y = evt.getY();
	            
	            addPointToFigure(getView().viewToDrawing(new Point(x, y)));
	        }
	    }
	    public void mouseMoved(MouseEvent evt) {
	    	/*
	        if (createdFigure != null) {
	        }*/
	    }
		/* (non-Javadoc)
		 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#isResetToSelect()
		 */
		public boolean isResetToSelect()
		{
			return resetToSelect;
		}
		/* (non-Javadoc)
		 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#setResetToSelect(boolean)
		 */
		public void setResetToSelect(boolean create)
		{
			resetToSelect = create;
		}
}


