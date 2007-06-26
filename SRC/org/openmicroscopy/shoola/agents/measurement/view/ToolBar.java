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
import java.awt.Button;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierTool;
import org.jhotdraw.draw.ConnectionTool;
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.ToolBarButtonFactory;
import org.jhotdraw.util.ResourceBundleUtil;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.PointCreationTool;
import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes;
import org.openmicroscopy.shoola.util.roi.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.PointAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.figures.RectAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
{

	private final static HashMap<AttributeKey, Object>	defaultAttributes;
	static
	{
		defaultAttributes=new HashMap<AttributeKey, Object>();
		defaultAttributes.put(AttributeKeys.FILL_COLOR,
			IOConstants.DEFAULT_FILL_COLOUR);
		defaultAttributes.put(AttributeKeys.STROKE_COLOR,
			IOConstants.DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(AttributeKeys.TEXT_COLOR,
			IOConstants.DEFAULT_TEXT_COLOUR);
		defaultAttributes.put(AttributeKeys.FONT_SIZE, new Double(10));
		defaultAttributes.put(AttributeKeys.FONT_BOLD, false);
		defaultAttributes.put(AttributeKeys.STROKE_WIDTH, new Double(1.0));
		defaultAttributes.put(AttributeKeys.TEXT, "Text");
		defaultAttributes.put(DrawingAttributes.MEASUREMENTTEXT_COLOUR,
			IOConstants.DEFAULT_MEASUREMENT_TEXT_COLOUR);
		defaultAttributes.put(DrawingAttributes.SHOWMEASUREMENT, new Boolean(
			false));
		defaultAttributes.put(DrawingAttributes.SHOWTEXT, new Boolean(false));
	}
	
	/** The default string added to the type of Figure to create. */
	private static final String			CREATE_KEY = "create";
	
	/** The base name used for Labels. */
	private static final String			BASE_NAME = "org.jhotdraw.draw.Labels";
	
	/** Size of the horizontal box. */
    private static final Dimension 		HGLUE = new Dimension(5, 5);
    
	/** Tool bar hosting the control defined by <code>JHotDraw</code>. */
	private JToolBar					toolBar;
	
	/** Reference to the Control. */
    private MeasurementViewerControl	controller;
	
    /** Reference to the Model. */
    private MeasurementViewerModel		model;
    
    /** Initializes the component composing the display. */
	private void initComponents()
	{
		ButtonGroup group = new ButtonGroup();
		ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle(BASE_NAME);
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("toolButtonGroup", group);
		DrawingEditor editor = model.getDrawingEditor();
		ToolBarButtonFactory.addSelectionToolTo(toolBar, editor);
		toolBar.add(new JSeparator());
		toolBar.add(Box.createRigidArea(HGLUE));
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new RectAnnotationFigure()), 
				CREATE_KEY+ROIFigure.RECTANGLE_TYPE, 
				labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new EllipseAnnotationFigure()), 
				CREATE_KEY+ROIFigure.ELLIPSE_TYPE, 
				labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new PointCreationTool(new PointAnnotationFigure()), 
				CREATE_KEY+ROIFigure.ELLIPSE_TYPE, 
				labels);
		Component component = toolBar.getComponent(toolBar.getComponentCount()-1);
		if(component instanceof JToggleButton)
		{
			JToggleButton button = (JToggleButton)component;
			button.setIcon(IconManager.getInstance().getIcon(IconManager.POINTICON));
		}
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new LineAnnotationFigure()), 
					CREATE_KEY+ROIFigure.LINE_TYPE, labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
	    		new ConnectionTool(new LineConnectionAnnotationFigure(), 
	    			defaultAttributes), 
	    			CREATE_KEY+ROIFigure.LINE_CONNECTION_TYPE, labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				  new BezierTool(new BezierAnnotationFigure()), 
				  CREATE_KEY+ROIFigure.SCRIBBLE_TYPE, labels);
	    ToolBarButtonFactory.addToolTo(toolBar, editor, 
	    		  new BezierTool(new BezierAnnotationFigure(true)), 
	    		  CREATE_KEY+ROIFigure.POLYGON_TYPE, labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new MeasureTextFigure()), 
				CREATE_KEY+ROIFigure.TEXT_TYPE, labels);
		
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
	ToolBar(MeasurementViewerControl controller,
			MeasurementViewerModel model)
	{
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		this.controller = controller;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
}
