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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.DelegationSelectionTool;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ImageViewport;
import org.openmicroscopy.shoola.agents.events.measurement.SelectPlane;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import pojos.WorkflowData;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

import com.sun.org.apache.bcel.internal.classfile.Code;

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
	extends TopWindow 
{
	
	/** The message displayed when a ROI cannot be retrieved. */
	static final String					RETRIEVE_MSG = "Cannot retrieve the " +
															"ROI";
	
	/** The message displayed when a ROI cannot be created. */
	static final String					CREATE_MSG = "Cannot create the ROI";

	/** The message displayed when a ROI cannot be deleted. */
	static final String					DELETE_MSG = "Cannot delete the ROI";
	
	/** 
	 * The message displayed when a an ROI exception occurred but cause 
	 * is unknown. 
	 */
	static final String					UNKNOWN_MSG = "An unknown, " +
										"unexpected error occurred in ";
	
	/** The default message. */
	static final String					DEFAULT_MSG = "";
	
	/** The default size of the window. */
	private static final Dimension		DEFAULT_SIZE = new Dimension(400, 300);
	
	/** The maximum size of the window. */
	private static final Dimension		MAXIMUM_SIZE = new Dimension(700, 300);
	
	/** The title for the measurement tool main window. */
	private static final String			WINDOW_TITLE = "Measurement Tool ";
	
	/** index to identify inspector tab. */
	public static final int				INSPECTOR_INDEX = 0;

	/** index to identify manager tab. */
	public static final int				MANAGER_INDEX = 1;
	
	/** index to identify results tab. */
	public static final int				RESULTS_INDEX = 2;
	
	/** index to identify graph tab. */
	public static final int				GRAPH_INDEX = 3;

	/** index to identify intensity tab. */
	public static final int				INTENSITY_INDEX = 4;
		
	/** index to identify calculation tab. */
	public static final int				CALCWIZARD_INDEX = 5;
	
	/** index to identify intensity results view tab. */
	public static final int				INTENSITYRESULTVIEW_INDEX = 6;
	
	/** Reference to the Model. */
	private MeasurementViewerModel 		model;

	/** Reference to the Control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the Component. */
	private MeasurementViewer			component;
	
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

	/** The graphing component. */
	private IntensityResultsView	 	intensityResultsView;
	
	/** The calculation Wizard component. */
	private CalculationWizard			calcWizard;
	
    /** Tab pane hosting the various panel. */
    private JTabbedPane					tabs;
 
    /** The status bar. */
    private StatusBar					statusBar;

    /** the creation option to create multiple figures in the UI. */
    private JCheckBoxMenuItem 			createMultipleFigure;
    
    /** the creation option to create single figures in the UI. */
    private JCheckBoxMenuItem 			createSingleFigure;
    
    /** The collection of components displaying the tables. */
    private List<ServerROITable>		roiTables;
    
    /** The main menu bar. */
    private JMenuBar 					mainMenu;
    
    /** The menu bar handling the workflows. */
    private JMenu 						workflowMenu;
   
    /** The existing workflow menu. */
    private JMenu 						existingWorkflow;
    
    /** Buttong group of exisitng workflows. */
    private ButtonGroup					workflows;
    
    /** The map holding the work-flow objects. */
    private Map<String, String>			workflowsUIMap;
    
    /** 
     * Flag indicating that the measurement was shown before adding 
     * a new figure.
     */
    private Boolean measurementShown;
    
    /**
     * Scrolls to the passed figure.
     * 
     * @param figure The figure to handle.
     */
    private void scrollToFigure(ROIFigure figure)
    {
    	EventBus bus = MeasurementAgent.getRegistry().getEventBus();
    	bus.post(new ImageViewport(model.getImageID(), model.getPixelsID(),
    			figure.getBounds().getBounds()));
    }
    
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
    	workflowMenu = createWorkFlowMenu();
    	//menuBar.add(workflowMenu);
        return menuBar;
    }
    
    /**
     * Helper method to create the controls menu.
     * 
     * @return The controls sub-menu.
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
     * @return The options sub-menu.
     */
    private JMenu createOptionsMenu()
    {
        JMenu menu = new JMenu("Options");
        ButtonGroup displayUnits = new ButtonGroup();
    	
        menu.setMnemonic(KeyEvent.VK_O);
        
        JMenu subMenu = new JMenu("Units");
        JCheckBoxMenuItem item;
        MeasurementViewerAction a;
        if (model.sizeInMicrons()) {
        	a = controller.getAction(MeasurementViewerControl.IN_MICRONS);
        	item = new JCheckBoxMenuItem(a);
            item.setText(a.getName());
            displayUnits.add(item);
            subMenu.add(item);
            item.setSelected(true);
           
        }
        model.showMeasurementsInMicrons(model.sizeInMicrons());
        a = controller.getAction(MeasurementViewerControl.IN_PIXELS);
        item = new JCheckBoxMenuItem(a);
        item.setText(a.getName());
        displayUnits.add(item);
        subMenu.add(item);
        if (!model.sizeInMicrons()) item.setSelected(true);
        
        menu.add(subMenu);
        
        ButtonGroup createFigureGroup = new ButtonGroup();
    	JMenu creationMenu = new JMenu("ROI Creation");
        a = controller.getAction(
    			MeasurementViewerControl.CREATE_SINGLE_FIGURE);
        createSingleFigure = new JCheckBoxMenuItem(a);
        createSingleFigure.setText(a.getName());
        createFigureGroup.add(createSingleFigure);
        creationMenu.add(createSingleFigure);
        a = controller.getAction(
        		MeasurementViewerControl.CREATE_MULTIPLE_FIGURES);
        createMultipleFigure = new JCheckBoxMenuItem(a);
        createMultipleFigure.setText(a.getName());
        createFigureGroup.add(createMultipleFigure);
        creationMenu.add(createMultipleFigure);
        createMultipleFigure.setSelected(true); //TODO: retrieve info
        menu.add(creationMenu);
        return menu;
    }
    
    /**
     * Helper method to create the Workflow menu.
     * 
     * @return The options sub-menu.
     */
    private JMenu createWorkFlowMenu()
    {
        JMenu menu = new JMenu("Workflow");
        existingWorkflow = new JMenu("Existing Workflows");
       	workflows = new ButtonGroup();
        menu.setMnemonic(KeyEvent.VK_W);
        
        List<String> workFlows = model.getWorkflows();
        MeasurementViewerAction a;
        JCheckBoxMenuItem workflowItem;
        String uiWorkFlow;
        for (String workFlow : workFlows)
        {
        	a = controller.getAction(MeasurementViewerControl.SELECT_WORKFLOW);
        	workflowItem = new JCheckBoxMenuItem(a);
        	workflowItem.setSelected(WorkflowData.DEFAULTWORKFLOW.equals(
        			workFlow));
        	uiWorkFlow = getWorkflowDisplay(workFlow);
        	workflowItem.setText(uiWorkFlow);
        	workflows.add(workflowItem);
        	existingWorkflow.add(workflowItem);
        }
        menu.add(existingWorkflow);    
       	a = controller.getAction(MeasurementViewerControl.CREATE_WORKFLOW);
       	
       	JMenuItem createWorkflow = new JMenuItem();
        createWorkflow.setText(a.getName());
    	createWorkflow.addActionListener(a);
    	//tmp
       	//menu.add(createWorkflow);
        return menu;
    }

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		workflowsUIMap = new HashMap<String, String>();
		roiTables = new ArrayList<ServerROITable>();
		statusBar = new StatusBar();
		toolBar = new ToolBar(component, this, controller, model);
		roiManager = new ObjectManager(this, model);
		roiInspector = new ObjectInspector(controller, model);
		roiResults = new MeasurementResults(controller, model, this);
		graphPane = new GraphPane(this, controller, model);
		intensityView = new IntensityView(this, model);
		intensityResultsView = new IntensityResultsView(this, model);
		calcWizard = new CalculationWizard(controller, model);
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		DrawingCanvasView canvasView = model.getDrawingView();
		canvasView.addMouseListener(new MouseAdapter() {
			
		    /**
		     * Sets the cursor.
			 * @see MouseListener#mouseEntered(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e)
			{
				Cursor cursor;
				if (model.getDrawingEditor().getTool() instanceof 
					DelegationSelectionTool)
					cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				else
					cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
				getDrawingView().setCursor(cursor);
			}
			
		});
		
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addChangeListener(new ChangeListener()
		{
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt)
			{
				if (model.isHCSData()) {
					updateDrawingArea();
				} else {
					if (inDataView())
						controller.analyseSelectedFigures();
				}
			}
		});
	}

	
	
	/**
     * Creates a new instance.
     * The 
     * {@link #initialize(MeasurementViewer, MeasurementViewerControl,
     *  MeasurementViewerModel) initialize}
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
	 * @param component    Reference to the Component.
	 *                      Mustn't be <code>null</code>.
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
    void initialize(MeasurementViewer component, 
    			MeasurementViewerControl controller, 
    			MeasurementViewerModel model)
    {
        if (component == null) throw new NullPointerException("No control.");
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.component = component;
        this.model = model;
        this.controller = controller;
        
        controller.attachListeners();
        ImageIcon icon = IconManager.getInstance().getImageIcon(
        		IconManager.MEASUREMENT_TOOL);
        if (icon != null) setIconImage(icon.getImage());
        initComponents();
        //buildGUI();
    }
    
    /** Builds and lays out the GUI. */
	void buildGUI()
	{
		mainMenu = createMenuBar();
		setJMenuBar(mainMenu);
		tabs.addTab(roiManager.getComponentName(), 
					roiManager.getComponentIcon(), roiManager);
		tabs.addTab(roiInspector.getComponentName(), 
			roiInspector.getComponentIcon(), roiInspector);
		if (!model.isBigImage()) {
			tabs.addTab(roiResults.getComponentName(),
				roiResults.getComponentIcon(), roiResults);
			tabs.addTab(graphPane.getComponentName(),
				graphPane.getComponentIcon(), graphPane);
			tabs.addTab(intensityView.getComponentName(),
				intensityView.getComponentIcon(), intensityView);
			tabs.addTab(intensityResultsView.getComponentName(),
				intensityResultsView.getComponentIcon(), intensityResultsView);
		}
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
	}

    /** 
     * Displays the menu at the specified location if not already visible.
     * 
     * @param x The x-coordinate of the mouse click.
     * @param y The y-coordinate of the mouse click.
     */
    void showROIManagementMenu(int x, int y)
    {
    	roiManager.showROIManagementMenu(x, y);
    }
    
	/**
	 * Merge the ROIShapes with ids in the idList and the ROIShapes selected 
	 * in the shapeList from those ROI.
	 * 
	 * @param idList see above.
	 * @param shapeList see above.
	 */
	void mergeROI(List<Long> idList, List<ROIShape> shapeList)
	{
		try
		{
			model.notifyDataChanged(true);
			ROI newROI = model.cloneROI(idList.get(0));
			ROIShape newShape;
			for (ROIShape shape : shapeList)
			{
				newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if (getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			if (e instanceof ROICreationException)
				handleROIException(e, CREATE_MSG);
			else if (e instanceof NoSuchROIException)
				handleROIException(e, RETRIEVE_MSG);
			else handleROIException(e, UNKNOWN_MSG+"Merging ROI");
		}
		
	}
	
	/**
	 * Splits the ROIShapes from the ROI with id and the ROIShapes selected in 
	 * the shapeList from that ROI.
	 * 
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void splitROI(long id, List<ROIShape> shapeList)
	{
		try
		{
			model.notifyDataChanged(true);
			ROI newROI = model.cloneROI(id);
			ROIShape newShape;
			for (ROIShape shape : shapeList)
			{
				newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if (getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if (newShape.getCoord3D().equals(model.getCurrentView()))
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
			if(e instanceof ROICreationException)
				handleROIException(e, CREATE_MSG);
			else if(e instanceof NoSuchROIException)
				handleROIException(e, RETRIEVE_MSG);
			else 
				handleROIException(e, UNKNOWN_MSG+"Splitting ROI.");
		}
			
	}
	
	/**
	 * Duplicate the ROI with id and the ROIShapes selected in the shapeList 
	 * from that ROI.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void duplicateROI(long id, List<ROIShape> shapeList)
	{
		try
		{
			model.notifyDataChanged(true);
			ROI newROI = model.cloneROI(id);
			ROIShape newShape;
			Drawing drawing = model.getDrawing();
			for (ROIShape shape : shapeList)
			{
				newShape = new ROIShape(newROI, shape.getCoord3D(), shape);
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					drawing.removeDrawingListener(controller);
					drawing.add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					drawing.addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
			updateDrawingArea();
		}
		catch (Exception e)
		{
			handleROIException(e, CREATE_MSG);
		}
	}
	
	/**
	 * Deletes the ROI with id and the ROIShapes selected in the shapeList.
	 * 
	 * @param shapeList see above.
	 */
	void deleteROIShapes(List<ROIShape> shapeList)
	{
		try
		{
			ROIFigure roi;
			ROI r;
			boolean b = false;
			for (ROIShape shape : shapeList)
			{
				roi = shape.getFigure();
				r = roi.getROI();
				if (!r.isClientSide()) b = true;
				if (getDrawing().contains(roi))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(roi);
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				model.markROIForDelete(shape.getID(), r);
			}
			model.notifyDataChanged(b);
		} catch (Exception e) {
			handleROIException(e, DELETE_MSG);
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
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(calcWizard.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the graph view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inGraphView()
	{
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(graphPane.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the intensity view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inIntensityView()
	{
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(intensityView.getComponentName()));
	}
	

	/**
	 * Returns <code>true</code> if in the intensity Results view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inIntensityResultsView()
	{
		return (tabs.getTitleAt(tabs.getSelectedIndex()).
				equals(intensityResultsView.getComponentName()));
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
    	int row = -1;
		if (roiInspector != null) row = roiInspector.setCellColor(color);
		Collection<Figure> l = model.getSelectedFigures();
		if (l == null || l.size() == 0) return;
		Iterator<Figure> i = l.iterator();
		switch (row) {
			case ObjectInspector.FILL_COLOR_ROW:
				AttributeKeys.FILL_COLOR.set(i.next(), color);
				break;
			case ObjectInspector.LINE_COLOR_ROW:
				AttributeKeys.STROKE_COLOR.set(i.next(), color);
		}
		model.getDrawingView().repaint();
	}
    
    /**
     * Selects the current figure based on ROIid, t and z sections.
     * 
     * @param ROIid     The id of the selected ROI.
     * @param t 	The corresponding time-point.
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
    
    /** Displays the ROI assistant. */
	void showROIAssistant()
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view.");
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
    	updateDrawingArea();
    	
	}
	
	/**
	 * Displays the {@link ROIAssistant} for the passed ROI.
	 * 
	 * @param roi The ROI to handle.
	 */
	void showROIAssistant(ROI roi)
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
    	updateDrawingArea();
	}
	
    /**
     * Selects the passed figure.
     * 
     * @param figure The figure to select.
     */
    void selectFigure(ROIFigure figure)
    {
    	if (figure == null) {
    		model.getDrawingView().setToolTipText("");
    		return;
    	}
    	Coord3D coord3D = figure.getROIShape().getCoord3D();
    	if (coord3D == null) return;
    	if (!coord3D.equals(model.getCurrentView()) || model.isBigImage()) {
    		model.setPlane(coord3D.getZSection(), coord3D.getTimePoint());
    		SelectPlane request = 
    			new SelectPlane(model.getPixelsID(), coord3D.getZSection(), 
    							coord3D.getTimePoint());
    		if (model.isBigImage()) {
    			request.setBounds(figure.getBounds().getBounds());
    		}
    		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
    		bus.post(request);
    		updateDrawingArea();
    		//return;
    	}
    	
    	DrawingCanvasView dv = model.getDrawingView();
    	dv.setToolTipText(""+figure.getAttribute(MeasurementAttributes.TEXT));
    	dv.clearSelection();
    	dv.addToSelection(figure);
		List<ROIShape> roiShapeList = new ArrayList<ROIShape>();
		roiShapeList.add(figure.getROIShape());
		dv.grabFocus();
		if (model.isHCSData()) {
			List<Long> ids = new ArrayList<Long>();
			Iterator<ROIShape> j = roiShapeList.iterator();
			ROIShape roiShape;
			while (j.hasNext()) {
				roiShape = (ROIShape) j.next();
				ids.add(roiShape.getROI().getID());
			}
			Component c = tabs.getSelectedComponent();
			if (c instanceof ServerROITable) {
				((ServerROITable) c).selectROI(ids);
			}
		} else {
			roiInspector.setSelectedFigures(roiShapeList);
			roiManager.setSelectedFigures(roiShapeList, false);
			intensityResultsView.onFigureSelected();
			intensityView.onFigureSelected();
			toolBar.onFigureSelected();
			displayAnalysisResults();
		}
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
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi;
		ROIShape shape;
		try {
			while (i.hasNext()) {
				figure = (ROIFigure) i.next();
				shape = figure.getROIShape();
				if (shape != null) shapeList.add(shape);
			}
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		if (model.isHCSData()) {
			List<Long> ids = new ArrayList<Long>();
			Iterator<ROIShape> j = shapeList.iterator();
			while (j.hasNext()) {
				shape = (ROIShape) j.next();
				ids.add(shape.getROI().getID());
			}
			Component c = tabs.getSelectedComponent();
			if (c instanceof ServerROITable) {
				((ServerROITable) c).selectROI(ids);
			}
		} else {
			roiInspector.setSelectedFigures(shapeList);
			roiManager.setSelectedFigures(shapeList, true);
			intensityResultsView.onFigureSelected();
			intensityView.onFigureSelected();
			toolBar.onFigureSelected();
		}
	}
    
    /**
     * Sets the figures selected from the table.
     * 
     * @param figures
     */
    void setTableSelectedFigure(List<ROIFigure> figures)
    {
    	if (figures == null) return;
    	DrawingCanvasView dv = model.getDrawingView();
    	Iterator<ROIFigure> k = figures.iterator();
    	ROIFigure figure;
    	dv.clearSelection();
    	if (figures == null || figures.size() == 0) return;
    	dv.removeFigureSelectionListener(controller);
    	int n = figures.size()-1;
    	int index = 0;
    	while (k.hasNext()) {
    		figure = k.next();
    		dv.addToSelection(figure);
    		if (index == n) {
    			scrollToFigure(figure);	
    		}
    		index++;
		}
    	dv.addFigureSelectionListener(controller);
		dv.grabFocus();
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
    		if (!model.isHCSData()) {
    			roiManager.removeFigure(figure);
    			roiResults.refreshResults();
    			roiInspector.removeROIFigure(figure);
    			//intensityResultsView
    			//graphPane
    		}
		} catch (Exception e) {
			handleROIException(e, DELETE_MSG);
		}
    }
    
	/** 
	 * Deletes the ROI from Display.
	 * 
	 * @param figures The figure to remove.
	 */
	void deleteROIs(List<ROIFigure> figures)
	{
		if (figures == null || figures.size() == 0) return;
		try {
			roiManager.removeFigures(figures);
			roiResults.refreshResults();
			roiInspector.removeROIFigures(figures);
			intensityView.onFigureRemoved();
			intensityResultsView.removeAllResults();
			graphPane.clearData();
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
    		boolean isDuplicate = getDrawingView().isDuplicate();
    		roi = model.createROI(figure, !isDuplicate);
    		if (!isDuplicate) {
	    		MeasurementAttributes.SHOWTEXT.set(figure, 
	    					roiInspector.isShowText());
	    		if (figure instanceof MeasureLineFigure) {
	    			measurementShown = roiInspector.isShowMeasurement();
	    			MeasurementAttributes.SHOWMEASUREMENT.set(figure, 
	        				true);
	    		} else {
	    			boolean b = roiInspector.isShowMeasurement();
	    			if (measurementShown != null)
	    				b = measurementShown.booleanValue();
	    			MeasurementAttributes.SHOWMEASUREMENT.set(figure, b);
	    			measurementShown = null;
	    		}
    		}
    		getDrawingView().unsetDuplicate();
    	} catch (Exception e) {
			handleROIException(e, CREATE_MSG);
		}
    	if (roi == null) return;
    	List<ROI> roiList = new ArrayList<ROI>();
    	roiList.add(roi);
    	if (!model.isHCSData()) {
    		roiManager.addFigures(roiList);
        	roiResults.refreshResults();
    	}
    }
    
    /**
     * Reacts to the changes of attributes for the specified figure.
     * 
     * @param figure The figure to handle.
     */
    void onAttributeChanged(ROIFigure figure)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
    	if (figure == null) return;
    	//getDrawingView().repaint();
    	if (!model.isHCSData()) {
    		roiInspector.setModelData(figure);
        	//roiManager.update();
        	roiResults.refreshResults();
    	}
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
    void rebuildManagerTable()
    { 
    	if (!model.isHCSData()) roiManager.rebuildTable(); 
    }
    
    /** Sets the value in the tool bar.*/
    void refreshToolBar()
    {
    	toolBar.refreshToolBar();
    }
    
    /** Rebuilds the results table. */
    void refreshResultsTable()
    { 
    	if (!model.isHCSData())
    		roiResults.refreshResults();
    }
    
    /** Rebuild the inspector table. */
    void refreshInspectorTable()
    { 
    	if (!model.isHCSData())
    		roiInspector.repaint();
    } 

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
    	} 
    	else 
    	{
    		String s = "An unexpected error occured while handling ROI ";
    		reg.getLogger().error(this, s+e.getMessage());
    		reg.getUserNotifier().notifyError("ROI", s, e);
    	}
    }
    
    /** Lays out the UI. */
    void layoutUI()
    {
    	if (model.isHCSData()) {
    		tabs.removeAll();
    		Collection l = model.getMeasurementResults();
    		Iterator i = l.iterator();
    		ROIResult result;
    		ServerROITable comp;
    		while (i.hasNext()) {
    			result = (ROIResult) i.next();
				comp = new ServerROITable(this, model);
				comp.setResult(result);
				roiTables.add(comp);
				tabs.addTab(comp.getComponentName(), comp.getComponentIcon(), 
						comp);
			}
    		if (l.size() > 0) tabs.setSelectedIndex(0);
    		getContentPane().remove(toolBar);
    		setJMenuBar(null);
    	}
    }
    
    /** Updates the drawing area. */
	void updateDrawingArea()
	{
		Drawing drawing = model.getDrawing();
		drawing.removeDrawingListener(controller);
		drawing.clear();
		ShapeList list = null;
		ROIFigure figure;
		Iterator<ROIFigure> f;
		List<ROIFigure> first = new ArrayList<ROIFigure>();
		List<ROIFigure> second = new ArrayList<ROIFigure>();
		if (model.isHCSData()) {
			Component comp = tabs.getSelectedComponent();
			if (comp instanceof ServerROITable) {
				ServerROITable table = (ServerROITable) comp;
				try {
					long fileID = table.getFileID();
					List<ROI> rois;
					if (fileID >= 0) {
						 rois = model.getROIList(fileID);
						 if (rois != null) {
							 Iterator<ROI> k = rois.iterator();
							 ROI roi;
							 TreeMap<Coord3D, ROIShape> shapes;
							 Iterator<ROIShape> j;
							 ROIShape shape;
							 
							 while (k.hasNext()) {
								 roi = k.next();
								 shapes = roi.getShapes();
								 j = shapes.values().iterator();
								 while (j.hasNext()) {
									 shape = j.next();
									 figure = shape.getFigure();
									 if (!(figure instanceof MeasureMaskFigure)) 
										 second.add(figure);
									 else
										 first.add(figure);
								 }
							 }
							 f = first.iterator();
							 while (f.hasNext()) {
								 figure = f.next();
								 drawing.add(figure);
								 figure.addFigureListener(controller);
							 }
							 f = second.iterator();
							 while (f.hasNext()) {
								 figure = f.next();
								 drawing.add(figure);
								 figure.addFigureListener(controller);
							 }
						 }
					} else {
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
								if (shape != null) 
								{
									figure = shape.getFigure();
									drawing.add(figure);
									figure.addFigureListener(controller);
								}
							}
						}
					}
					
				} catch (Exception e) {
					handleROIException(e, RETRIEVE_MSG);
				}
			}
			DrawingCanvasView canvas = model.getDrawingView();
			KeyListener[] l = canvas.getKeyListeners();
			if (l != null) {
				for (int i = 0; i < l.length; i++)
					canvas.removeKeyListener(l[i]);
			}
		} else { //non HCS data
			try {
				list = model.getShapeList();
			} catch (Exception e) {
				handleROIException(e, RETRIEVE_MSG);
			}
			if (list != null) {
				TreeMap map = list.getList();
				Iterator i = map.values().iterator();
				ROIShape shape;
				//mask
				while (i.hasNext()) {
					shape = (ROIShape) i.next();
					if (shape != null) {
						figure = shape.getFigure();
						 if (!(figure instanceof MeasureMaskFigure)) 
							 second.add(figure);
						 else
							 first.add(figure);
					}
				}
				f = first.iterator();
				 while (f.hasNext()) {
					 figure = f.next();
					 drawing.add(figure);
					 figure.addFigureListener(controller);
				 }
				 f = second.iterator();
				 while (f.hasNext()) {
					 figure = f.next();
					 drawing.add(figure);
					 figure.addFigureListener(controller);
				 }
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
		List<ROIShape> addedShapes;
		try
		{
			addedShapes = model.propagateShape(shape, timePoint, zSection);
			ROIFigure figToDelete = null;
			ROIFigure roiFig;
			for (ROIShape newShape : addedShapes)
			{
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					figToDelete = null;
					for (Figure f : getDrawing().getFigures()) {
						roiFig = (ROIFigure) f;
						if (roiFig.getROI().getID() == newShape.getID())
							figToDelete = roiFig;
					}
					if (figToDelete!=null)
						getDrawing().remove(figToDelete);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				newShape.getFigure().calculateMeasurements();
			}
			if (!model.isHCSData()) roiManager.addROIShapes(addedShapes);
		}
		catch (ROICreationException e)
		{
			handleROIException(e, CREATE_MSG);
		}
		catch (NoSuchROIException e)
		{
			handleROIException(e, RETRIEVE_MSG);
		}
		setStatus(DEFAULT_MSG);
	}
	
	/**
	 * Deletes the selected shape from current coordinate to timepoint 
	 * and z-section.
	 *  
	 * @param shape 	The initial shape to delete.
	 * @param timePoint The timepoint to delete to.
	 * @param zSection 	The z-section to delete to.
	 */
	void deleteShape(ROIShape shape, int timePoint, int zSection) 
	{
		try 
		{
			model.deleteShape(shape, timePoint, zSection);
		} catch (Exception e) 
		{
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
	
	/**
	 * Sets a message in the status bar.
	 * 
	 * @param text The text to display.
	 */
	void setPlaneStatus(String text) { statusBar.setPlaneStatus(text); }
	
	/**
	 * Sets ready message in the status bar.
	 */
	void setReadyStatus() { setStatus(DEFAULT_MSG); }
	
	/** Builds the graphs and displays them in the results pane. */
	void displayAnalysisResults()
	{
		if (inGraphView()) graphPane.displayAnalysisResults();
		else if (inIntensityView()) intensityView.displayAnalysisResults();
		else if (inIntensityResultsView()) 
			intensityResultsView.displayAnalysisResults();
	}
	
	/**
     * Creates a single figure and returns to the selection tool.
     * 
     * @param createSingleFig See above.
     */
    void createSingleFigure(boolean createSingleFig)
    {
    	if (createSingleFig)
    	{
    		createSingleFigure.setSelected(true);
    		createMultipleFigure.setSelected(false);
    	}
    	else
    	{
    		createSingleFigure.setSelected(false);
    		createMultipleFigure.setSelected(true);
    	}
    	toolBar.createSingleFigure(createSingleFig);
    	
    }
    
	/**
     * is the user menu set to create single figures
     * 
     * @return see above.
     */
    boolean isCreateSingleFigure()
    {
    	return createSingleFigure.isSelected();
    }
    
    /**
     * Returns the id of the pixels set this tool is for.
     * 
     * @return See above.
     */
    long getPixelsID() { return model.getPixelsID(); }
 
    /**
	 * Calculate the stats for the Rois in the shapelist. This method
	 * will call the graphView.
	 * 
	 * @param shapeList see above.
	 */
	void calculateStats(List<ROIShape> shapeList)
	{
		if (model.getState() != MeasurementViewer.READY) return;
		model.calculateStats(shapeList);
	}

    /** Updates the workflow in the toolbar. */
	void addedWorkflow()
	{
		if (workflowMenu != null && mainMenu != null 
				&& existingWorkflow != null)
		{
			 Enumeration<AbstractButton> buttons = workflows.getElements();
			 List<AbstractButton> buttonList = new ArrayList<AbstractButton>();
			 while(buttons.hasMoreElements())
				 buttonList.add(buttons.nextElement());
			 
			ActionListener[] l = existingWorkflow.getActionListeners();
			for (ActionListener a :l )
				existingWorkflow.removeActionListener(a);
			for (AbstractButton button : buttonList)
				workflows.remove(button);
			existingWorkflow.removeAll();
			workflows = new ButtonGroup();
			List<String> workFlows = model.getWorkflows();
		    JCheckBoxMenuItem workflowItem;
		    MeasurementViewerAction action;
		    String uiWorkFlow;
		    for (String workFlow : workFlows)
		    {
		    	action = controller.getAction(
		    			MeasurementViewerControl.SELECT_WORKFLOW);
		    	workflowItem = new JCheckBoxMenuItem(action);
		    	uiWorkFlow = getWorkflowDisplay(workFlow);
		    	workflowsUIMap.put(uiWorkFlow, workFlow);
		    	workflowItem.setSelected(
		    			WorkflowData.DEFAULTWORKFLOW.equals(workFlow));
		    	workflowItem.setText(uiWorkFlow);
		    	workflows.add(workflowItem);
		    	existingWorkflow.add(workflowItem);
		    	workflowItem.setEnabled(true);
		    }
		    for (ActionListener a :l )
				existingWorkflow.addActionListener(a);
		    toolBar.addedWorkflow();
		}
	}
	
	/** Updates the workflow list. */
	void updateWorkflow() { toolBar.updateWorkflow(); }
	
 	/** Adds the workflow to the toolbar.  */
	void createWorkflow() { toolBar.createWorkflow(); }
	
	/**
	 * Returns The UI representations of the workflow.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	String getWorkflowDisplay(String value)
	{
		/*
		String result = value;
		if (value.contains("/")) {
			String[] list = value.split("/");
			result = list[list.length-1];
		}
		*/
		String result = EditorUtil.getWorkflowForDisplay(value);
		if (!workflowsUIMap.containsKey(result))
			workflowsUIMap.put(result, value);
		return result;
	}
	
	/**
	 * Returns the workflow corresponding to the specified UI value.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	String getWorkflowFromDisplay(String value)
	{
		if (value == null) return null;
		return workflowsUIMap.get(value);
	}
	
	/** Invokes when the figures are selected. */
	void onSelectedFigures()
	{
		roiManager.onSelectedFigures();
	}
	
	/**
	 * Creates a file chooser used to select where to save the results
	 * as an Excel file.
	 * 
	 * @return See above.
	 */
	FileChooser createSaveToExcelChooser()
	{
		List<FileFilter> filterList = new ArrayList<FileFilter>();
		FileFilter filter = new ExcelFilter();
		filterList.add(filter);
		FileChooser chooser =
			new FileChooser(this, FileChooser.SAVE, "Save Results to Excel", 
					"Save the Results data to a file which can be loaded by " +
					"a spreadsheet.", filterList);
		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) chooser.setCurrentDirectory(f);
		} catch (Exception ex) {}
		return chooser;
	}
	

	/**
	 * Shows or hides the Text of all shapes.
	 * 
	 * @param show  Pass <code>true</code> to show the text, <code>false</code>
	 * 				otherwise. 
	 */
	void showText(boolean show)
	{
		Collection<ROIFigure> figures = model.getAllFigures();
		if (figures.size() == 0) return;
		Iterator<ROIFigure> i = figures.iterator();
		ROIFigure figure;
		while (i.hasNext()) {
			figure = i.next();
			if (!figure.isReadOnly()) {
				MeasurementAttributes.SHOWTEXT.set(figure, show);
				if (roiInspector != null) roiInspector.showText(show, figure);
			}
		}
		model.getDrawingView().repaint();
	}
	
 	/**
 	 * Indicates any on-going analysis.
 	 * 
 	 * @param analyse Passes <code>true</code> when analyzing,
 	 * <code>false</code> otherwise.
 	 */
	void onAnalysed(boolean analyse)
	{
		graphPane.onAnalysed(analyse);
		intensityView.onAnalysed(analyse);
		toolBar.onAnalysed(analyse);
	}
	
    /** 
     * Overridden to the set the location of the {@link MeasurementViewer}.
     * @see TopWindow#setOnScreen() 
     */
    public void setOnScreen()
    {
    	setSize(DEFAULT_SIZE);
        if (model != null) { //Shouldn't happen
            UIUtilities.setLocationRelativeToAndSizeToWindow(
            		model.getRequesterBounds(), this, MAXIMUM_SIZE);
        } else {
            //pack();
            UIUtilities.incrementRelativeToAndShow(null, this);
        }
    }
    
	/** 
     * Overridden to the hide the window'd items of the UI.
     * @see TopWindow#setVisible() 
     */
 	public void setVisible(boolean value)
	{
		if (!value)
			toolBar.getWorkflowPanel().setVisible(false);
		super.setVisible(value);
	}
    
}
