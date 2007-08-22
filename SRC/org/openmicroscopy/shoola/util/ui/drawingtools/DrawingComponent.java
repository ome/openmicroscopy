/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvas 
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
package org.openmicroscopy.shoola.util.ui.drawingtools;


//Java imports
import java.util.ArrayList;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

/** 
 * This is a wrapper class, managing the creation and linking of the JHotdraw
 * management objects.
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
public class DrawingComponent
		implements DrawingListener
{	
	/** Component managaging the drawing. */
    private	DefaultDrawing				drawing;

    /** Component managaging the drawing. */
	private	DrawingEditor				drawingEditor;
	
	/** Component hosting the drawing. */
	private DrawingCanvasView			drawingView;
	
	/** List of figure listeners. */
	private ArrayList<FigureListener> 	figureListeners;
	
	/** 
	 * This variable is true if the drawingCanvas will attach all the figure
	 * listeners in the figureListeners arraylist to newly created figures. 
	 */
	private boolean 				createListeners;
	
	/** 
	 * Constructor for the drawing canvas. This creates and links the 
	 * defaultDrawing, drawing editro and drawingViews. 
	 */
	public DrawingComponent()
	{
		drawingEditor = new DefaultDrawingEditor();
		drawing = new DefaultDrawing();
		drawingView = new DrawingCanvasView();
		drawingView.setDrawing(drawing);
		drawingEditor.add(drawingView);
		createListeners = false;
	}
	
	/**
	 * Return true if the arraylist of figureListeners will be added to
	 * all newly created figures. 
	 */
	public boolean willCreateFigureListeners()
	{
		return createListeners;
	}
	
	/** 
	 * Set up the listener structure so that all figure listeners in the 
	 * figure listner arraylist will be added to the createdfigures.
	 */ 
	public void createFigureListeners()
	{
		createListeners = true;
		addDrawingListener(this);
	}
	
	/**
	 * Get the DefaultDrawing this is the model for the drawing. 
	 * @return see above.
	 */
	public DefaultDrawing getDrawing()
	{
		return drawing;
	}

	/**
	 * Get the drawing editor, this stores the undo information for the 
	 * drawing.
	 * @return see above.
	 */
	public DrawingEditor getEditor()
	{
		return drawingEditor;
	}
	
	/**
	 * Get the Drawing view.
	 * @return see above.
	 */
	public DrawingCanvasView getDrawingView()
	{
		return drawingView;
	}
	
	/** 
	 * Add Drawing Listener
	 * @param listener the drawing listener.  
	 **/
	public void addDrawingListener(DrawingListener listener)
	{
		drawing.addDrawingListener(listener);
	}
	
	/** 
	 * Add Figure selection listener.
	 * @param listener the figure selection listener. 
	 */
	public void addFigureSelectionListener(FigureSelectionListener listener)
	{
		drawingView.addFigureSelectionListener(listener);
	}
	
	/**
	 *  Add Figure listener.
	 *  @param listener this is the listener which will be added to a list 
	 *  that is then added to any figure which is created.  
	 **/
	public void addFigureListener(FigureListener listener)
	{
		figureListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#areaInvalidated(org.jhotdraw.draw.DrawingEvent)
	 */
	public void areaInvalidated(DrawingEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#figureAdded(org.jhotdraw.draw.DrawingEvent)
	 */
	public void figureAdded(DrawingEvent e)
	{
		for(int i = 0 ; i < figureListeners.size(); i++)
			e.getFigure().addFigureListener(figureListeners.get(i));
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#figureRemoved(org.jhotdraw.draw.DrawingEvent)
	 */
	public void figureRemoved(DrawingEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	
	
}


