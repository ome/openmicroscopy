/*
 * measurement.ui.UIModel 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JComponent;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.draw.FigureSelectionListener;
import org.jhotdraw.draw.TextFigure;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.FIGURETYPE;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.INMICRONS;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.MEASUREMENTTEXT_COLOUR;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.SHOWMEASUREMENT;
import static org.openmicroscopy.shoola.util.ui.measurement.model.ROIAttributes.ROIID;

import org.openmicroscopy.shoola.util.ui.measurement.MeasurementModel;
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelInfo;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingEventList;
import org.openmicroscopy.shoola.util.ui.measurement.ui.drawingcanvas.ImageDrawingView;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.util.ExceptionHandler;
import static org.openmicroscopy.shoola.util.ui.measurement.ui.util.UIUtils.setComponentSize;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;




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
public class UIModel
	extends Component
	implements PropertyChangeListener, DrawingListener, FigureSelectionListener,
	FigureListener
{
	public 		MeasurementModel		model;
	private		Drawing					drawing;
	private 	ImageDrawingView 		view;
	private		DrawingEditor			editor;
	private 	Coord3D					currentView;
	
	public UIModel(MeasurementModel model)
	{
		this.model = model;
		currentView = model.getCoord3D();
		drawing = new DefaultDrawing();
		view = new ImageDrawingView();
		setComponentSize(view, model.getImageDimensions());
		view.setDrawing(drawing);
		editor = new DefaultDrawingEditor();
		editor.add(view);
		drawing.addDrawingListener(this);
		view.addFigureSelectionListener(this);
	}

	public JComponent getUI()
	{
		return view.getComponent();
	} 
	
	public Coord3D getCoord3D()
	{
		return currentView;
	}
	
	public ChannelInfo getChannelInfo()
	{
		return model.getChannelInfo();
	}
	
	public int	getCurrentChannel()
	{
		return model.getCurrentChannel();
	}
	
	public void setCoord3D(Coord3D coord)
	{
		if(currentView.equals(coord))
			return;
		System.err.println("Changed view");
		currentView = coord;
		updateView();
	}
	
	private void updateView()
	{
		drawing.removeDrawingListener(this);
		drawing.clear();
		ShapeList list = null;
		try {
			System.err.println("Trying to add figures to drawing");
			list = model.getShapeList(currentView);
			System.err.println("got List");
			TreeMap<Long, ROIShape> roiList = list.getList();
			System.err.println("got roiList");
			Iterator shapeIterator = roiList.values().iterator();
			System.err.println("UIModel : " + roiList.size());
			System.err.println("UIModel.shapeIterator : " + shapeIterator.hasNext());
			while(shapeIterator.hasNext())
			{
				System.err.println("fucker");
				ROIShape shape = (ROIShape) shapeIterator.next();
				System.err.println("UIModel ROIShape : " + shape.getID());
				drawing.add(shape.getFigure());
				System.err.println("added shape");
			}
		} catch (NoSuchShapeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Do nothing as no shapes on currentPlane
			System.err.println("No figures at " + currentView.c + " " + currentView.t + " " + currentView.z);
		}
		view.setDrawing(drawing);
		drawing.addDrawingListener(this);
	}
	
	public DrawingEditor getEditor()
	{
		return editor;
	}
	
	public ImageDrawingView getView()
	{
		return view;
	}
	
	public Drawing getDrawing()
	{
		return drawing;
	}
	
	public void setZoomFactor(double zoom)
	{
		view.setScaleFactor(zoom);
	}
	
	public void setFigureAttributes(Figure fig)
	{
		FONT_SIZE.set(fig, 10.0);
		TEXT_COLOR.set(fig, Color.orange);
		STROKE_WIDTH.set(fig, 1.5);
		FILL_COLOR.set(fig, new Color(220, 220, 220, 64));
		INMICRONS.set(fig, false);
		SHOWMEASUREMENT.set(fig, true);
		MEASUREMENTTEXT_COLOUR.set(fig, Color.yellow);
		STROKE_COLOR.set(fig, Color.gray);
		if(fig instanceof MeasureRectangleFigure)
			FIGURETYPE.set(fig, "Rectangle");
		if(fig instanceof MeasureEllipseFigure)
			FIGURETYPE.set(fig,  "Ellipse");
		if(fig instanceof BezierFigure)
		{
			BezierFigure figure = (BezierFigure)fig;
			if(figure.isClosed()==false)
				FIGURETYPE.set(fig, "Scribble");
			else
				FIGURETYPE.set(fig,  "Polygon");
		}
		if(fig instanceof MeasureLineFigure || fig instanceof MeasureLineConnectionFigure)
			FIGURETYPE.set(fig, "Line");
		if(fig instanceof TextFigure)
			FIGURETYPE.set(fig,  "Text");
	}

	public void selectFigure(Figure figure)
	{
		view.clearSelection();
		view.addToSelection(figure);
		Collection<Figure> selectedFigs = view.getSelectedFigures();
		Iterator figIterator = selectedFigs.iterator();
		
		ArrayList<ROI> roiList = new ArrayList<ROI>();
		while(figIterator.hasNext())
		{
			Figure fig = (Figure)figIterator.next();
			try
			{
				ROI roi = model.getROI(ROIID.get(fig));
				roiList.add(roi);
			}
			catch(Exception e)
			{
				ExceptionHandler.get().handleException(e);
			}
			
		}
		
		firePropertyChange(DrawingEventList.UIMODEL_FIGURESELECTED, null, 
				roiList);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent changeEvent) 
	{

	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#areaInvalidated(org.jhotdraw.draw.DrawingEvent)
	 */
	public void areaInvalidated(DrawingEvent e) 
	{

	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#figureAdded(org.jhotdraw.draw.DrawingEvent)
	 */
	public void figureAdded(DrawingEvent e)
	{
		try
		{
			addROI(e.getFigure());
		}
		catch(Exception exception)
		{
			ExceptionHandler.get().handleException(exception);
		}
	}

	public void addROI(Figure fig) throws 		ROICreationException, 
												ROIShapeCreationException, 
												NoSuchROIException
	{
		fig.addFigureListener(this);
		setFigureAttributes(fig);
		long id;
		id = model.addROI(fig);
		ROIID.set(fig, id);
		ArrayList<ROI> roiList = new ArrayList<ROI>();
		roiList.add(model.getROI(id));
		firePropertyChange(DrawingEventList.get().UIMODEL_FIGUREADDED, null, 
				roiList);
	}
	

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#figureRemoved(org.jhotdraw.draw.DrawingEvent)
	 */
	public void figureRemoved(DrawingEvent e) 
	{
	    long id = ROIID.get(e.getFigure());
	    try 
	    {
	    model.removeROIShape(id, currentView);
		firePropertyChange(DrawingEventList.get().UIMODEL_FIGUREREMOVED, null, e);
	    }
	    catch(Exception exception)
	    {
	    	exception.printStackTrace();
	    }
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureSelectionListener#selectionChanged(org.jhotdraw.draw.FigureSelectionEvent)
	 */
	public void selectionChanged(FigureSelectionEvent evt)
	{
		Collection<Figure> selectedFigs = evt.getView().getSelectedFigures();
		Iterator figIterator = selectedFigs.iterator();
		
		ArrayList<ROI> roiList = new ArrayList<ROI>();
		while(figIterator.hasNext())
		{
			Figure fig = (Figure)figIterator.next();
			try
			{
				ROI roi = model.getROI(ROIID.get(fig));
				roiList.add(roi);
			}
			catch(Exception e)
			{
				ExceptionHandler.get().handleException(e);
			}
			
		}
		
		firePropertyChange(DrawingEventList.UIMODEL_FIGURESELECTED, null, 
				roiList);

	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureAdded(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureAdded(FigureEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureAreaInvalidated(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureAreaInvalidated(FigureEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureAttributeChanged(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureAttributeChanged(FigureEvent e) 
	{
		firePropertyChange(DrawingEventList.UIMODEL_FIGUREATTRIBUTECHANGED, null, e);		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureChanged(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureChanged(FigureEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureRemoved(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureRemoved(FigureEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureRequestRemove(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureRequestRemove(FigureEvent e) {
		// TODO Auto-generated method stub
		
	}

}


