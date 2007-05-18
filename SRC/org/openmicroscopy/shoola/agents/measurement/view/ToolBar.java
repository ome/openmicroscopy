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
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

//Third-party libraries
import org.jhotdraw.draw.BezierTool;
import org.jhotdraw.draw.ConnectionTool;
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.ToolBarButtonFactory;
import org.jhotdraw.util.ResourceBundleUtil;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.RectAnnotationFigure;
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
	
	/** The label key to create a <code>rectangle</code>. */
	private static final String			RECTANGLE = "createRectangle";
	
	/** The label key to create an <code>ellipse</code>. */
	private static final String			ELLIPSE = "createEllipse";
	
	/** The label key to create a <code>line</code>. */
	private static final String			LINE = "createLine";
	
	/** The label key to create a <code>scribble</code>. */
	private static final String			SCRIBBLE = "createScribble";
	
	/** The label key to create a <code>polygon</code>. */
	private static final String			POLYGON = "createPolygon";
	
	/** The label key to create a <code>text</code>. */
	private static final String			TEXT = "createText";
	
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
				new CreationTool(new RectAnnotationFigure()), RECTANGLE, 
				labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new EllipseAnnotationFigure()), ELLIPSE, 
				labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new LineAnnotationFigure()), LINE, labels);
	    ToolBarButtonFactory.addToolTo(toolBar, editor, 
	    		new ConnectionTool(new LineConnectionAnnotationFigure()), 
	    		"createLineConnection", labels);
		  ToolBarButtonFactory.addToolTo(toolBar, editor, 
				  new BezierTool(new BezierAnnotationFigure()), SCRIBBLE, 
				  	labels);
	      ToolBarButtonFactory.addToolTo(toolBar, editor, 
	    		  new BezierTool(new BezierAnnotationFigure(true)), POLYGON, 
	    		  labels);
		ToolBarButtonFactory.addToolTo(toolBar, editor, 
				new CreationTool(new MeasureTextFigure()), TEXT, 
				labels);
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
