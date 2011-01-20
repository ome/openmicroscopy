/*
 * org.openmicroscopy.shoola.util.ui.graphutils.LinePlot 
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
package org.openmicroscopy.shoola.util.ui.graphutils;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

//Third-party libraries
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

//Application-internal dependencies

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
public class LinePlot
{	
	
	/** The X-Axis label. Also can,but not currenly used set the range. */
	private NumberAxis  			domainAxis;
	
	/** The Y-Axis label. Also can,but not currenly used set the range. */
	private NumberAxis 				rangeAxis;
	
	/** Title of the graph. */
	private String					title;
	
	/** Legends of each series. */
	private List<String> 			legends;
	
	/** Colours for each series. */
	private List<Color>				colours;

	/** The dataset for the line plot. */
	private DefaultXYDataset		dataset;
	
	/** The renderer to renderer the line of the plot. */
	private StandardXYItemRenderer 	renderer;
	
	/** The x, y data of the plot. */
	private List<double[][]>   		data;
		
	/** Initialises the array lists of the plot. */
	private void init()
	{
		legends = new ArrayList<String>();
		data = new ArrayList<double[][]>();
		colours = new ArrayList<Color>();
		dataset = new DefaultXYDataset();
	}
	
	/** Creates a new instance. */
	public LinePlot()
	{
		init();
	}
	
	/**
	 * Creates a new instance. 
	 *  
	 * @param title 		The title of the graph.
	 * @param newLegends 	The legends of each series. 
	 * @param newData 		The data for each series. 
	 * @param newColours 	The colours for each series. 
	 */
	public LinePlot(String title, List<String> newLegends, 
					List<double[][]> newData, List<Color> newColours)
	{
		if (newLegends == null || newData == null || newColours == null ||
			newLegends.size() != newData.size() && 
				newLegends.size() != newColours.size())
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		this.title = title;
		init();
		for (int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i));
		setDefaultAxis();
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param title 		The title of the graph.
	 * @param newLegends 	The legends of each series. 
	 * @param newData 		The data for each series. 
	 * @param newColours 	The colours for each series. 
	 * @param minValue 		The min value of the axis.
	 * @param maxValue 		The max value of the axis.
	 */
	public LinePlot(String title, List<String> newLegends, 
			List<double[][]> newData, List<Color> newColours, 
			double minValue, double maxValue)
	{
		if (newLegends == null || newData == null || newColours == null ||
			newLegends.size() != newData.size() && 
			newLegends.size() != newColours.size())
			throw new IllegalArgumentException("Mismatch between argument " +
						"length");
		this.title = title;
		init();
		for (int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i));
		setDefaultAxis();
		rangeAxis.setRange(minValue, maxValue);
	}
	
	/** Sets the default names for the x and y axis in the plot. */
	public void setDefaultAxis()
	{
		setXAxisName("X");
		setYAxisName("Y");
	}
	
	/** 
	 * Sets the range of the x axis to axisName.
	 *  
	 * @param axisMinRange The min value to set. 
	 * @param axisMaxRange The max value to set. 
	 */
	public void setXAxisRange(double axisMinRange, double axisMaxRange)
	{
		domainAxis.setRange(axisMinRange, axisMaxRange);
		domainAxis.setAutoRange(false);
	}

	/** 
	 * Sets the name of the x axis to axisName.
	 *  
	 * @param axisName The value to set.
	 */
	public void setXAxisName(String axisName)
	{
		if (axisName==null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		domainAxis = new NumberAxis(axisName);
	}

	/** 
	 * Sets the range of the y axis to axisName. 
	 * 
	 * @param axisMinRange The min value to set. 
	 * @param axisMaxRange The max value to set.  
	 */
	public void setYAxisRange(double axisMinRange, double axisMaxRange)
	{
		rangeAxis.setRange(axisMinRange, axisMaxRange);
		rangeAxis.setAutoRange(false);
	}

	/** 
	 * Sets the name of the y axis to axisName. 
	 * 
	 * @param axisName  The value to set.
	 */
	public void setYAxisName(String axisName)
	{
		if (axisName == null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		rangeAxis = new NumberAxis(axisName);
	}
	
	/**
	 * Adds a new Series to the plot. 
	 * 
	 * @param legend The name of the new sereis. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * of the just added series. 
	 */
	public int addSeries(String legend, double[][] newData, Color color)
	{
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.addSeries(legend, newData);
		return dataset.getSeriesCount();
	}

	/**
	 * Builds the graph and returns the UI component hosting it.
	 * 
	 * @return See above.
	 */
	public JPanel getChart()
	{
		renderer = new StandardXYItemRenderer();
		for (int i = 0 ; i < colours.size(); i++)
			renderer.setSeriesPaint(i, colours.get(i));
		XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
		JFreeChart freeChart = new JFreeChart(title, plot);
		ChartPanel charts = new ChartPanel(freeChart);
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(charts, BorderLayout.CENTER);
		return graphPanel;
	}
	
}


