/*
 * org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot 
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
import org.jfree.data.statistics.HistogramDataset;

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
public class HistogramPlot
{	
	/** The graph containing the plot. */
	JFreeChart  			freeChart;
		
	/** The X-Axis label. Also can,but not currenly used set the range. */
	NumberAxis  			domainAxis;
	
	/** The Y-Axis label. Also can,but not currenly used set the range. */
	NumberAxis 				rangeAxis;
		
	/** Container for the charts. */
	ChartPanel				charts;

	/** Panel returned to user containe graph. */
	JPanel					graphPanel;
	
	/** Title of the graph. */
	String					title;
	
	/** Legends of each series. */
	ArrayList<String> 		legends;
	
	/** Colours for each series. */
	ArrayList<Color>		colours;

	/** Data for each series. */ 
	ArrayList<double[]>  	data;
	
	/** The histogram dataset. */
	HistogramDataset		dataset;
	
	/** Renderer for the points in the histogram. */
	HistogramBarRenderer 			renderer;
	
		
	/** Constructor for the histogram. */
	public HistogramPlot()
	{
		init();
	}
	
	/**
	 * Constructor for the histogram. 
	 * @param title graph title. 
	 * @param newLegends The legends of each series. 
	 * @param newData The data for each series. 
	 * @param newColours The colours for each series. 
	 * @param bins The number of bins in the histogram. 
	 */
	public HistogramPlot(String title, List<String> newLegends,
			List<double[]> newData,	List<Color> newColours, int bins)
	{
		if(newLegends.size()!=newData.size() && 
				newLegends.size()!=newColours.size() || (newLegends.size()==0)
				|| bins < 1)
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		init();
		for(int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i), bins);
		setDefaultAxis();
	}
	
	/** Set the default names for the x and y axis in the plot. */
	public void setDefaultAxis()
	{
		setXAxis("X");
		setYAxis("Y");
	}
	
	/** 
	 * Set the name of the x axis to axisName. 
	 * @param axisName see above. 
	 */
	public void setXAxis(String axisName)
	{
		if(axisName==null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		domainAxis = new NumberAxis(axisName);
	}

	/** 
	 * Set the name of the y axis to axisName. 
	 * @param axisName see above. 
	 */
	public void setYAxis(String axisName)
	{
		if(axisName==null)
			throw new IllegalArgumentException("Null parameter for Axis name."); 
		rangeAxis = new NumberAxis(axisName);
	}
	
	/**
	 * Add a new Series to the histogram. 
	 * @param legend The name of the new sereis. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @param bins The number of bins associated with the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * of the just added series. 
	 */
	public int addSeries(String legend, double[] newData,Color color, int bins)
	{
		if(legend == null || newData == null || color == null || bins < 1)
			throw new IllegalArgumentException("Illegal argument in addSeries.");
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.addSeries(legend, newData, bins);
		return dataset.getSeriesCount();
	}

	/**
	 * Build the graph and return a jpanel containing it.
	 * @return see above.
	 */
	public JPanel getChart()
	{
		renderer = new HistogramBarRenderer(colours);
		XYPlot plot = new XYPlot(dataset, domainAxis,
            rangeAxis, renderer);
		freeChart = new JFreeChart(plot);
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
		data = new ArrayList<double[]>();
		colours = new ArrayList<Color>();
		dataset = new HistogramDataset();
	}
	
	
	
}


