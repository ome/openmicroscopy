/*
 * org.openmicroscopy.shoola.util.ui.graphutils.ChartObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.graphutils;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;

/** 
 * Top class uses to display chart.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class ChartObject 
{

	/** The default name of the X-axis. */
	public static final String X_AXIS = "X";
	
	/** The default name of the Y-axis. */
	public static final String Y_AXIS = "Y";
	
	/** Indicates to save the chart as <code>PNG</code>. */
	public static final int SAVE_AS_PNG = 0;
	
	/** Indicates to save the chart as <code>JPEG</code>. */
	public static final int SAVE_AS_JPEG = 1;
	
	/** 
	 * The width when saving the chart as <code>JPEG</code> or 
	 * <code>PNG</code>. 
	 */
	static final int WIDTH = 500;
	
	/** 
	 * The width when saving the chart as <code>JPEG</code> or 
	 * <code>PNG</code>. 
	 */
	static final int HEIGHT = 300;
	
	/** The chart. */
	protected JFreeChart chart;
	
	/** Title of the graph. */
	protected String	title;
	
	/** Legends of each series. */
	protected List<String> legends;
	
	/** Colors for each series. */
	protected List<Color>  colours;
	
	/** The Y-Axis label. Also can,but not currently used set the range. */
	protected NumberAxis 	rangeAxis;
	
	/** The X-Axis label. Also can, but not currently used set the range. */
	protected NumberAxis  	domainAxis;
	
	/** Initializes all the lists*/
	protected void init()
	{
		legends = new ArrayList<String>();
		colours = new ArrayList<Color>();
	}
	
	public ChartObject(String title)
	{
		this.title = title;
		init();
	}
	
	/** Sets the default names for the x and y axis in the plot. */
	public void setDefaultAxis()
	{
		setXAxisName(X_AXIS);
		setYAxisName(Y_AXIS);
	}

	/** 
	 * Sets the name of the y axis to axisName. 
	 * 
	 * @param axisName The name to set. 
	 */
	public void setYAxisName(String axisName)
	{
		if (axisName == null || axisName.trim().length() == 0) 
			axisName = Y_AXIS; 
		rangeAxis = new NumberAxis(axisName);
	}
	
	/** 
	 * Sets the range of the y axis to axisName. 
	 * 
	 * @param axisMinRange The minimum value to set. 
	 * @param axisMaxRange The maximum value to set. 
	 */
	public void setYAxisRange(double axisMinRange, double axisMaxRange)
	{
		if (rangeAxis == null) setYAxisName(Y_AXIS);
		rangeAxis.setRange(axisMinRange, axisMaxRange);
		rangeAxis.setAutoRange(false);
	}
	
	/** 
	 * Sets the range of the x axis to axisName. 
	 * 
	 * @param axisMinRange The minimum value to set. 
	 * @param axisMaxRange The maximum value to set. 
	 */
	public void setXAxisRange(double axisMinRange, double axisMaxRange)
	{
		if (domainAxis == null) setXAxisName(X_AXIS);
		domainAxis.setRange(axisMinRange, axisMaxRange);
		domainAxis.setAutoRange(false);
	}

	/** 
	 * Sets the name of the x axis to axisName. 
	 * 
	 * @param axisName The name to set.
	 */
	public void setXAxisName(String axisName)
	{
		if (axisName == null || axisName.trim().length() == 0)
			axisName = X_AXIS;
		domainAxis = new NumberAxis(axisName);
	}
	
	
	/**
	 * Saves the chart as a <code>JPEG</code> or <code>PNG</code>.
	 * 
	 * @param file 		 The file where to save the chart.
	 * @param savingType Indicates either to save as a 
	 * 					 <code>JPEG</code> or <code>PNG</code>
	 */
	public void saveAs(File file, int savingType)
		throws Exception
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified");
		try {
			switch (savingType) {
				case SAVE_AS_PNG:
					ChartUtilities.saveChartAsPNG(file, chart, WIDTH, HEIGHT);
					break;
				case SAVE_AS_JPEG:
					ChartUtilities.saveChartAsJPEG(file, chart, WIDTH, HEIGHT);
			}
		} catch (Exception e) {
			throw new Exception("Unable to save the file.", e);
		}
	}
	
	/**
	 * Builds the graph and returns the UI component hosting it.
	 * 
	 * @return See above.
	 */
	public JPanel getChart()
	{
		createChart();
		JPanel graphPanel = new JPanel();
		if (chart == null) return graphPanel;
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
		return graphPanel;
	}
	
	/** Creates the chart. */
	abstract void createChart();
	
}
