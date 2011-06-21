/*
 * org.openmicroscopy.shoola.agents.measurement.view.CalculationWizard 
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.graphutils.LinePlot;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CalculationWizard
	extends JPanel 
	implements TabPaneInterface
{
	
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.CALCWIZARD_INDEX;
	
	/** The name of the panel. */
	private static final String			NAME = "Calculation Wizard";
	
	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;


	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	CalculationWizard(MeasurementViewerControl controller, 
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
	
	
	/* UI FUNCTIONS */
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
	}

	/* CALCULATION WIZARD */
	
    /**
	 * Calculate the stats for the roi in the shapelist with id. This method
	 * will call the CalcWizard.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	public void calculateStats(long id, ArrayList<ROIShape> shapeList)
	{
		
	}

	
	/* GRAPHING FUNCTIONS */
	
	/**
	 * Draws the current data as a line plot in the graph.
	 * 
	 * @param title 			The graph title.
	 * @param data 				The data to render.
	 * @param channelNames 		The channel names.
	 * @param channelColours	The channel colours.
	 * @param axisMin			This minimum value in the Axis.
	 * @param axisMax			This maximum value in the Axis.
	 * @return See above.
	 */
	JPanel drawLineplot(String title,  List<String> channelNames, 
			List<double[][]> data, List<Color> channelColours, double axisMin, 
			double axisMax)
	{
		LinePlot plot = new LinePlot(title, channelNames, data, 
			channelColours, axisMin, axisMax);
		
		return plot.getChart();
	}
	
	
	
	
	
	
	
	
	
	
	
	/* BASE FUNCTIONS */
	
	/**
	 * Returns the name of the component.
	 * 
	 * @return See above.
	 */
	String getComponentName() { return NAME; }
	
	/**
	 * Returns the icon of the component.
	 * 
	 * @return See above.
	 */
	Icon getComponentIcon()
	{
		IconManager icons = IconManager.getInstance();
		return icons.getIcon(IconManager.GRAPHPANE);
	}

	/**
	 * Implemented as specified by the I/F {@link TabPaneInterface}
	 * @see TabPaneInterface#getIndex()
	 */
	public int getIndex() { return INDEX; }

}


