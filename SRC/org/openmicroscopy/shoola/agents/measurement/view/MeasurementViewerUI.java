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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTabbedPane;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.TextFigure;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
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
class MeasurementViewerUI 
	extends TopWindow
{

	/** The default color of the text. */
	private static final Color			TEXT_COLOR = Color.ORANGE;
	
	/** The default color of the measurement text. */
	private static final Color			MEASUREMENT_COLOR = 
											new Color(255, 204, 102, 0);
	
	/** The default color used to fill area. */
	private static final Color			FILL_COLOR = 
											new Color(220, 220, 220, 0);
	
	/** The default color of the text. */
	private static final double			FONT_SIZE = 10.0;
	
	/** The default width of the stroke. */
	private static final double			STROKE_WIDTH = 1.0;
	
	/** The default color of the stroke. */
	private static final Color			STROKE_COLOR = Color.WHITE;
	
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
 
    /**
     * Helper method to set the attributes of the newly created figure.
     * 
     * @param figure The figure to handle.
     */
    private void setFigureAttributes(ROIFigure figure)
    {
    	AttributeKeys.FONT_SIZE.set(figure, FONT_SIZE);
		AttributeKeys.TEXT_COLOR.set(figure, TEXT_COLOR);
		AttributeKeys.STROKE_WIDTH.set(figure, STROKE_WIDTH);
		AttributeKeys.FILL_COLOR.set(figure, FILL_COLOR);
		AttributeKeys.STROKE_COLOR.set(figure, STROKE_COLOR);
		DrawingAttributes.SHOWMEASUREMENT.set(figure, true);
		DrawingAttributes.MEASUREMENTTEXT_COLOUR.set(figure, MEASUREMENT_COLOR);
    }
    
    /**
     * Helper method to set the annotations of the newly created shape.
     * 
     * @param shape The shape to handle.
     */
    private void setShapeAnnotations(ROIShape shape)
	{
    	//Type should be a static field in the Figure object.
    	ROIFigure figure = shape.getFigure();
		String type = null;
		
		if (figure instanceof MeasureRectangleFigure) type = "Rectangle";
		if (figure instanceof MeasureEllipseFigure) type = "Ellipse";
		if (figure instanceof BezierFigure) {
			BezierFigure f = (BezierFigure) figure;
			if (f.isClosed()) type = "Polygon";
			else type = "Scribble";
		}
		if (figure instanceof MeasureLineFigure || 
			figure instanceof MeasureLineConnectionFigure)
			type = "Line";
		if (figure instanceof TextFigure) type = "Text";
		if (type != null) AnnotationKeys.FIGURETYPE.set(shape, type);
	}
    
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		toolBar = new ToolBar(controller, model);
		roiInspector = new ObjectInspector(controller, model);
		roiManager = new ObjectManager(this, model);
		roiResults = new MeasurementResults(controller, model);
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
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
	}
	
	/**
     * Creates a new instance.
     * The 
     * {@link #initialize(MeasurementViewerControl, MeasurementViewerModel) initialize} 
     * method should be called straight after to link this View 
     * to the Controller.
     * 
     * @param title The window title.
     */
	MeasurementViewerUI(String title)
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
     * Selects the passed figure.
     * 
     * @param figure The figure to select.
     */
    void selectFigure(ROIFigure figure)
    {
    	if (figure == null) return;
    	DrawingView dv = model.getDrawingView();
    	dv.clearSelection();
		dv.addToSelection(figure);
		Collection figures = dv.getSelectedFigures();
		if (figures == null) return;
		Iterator i = figures.iterator();
		List roiList = new ArrayList();
		ROIFigure f;
		ROI roi;
		while (i.hasNext()) {
			f = (ROIFigure) i.next();
			roi = model.getROI(f.getROI().getID());
			if (roi != null) roiList.add(roi);
		}
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
		while (i.hasNext()) {
			figure = (ROIFigure) i.next();
			roi = model.getROI(figure.getROI().getID());
			if (roi != null) roiList.add(roi);
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
    	if (model.getState() != MeasurementViewer.READY) return;
    	if (figure == null) return;
    	model.removeROIShape(figure.getROI().getID());
    	roiManager.removeFigure(figure);
    }
    
    /**
     * Adds the specified figure to the display.
     * 
     * @param figure The figure to add.
     */
    void addROI(ROIFigure figure)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
    	if (figure == null) return;
    	setFigureAttributes(figure);
    	ROI roi = model.createROI(figure);
    	if (roi == null) return;
    	ROIShape shape = figure.getROIShape();
    	setShapeAnnotations(shape);
    	List roiList = new ArrayList();
    	roiList.add(roi);
    	//AnnotationsKeys.ROIID.setShape(shape, roi.getID());
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
    
    /** 
     * Overridden to the set the location of the {@link MeasurementViewer}.
     * @see TopWindow#setOnScreen() 
     */
    public void setOnScreen()
    {
        if (model != null) {
        	setSize(300, 300);
            UIUtilities.incrementRelativeToAndShow(model.getRequesterBounds(), 
                    this);
        } else {
            pack();
            UIUtilities.incrementRelativeToAndShow(null, this);
        }
    }

}
