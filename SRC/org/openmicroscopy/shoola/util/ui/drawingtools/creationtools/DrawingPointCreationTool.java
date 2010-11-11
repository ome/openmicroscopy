/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingPointCreationTool 
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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

//Third-party libraries
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.PointFigure;

/** 
 * A tool to create Drawing point figure.
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
public class DrawingPointCreationTool 
	extends CreationTool
	implements DrawingCreationTool
{
	
	/**
	 * ResetToSelect will change the tool to the selection tool after 
	 * figure creation. 
	 */
	private boolean	resetToSelect;

	/**
	 * Sets the bounds of the figure.
	 * 
	 * @param x	The x-coordinate of the mouse.
	 * @param y	The y-coordinate of the mouse.
	 */
	private void setBasicBounds(int x, int y)
	{
		double size = PointFigure.FIGURE_SIZE/2;
		Point2D.Double p = constrainPoint(viewToDrawing(new 
			Point((int) (x-size), (int) (y-size))));
		Point2D.Double p2 = constrainPoint(viewToDrawing(new 
			Point((int) (x+size), (int) (y+size))));
		createdFigure.willChange();
		createdFigure.setBounds(p, p2);
		createdFigure.changed();
	}
	/**
	 * Creates a new instance.
	 * 
	 * @param prototype The prototype.
	 */
	public DrawingPointCreationTool(Figure prototype)
	{
		super(prototype);
	}
		
	/**
	 * Overridden to set modify the basic bounds.
	 * @see CreationTool#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent evt)
	{	
		super.mousePressed(evt);
		setBasicBounds(evt.getX(), evt.getY());
	}
	
	/**
	 * Overridden to set modify the basic bounds.
	 * @see CreationTool#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent evt)
	{
		if (createdFigure == null) return;
		setBasicBounds(evt.getX(), evt.getY());
	}
	
	/**
	 * Overridden to set modify the basic bounds.
	 * @see CreationTool#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent evt)
	{
		if (createdFigure == null) return;
		Rectangle2D.Double bounds = createdFigure.getBounds();
		if (bounds.width == 0 && bounds.height == 0)
			getDrawing().remove(createdFigure);
		else
		{
			Point2D p = createdFigure.getStartPoint();
			Point2D p1 = createdFigure.getEndPoint();
			double width = Math.abs(p.getX()-p1.getX());
			if (width < PointFigure.FIGURE_SIZE)
			{
				double size = PointFigure.FIGURE_SIZE/2;
				Point2D	centre = new Point2D.Double(
					createdFigure.getBounds().getCenterX(),
					createdFigure.getBounds().getCenterY());
				Point2D.Double newP1 = new Point2D.Double(
					centre.getX()-size, centre.getY()-size);
				Point2D.Double newP2 = new Point2D.Double(
					centre.getX()+size, centre.getY()+size);
				createdFigure.willChange();
				createdFigure.setBounds(newP1, newP2);
				createdFigure.changed();
			}
			getView().addToSelection(createdFigure);
		}
		if (createdFigure instanceof CompositeFigure)
		{
			((CompositeFigure) createdFigure).layout();
		}
		 final Figure addedFigure = createdFigure;
         final Drawing addedDrawing = getDrawing();
         getDrawing().fireUndoableEditHappened(new AbstractUndoableEdit() {
            public String getPresentationName() {
                return super.getPresentationName();
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
		createdFigure = null;
	}
		
	/**
	 * Overridden to fire an event only if the {@link #resetToSelect} flag
	 * is <code>true</code>.
	 * @see CreationTool#creationFinished(Figure)
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
