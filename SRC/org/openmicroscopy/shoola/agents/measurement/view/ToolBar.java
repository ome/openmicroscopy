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
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;
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
	private final static HashMap<AttributeKey, Object>	defaultConnecionAttributes;
	
	static
	{
		defaultConnecionAttributes=new HashMap<AttributeKey, Object>();
		defaultConnecionAttributes.put(MeasurementAttributes.FILL_COLOR,
						IOConstants.DEFAULT_FILL_COLOUR);
		defaultConnecionAttributes.put(MeasurementAttributes.STROKE_COLOR,
						IOConstants.DEFAULT_STROKE_COLOUR);
		defaultConnecionAttributes.put(MeasurementAttributes.TEXT_COLOR,
						IOConstants.DEFAULT_TEXT_COLOUR);
		defaultConnecionAttributes.put(MeasurementAttributes.FONT_SIZE, new Double(10));
		defaultConnecionAttributes.put(MeasurementAttributes.FONT_BOLD, false);
		defaultConnecionAttributes.put(MeasurementAttributes.STROKE_WIDTH, new Double(1.0));
		defaultConnecionAttributes.put(MeasurementAttributes.TEXT, "Text");
		defaultConnecionAttributes.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
			IOConstants.DEFAULT_MEASUREMENT_TEXT_COLOUR);
		defaultConnecionAttributes.put(MeasurementAttributes.SHOWMEASUREMENT, 
							Boolean.FALSE);
		defaultConnecionAttributes.put(DrawingAttributes.SHOWTEXT, Boolean.FALSE);
	}
	
	/** The properties figure for a line connection object. */
	private FigureProperties 			lineConnectionProperties;
		
	/** The default string added to the type of Figure to create. */
	private static final String			CREATE_KEY = "create";
	
	/** Size of the horizontal box. */
    private static final Dimension 		HGLUE = new Dimension(5, 5);
    
	/** Tool bar hosting the control defined by <code>JHotDraw</code>. */
	private JToolBar					toolBar;

	/** Reference to the Control. */
    private MeasurementViewerControl	controller;

	/** Reference to the Control. */
    private MeasurementViewerUI			view;
    
    /** Reference to the Model. */
    private MeasurementViewerModel		model;
    
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
    
    /** The connetion creation tool. */
    private DrawingConnectionTool		connectionTool;
    
    /** Initializes the component composing the display. */
	private void initComponents()
	{
		lineConnectionProperties = new FigureProperties(defaultConnecionAttributes);
		ellipseTool = new DrawingObjectCreationTool(new MeasureEllipseFigure());
		rectTool = new DrawingObjectCreationTool(new MeasureRectangleFigure());
		textTool = new DrawingObjectCreationTool(new MeasureTextFigure());
		lineTool = new DrawingObjectCreationTool(new MeasureLineFigure());
		connectionTool = new DrawingConnectionTool(
						new MeasureLineConnectionFigure(), 
						lineConnectionProperties.getProperties());
		pointTool = new DrawingPointCreationTool(new MeasurePointFigure());
	    polygonTool = new DrawingBezierTool(new MeasureBezierFigure(true));
	    polylineTool = new DrawingBezierTool(new MeasureBezierFigure(false));
	    
	    Component component;
	    
		toolBar = DrawingToolBarButtonFactory.createDefaultBar();
		DrawingEditor editor = model.getDrawingEditor();
		DrawingToolBarButtonFactory.addSelectionToolTo(toolBar, editor);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}

		toolBar.add(new JSeparator());
		toolBar.add(Box.createRigidArea(HGLUE));
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, rectTool, 
				CREATE_KEY+FigureUtil.RECTANGLE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, ellipseTool, 
				CREATE_KEY+FigureUtil.ELLIPSE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, pointTool, 
				CREATE_KEY+FigureUtil.ELLIPSE_TYPE);
		component = toolBar.getComponent(
							toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
			IconManager icons = IconManager.getInstance();
			button.setIcon(icons.getIcon(IconManager.POINTICON));
			button.setToolTipText(FigureUtil.POINT_TYPE);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, lineTool, 
					CREATE_KEY+FigureUtil.LINE_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, connectionTool, 
	    			CREATE_KEY+FigureUtil.LINE_CONNECTION_TYPE);
		 component = toolBar.getComponent(
			toolBar.getComponentCount()-1);
		 if (component instanceof JToggleButton)
		 {
			 JToggleButton button = (JToggleButton) component;
			 IconManager icons = IconManager.getInstance();
			 button.setToolTipText(FigureUtil.LINE_CONNECTION_TYPE);
		 }
		 DrawingToolBarButtonFactory.addToolTo(toolBar, editor, polylineTool, 
				  CREATE_KEY+FigureUtil.SCRIBBLE_TYPE);
		 component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, polygonTool, 
	    	  CREATE_KEY+FigureUtil.POLYGON_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}
		DrawingToolBarButtonFactory.addToolTo(toolBar, editor, textTool, 
			CREATE_KEY+FigureUtil.TEXT_TYPE);
		component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if (component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton) component;
			button.addMouseListener(this);
		}

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
        button = new JButton(controller.getAction(
        	MeasurementViewerControl.ROI_ASSISTANT));
        UIUtilities.unifiedButtonLookAndFeel(button);
    	bar.add(button);
    	bar.add(new JSeparator());
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(buildControlsBar());
		p.add(Box.createHorizontalStrut(5));
		p.add(toolBar);
		setLayout(new FlowLayout(FlowLayout.LEFT));
	    add(p);
	}
	
	/**
	 * Creates a new instance.
	 * 
		 * @param controller	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 * @param model			Reference to the View. Mustn't be <code>null</code>.
	 */
	ToolBar(MeasurementViewerUI view, MeasurementViewerControl controller,
			MeasurementViewerModel model)
	{
		if (view == null) 
			throw new IllegalArgumentException("No view.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		this.view = view;
		this.controller = controller;
		this.model = model;
		initComponents();
		buildGUI();
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
	    polygonTool.setResetToSelect(option);
	    polylineTool.setResetToSelect(option); 
	}
	/**
	 * Return true if the right button was clicked or left button was clicked with
	 * control held down.
	 * @param e mouse event.
	 * @return see above.
	 */
	protected boolean rightClick(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON3 || 
				(e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()))
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		if(rightClick(e))
		{
			view.createSingleFigure(false);
			JToggleButton button = (JToggleButton)e.getSource();
			button.setSelected(true);
		}
		else
			view.createSingleFigure(true);
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
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

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
}
