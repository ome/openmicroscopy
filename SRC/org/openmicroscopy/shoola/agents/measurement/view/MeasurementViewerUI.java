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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

//Third-party libraries
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.agents.measurement.view.roiassistant.ROIAssistant;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class MeasurementViewerUI 
	extends TopWindow
{

	/** The default size of the window. */
	private static final Dimension		DEFAULT_SIZE = new Dimension(400, 300);
	
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
	
	/** The Results componetn. */
	private MeasurementResults			roiResults;
	
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
        return menu;
    }
    
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		statusBar = new StatusBar();
		toolBar = new ToolBar(controller, model);
		roiInspector = new ObjectInspector(controller, model);
		roiManager = new ObjectManager(this, model);
		roiResults = new MeasurementResults(controller, model, this);
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setJMenuBar(createMenuBar());
		tabs.addTab(roiInspector.getComponentName(), 
					roiInspector.getComponentIcon(), roiInspector);
		tabs.addTab(roiManager.getComponentName(), 
					roiManager.getComponentIcon(), roiManager);
		tabs.addTab(roiResults.getComponentName(), 
					roiResults.getComponentIcon(), roiResults);
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
	}
	
	/**
     * Creates a new instance.
     * The 
     * {@link #initialize(MeasurementViewerControl, 
     * MeasurementViewerModel) initialize} method should be called straight 
     * after to link this View to the Controller.
     * 
     * @param title The window title.
     */
	public MeasurementViewerUI(String title)
    {
        super(title);
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
    		ROIFigure fig = roi.getFigure(new Coord3D(t, z));
    		selectFigure(fig);
		} catch (Exception e) {
			handleROIException(e);
		}	
    }
    
    /**
     * Selects the passed figure.
     * 
     * @param figure The figure to select.
     */
    void selectFigure(ROIFigure figure)
    {
    	if (figure == null) return;
    	if(!figure.getROIShape().getCoord3D().equals(model.getCurrentView())) 
    		return;
    	DrawingView dv = model.getDrawingView();
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
			handleROIException(e);
		}
		dv.grabFocus();
		roiInspector.setSelectedFigures(roiList);
		roiManager.setSelectedFigures(roiList);
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
			handleROIException(e);
		}
		roiInspector.setSelectedFigures(roiList);
		roiManager.setSelectedFigures(roiList);
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
			handleROIException(e);
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
    		roi = model.createROI(figure);
		} catch (Exception e) {
			handleROIException(e);
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
    DrawingView getDrawingView() { return model.getDrawingView(); }
    
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
	 * Shows the ROIAssistant and updates the ROI based on the users 
	 * selection.
	 */
	void showROIAssistant() { createDisplayROIAssistant(); }    
    
    /**
     * Handles the exception thrown by the <code>ROIComponent</code>.
     * 
     * @param e The exception to handle.
     */
    void handleROIException(Exception e)
    {
    	Registry reg = MeasurementAgent.getRegistry();
    	if (e instanceof ROICreationException || 
    		e instanceof NoSuchROIException)
    	{
    		reg.getLogger().error(this, 
    						"Problem while handling ROI "+e.getMessage());
    		statusBar.setStatus(e.getMessage());
    	} else {
    		String s = "An unexpected error occured while handling ROI ";
    		reg.getLogger().error(this, s+e.getMessage());
    		reg.getUserNotifier().notifyError("ROI", s, e);
    	}
    }
    
    /** 
     * Overridden to the set the location of the {@link MeasurementViewer}.
     * @see TopWindow#setOnScreen() 
     */
    public void setOnScreen()
    {
        if (model != null) { //Shouldn't happen
        	setSize(DEFAULT_SIZE);
            UIUtilities.setLocationRelativeTo(model.getRequesterBounds(), this);
        } else {
            pack();
            UIUtilities.incrementRelativeToAndShow(null, this);
        }
    }

    
    /**
	 * Shows the ROIAssistant and updates the ROI based on the users 
	 * selection.
	 */
	private void createDisplayROIAssistant()
    {
		Collection<ROI> roiList = model.getSelectedROI();
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if(roiList.size()==0)
		{
				un.notifyInfo("ROI Assistant", "Select a Figure to modify " +
												"using the ROI Assistant.");
				return;
		}
		if(roiList.size()>1)
		{
				un.notifyInfo("ROI Assistant", "The ROI Assistant can" +
												"only be used on one ROI" +
												"at a time.");
				return;
		}
		ROI currentROI = roiList.iterator().next();
    	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), currentROI, this);
    	assistant.setVisible(true);
    }
	
	public void propagateShape(ROIShape shape, int timePoint, int zSection) 
	throws 	ROICreationException, 
			NoSuchROIException
	{
		model.propagateShape(shape, timePoint, zSection);
		rebuildManagerTable();
	}
	
	public void deleteShape(ROIShape shape, int timePoint, int zSection) 
	throws 	ROICreationException, 
	NoSuchROIException
	{
		model.deleteShape(shape, timePoint, zSection);
		rebuildManagerTable();
	}
}
