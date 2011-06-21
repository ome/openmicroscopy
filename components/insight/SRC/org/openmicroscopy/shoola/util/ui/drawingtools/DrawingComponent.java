/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingComponent
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
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionListener;

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
	
	/** Component managing the drawing. */
    private	DefaultDrawing				drawing;

    /** Component managing the drawing. */
	private	DrawingEditor				drawingEditor;
	
	/** Component hosting the drawing. */
	private DrawingCanvasView			drawingView;
	
	/** List of figure listeners. */
	private List<FigureListener> 	figureListeners;
	
	/** 
	 * This variable is true if the drawingCanvas will attach all the figure
	 * listeners in the figureListeners list to newly created figures. 
	 */
	private boolean 					createListeners;
	
	/** 
	 * Constructor for the drawing canvas. This creates and links the 
	 * defaultDrawing, drawing editor and drawingViews. 
	 */
	public DrawingComponent()
	{
		drawingEditor = new DefaultDrawingEditor();
		drawing = new DefaultDrawing();
		drawingView = new DrawingCanvasView();
		drawingView.setDrawing(drawing);
		drawingEditor.add(drawingView);
		createListeners = false;
		figureListeners = new ArrayList<FigureListener>();
	}
	
	/**
	 * Returns <code>true</code>if the list of figureListeners will be added to
	 * all newly created figures, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean willCreateFigureListeners() { return createListeners; }
	
	/** 
	 * Sets up the listener structure so that all figure listeners in the 
	 * figure listener list will be added to the created figures.
	 */ 
	public void createFigureListeners()
	{
		createListeners = true;
		addDrawingListener(this);
	}
	
	/**
	 * Returns the DefaultDrawing this is the model for the drawing. 
	 * 
	 * @return See above.
	 */
	public DefaultDrawing getDrawing() { return drawing; }

	/**
	 * Returns the drawing editor, this stores the undo information for the 
	 * drawing.
	 * 
	 * @return See above.
	 */
	public DrawingEditor getEditor() { return drawingEditor; }
	
	/**
	 * Returns the Drawing view.
	 * 
	 * @return See above.
	 */
	public DrawingCanvasView getDrawingView() { return drawingView; }
	
	/** 
	 * Adds the passed Drawing Listener.
	 * 
	 * @param listener The drawing listener to add.
	 **/
	public void addDrawingListener(DrawingListener listener)
	{
		if (listener == null) return;
		drawing.addDrawingListener(listener);
	}
	
	/** 
	 * Adds the passed Figure selection listener.
	 * 
	 * @param listener The figure selection listener to add. 
	 */
	public void addFigureSelectionListener(FigureSelectionListener listener)
	{
		if (listener == null) return;
		drawingView.addFigureSelectionListener(listener);
	}
	
	/**
	 * Adds Figure listener.
	 * 
	 * @param listener	This is the listener which will be added to a list 
	 * 					that is then added to any figure which is created.  
	 */
	public void addFigureListener(FigureListener listener)
	{
		if (listener == null) return;
		figureListeners.add(listener);
	}

	/**
	 * Remove the figure from the drawing.
	 * @param f figure to remove. 
	 */
	public void removeFigure(Figure f) { drawing.remove(f); }
	
	/**
	 * Remove the figure from the drawing.
	 * @param f figure to remove. 
	 */
	public void addFigure(Figure f) { drawing.add(f); }
	
	/** 
	 * Return true if drawing contains figure.
	 * @param f figure, see above.
	 * @return see above.
	 */
	public boolean contains(Figure f) { return drawing.contains(f); }
	
	/**
	 * Notifies all figure listeners.
	 * @see DrawingListener#figureAdded(DrawingEvent)
	 */
	public void figureAdded(DrawingEvent e)
	{
		if (e == null) return;
		for (int i = 0 ; i < figureListeners.size(); i++)
			e.getFigure().addFigureListener(figureListeners.get(i));
	}

	/**
	 * Remove all figures from the drawingView
	 */
	public void removeAllFigures()
	{
		List<Figure> figures = drawing.getFigures();
		for(int i = 0 ; i < figures.size() ; i++)
			drawing.remove(figures.get(i));
	}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see DrawingListener#figureRemoved(DrawingEvent)
	 */
	public void figureRemoved(DrawingEvent e) {}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see DrawingListener#areaInvalidated(DrawingEvent)
	 */
	public void areaInvalidated(DrawingEvent e) {}

}