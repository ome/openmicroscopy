/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerControl 
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.draw.FigureSelectionListener;
import org.openmicroscopy.shoola.agents.events.FocusGainedEvent;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.CreateFigureAction;
import org.openmicroscopy.shoola.agents.measurement.actions.LoadROIAction;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.agents.measurement.actions.RefreshResultsTableAction;
import org.openmicroscopy.shoola.agents.measurement.actions.ResultsWizardAction;
import org.openmicroscopy.shoola.agents.measurement.actions.SaveROIAction;
import org.openmicroscopy.shoola.agents.measurement.actions.SaveResultsAction;
import org.openmicroscopy.shoola.agents.measurement.actions.ShowROIAssistant;
import org.openmicroscopy.shoola.agents.measurement.actions.UnitsAction;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;

/** 
 * The MeasurementViewer's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MeasurementViewerControl 
	implements ChangeListener, DrawingListener, FigureListener, 
				FigureSelectionListener, PropertyChangeListener,
				WindowFocusListener
{

	/** Identifies the <code>SAVE</code> action in the menu. */
    static final Integer     SAVE = new Integer(0);
    
    /** Identifies the <code>LOAD</code> action in the menu. */
    static final Integer     LOAD = new Integer(1);
    
    /** Identifies the <code>SAVE Results</code> action in the menu. */
    static final Integer     SAVE_RESULTS = new Integer(2);
    
    /** Identifies the <code>Refresh Results</code> action in the menu. */
    static final Integer     REFRESH_RESULTS = new Integer(3);
    
    /** Identifies the <code>Results Wizard</code> action in the menu. */
    static final Integer     RESULTS_WIZARD = new Integer(4);

    /** Identifies the <code>ROI Assistant</code> action in the menu. */
    static final Integer     ROI_ASSISTANT = new Integer(5);

    /** 
     * Identifies the <code>showMeasurementinMicrons</code> action in 
     * the menu.
     */
    static final Integer     IN_MICRONS = new Integer(6);
    
    /** 
     * Identifies the <code>showMeasurementinMicrons</code> action in 
     * the menu.
     */
    static final Integer     IN_PIXELS = new Integer(7);
    
    /** 
     * Identifies the <code>createSingleFigures</code> action in 
     * the menu.
     */
    static final Integer     CREATESINGLEFIGURE = new Integer(8);
    
    /** 
     * Identifies the <code>showMeasurementinMicrons</code> action in 
     * the menu.
     */
    static final Integer     CREATEMULTIPLEFIGURE = new Integer(9);
    
    /** 
     * Reference to the {@link MeasurementViewer} component, which, 
     * in this context, is regarded as the Model.
     */
    private MeasurementViewer						model;

    /** Reference to the View. */
    private MeasurementViewerUI						view;

    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, MeasurementViewerAction>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
    	actionsMap.put(SAVE, new SaveROIAction(model));
    	actionsMap.put(LOAD, new LoadROIAction(model));
    	actionsMap.put(SAVE_RESULTS, new SaveResultsAction(model));
    	actionsMap.put(REFRESH_RESULTS, new RefreshResultsTableAction(model));
    	actionsMap.put(RESULTS_WIZARD, new ResultsWizardAction(model));
    	actionsMap.put(ROI_ASSISTANT, new ShowROIAssistant(model));
    	actionsMap.put(IN_MICRONS, new UnitsAction(model, true));
    	actionsMap.put(IN_PIXELS, new UnitsAction(model, false));
    	actionsMap.put(CREATESINGLEFIGURE, new CreateFigureAction(model, true));
    	actionsMap.put(CREATEMULTIPLEFIGURE, new CreateFigureAction(model, 
    												false));
    }

    /**
     * Creates a new instance.
     * The {@link #initialize(MeasurementViewer, MeasurementViewerUI) initialize} 
     * method should be called straigh 
     * after to link this Controller to the other MVC components.
     */
    MeasurementViewerControl() {}
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param model  Reference to the {@link MeasurementViewer} component, 
     * 				 which, in this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(MeasurementViewer model, MeasurementViewerUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        model.addChangeListener(this);   
        model.addPropertyChangeListener(this);
        actionsMap = new HashMap<Integer, MeasurementViewerAction>();
        createActions();
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window.
     */
    void attachListeners()
    {
    	 view.getLoadingWindow().addPropertyChangeListener(
                 LoadingWindow.CLOSED_PROPERTY, this);
    	 view.getDrawing().addDrawingListener(this);
    	 view.getDrawingView().addFigureSelectionListener(this);
    	 view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	 view.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) { model.close(); }
             public void windowOpened(WindowEvent e) { 
            	 view.addWindowFocusListener(this); }
         });
    }
    
    /** 
	 * Moves the view to the front, to avoid loops, first removes the 
	 * WindowFocusListener.
	 */
	void toFront()
	{
		if (view.getExtendedState() != Frame.NORMAL) return;
		if (!view.isFocused()) {
			view.removeWindowFocusListener(this);
			//view.requestFocus();
			if (view.isVisible())
				view.setVisible(true);
			//view.addWindowFocusListener(this);
		}
	}
	
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    MeasurementViewerAction getAction(Integer id) { return actionsMap.get(id); }

    /**
     * Brings up on screen the {@link ColourPicker}.
     * 
     * @param color The color to pass to the {@link ColourPicker}.
     */
    void showColorPicker(Color color)
    {
		if (color == null) return;
		ColourPicker colourPicker = new ColourPicker(view, color);
		colourPicker.addPropertyChangeListener(ColourPicker.COLOUR_PROPERTY, 
												this);
		UIUtilities.setLocationRelativeTo(view, colourPicker);
	}
    
    /** Analyses the selected figures. */
	void analyseSelectedFigures()
	{
		Collection<Figure> figures = model.getSelectedFigures();
		if (figures.size() == 1) 
		{
			ROIFigure figure = (ROIFigure) figures.iterator().next();
			ROIShape shape = figure.getROIShape();
			if (view.inDataView()) 
			{
				ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
				ROI roi = shape.getROI();
				TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
				Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
				while(shapeIterator.hasNext())
					shapeList.add(shapeMap.get(shapeIterator.next()));
				if (shapeList.size() != 0) model.analyseShapeList(shapeList);
			}
		}
	}
	
    /**
     * Reacts to property change.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ColourPicker.COLOUR_PROPERTY.equals(name))
			view.setCellColor((Color) evt.getNewValue());
		else if (LoadingWindow.CLOSED_PROPERTY.equals(name)) 
            model.discard();
	}

	/**
     * Reacts to state changes in the {@link MeasurementViewer}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		int state = model.getState();
		switch (state) {
			case MeasurementViewer.ANALYSE_SHAPE:
				view.setStatus("Analysing Shape.");
				break;
			case MeasurementViewer.LOADING_DATA:
				LoadingWindow w = view.getLoadingWindow();
				if (!w.isVisible()) UIUtilities.centerAndShow(w);
				view.setStatus("Loading.");
				break;
			case MeasurementViewer.READY:
				view.getLoadingWindow().setVisible(false);
				view.setStatus("Ready.");
				if(!view.isVisible())
					view.setOnScreen();
				break;
			case MeasurementViewer.DISCARDED:
                LoadingWindow window = view.getLoadingWindow();
                window.setVisible(false);
                window.dispose();
                view.setVisible(false);
                view.dispose();
                break;
		}
	}

	/**
	 * Adds a new figure.
	 * @see DrawingListener#figureAdded(DrawingEvent)
	 */
	public void figureAdded(DrawingEvent e)
	{
		//if (model.getState() != MeasurementViewer.READY) return;
		Figure f = e.getFigure();
		if (!(f instanceof ROIFigure)) return;
		ROIFigure roiFigure = (ROIFigure) f;
		view.addROI(roiFigure);
		roiFigure.addFigureListener(this);
		model.setDataChanged();
		if (!view.inDataView()) return;
		ROIShape shape = roiFigure.getROIShape();
		if (view.inDataView())
		{
			ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
			ROI roi = shape.getROI();
			TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
			Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
			while(shapeIterator.hasNext())
				shapeList.add(shapeMap.get(shapeIterator.next()));
			if (shapeList.size() != 0) model.analyseShapeList(shapeList);
		}
		
	}
	
	/**
	 * Removes the selected figure from the display.
	 * @see DrawingListener#figureRemoved(DrawingEvent)
	 */
	public void figureRemoved(DrawingEvent e)
	{
		if (model.getState() != MeasurementViewer.READY) return;
		Figure f = e.getFigure();
		if (f instanceof ROIFigure) view.removeROI((ROIFigure) f);
		model.setDataChanged();
	}

	/**
	 * Displays the selected figures.
	 * @see FigureSelectionListener#selectionChanged(FigureSelectionEvent)
	 */
	public void selectionChanged(FigureSelectionEvent evt)
	{
		Collection figures = evt.getView().getSelectedFigures();
		if (figures == null) return;
		if (view.inDataView() && figures.size() == 1) 
		{
			ROIFigure figure = (ROIFigure) figures.iterator().next();
			if(figure==null)
				return;
			ROIShape shape = figure.getROIShape();
			if(shape ==null)
				return;
			ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
			ROI roi = shape.getROI();
			if(roi==null)
				return;
			TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
			Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
			while(shapeIterator.hasNext())
				shapeList.add(shapeMap.get(shapeIterator.next()));
			if (shapeList.size() != 0) model.analyseShapeList(shapeList);
		}
		view.setSelectedFigures(figures);
	}

	/**
	 * Required by the {@link DrawingListener} I/F. 
	 * This allows the viewer to manage the link between figure attributes and 
	 * ROIShape, ROI objects. 
	 * @see FigureListener#figureAttributeChanged(FigureEvent)
	 */
	public void figureAttributeChanged(FigureEvent e)
	{
		Figure f = e.getFigure();
		if (f instanceof ROIFigure) {
			ROIFigure fig = (ROIFigure) f;
			view.onAttributeChanged(fig);
			model.figureAttributeChanged(e.getAttribute(), fig);
			model.setDataChanged();
		}
	}

	/**
	 * Required by the {@link DrawingListener} I/F used to update 
	 * the measurements of a component and the different dataviews. 
	 * @see FigureListener#figureChanged(FigureEvent)
	 */
	public void figureChanged(FigureEvent e)
	{
		Figure f = e.getFigure();
		if (f instanceof ROIFigure) 
		{
			ROIFigure roiFigure = (ROIFigure) f;
			roiFigure.calculateMeasurements();
			model.setDataChanged();
			if (view.inDataView())
			{
				ROIShape shape = roiFigure.getROIShape();
				ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
				ROI roi = shape.getROI();
				TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
				Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
				while(shapeIterator.hasNext())
					shapeList.add(shapeMap.get(shapeIterator.next()));
				if (shapeList.size() != 0) model.analyseShapeList(shapeList);
			}
		}
	}
	
	/**
	 * Posts an event to bring the related window to the front.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e)
	{
		/*
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		bus.post(new FocusGainedEvent(view.getPixelsID(), 
				FocusGainedEvent.MEASUREMENT_TOOL_FOCUS));
				*/
	}

	/**
	 * Required by the I/F but no-op implementation in our case.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e)
	{
		
	}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see DrawingListener#areaInvalidated(DrawingEvent)
	 */
	public void areaInvalidated(DrawingEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see FigureListener#figureAdded(FigureEvent)
	 */
	public void figureAdded(FigureEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see FigureListener#figureAreaInvalidated(FigureEvent)
	 */
	public void figureAreaInvalidated(FigureEvent e) {}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see FigureListener#figureRemoved(FigureEvent)
	 */
	public void figureRemoved(FigureEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-op implementation
	 * in our case.
	 * @see FigureListener#figureRequestRemove(FigureEvent)
	 */
	public void figureRequestRemove(FigureEvent e) {}

}
