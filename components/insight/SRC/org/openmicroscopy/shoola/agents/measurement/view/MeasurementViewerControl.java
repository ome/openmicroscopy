/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

import org.jhotdraw.draw.AttributeKey;
//Third-party libraries
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.DrawingListener;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.draw.FigureSelectionListener;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.measurement.SelectChannel;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.CreateFigureAction;
import org.openmicroscopy.shoola.agents.measurement.actions.DeleteROIAction;
import org.openmicroscopy.shoola.agents.measurement.actions.KeywordSelectionAction;
import org.openmicroscopy.shoola.agents.measurement.actions.LoadROIAction;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.agents.measurement.actions.ExportGraphAction;
import org.openmicroscopy.shoola.agents.measurement.actions.SaveROIAction;
import org.openmicroscopy.shoola.agents.measurement.actions.ShowROIAssistant;
import org.openmicroscopy.shoola.agents.measurement.actions.UnitsAction;
import org.openmicroscopy.shoola.agents.measurement.actions.WorkflowAction;
import org.openmicroscopy.shoola.agents.util.ui.PermissionMenu;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;

/** 
 * The MeasurementViewer's Controller.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
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
	implements ChangeListener, DrawingListener, 
	FigureListener, FigureSelectionListener, PropertyChangeListener,
	WindowFocusListener, KeyListener
{

	/** Identifies the <code>SAVE</code> action in the menu. */
    static final Integer     SAVE = Integer.valueOf(0);
    
    /** Identifies the <code>LOAD</code> action in the menu. */
    static final Integer     LOAD =  Integer.valueOf(1);

    /** Identifies the <code>ROI Assistant</code> action in the menu. */
    static final Integer     ROI_ASSISTANT =  Integer.valueOf(5);

    /** 
     * Identifies the <code>showMeasurementinMicrons</code> action in 
     * the menu.
     */
    static final Integer     IN_MICRONS =  Integer.valueOf(6);
    
    /** 
     * Identifies the <code>showMeasurementinMicrons</code> action in 
     * the menu.
     */
    static final Integer     IN_PIXELS =  Integer.valueOf(7);
    
    /** 
     * Identifies the <code>createSingleFigure</code> action in 
     * the menu.
     */
    static final Integer     CREATE_SINGLE_FIGURE =  Integer.valueOf(8);
    
    /** 
     * Identifies the <code>create multiples figures</code> action in 
     * the menu.
     */
    static final Integer     CREATE_MULTIPLE_FIGURES =  Integer.valueOf(9);
    

    /** 
     * Identifies the <code>selectWorkFlow</code> action in 
     * the menu.
     */
    static final Integer     SELECT_WORKFLOW =  Integer.valueOf(10);
   
    /** 
     * Identifies the <code>createWorkflow</code> action in 
     * the menu.
     */
    static final Integer     CREATE_WORKFLOW =  Integer.valueOf(11);
    
    /** 
     * Identifies the <code>keywordSelection</code> action in 
     * the keyword combobox.
     */
    static final Integer     KEYWORD_SELECTION =  Integer.valueOf(12);
    
    /** Identifies the <code>DELETE</code> action in the menu. */
    static final Integer     DELETE = Integer.valueOf(13);

    /** Identifies the <code>EXPORT_GRAPH</code> action in the menu. */
    static final Integer     EXPORT_GRAPH = Integer.valueOf(14);
    
    /** 
     * Reference to the {@link MeasurementViewer} component, which, 
     * in this context, is regarded as the Model.
     */
    private MeasurementViewer						model;

    /** Reference to the View. */
    private MeasurementViewerUI						view;

    /** Maps actions identifiers onto actual <code>Action</code> object. */
    private Map<Integer, MeasurementViewerAction>	actionsMap;
    
    /** Flag indicating that the shape is removed via the delete key.*/
    private boolean keyRemove;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
    	actionsMap.put(SAVE, new SaveROIAction(model));
    	actionsMap.put(LOAD, new LoadROIAction(model));
    	actionsMap.put(ROI_ASSISTANT, new ShowROIAssistant(model));
    	actionsMap.put(IN_MICRONS, new UnitsAction(model, true));
    	actionsMap.put(IN_PIXELS, new UnitsAction(model, false));
    	actionsMap.put(CREATE_SINGLE_FIGURE, 
    			new CreateFigureAction(model, true));
    	actionsMap.put(CREATE_MULTIPLE_FIGURES, new CreateFigureAction(model, 
    												false));
    	actionsMap.put(SELECT_WORKFLOW, new WorkflowAction(model, false));
    	actionsMap.put(CREATE_WORKFLOW, new WorkflowAction(model, true));
    	actionsMap.put(KEYWORD_SELECTION, new KeywordSelectionAction(model));
    	actionsMap.put(DELETE, new DeleteROIAction(model));
    	actionsMap.put(EXPORT_GRAPH, new ExportGraphAction(model));
    }

	/**
	 * Return <code>true</code> if the right button was clicked or 
	 * left button was clicked with control held down,
	 * <code>false</code> otherwise.
	 * 
	 * @param e The mouse event to handle.
	 * @return See above.
	 */
	public static boolean isRightClick(MouseEvent e)
	{
		return (e.getButton() == MouseEvent.BUTTON3 ||
				(e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()));
		//return SwingUtilities.isRightMouseButton(e);
	}
	
    /**
     * Sets the status of the currently selected Region of Interest.
     * 
     * @param status The value to set.
     */
    private void setROIFigureStatus(int status)
    {
    	Collection<Figure> selectedFigures = 
				view.getDrawingView().getSelectedFigures(); 
    	if (selectedFigures.size() != 1) return;
    	Iterator<Figure> i = selectedFigures.iterator();
    	Figure fig;
    	ROIFigure roiFigure;
    	while (i.hasNext()) {
    		fig =  i.next();
    		if (fig instanceof ROIFigure) {
    			roiFigure = (ROIFigure) fig;
    			roiFigure.setStatus(status);
    			handleFigureChange(roiFigure);
    		}
    	}
    }
    
    /**
     * Handles the selection of new Region of Interest.
     * 
     * @param figure The value to handle.
     */
    private void handleFigureChange(ROIFigure figure)
	{
    	view.onSelectedFigures();
    	ROI roi = figure.getROI();
    	
		//TODO clean that code
    	if ((figure instanceof MeasureLineFigure) || 
				(figure instanceof MeasurePointFigure)) {
    		figure.calculateMeasurements();
    		view.refreshResultsTable();
    		if (!view.inDataView()) return;
    		ROIShape shape = figure.getROIShape();
    		List<ROIShape> shapeList = new ArrayList<ROIShape>();
    		roi = shape.getROI();
    		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
    		Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
    		while (shapeIterator.hasNext())
    			shapeList.add(shapeMap.get(shapeIterator.next()));
    		
    		if (shapeList.size() != 0)
    			model.analyseShapeList(shapeList);
    		return;
		}
 		if (figure.getStatus() != ROIFigure.IDLE) return;
		figure.calculateMeasurements();
		view.refreshResultsTable();
		if (!view.inDataView()) return;
		ROIShape shape = figure.getROIShape();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		roi = shape.getROI();
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
		ROIShape thisShape;
		while (shapeIterator.hasNext())
		{
			thisShape = shapeMap.get(shapeIterator.next()); 
			shapeList.add(thisShape);
		}
		
		if (shapeList.size() != 0)
			model.analyseShapeList(shapeList);
	}
    
    /**
     * Creates a new instance.
     * The {@link #initialize(MeasurementViewer, MeasurementViewerUI) initialize} 
     * method should be called straight after to link this Controller 
     * to the other MVC components.
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
    	view.getDrawingView().addKeyListener(this);
    	view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	view.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) { model.close(); }
    		public void windowOpened(WindowEvent e) { 
    			view.addWindowFocusListener(this); }
    	});
    	view.getDrawingView().addMouseListener(new MouseAdapter() {

    		public void mouseReleased(MouseEvent e)
    		{
    			setROIFigureStatus(ROIFigure.IDLE);
    			if (!UIUtilities.isWindowsOS()) {
        			if (isRightClick(e)) {
        				Collection l = 
        					view.getDrawingView().getSelectedFigures();
        				if (l != null && l.size() == 1)
        					view.showROIManagementMenu(e.getX(), e.getY());
        			}
    			}
    		}
    		
    		public void mousePressed(MouseEvent e)
    		{
    			setROIFigureStatus(ROIFigure.IDLE);
    			if (UIUtilities.isWindowsOS()) {
        			if (isRightClick(e)) {
        				Collection l = 
        					view.getDrawingView().getSelectedFigures();
        				if (l != null && l.size() == 1)
        					view.showROIManagementMenu(e.getX(), e.getY());
        			}
    			}
    		}
    		
    	});

    	view.getDrawingView().addMouseMotionListener(new MouseMotionAdapter()
    	{

    		@Override
    		public void mouseDragged(MouseEvent e)
    		{
    			if (isRightClick(e)) return;
    			setROIFigureStatus(ROIFigure.MOVING);
    		}

    	});
    	JMenu menu = MeasurementViewerFactory.getWindowMenu();
    	menu.addMenuListener(new MenuListener() {

    		public void menuSelected(MenuEvent e)
    		{ 
    			Object source = e.getSource();
    			if (source instanceof JMenu)
    				MeasurementViewerFactory.register((JMenu) source);
    		}

    		/** 
    		 * Required by I/F but not actually needed in our case, 
    		 * no-operation implementation.
    		 * @see MenuListener#menuCanceled(MenuEvent)
    		 */ 
    		public void menuCanceled(MenuEvent e) {}

    		/** 
    		 * Required by I/F but not actually needed in our case, 
    		 * no-operation implementation.
    		 * @see MenuListener#menuDeselected(MenuEvent)
    		 */ 
    		public void menuDeselected(MenuEvent e) {}

    	});

    	//Listen to keyboard selection
    	menu.addMenuKeyListener(new MenuKeyListener() {

    		public void menuKeyReleased(MenuKeyEvent e)
    		{
    			Object source = e.getSource();
    			if (source instanceof JMenu)
    				MeasurementViewerFactory.register((JMenu) source);
    		}

    		/** 
    		 * Required by I/F but not actually needed in our case, 
    		 * no-operation implementation.
    		 * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
    		 */
    		public void menuKeyPressed(MenuKeyEvent e) {}

    		/** 
    		 * Required by I/F but not actually needed in our case, 
    		 * no-operation implementation.
    		 * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
    		 */
    		public void menuKeyTyped(MenuKeyEvent e) {}

    	});
    	MeasurementViewerFactory.attachWindowMenuToTaskBar();
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
		ColourPicker picker = new ColourPicker(view, color);
		picker.addPropertyChangeListener(ColourPicker.COLOUR_PROPERTY, this);
		UIUtilities.setLocationRelativeTo(view, picker);
	}
    
    /** Analyzes the selected figures. */
	void analyseSelectedFigures()
	{
		Collection<Figure> figures = model.getSelectedFigures();
		if (figures.size() != 1 && !view.inDataView()) return;
		Iterator<Figure> j = figures.iterator();
		ROIFigure figure = null;
		if (j.hasNext()) figure = (ROIFigure) j.next();
		if (figure == null) return;
		ROIShape shape = figure.getROIShape();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi = shape.getROI();
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		Entry entry;
		Iterator i = shapeMap.entrySet().iterator();
		ROIShape currentShape;
		Coord3D c;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			c = (Coord3D) entry.getKey();
			currentShape = (ROIShape) entry.getValue();
			if (!(currentShape.getFigure() instanceof MeasureTextFigure))
				shapeList.add(currentShape);
		}
		if (shapeList.size() != 0) model.analyseShapeList(shapeList);
	}

	/** Loads the tags.*/
    void loadTags()
    {
        model.loadTags();
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
		else if (PermissionMenu.SELECTED_LEVEL_PROPERTY.equals(name)) {
			model.deleteAllROIs((Integer) evt.getNewValue());
		}
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
				view.onAnalysed(true);
				break;
			case MeasurementViewer.LOADING_DATA:
				LoadingWindow w = view.getLoadingWindow();
				if (!w.isVisible()) UIUtilities.centerAndShow(w);
				view.setStatus("Loading.");
				break;
			case MeasurementViewer.READY:
				view.getLoadingWindow().setVisible(false);
				view.setStatus("Ready.");
				view.onAnalysed(false);
				if(!view.isVisible())
					view.setOnScreen();
				break;
			case MeasurementViewer.DISCARDED:
                LoadingWindow window = view.getLoadingWindow();
                window.setVisible(false);
                window.dispose();
                view.setVisible(false);
                view.dispose();
		}
	}

	/**
	 * Adds a new figure.
	 * @see DrawingListener#figureAdded(DrawingEvent)
	 */
	public void figureAdded(DrawingEvent e)
	{
		Figure f = e.getFigure();
		if (!(f instanceof ROIFigure)) return;
		ROIFigure roiFigure = (ROIFigure) f;
		roiFigure.setStatus(ROIFigure.MOVING);
		view.addROI(roiFigure);
		if (roiFigure.canEdit())
			roiFigure.addFigureListener(this);
		model.setDataChanged();
		if (!view.inDataView()) return;
		ROIShape shape = roiFigure.getROIShape();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi = shape.getROI();
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
		while (shapeIterator.hasNext())
			shapeList.add(shapeMap.get(shapeIterator.next()));
		if (shapeList.size() == 0) return;
		if ((f instanceof MeasureLineFigure) || 
				(f instanceof MeasurePointFigure)) {
			roiFigure.setStatus(ROIFigure.IDLE);
			model.analyseShapeList(shapeList);
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
		if (f instanceof ROIFigure) {
			ROIFigure roi = (ROIFigure) f;
			if (keyRemove) {
				if (roi.isReadOnly() || !roi.canDelete()) {
					view.getDrawing().removeDrawingListener(this);
					view.getDrawing().add(roi);
					view.getDrawing().addDrawingListener(this);
					return;
				}
				view.markROIForDelete(roi);
				keyRemove = false;
			}
			view.removeROI(roi);
			model.setDataChanged();
		}
	}

	/**
	 * Displays the selected figures.
	 * @see FigureSelectionListener#selectionChanged(FigureSelectionEvent)
	 */
	public void selectionChanged(FigureSelectionEvent evt)
	{	
	        // ignore events if viewer is 'closed'
	        if (!view.isVisible()) 
	            return;
	        
		Collection<Figure> figures = evt.getView().getSelectedFigures();
		if (figures == null) return;
		final List<ROIShape> shapeList = new ArrayList<ROIShape>();
		if (view.inDataView() && figures.size() == 1) {
			ROIFigure figure = (ROIFigure) figures.iterator().next();
			if (figure == null) return;
			ROIShape shape = figure.getROIShape();
			if (shape == null) return;
			ROI roi = shape.getROI();
			if (roi == null) return;
			TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
			Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
			while (shapeIterator.hasNext())
				shapeList.add(shapeMap.get(shapeIterator.next()));
		}
		view.setSelectedFigures(figures);
		if (!shapeList.isEmpty()) 
			/* delayed because setSelectedFigures cannot work during shape analysis */
			model.analyseShapeList(shapeList);
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
		if (f instanceof ROIFigure) 
		{
			ROIFigure fig = (ROIFigure) f;
			view.onAttributeChanged(fig);
			view.refreshInspectorTable();
			model.figureAttributeChanged(e.getAttribute(), fig);
			if (!fig.isReadOnly()) {
				if (fig.canEdit()) {
				    AttributeKey<?> key = e.getAttribute();
		            if (key != MeasurementAttributes.SHOWTEXT) {
		                model.setDataChanged();
		            }
				}
			}
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
		if (f instanceof ROIFigure) {
			ROIFigure fig = (ROIFigure) f;
			Coord3D coord = fig.getROIShape().getCoord3D();
	    	int c = coord.getChannel();
	    	if (c >= 0 && !view.isChannelActive(c)) {
	    		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
	    		bus.post(new SelectChannel(view.getPixelsID(), c));
	    	}
			handleFigureChange(fig);
		}
	}
	
	/** 
	 * Calculates the statistics for the selected shape.
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		keyRemove = false;
		if (e.getKeyCode() == KeyEvent.VK_DELETE)
			keyRemove = true;
		char ANALYSECHAR = 'a';
		if (e.getKeyChar() == ANALYSECHAR) {
			Collection<Figure> selectedFigures = 
				view.getDrawingView().getSelectedFigures(); 
			if (selectedFigures.size() != 1) return;
			
			Iterator<Figure> iterator = selectedFigures.iterator();
			ROIFigure fig = (ROIFigure) iterator.next();
			if (fig instanceof MeasureTextFigure) return;
			
			List<ROIShape> shapeList = new ArrayList<ROIShape>();
			shapeList.add(fig.getROIShape());
			view.calculateStats(shapeList);
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
	 * Required by the I/F but no-operation implementation in our case.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e) {}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-operation 
	 * implementation in our case.
	 * @see DrawingListener#areaInvalidated(DrawingEvent)
	 */
	public void areaInvalidated(DrawingEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-operation 
	 * implementation in our case.
	 * @see FigureListener#figureAdded(FigureEvent)
	 */
	public void figureAdded(FigureEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-operation
	 * implementation in our case.
	 * @see FigureListener#figureAreaInvalidated(FigureEvent)
	 */
	public void figureAreaInvalidated(FigureEvent e) {}
	
	/**
	 * Required by the {@link DrawingListener} I/F but no-operation 
	 * implementation in our case.
	 * @see FigureListener#figureRemoved(FigureEvent)
	 */
	public void figureRemoved(FigureEvent e) {}

	/**
	 * Required by the {@link DrawingListener} I/F but no-operation 
	 * implementation in our case.
	 * @see FigureListener#figureRequestRemove(FigureEvent)
	 */
	public void figureRequestRemove(FigureEvent e) {}

	/**
	 * Required by the {@link FigureListener} I/F but no-operation 
	 * implementation in our case.
	 * @see FigureListener#figureHandlesChanged(FigureEvent)
	 */
	public void figureHandlesChanged(FigureEvent e) {}

	/**
	 * Required by the {@link KeyListener} I/F but no-operation implementation
	 * in our case.
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}

	/**
	 * Required by the {@link KeyListener} I/F but no-operation implementation
	 * in our case.
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {}

}
