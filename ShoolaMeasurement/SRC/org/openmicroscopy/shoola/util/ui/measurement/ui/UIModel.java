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
import java.io.File;
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
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.draw.FigureSelectionListener;
import org.jhotdraw.draw.TextFigure;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.MEASUREMENTTEXT_COLOUR;
import static org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes.SHOWMEASUREMENT;

import static org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys.ROIID;
import static org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys.FIGURETYPE;
import static org.openmicroscopy.shoola.util.ui.measurement.ui.util.UIUtils.setComponentSize;

import org.openmicroscopy.shoola.util.ui.measurement.MeasurementModel;
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelInfo;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingEventList;
import org.openmicroscopy.shoola.util.ui.measurement.ui.drawingcanvas.ImageDrawingView;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.util.ExceptionHandler;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys;
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
	
	public void saveResults(String filename)
	{
		model.saveResults(filename);
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
		currentView = coord;
		updateView();
	}
	
	private void updateView()
	{
		drawing.removeDrawingListener(this);
		drawing.clear();
		ShapeList list = null;
		try {
			list = model.getShapeList(currentView);
			TreeMap<Long, ROIShape> roiList = list.getList();
			Iterator shapeIterator = roiList.values().iterator();
			while(shapeIterator.hasNext())
			{
				ROIShape shape = (ROIShape) shapeIterator.next();
				drawing.add(shape.getFigure());
			}
		} catch (NoSuchShapeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Do nothing as no shapes on currentPlane
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
	
	public void setFigureAttributes(ROIFigure fig)
	{
		FONT_SIZE.set(fig, 10.0);
		TEXT_COLOR.set(fig, Color.orange);
		STROKE_WIDTH.set(fig, 1.0);
		FILL_COLOR.set(fig, new Color(220, 220, 220, 0));
		SHOWMEASUREMENT.set(fig, true);
		MEASUREMENTTEXT_COLOUR.set(fig, new Color(255, 204, 102, 0));
		STROKE_COLOR.set(fig, Color.WHITE);
		
	}

	public void selectFigure(ROIFigure figure)
	{
		view.clearSelection();
		view.addToSelection(figure);
		Collection<Figure> selectedFigs = view.getSelectedFigures();
		Iterator figIterator = selectedFigs.iterator();
		
		ArrayList<ROI> roiList = new ArrayList<ROI>();
		while(figIterator.hasNext())
		{
			ROIFigure fig = (ROIFigure)figIterator.next();
			try
			{
				ROI roi = model.getROI(fig.getROI().getID());
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
			addROI((ROIFigure)e.getFigure());
		}
		catch(Exception exception)
		{
			ExceptionHandler.get().handleException(exception);
		}
	}

	public void addROI(ROIFigure fig) throws 		ROICreationException, 
												ROIShapeCreationException, 
												NoSuchROIException
	{
		fig.addFigureListener(this);
		setFigureAttributes(fig);
		long id;
		id = model.addROI(fig);
		ROIShape shape = fig.getROIShape();
		setShapeAnnotations(shape);
		ArrayList<ROI> roiList = new ArrayList<ROI>();
		ROIID.set(shape, id);
		roiList.add(model.getROI(id));
		firePropertyChange(DrawingEventList.get().UIMODEL_FIGUREADDED, null, 
				roiList);
	}
	
	private void setShapeAnnotations(ROIShape shape)
	{
		ROIFigure fig = shape.getFigure();
		
		if(fig instanceof MeasureRectangleFigure)
			FIGURETYPE.set(shape, "Rectangle");
		if(fig instanceof MeasureEllipseFigure)
			FIGURETYPE.set(shape,  "Ellipse");
		if(fig instanceof BezierFigure)
		{
			BezierFigure figure = (BezierFigure)fig;
			if(figure.isClosed()==false)
				FIGURETYPE.set(shape, "Scribble");
			else
				FIGURETYPE.set(shape,  "Polygon");
		}
		if(fig instanceof MeasureLineFigure || fig instanceof MeasureLineConnectionFigure)
			FIGURETYPE.set(shape, "Line");
		if(fig instanceof TextFigure)
			FIGURETYPE.set(shape,  "Text");
	}
	
	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.DrawingListener#figureRemoved(org.jhotdraw.draw.DrawingEvent)
	 */
	public void figureRemoved(DrawingEvent e) 
	{
		ROIFigure fig = (ROIFigure)e.getFigure();
	    long id = fig.getROI().getID();
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
			ROIFigure fig = (ROIFigure)figIterator.next();
			try
			{
				ROI roi = model.getROI(fig.getROI().getID());
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

	private void handleAttributeChange(FigureEvent e)
	{
		if(e.getAttribute().equals(AttributeKeys.TEXT.getKey()))
		{
			ROIShape shape = ((ROIFigure)e.getFigure()).getROIShape();
			shape.setAnnotation(AnnotationKeys.BASIC_TEXT, e.getNewValue());
		}
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
		handleAttributeChange(e);
		firePropertyChange(DrawingEventList.UIMODEL_FIGUREATTRIBUTECHANGED, null, e);		
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.FigureListener#figureChanged(org.jhotdraw.draw.FigureEvent)
	 */
	public void figureChanged(FigureEvent e) 
	{
		if(e.getFigure() instanceof ROIFigure)
		{
			ROIFigure fig = (ROIFigure)e.getFigure();
			fig.calculateMeasurements();
		}
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

	/**
	 * @return
	 */
	public TreeMap<Long, ROI> getROIMap() 
	{
		return model.getROIMap();
	}

}


