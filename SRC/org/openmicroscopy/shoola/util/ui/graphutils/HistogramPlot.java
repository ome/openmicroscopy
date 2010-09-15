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
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

//Application-internal dependencies


/** 
 * Displays a histogram using <code>JfreeChart</code>.
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
	extends ChartObject
{	

	/** Data for each series. */ 
	private List<double[]>  		data;
	
	/** The histogram dataset. */
	private HistogramDataset		dataset;
	
	/** Initializes. */
	private void initialize()
	{
		data = new ArrayList<double[]>();
		dataset = new HistogramDataset();
	}
	
	/** Creates a new instance. */
	public HistogramPlot()
	{
		super("");
		initialize();
	}
	
	/**
	 * Creates a new instance.
	 *  
	 * @param title 		The title of the graph.
	 * @param newLegends 	The legends of each series. 
	 * @param newData 		The data for each series. 
	 * @param newColours 	The colours for each series. 
	 * @param bins 			The number of bins in the histogram. 
	 */
	public HistogramPlot(String title, List<String> newLegends, 
						List<double[]> newData, List<Color> newColours, 
						int bins)
	{
		super(title);
		if (newLegends == null || newData == null || newColours == null || 
			newLegends.size() != newData.size() && 
			newLegends.size() != newColours.size() || newLegends.size() == 0
			|| bins < 1)
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		initialize();
		for (int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i), 
						bins);
		setDefaultAxis();
	}
	
	/**
	 * Creates a new instance.
	 *  
	 * @param title 		The title of the graph.
	 * @param newLegends 	The legends of each series. 
	 * @param newData 		The data for each series. 
	 * @param newColours 	The colours for each series. 
	 * @param bins 			The number of bins in the histogram. 
	 * @param minValue 		The minimum value of the axis.
	 * @param maxValue 		The maximum value of the axis.
	 */
	public HistogramPlot(String title, List<String> newLegends, 
			List<double[]> newData, List<Color> newColours, int bins, 
			double minValue, double maxValue)
	{
		super(title);
		if (newLegends == null || newData == null || newColours == null || 
			newLegends.size() != newData.size() && 
			newLegends.size() != newColours.size())// || 
			//newLegends.size() == 0 || bins < 1)
			throw new IllegalArgumentException("Mismatch between argument " +
						"length");
		initialize();
		for (int i = 0 ; i < newLegends.size(); i++)
			addSeries(newLegends.get(i), newData.get(i), newColours.get(i), 
						bins);
		setDefaultAxis();
		domainAxis.setRange(minValue, maxValue);
	}

	/**
	 * Adds a new Series to the histogram. 
	 * 
	 * @param legend The name of the new series. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @param bins The number of bins associated with the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * of the just added series. 
	 */
	public int addSeries(String legend, double[] newData, Color color, int bins)
	{
		if (legend == null || newData == null || color == null || bins < 1)
			throw new IllegalArgumentException("Illegal argument in " +
											"addSeries.");
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.addSeries(legend, newData, bins);
		return dataset.getSeriesCount();
	}

	/** 
	 * Creates the chart.
	 * @see ChartObject#createChar()
	 */
	void createChart()
	{
		HistogramBarRenderer renderer = new HistogramBarRenderer(colours);
		for (int i = 0 ; i < colours.size(); i++)
			renderer.setSeriesPaint(i, colours.get(i));
		XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
		chart = new JFreeChart(title, plot);
		chart.setTitle(title);
	}

}


