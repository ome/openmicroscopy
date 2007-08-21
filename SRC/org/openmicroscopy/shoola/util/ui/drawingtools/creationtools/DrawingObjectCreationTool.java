/*
 * org.openmicroscopy.shoola.agents.measurement.util.ObjectCreationTool 
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
import java.util.Map;

//Third-party libraries

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.Figure;

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
public class DrawingObjectCreationTool
	extends CreationTool 
	implements DrawingCreationTool
{	
	/** Reset the tool to the selection tool after figure creation. */
	private boolean resetToSelect = false;
	
    private Dimension minimalSizeTreshold = new Dimension(10,10);
    /**
     * We set the figure to the minimal size, if it is smaller than the minimal 
     * size treshold.
     */
    private Dimension minimalSize = new Dimension(10,10);
  
    /** Creates a new instance. */
    public DrawingObjectCreationTool(String prototypeClassName) {
        super(prototypeClassName, null, null);
    }
    public DrawingObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes) {
    	super(prototypeClassName, attributes, null);
    }
    
    public DrawingObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes, String name) {
    	super(prototypeClassName, attributes, name);
    }
    
    public DrawingObjectCreationTool(Figure prototype) {
        super(prototype, null, null);
    }
    /** Creates a new instance. */
    public DrawingObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes) {
        super(prototype, attributes, null);
    }
    /** Creates a new instance. */
    public DrawingObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes, String name) {
    	super(prototype, attributes, name);
    }
        
    public void mouseReleased(MouseEvent evt) {
        if (createdFigure != null) {
            Rectangle2D.Double bounds = createdFigure.getBounds();
            if (bounds.width == 0 && bounds.height == 0) {
                getDrawing().remove(createdFigure);
            } else {
                if (Math.abs(anchor.x - evt.getX()) < minimalSizeTreshold.width && 
                        Math.abs(anchor.y - evt.getY()) < minimalSizeTreshold.height) {
                    createdFigure.basicSetBounds(
                            constrainPoint(new Point(anchor.x, anchor.y)),
                            constrainPoint(new Point(
                            anchor.x + (int) Math.max(bounds.width, minimalSize.width), 
                            anchor.y + (int) Math.max(bounds.height, minimalSize.height)
                            ))
                            );
                }
                getView().addToSelection(createdFigure);
            }
            if (createdFigure instanceof CompositeFigure) {
                ((CompositeFigure) createdFigure).layout();
            }
            getDrawing().fireUndoableEditHappened(creationEdit);
            creationFinished(createdFigure);
            createdFigure = null;
        }
    }
    
    /**
     * This method allows subclasses to do perform additonal user interactions
     * after the new figure has been created.
     * The implementation of this class just invokes fireToolDone.
     */
    protected void creationFinished(Figure createdFigure) 
    {
        if(resetToSelect)
        	fireToolDone();
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


