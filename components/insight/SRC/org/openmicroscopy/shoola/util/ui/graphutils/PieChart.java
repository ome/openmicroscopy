/*
 * org.openmicroscopy.shoola.util.ui.graphutils.PieChart 
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

//Application-internal dependencies

/** 
 * Displays a pie chart using <code>JfreeChart</code>.
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
public class PieChart
	extends ChartObject
{	

	/** Data for each series. */ 
	private List<Double>  			data;
	
	/** The pie chart dataset. */
	private DefaultPieDataset		dataset;
	
	/** Initializes */
	private void initialize()
	{
		data = new ArrayList<Double>();
		colours = new ArrayList<Color>();
		dataset = new DefaultPieDataset();
	}
	
	/** Creates a new instance.*/
	public PieChart()
	{
		super("");
		initialize();
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param title 		The title of the graph.
	 * @param newLegends 	The legends of each value. 
	 * @param newData 		The data for each value. 
	 * @param newColours 	The colours for each value. 
	 */
	public PieChart(String title, List<String> newLegends, List<Double> newData,
					List<Color> newColours)
	{
		super(title);
		if (newLegends == null || newData == null || newColours == null ||
			newLegends.size() != newData.size() && 
			newLegends.size()!=newColours.size())
			throw new IllegalArgumentException("Mismatch between argument " +
					"length");
		initialize();
		for (int i = 0 ; i < newLegends.size(); i++)
			addValue(newLegends.get(i), newData.get(i), newColours.get(i));
	}
	
	/**
	 * Adds a new values to the piechars. 
	 * 
	 * @param legend The name of the new values. 
	 * @param newData The data. 
	 * @param color The colour of the values. 
	 * @return The total number of values in the plot, this also gives the id
	 * of the just added value. 
	 */
	public int addValue(String legend, double newData, Color color)
	{
		legends.add(legend);
		data.add(newData);
		colours.add(color);
		dataset.setValue(legend, newData);
		return dataset.getItemCount();
	}

	/** 
	 * Creates the chart.
	 * @see ChartObject#createChar()
	 */
	void createChart()
	{
		chart = ChartFactory.createPieChart3D(title, dataset, false, true, 
				false);
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		if (backgroundImage != null) {
			plot.setBackgroundImage(backgroundImage);
		}
		plot.setDirection(Rotation.CLOCKWISE);
	    plot.setForegroundAlpha(0.55f);
	}
	
}