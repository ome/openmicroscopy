/*
 * org.openmicroscopy.shoola.agents.measurement.view.ToolBar 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.actions.DrawingAction;
import org.openmicroscopy.shoola.agents.measurement.util.workflow.CreateWorkflowDialog;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.FigureProperties;
import org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingBezierTool;
import org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingConnectionTool;
import org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingObjectCreationTool;
import org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingPointCreationTool;
import org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingToolBarButtonFactory;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

import pojos.ShapeSettingsData;
import pojos.WorkflowData;

/** 
 * UI component acting as toolbar. Used to create Region of Interest.
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
class ToolBar 
	extends JPanel 
	implements MouseListener
{

	/** The defaults attributes of the line connection figure. */
	private final static Map<AttributeKey, Object>	
	defaultConnectionAttributes;
	
	/** Add the polyline tool to the toolbar. */
	private final static boolean addPolyline = false;

	/** Add the polygon tool to the toolbar. */
	private final static boolean addPolygon = true;
	
	/** Add the connection tool to the toolbar. */
	private final static boolean addConnection = false;
	
	static
	{
		defaultConnectionAttributes = new HashMap<AttributeKey, Object>();
		defaultConnectionAttributes.put(MeasurementAttributes.FILL_COLOR,
				ShapeSettingsData.DEFAULT_FILL_COLOUR);
		defaultConnectionAttributes.put(MeasurementAttributes.STROKE_COLOR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultConnectionAttributes.put(MeasurementAttributes.TEXT_COLOR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultConnectionAttributes.put(MeasurementAttributes.FONT_SIZE, 
				new Double(ShapeSettingsData.DEFAULT_FONT_SIZE));
		defaultConnectionAttributes.put(MeasurementAttributes.FONT_BOLD, 
				Boolean.valueOf(false));
		defaultConnectionAttributes.put(MeasurementAttributes.STROKE_WIDTH, 
				ShapeSettingsData.DEFAULT_STROKE_WIDTH);
		defaultConnectionAttributes.put(MeasurementAttributes.TEXT, 
				ROIFigure.DEFAULT_TEXT);
		defaultConnectionAttributes.put(
				MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultConnectionAttributes.put(MeasurementAttributes.SHOWMEASUREMENT, 
							Boolean.valueOf(false));
		defaultConnectionAttributes.put(DrawingAttributes.SHOWTEXT, 
				Boolean.valueOf(false));
	}

	/** The default string added to the type of Figure to create. */
	private static final String			CREATE_KEY = "create";
	
	/** Size of the horizontal box. */
    private static final Dimension 		HGLUE = new Dimension(5, 5);
    
    /** The properties figure for a line connection object. */
	private FigureProperties 			lineConnectionProperties;
	
	/** Tool bar hosting the control defined by <code>JHotDraw</code>. */
	private JToolBar					toolBar;

	/** Reference to the Control. */
    private MeasurementViewerControl	controller;

	/** Reference to the Control. */
    private MeasurementViewerUI			view;
    
    /** Reference to the Model. */
    private MeasurementViewerModel		model;

    /** Reference to the Component. */
    private MeasurementViewer			measurementcomponent;
    
    /** The ellipse creation tool. */
    private DrawingObjectCreationTool	ellipseTool;

    /** The rectangle creation tool. */
    private DrawingObjectCreationTool	rectTool;
    
    /** The line creation tool. */
    private DrawingObjectCreationTool	lineTool;
    
    /** The text creation tool. */
    private DrawingObjectCreationTool	textTool;

    /** The point creation tool. */
    private DrawingPointCreationTool	pointTool;
    
    /** The polygon creation tool. */
    private DrawingBezierTool 			polygonTool;
    
    /** The polyline creation tool. */
    private DrawingBezierTool 			polylineTool;
    
    /** The connection creation tool. */
    private DrawingConnectionTool		connectionTool;
    
    /** The button to bring up the assistant. */
    private JButton						assistantButton;
    
    /** The component to show/hide the text. */
    private JCheckBox					showTextButton;
    
    /** The workflow panel controls what workflow elements will be added to the 
     * created figure. 
     */
    private WorkflowPanel				workflowPanel;
    
    /** Dialog used to create the workflow. */
    private CreateWorkflowDialog 		createWorkflowDialog;
   
    /**
     * Sets the property of the toggle button.
     * 
     * @param button The button to handle.
     */
    private void setUpToggleButton(JToggleButton button)
    {
    	button.setAction(new DrawingAction(measurementcomponent, button));	
		button.addMouseListener(this);
    }
    
    /** Initializes the component composing the display. */
	private void initComponents()
	{
		showTextButton = new JCheckBox("Show Text");
		
		lineConnectionProperties = new FigureProperties(
				defaultConnectionAttributes);
		ellipseTool = new DrawingObjectCreationTool(new MeasureEllipseFigure(
				false, true));
		rectTool = new DrawingObjectCreationTool(new MeasureRectangleFigure(
				false, true));
		textTool = new DrawingObjectCreationTool(
				new MeasureTextFigure(false, true));
		lineTool = new DrawingObjectCreationTool(
				new MeasureLineFigure(false, true));
		connectionTool = new DrawingConnectionTool(
						new MeasureLineConnectionFigure(), 
						lineConnectionProperties.getProperties());
		pointTool = new DrawingPointCreationTool(
				new MeasurePointFigure(false, true));
	    polygonTool = new DrawingBezierTool(
	    		new MeasureBezierFigure(true, false, true));
	    polylineTool = new DrawingBezierTool(
	    		new MeasureBezierFigure(false, false, true));
	    
	    Component component;
	    
		toolBar = DrawingToolBarButtonFactory.createDefaultBar();
		DrawingEditor editor = model.getDrawingEditor();
		DrawingToolBarButtonFactory.addSelectionToolTo(toolBar, editor,
				true);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
			setUpToggleButton((JToggleButton) component);

		toolBar.add(new JSeparator());
		toolBar.add(Box.createRigidArea(HGLUE));
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, rectTool, 
				CREATE_KEY+FigureUtil.RECTANGLE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
			setUpToggleButton((JToggleButton) component);
		
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, ellipseTool, 
				CREATE_KEY+FigureUtil.ELLIPSE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
			setUpToggleButton((JToggleButton) component);
		
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, pointTool, 
				CREATE_KEY+FigureUtil.ELLIPSE_TYPE);
		component = toolBar.getComponent(
							toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			IconManager icons = IconManager.getInstance();
			button.setIcon(icons.getIcon(IconManager.POINTICON_22));
			button.setToolTipText(FigureUtil.POINT_TYPE);
			setUpToggleButton(button);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, lineTool, 
					CREATE_KEY+FigureUtil.LINE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
			setUpToggleButton((JToggleButton) component);
		
		if (addPolyline)
		{
			DrawingToolBarButtonFactory.addToolTo(toolBar, editor, polylineTool, 
				  CREATE_KEY+FigureUtil.SCRIBBLE_TYPE);
			component = toolBar.getComponent(toolBar.getComponentCount()-1);
			if (component instanceof JToggleButton)
				setUpToggleButton((JToggleButton) component);
		}
		if (addPolygon)
		{
			DrawingToolBarButtonFactory.addToolTo(toolBar, editor, polygonTool, 
		    	  CREATE_KEY+FigureUtil.POLYGON_TYPE);
			component = toolBar.getComponent(toolBar.getComponentCount()-1);
			if (component instanceof JToggleButton)
				setUpToggleButton((JToggleButton) component);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, textTool, 
			CREATE_KEY+FigureUtil.TEXT_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
			setUpToggleButton((JToggleButton) component);
		if (addConnection)
		{
			DrawingToolBarButtonFactory.addToolTo(toolBar, editor, 
					connectionTool, 
    			CREATE_KEY+FigureUtil.LINE_CONNECTION_TYPE);
			component = toolBar.getComponent(
					toolBar.getComponentCount()-1);
			if (component instanceof JToggleButton)
			{
				JToggleButton button = (JToggleButton) component;
				button.setToolTipText("Connector");
				setUpToggleButton(button);	
			}
		}
		workflowPanel = new WorkflowPanel(view, model, controller);
	}
	
	/**
	 * Builds the tool bar hosting the controls.
	 * 
	 * @return See above.
	 */
	private JToolBar buildControlsBar()
	{
		JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        JButton button = new JButton(
				controller.getAction(MeasurementViewerControl.SAVE));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        button = new JButton(controller.getAction(
				MeasurementViewerControl.LOAD));
        UIUtilities.unifiedButtonLookAndFeel(button);
        bar.add(button);
        assistantButton = new JButton(controller.getAction(
        	MeasurementViewerControl.ROI_ASSISTANT));
        UIUtilities.unifiedButtonLookAndFeel(assistantButton);
    	bar.add(assistantButton);
    	button = new JButton(controller.getAction(
    			MeasurementViewerControl.DELETE));
    	UIUtilities.unifiedButtonLookAndFeel(button);
    	bar.add(button);
    	bar.add(new JSeparator());
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel j = new JPanel();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(buildControlsBar());
		p.add(Box.createHorizontalStrut(5));
		p.add(toolBar);
		p.add(Box.createHorizontalStrut(5));
		p.add(showTextButton);
		setLayout(new FlowLayout(FlowLayout.LEFT));
	    j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
	    j.add(p);
	    add(j);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param component		Reference to the component. 
	 * 						Mustn't be <code>null</code>.
	 * @param view			Reference to the view. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the controller. 
	 * 						Mustn't be <code>null</code>.
	 * @param model			Reference to the View. Mustn't be <code>null</code>.
	 */
	ToolBar(MeasurementViewer component, MeasurementViewerUI view, 
			MeasurementViewerControl controller,
			MeasurementViewerModel model)
	{
		if (component == null) 
			throw new IllegalArgumentException("No component.");
		if (view == null) 
			throw new IllegalArgumentException("No view.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		measurementcomponent = component;
		this.view = view;
		this.controller = controller;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Return true if the right button was clicked or left button was clicked 
	 * with control held down.
	 * @param e mouse event.
	 * @return see above.
	 */
	protected boolean rightClick(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3 || 
				(e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()))
			return true;
		return false;
	}
	
	/**
	 * Creates a single figure and moves the tool in the menu back to the 
	 * selection tool, if the passed parameter is <code>true</code>. 
	 * 
	 * @param option see above.
	 */
	void createSingleFigure(boolean option)
	{
		ellipseTool.setResetToSelect(option);
		rectTool.setResetToSelect(option);
		textTool.setResetToSelect(option); 
		lineTool.setResetToSelect(option); 
		// TODO : connectionTool.setResetToSelect(option);
		pointTool.setResetToSelect(option);
	    if (addPolygon)
	    	polygonTool.setResetToSelect(option);
	    if (addPolyline)
	    	polylineTool.setResetToSelect(option); 
	}
	
	/** Sets the value in the tool bar.*/
    void refreshToolBar()
    {
    	Collection<ROIFigure> figures = model.getAllFigures();
		if (figures.size() > 0) {
			Iterator<ROIFigure> i = figures.iterator();
			ROIFigure figure;
			boolean b = false;
			Boolean value;
			while (i.hasNext()) {
				figure = i.next();
				if (!figure.isReadOnly()) {
					value = MeasurementAttributes.SHOWTEXT.get(figure);
					if (value != null && value.booleanValue()) {
						b = value.booleanValue();
						break;
					}
				}
			}
			showTextButton.setSelected(b);
		}
		showTextButton.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				view.showText(showTextButton.isSelected());
				
			}
		});
    }
    
	/** Invokes when figures are selected. */
	void onFigureSelected()
	{
		int size = view.getDrawingView().getSelectedFigures().size();
		assistantButton.getAction().setEnabled(size == 1);
	}
	
	/**
	 * Updates the workflow in the toolbar UI.
	 */
	void updateWorkflow()
	{
		workflowPanel.updateWorkflow();
	}
	
	/**
	 * Returns the workflow panel.
	 * 
	 * @return See above.
	 */
	WorkflowPanel getWorkflowPanel()
	{
		return workflowPanel;
	}

	/** Shows the create workflow menu. */
	void createWorkflow() 
	{ 
		createWorkflowDialog = 
			new CreateWorkflowDialog(model.getWorkflowDataList());
		List<WorkflowData> newWorkflows = createWorkflowDialog.show();
		if (newWorkflows != null)
		{
			model.resetWorkflows(newWorkflows);
			model.saveWorkflowToServer(false);
			model.retrieveWorkflowsFromServer();
		}
	}

	/** Adds the workflows. */
	void addedWorkflow() { workflowPanel.addedWorkflow(); }
	
	/**
	 * Sets the selected flag of the source of the event to 
	 * <code>true</code> when a right click event occurs.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		if (rightClick(e))
		{
			createSingleFigure(false);
			JToggleButton button = (JToggleButton)e.getSource();
			button.setSelected(true);
		}
		else
			createSingleFigure(view.isCreateSingleFigure());
	}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {}

}
