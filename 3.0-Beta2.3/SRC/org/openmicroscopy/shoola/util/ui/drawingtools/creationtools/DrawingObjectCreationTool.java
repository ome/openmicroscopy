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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
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
	extends CreationTool 
	implements DrawingCreationTool
{	
	
	/** The minimal size of the threshold. */
	private static final Dimension MINIMAL_SIZE_THRESHOLD = 
		 							new Dimension(10,10);
	 
	/** The minimal size of the figure. */
	private static final Dimension MINIMAL_SIZE = new Dimension(10,10);
	
	/** Reset the tool to the selection tool after figure creation. */
	private boolean resetToSelect;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param prototype The prototype.
	 */
    public DrawingObjectCreationTool(Figure prototype)
    {
        super(prototype, null, null);
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
     * Overriddent to handle the created object when the mouse is released.
     * @see CreationTool#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent evt)
    {
    	if (createdFigure == null) return;
    	Rectangle2D.Double bounds = createdFigure.getBounds();
        if (bounds.width == 0 && bounds.height == 0) {
            getDrawing().remove(createdFigure);
        } else {
            if (Math.abs(anchor.x-evt.getX()) < MINIMAL_SIZE_THRESHOLD.width && 
               Math.abs(anchor.y-evt.getY()) < MINIMAL_SIZE_THRESHOLD.height) {
                createdFigure.setBounds(
                        constrainPoint(new Point(anchor.x, anchor.y)),
                        constrainPoint(new Point(
                        anchor.x + (int) Math.max(bounds.width, 
                        		MINIMAL_SIZE.width), 
                        anchor.y + (int) Math.max(bounds.height, 
                        		MINIMAL_SIZE.height)
                        ))
                        );
            }
            getView().addToSelection(createdFigure);
        }
        if (createdFigure instanceof CompositeFigure) {
            ((CompositeFigure) createdFigure).layout();
        }
        final Figure addedFigure = createdFigure;
        final Drawing addedDrawing = getDrawing();
        getDrawing().fireUndoableEditHappened(new AbstractUndoableEdit() {
           public String getPresentationName() {
               return getPresentationName();
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


