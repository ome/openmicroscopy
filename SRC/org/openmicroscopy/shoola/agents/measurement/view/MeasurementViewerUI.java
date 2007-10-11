/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerUI 
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jhotdraw.draw.DelegationSelectionTool;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.measurement.SelectPlane;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

/** 
 * The {@link MeasurementViewer} view.
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
class MeasurementViewerUI 
	extends TopWindow implements MouseListener
{

	/** The message displayed when a ROI cannot be retrieved. */
	static final String					RETRIEVE_MSG = "Cannot retrieve the " +
															"ROI";
	
	/** The message displayed when a ROI cannot be created. */
	static final String					CREATE_MSG = "Cannot create the ROI";
	
	/** The message displayed when a ROI cannot be deleted. */
	static final String					DELETE_MSG = "Cannot delete the ROI";
	
	/** The default message. */
	static final String					DEFAULT_MSG = "";
	
	/** The default size of the window. */
	private static final Dimension		DEFAULT_SIZE = new Dimension(400, 300);
	
	/** The maximum size of the window. */
	private static final Dimension		MAXIMUM_SIZE = new Dimension(700, 300);
	
	/** The title for the measurement tool main window. */
	private static final String			WINDOW_TITLE = "Measurement Tool ";
	
	/** index to identify inspector tab. */
	public static final int			INSPECTOR_INDEX = 0;

	/** index to identify manager tab. */
	public static final int			MANAGER_INDEX = 1;
	
	/** index to identify results tab. */
	public static final int			RESULTS_INDEX = 2;
	
	/** index to identify graph tab. */
	public static final int			GRAPH_INDEX = 3;

	/** index to identify intensity tab. */
	public static final int			INTENSITY_INDEX = 4;
	
	/** index to identify calculation tab. */
	public static final int			CALCWIZARD_INDEX = 5;
	
	/** Reference to the Model. */
	private MeasurementViewerModel 		model;

	/** Reference to the Control. */
	private MeasurementViewerControl	controller;
	
	 /** The loading window. */
    private LoadingWindow   			loadingWindow;
    
	/** The tool bar. */
	private ToolBar						toolBar;
	
	/** The ROI inspector. */
	private ObjectInspector				roiInspector;
	
	/** The ROI manager. */
	private ObjectManager				roiManager;
	
	/** The Results component. */
	private MeasurementResults			roiResults;
	
	/** The graphing component. */
	private GraphPane					graphPane;
	
	/** The graphing component. */
	private IntensityView				intensityView;
	
	/** The calculation Wizard component. */
	private CalculationWizard			calcWizard;
	
    /** Tabbed pane hosting the various panel. */
    private JTabbedPane					tabs;
 
    /** The status bar. */
    private StatusBar					statusBar;
    
    /** 
     * Creates the menu bar.
     * 
     * @return The menu bar. 
     */
    private JMenuBar createMenuBar()
    {
    	JMenuBar menuBar = new JMenuBar(); 
    	menuBar.add(createControlsMenu());
    	menuBar.add(createOptionsMenu());
        return menuBar;
    }
    
    /**
     * Helper method to create the controls menu.
     * 
     * @return The controls submenu.
     */
    private JMenu createControlsMenu()
    {
        JMenu menu = new JMenu("Controls");
        menu.setMnemonic(KeyEvent.VK_C);
        MeasurementViewerAction a = 
        	controller.getAction(MeasurementViewerControl.LOAD);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        a = controller.getAction(MeasurementViewerControl.SAVE);
        item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        a = controller.getAction(MeasurementViewerControl.ROI_ASSISTANT);
        item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        return menu;
    }
    
    /**
     * Helper method to create the Options menu.
     * 
     * @return The options submenu.
     */
    private JMenu createOptionsMenu()
    {
        JMenu menu = new JMenu("Options");
        ButtonGroup displayUnits = new ButtonGroup();
    	
        menu.setMnemonic(KeyEvent.VK_O);
        
        JMenu subMenu = new JMenu("Units");
        MeasurementViewerAction a = controller.getAction(
    			MeasurementViewerControl.IN_MICRONS);
        JCheckBoxMenuItem inMicronsMenu = new JCheckBoxMenuItem(a);
        inMicronsMenu.setText(a.getName());
        displayUnits.add(inMicronsMenu);
        subMenu.add(inMicronsMenu);
        
        a = controller.getAction(MeasurementViewerControl.IN_PIXELS);
        JCheckBoxMenuItem inPixelsMenu = new JCheckBoxMenuItem(a);
        inPixelsMenu.setText(a.getName());
        displayUnits.add(inPixelsMenu);
        subMenu.add(inPixelsMenu);
        inPixelsMenu.setSelected(true); //TODO: retrieve info
        
        menu.add(subMenu);
        
        ButtonGroup createFigureGroup = new ButtonGroup();
    	JMenu creationMenu = new JMenu("ROI Creation");
        a = controller.getAction(
    			MeasurementViewerControl.CREATESINGLEFIGURE);
        JCheckBoxMenuItem createSingleFigure = new JCheckBoxMenuItem(a);
        createSingleFigure.setText(a.getName());
        createFigureGroup.add(createSingleFigure);
        creationMenu.add(createSingleFigure);
        
        a = controller.getAction(
        		MeasurementViewerControl.CREATEMULTIPLEFIGURE);
        JCheckBoxMenuItem createMultipleFigure = new JCheckBoxMenuItem(a);
        createMultipleFigure.setText(a.getName());
        createFigureGroup.add(createMultipleFigure);
        creationMenu.add(createMultipleFigure);
        createMultipleFigure.setSelected(true); //TODO: retrieve info
        
        menu.add(creationMenu);
        
        return menu;
    }
    
    
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		statusBar = new StatusBar();
		toolBar = new ToolBar(controller, model);
		roiManager = new ObjectManager(this, model);
		roiInspector = new ObjectInspector(controller, model);
		roiResults = new MeasurementResults(controller, model, this);
		graphPane = new GraphPane(this, controller, model);
		intensityView = new IntensityView(this, controller, model);
		calcWizard = new CalculationWizard(controller, model);
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		DrawingCanvasView canvasView = model.getDrawingView();
		canvasView.addMouseListener(this);
		addTabbedPaneListener();
        tabs.setAlignmentX(LEFT_ALIGNMENT);
	}
	
	/** Adds a listener to the tabbed pane. */
	private void addTabbedPaneListener()
	{
		tabs.addChangeListener(new ChangeListener()
		{
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt)
			{
				JTabbedPane pane=(JTabbedPane) evt.getSource();
				// Get current tab
				int sel = pane.getSelectedIndex();
				if (inDataView())
				{
					controller.analyseSelectedFigures();
				}
			}
		});
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setJMenuBar(createMenuBar());
		tabs.addTab(roiManager.getComponentName(), 
					roiManager.getComponentIcon(), roiManager);
		tabs.addTab(roiInspector.getComponentName(), 
			roiInspector.getComponentIcon(), roiInspector);
		tabs.addTab(roiResults.getComponentName(), 
			roiResults.getComponentIcon(), roiResults);
		tabs.addTab(graphPane.getComponentName(), 
			graphPane.getComponentIcon(), graphPane);
		tabs.addTab(intensityView.getComponentName(), 
			intensityView.getComponentIcon(), intensityView);
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
	}
	
	/**
     * Creates a new instance.
     * The 
     * {@link #initialize(MeasurementViewerControl, MeasurementViewerModel) initialize}
     * method should be called straight 
     * after to link this View to the Controller.
     * 
     * @param title The window title.
     */
	MeasurementViewerUI(String title)
    {
        super(WINDOW_TITLE+title);
        loadingWindow = new LoadingWindow(this);
    }
    
	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
    void initialize(MeasurementViewerControl controller, 
    			MeasurementViewerModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.controller = controller;
        controller.attachListeners();
        initComponents();
        buildGUI();
    }

	/**
	 * Merge the ROIShapes with ids in the idList and the ROIShapes selected 
	 * in the shapeList from those ROI.
	 * @param idList see above.
	 * @param shapeList see above.
	 */
	public void mergeROI(ArrayList<Long> idList, ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.setDataChanged();
			ROI newROI = model.cloneROI(idList.get(0));
			for(ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if(getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if(newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * split the ROIShapes from the ROI with id and the ROIShapes selected in the shapeList 
	 * from that ROI.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void splitROI(long id, ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.setDataChanged();
			ROI newROI = model.cloneROI(id);
			for(ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if(getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if(newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	/**
	 * Duplicate the ROI with id and the ROIShapes selected in the shapeList 
	 * from that ROI.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void duplicateROI(long id, ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.setDataChanged();
			ROI newROI = model.cloneROI(id);
			for(ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if(newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete the ROI with id and the ROIShapes selected in the shapeList 
	 * @param shapeList see above.
	 */
	void deleteROIShapes( ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.setDataChanged();
			for(ROIShape shape : shapeList)
			{
				if(getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /**
	 * Returns <code>true</code> if in the graph or intensity view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inDataView()
	{
		return (inIntensityView() || inGraphView() || inCalcWizardView());
	}
	
	/**
	 * Returns <code>true</code> if in the calcWizard view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inCalcWizardView()
	{
		return (tabs.getTitleAt(tabs.getSelectedIndex()).
				equals(calcWizard.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the graph view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inGraphView()
	{
		return (tabs.getTitleAt(tabs.getSelectedIndex()).
				equals(graphPane.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the intensity view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inIntensityView()
	{
		return (tabs.getTitleAt(tabs.getSelectedIndex()).
				equals(intensityView.getComponentName()));
	}
	
    /**
     * Returns the {@link #loadingWindow}.
     * 
     * @return See above.
     */
    LoadingWindow getLoadingWindow() { return loadingWindow; }
    
    /**
	 * Sets the passed color to the currently selected cell.
	 * 
	 * @param color The color to set.
	 */
    void setCellColor(Color color)
    {
		if (roiInspector != null) roiInspector.setCellColor(color);
	}
    
    /**
     * Selects the current figure based on ROIid, t and z sections.
     * 
     * @param ROIid The id of the selected ROI.
     * @param t 	The corresponding timepoint.
     * @param z 	The corresponding z-section.
     */
    void selectFigure(long ROIid, int t, int z)
    {
    	try {
    		ROI roi = model.getROI(ROIid);
    		ROIFigure fig = roi.getFigure(new Coord3D(z, t));
    		selectFigure(fig);
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}	
    }
    /** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showROIAssistant()
	 */
	public void showROIAssistant()
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view");
			return;
		}
		
		Collection<ROI> roiList = model.getSelectedROI();
		if (roiList.size() == 0)
		{
			un.notifyInfo("ROI Assistant", "Select a Figure to modify " +
			"using the ROI Assistant.");
			return;
		}
		if (roiList.size() > 1)
		{
			un.notifyInfo("ROI Assistant", "The ROI Assistant can" +
					"only be used on one ROI" +
			"at a time.");
			return;
		}
		ROI currentROI = roiList.iterator().next();
			
    	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), currentROI, this);
    	UIUtilities.setLocationRelativeToAndShow(this, assistant);
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showROIAssistant(ROI)
	 */
	public void showROIAssistant(ROI roi)
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view");
			return;
		}

	  	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), roi, this);
    	UIUtilities.setLocationRelativeToAndShow(this, assistant);
	}
	
    /**
     * Selects the passed figure.
     * 
     * @param figure The figure to select.
     */
    void selectFigure(ROIFigure figure)
    {
    	if (figure == null) return;
    	Coord3D coord3D = figure.getROIShape().getCoord3D();
    	if (coord3D == null) return;
    	if (!coord3D.equals(model.getCurrentView())) {
    		model.setPlane(coord3D.getZSection(), coord3D.getTimePoint());
    		SelectPlane request = 
    			new SelectPlane(model.getPixelsID(), coord3D.getZSection(), 
    							coord3D.getTimePoint());
    		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
    		bus.post(request);
    		updateDrawingArea();
    		//return;
    	}
    	
    	DrawingCanvasView dv = model.getDrawingView();
    	dv.clearSelection();
    	dv.addToSelection(figure);
		Collection figures = dv.getSelectedFigures();
		if (figures == null) return;
		Iterator i = figures.iterator();
		List roiList = new ArrayList();
		ROIFigure f;
		ROI roi;
		try {
			while (i.hasNext()) {
				f = (ROIFigure) i.next();
				roi = model.getROI(f.getROI().getID());
				if (roi != null) roiList.add(roi);
			}
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		dv.grabFocus();
		roiInspector.setSelectedFigures(roiList);
		roiManager.setSelectedFigures(roiList, false);
    }
    
    /**
     * Sets the selected figures.
     * 
     * @param figures Collection of selected figures.
     */
    void setSelectedFigures(Collection figures)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
		if (figures == null) return;
		Iterator i = figures.iterator();
		ROIFigure figure;
		List roiList = new ArrayList();
		ROI roi;
		try {
			while (i.hasNext()) {
				figure = (ROIFigure) i.next();
				roi = model.getROI(figure.getROI().getID());
				if (roi != null) roiList.add(roi);
			}
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		roiInspector.setSelectedFigures(roiList);
		roiManager.setSelectedFigures(roiList, true);
	}
    
    /**
     * Removes the specified figure from the display.
     * 
     * @param figure The figure to remove.
     */
    void removeROI(ROIFigure figure)
    {
    	if (figure == null) return;
    	try {
    		model.removeROIShape(figure.getROI().getID());
    	    roiManager.removeFigure(figure);
		} catch (Exception e) {
			handleROIException(e, DELETE_MSG);
		}
    }
    
    /**
     * Adds the specified figure to the display.
     * 
     * @param figure The figure to add.
     */
    void addROI(ROIFigure figure)
    {
    	if (figure == null) return;
    	ROI roi = null;
    	try {
    		roi = model.createROI(figure, true);
    	} catch (Exception e) {
			handleROIException(e, CREATE_MSG);
		}
    	if (roi == null) return;
    	List<ROI> roiList = new ArrayList<ROI>();
    	roiList.add(roi);
    	roiManager.addFigures(roiList);
    }
    
    /**
     * Reacts to the changes of attributes for the specified figure.
     * 
     * @param figure The figure to handle.
     */
    void onAttributeChanged(Figure figure)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
    	if (figure == null) return;
    	roiInspector.setModelData(figure);
    	roiManager.update();
    }
    
    /**
     * Returns the drawing.
     * 
     * @return See above.
     */
    Drawing getDrawing() { return model.getDrawing(); }
    
    /**
     * Returns the drawing view.
     * 
     * @return See above.
     */
    DrawingCanvasView getDrawingView() { return model.getDrawingView(); }
    
    /** Rebuilds the ROI table. */
    void rebuildManagerTable() { roiManager.rebuildTable(); }
    
    /** Rebuilds the results table. */
    void refreshResultsTable() { roiResults.refreshResults(); }
    
    /** 
     * Saves the results table.
     * 
     * @throws IOException Thrown if the data cannot be written.
     * @return false if save cancelled. 
     */
    boolean saveResultsTable() 
    	throws IOException
    { 
    	return roiResults.saveResults(); 
    }
    
    /**
	 * Shows the results wizard and updates the fields based on the users 
	 * selection.
	 */
	void showResultsWizard() { roiResults.showResultsWizard(); }
    
    /**
     * Handles the exception thrown by the <code>ROIComponent</code>.
     * 
     * @param e 	The exception to handle.
     * @param text 	The message displayed in the status bar.
     */
    void handleROIException(Exception e, String text)
    {
    	Registry reg = MeasurementAgent.getRegistry();
    	if (e instanceof ROICreationException || 
    		e instanceof NoSuchROIException)
    	{
    		reg.getLogger().error(this, 
    						"Problem while handling ROI "+e.getMessage());
    		statusBar.setStatus(text);
    	} else {
    		String s = "An unexpected error occured while handling ROI ";
    		reg.getLogger().error(this, s+e.getMessage());
    		reg.getUserNotifier().notifyError("ROI", s, e);
    	}
    }
    
    /** Updates the drawing area. */
	void updateDrawingArea()
	{
		Drawing drawing = model.getDrawing();
		drawing.removeDrawingListener(controller);
		drawing.clear();
		
		ShapeList list = null;
		try {
			list = model.getShapeList();
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		if (list != null) {
			TreeMap map = list.getList();
			Iterator i = map.values().iterator();
			ROIShape shape;
			while (i.hasNext()) {
				shape = (ROIShape) i.next();
				if (shape != null) drawing.add(shape.getFigure());
			}
		}
		setStatus(DEFAULT_MSG);
		model.getDrawingView().setDrawing(drawing);
		drawing.addDrawingListener(controller);
	}
	
	/**
	 * Propagates the selected shape in the roi model. 
	 * 
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 */
	void propagateShape(ROIShape shape, int timePoint, int zSection) 
	{
		ArrayList<ROIShape> addedShapes;
		try
		{
			addedShapes = model.propagateShape(shape, timePoint, zSection);
			for(ROIShape newShape : addedShapes)
			{
				if(newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
			}
			roiManager.addROIShapes(addedShapes);
		}
		catch (ROICreationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchROIException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setStatus(DEFAULT_MSG);
	}
	
	/**
	 * Deletes the selected shape from current coord to timepoint and z-section.
	 *  
	 * @param shape 	The initial shape to delete.
	 * @param timePoint The timepoint to delete to.
	 * @param zSection 	The z-section to delete to.
	 */
	void deleteShape(ROIShape shape, int timePoint, int zSection) 
	{
		try {
			model.deleteShape(shape, timePoint, zSection);
			
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		setStatus(DEFAULT_MSG);
		rebuildManagerTable();
	}

	/**
	 * Sets a message in the status bar.
	 * 
	 * @param text The text to display.
	 */
	void setStatus(String text) { statusBar.setStatus(text); }
	
	/** Builds the graphs and displays them in the results pane. */
	void displayAnalysisResults()
	{
		if (inGraphView()) graphPane.displayAnalysisResults();
		if (inIntensityView()) intensityView.displayAnalysisResults();
	}
	
	/**
     * Creates a single figure and returns to the selection tool.
     * 
     * @param createSingleFig See above.
     */
    void createSingleFigure(boolean createSingleFig)
    {
    	toolBar.createSingleFigure(createSingleFig);
    }
    
    /** 
     * Overridden to the set the location of the {@link MeasurementViewer}.
     * @see TopWindow#setOnScreen() 
     */
    public void setOnScreen()
    {
        if (model != null) { //Shouldn't happen
        	setSize(DEFAULT_SIZE);
        	
            UIUtilities.setLocationRelativeToAndSizeToWindow(
            		model.getRequesterBounds(), this, MAXIMUM_SIZE);
        } else {
            pack();
            UIUtilities.incrementRelativeToAndShow(null, this);
        }
    }

    /**
	 * Calculate the stats for the roi in the shapelist with id. This method
	 * will call the graphView.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	public void calculateStats(long id, ArrayList<ROIShape> shapeList)
	{
		if(model.getState() != MeasurementViewer.READY)
			return;
		model.calculateStats(id, shapeList);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		Cursor cursor;
		if(model.getDrawingEditor().getTool() instanceof DelegationSelectionTool)
			cursor = new Cursor(Cursor.DEFAULT_CURSOR);
		else
			cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		getDrawingView().setCursor(cursor);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
    
}
