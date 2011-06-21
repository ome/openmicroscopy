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
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

//Application-internal dependencies

/** 
 * Displays a bars using <code>JfreeChart</code>.
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
	extends ChartObject
{	

	/** Default series. WTF. */
	private final static String DEFAULTSERIES = "default";
		
	/** The X-Axis label. Also can,but not currently used set the range. */
	private CategoryAxis  			categoryAxis;
	
	/** The BarPlot dataset. */
	private DefaultCategoryDataset	dataset;
		
	/** Data for each series. */ 
	protected List<Double>	data;
	
	/** Initializes. */
	private void initialize()
	{
		data = new ArrayList<Double>();
		dataset = new DefaultCategoryDataset();
	}
	
	/** Creates a new instance. */
	public BarPlot()
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
	 */
	public BarPlot(String title, List<String> newLegends, List<Double> newData,
				List<Color> newColours)
	{
		super(title);
		if (newLegends == null || newData == null ||
			newColours == null || newLegends.size() != newData.size() && 
			newLegends.size() != newColours.size() || newLegends.size() == 0)
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		initialize();
		for (int i = 0 ; i < newLegends.size(); i++)
			addValue(newLegends.get(i), newData.get(i), newColours.get(i));
		setDefaultAxis();
		categoryAxis = new CategoryAxis(X_AXIS);
	}

	/**
	 * Adds a new Series to the bar plot.
	 *  
	 * @param legend The name of the new series. 
	 * @param newData The data. 
	 * @param color The colour of the series. 
	 * @return The total number of series in the plot, this also gives the id
	 * 			of the just added series. 
	 */
	public int addValue(String legend, double newData, Color color)
	{
		if (legend == null || color == null)
			throw new IllegalArgumentException("Illegal argument in " +
					"addSeries.");
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.addValue(newData, DEFAULTSERIES, legend);
		return dataset.getColumnCount();
	}

	/** 
	 * Creates the chart.
	 * @see ChartObject#createChar()
	 */
	void createChart()
	{
		CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis, 
				new CustomBarRenderer(colours));
		if (backgroundImage != null) {
			plot.setRangeGridlinesVisible(false);
			plot.setDomainGridlinesVisible(false);
			plot.setBackgroundImage(backgroundImage);
		}
		chart = new JFreeChart(title, plot);
	}
	
}