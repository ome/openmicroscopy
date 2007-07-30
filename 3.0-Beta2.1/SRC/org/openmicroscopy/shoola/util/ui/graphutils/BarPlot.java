/*
 * org.openmicroscopy.shoola.util.ui.graphutils.BarPlot 
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

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
public class BarPlot
{	

	/** Default series. WTF. */
	private final static String DEFAULTSERIES = "default";

	/** The graph containing the plot. */
	JFreeChart  			freeChart;
		
	/** The X-Axis label. Also can,but not currenly used set the range. */
	CategoryAxis  			categoryAxis;
	
	/** The Y-Axis label. Also can,but not currenly used set the range. */
	NumberAxis 				rangeAxis;
		
	/** Container for the charts. */
	ChartPanel				charts;

	/** Panel returned to user contained graph. */
	JPanel					graphPanel;
	
	/** Title of the graph. */
	String					title;
	
	/** Legends of each series. */
	ArrayList<String> 		legends;
	
	/** Colours for each series. */
	ArrayList<Color>		colours;

	/** Data for each series. */ 
	ArrayList<Double>	  	data;
	
	/** The BarPlot dataset. */
	DefaultCategoryDataset	dataset;
	
	/** Renderer for the points in the histogram. */
	CustomBarRenderer 		renderer;
	
		
	/** Constructor for the histogram. */
	public BarPlot()
	{
		init();
	}
	
	/**
	 * Constructor for the histogram. 
	 * @param title graph title. 
	 * @param newLegends The legends of each series. 
	 * @param newData The data for each series. 
	 * @param newColours The colours for each series. 
	 */
	public BarPlot(String title, List<String> newLegends,
			List<Double> newData,	List<Color> newColours)
	{
		if(newLegends.size()!=newData.size() && 
				newLegends.size()!=newColours.size() || (newLegends.size()==0))
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		init();
		for(int i = 0 ; i < newLegends.size(); i++)
			addValue(newLegends.get(i), newData.get(i), newColours.get(i));
		setDefaultAxis();
	}
	
	/** Set the default names for the x and y axis in the plot. */
	public void setDefaultAxis()
	{
		setXAxisName("X");
		setYAxisName("Y");
	}
	
	/** 
	 * Set the name of the x axis to axisName. 
	 * @param axisName see above. 
	 */
	public void setXAxisName(String axisName)
	{
		if(axisName==null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		categoryAxis = new CategoryAxis(axisName);
	}

	/** 
	 * Set the name of the y axis to axisName. 
	 * @param axisName see above. 
	 */
	public void setYAxisName(String axisName)
	{
		if(axisName==null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		rangeAxis = new NumberAxis(axisName);
	}
	
	/** 
	 * Set the range of the y axis to axisName. 
	 * @param axisMinRange see above. 
	 * @param axisMaxRange see above. 
	 */
	public void setYAxisRange(double axisMinRange, double axisMaxRange)
	{
		rangeAxis.setRange(axisMinRange, axisMaxRange);
		rangeAxis.setAutoRange(false);
	}

	/**
	 * Add a new Series to the bar plot. 
	 * @param legend The name of the new sereis. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * of the just added series. 
	 */
	public int addValue(String legend, double newData, Color color)
	{
		if(legend == null || color == null)
			throw new IllegalArgumentException("Illegal argument in addSeries.");
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.addValue(newData, DEFAULTSERIES, legend);
		return dataset.getColumnCount();
	}

	/**
	 * Build the graph and return a jpanel containing it.
	 * @return see above.
	 */
	public JPanel getChart()
	{
		renderer = new CustomBarRenderer(colours);
		CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis, 
			                    renderer);
		
		freeChart = new JFreeChart(title, plot);
		charts = new ChartPanel(freeChart);
		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(charts, BorderLayout.CENTER);
		return graphPanel;
	}
	
	/** Initialise all the arrays and datasets. */
	private void init()
	{
		legends = new ArrayList<String>();
		data = new ArrayList<Double>();
		colours = new ArrayList<Color>();
		dataset = new DefaultCategoryDataset();
	}
	
}


