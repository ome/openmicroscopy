/*
 * org.openmicroscopy.shoola.util.ui.graphutils.ScatterPlot 
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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

//Third-party libraries
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
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
public class ScatterPlot
{	
	/**
	 * Default size for a shape to be created if no shape params supplied in
	 * constructor. 
	 */
	private  final static int SHAPESIZE = 3; 

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

	/** The dataset for the line plot. */
	DefaultXYDataset		dataset;
	
	/** The renderer to renderer the line of the plot. */
	PointRenderer 			renderer;
	
	/** The x, y data of the plot. */
	ArrayList<double[][]>   data;
	
	/** The shapes associated with each datapoint in the plot. */
	ArrayList<Shape> 		shapes;
	
	/**
	 * Constructor for the scatterplot. */
	public ScatterPlot()
	{
		init();
	}
	
	/**
	 * Constructor for the scatterplot. 
	 * @param title graph title. 
	 * @param newLegends The legends of each series. 
	 * @param newData The data for each series. 
	 * @param newColours The colours for each series. 
	 * @param newShapes  The shapes for each series.. 
	 */
	public ScatterPlot(String title, List<String> newLegends, 
			List<double[][]> newData, List<Color> newColours, 
			List<Shape> newShapes)
	{
		if(newLegends.size()!=newData.size() && 
				newLegends.size()!=newColours.size() && 
				newLegends.size()!=newShapes.size())
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		init();
		for(int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i), 
															newShapes.get(i));
		setDefaultAxis();
	}
	
	/**
	 * Constructor for the scatterplot. 
	 * @param title graph title. 
	 * @param newLegends The legends of each series. 
	 * @param newData The data for each series. 
	 * @param newColours The colours for each series. 
	 */
	public ScatterPlot(String title, List<String> newLegends, 
							List<double[][]> newData, List<Color> newColours)
	{
		if(newLegends.size()!=newData.size() && 
				newLegends.size()!=newColours.size())
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		init();
		for(int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i));
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
		domainAxis = new NumberAxis(axisName);
	}

	/** 
	 * Set the range of the x axis to axisName. 
	 * @param axisMinRange see above. 
	 * @param axisMaxRange see above. 
	 */
	public void setXAxisRange(double axisMinRange, double axisMaxRange)
	{
		domainAxis.setRange(axisMinRange, axisMaxRange);
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
	}

	/**
	 * Add a new Series to the plot. 
	 * @param legend The name of the new sereis. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @param shape The shape of the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * of the just added series. 
	 */
	public int addSeries(String legend, double[][] newData, Color color, 
			Shape shape)
	{
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		shapes.add(shape);
		dataset.addSeries(legend, newData);
		return dataset.getSeriesCount();
	}

	/**
	 * Add a new Series to the plot. 
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
		shapes.add(createDefaultShape());
		dataset.addSeries(legend, newData);
		return dataset.getSeriesCount();
	}

	/** 
	 * Create the default shape if no shape param was supplied in constuctor. 
	 * @return ellipse shape. 
	 */
	private Ellipse2D createDefaultShape()
	{
		return new Ellipse2D.Double(-SHAPESIZE, -SHAPESIZE,
			SHAPESIZE, SHAPESIZE);
	}
	
	/**
	 * Build the graph and return a jpanel containing it.
	 * @return see above.
	 */
	public JPanel getChart()
	{
		renderer = new PointRenderer(colours, shapes);
		XYPlot plot = new XYPlot(dataset, domainAxis,
            rangeAxis, renderer);
		freeChart = new JFreeChart(plot);
		charts = new ChartPanel(freeChart);
		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(charts, BorderLayout.CENTER);
		return graphPanel;
	}
	
	/** Initialise arraylists. */
	private void init()
	{
		legends = new ArrayList<String>();
		data = new ArrayList<double[][]>();
		colours = new ArrayList<Color>();
		shapes = new ArrayList<Shape>();
		dataset = new DefaultXYDataset();
	}
	
	
	
	
}


