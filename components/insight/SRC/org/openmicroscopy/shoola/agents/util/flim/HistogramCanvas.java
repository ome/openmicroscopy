/*
 * org.openmicroscopy.shoola.agents.util.flim.HistogramCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.flim;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PVector;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.processing.chart.FillType;
import org.openmicroscopy.shoola.util.processing.chart.HeatMap;
import org.openmicroscopy.shoola.util.processing.chart.HistogramChart;
import org.openmicroscopy.shoola.util.processing.chart.ImageData;

import controlP5.ControlP5;

/**
 * Component displaying the histogram.
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
class HistogramCanvas 
extends PApplet
{
//	/** The chart selected property. */
	public final static String CHARTSELECTED_PROPERTY = "HistogramCanvas.ChartSelected";

	/** The histogram. */
	private HistogramChart chart;
	
	/** The heatMap. */
	private HeatMap map;
	
	/** The object holding the data. */
	private ImageData data;
			
	/** Flag indicating that open or close heatMap.*/
	private boolean displayHeatMap = true;
	
	/** The width of the border between the graph and the window border.*/
	private int borderWidth = 15;
	
	/** The background colour of the windows. */
	final static Color windowsBackground = new Color(230,230,230);

	/** The offset of the heatmap from the Y-Axis.*/
	final static int heatMapOffsetY = 10;
	
	/** The list of datapoints. */
	List<Double> originalData;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orderedData The ordered data.
	 * @param data The data to display.
	 * @param bins The number of bins.
	 */
	HistogramCanvas(List<Double> orderedData, ImageData data, int bins)
	{
		this(orderedData, data, bins, true, -1);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orderedData The ordered data.
	 * @param data The data to display.
	 * @param bins The number of bins.
	 * @param showHeatMap 	Pass <code>true</code> to show the heatMap,
	 * 						<code>false</code> otherwise.
	 * @param thresholdValue Values equal to or less in the heatmap are black.
	 */
	HistogramCanvas(List<Double> originalData, ImageData data, int bins, 
			boolean showHeatMap, double threshold)
	{
		if (data == null)
			throw new IllegalArgumentException("No data to display.");
		if (originalData == null)
			throw new IllegalArgumentException("No data to display.");
		if (bins <= 0) bins = 1;
		this.data = data;
		chart = new HistogramChart(this, originalData, bins, threshold,FillType.NONE);
		chart.setPastelColours();
		chart.setRGB(true, bins/4, bins/2+bins/4);
		double heatMapThreshold=threshold;
		this.originalData = originalData;
		if(originalData.get(0).equals(originalData.get(originalData.size()-1)))
			heatMapThreshold = originalData.get(0);
		map = new HeatMap(this, data, chart, heatMapThreshold); 
		this.displayHeatMap = showHeatMap;
		init();
	}
	
	/**
	 * Set the new threshold of the data.
	 * @param value The threshold.
	 */
	public void setThreshold(double threshold)
	{
		chart.setDataFromThreshold(threshold);
		double heatMapThreshold=threshold;
		if(originalData.get(0).equals(originalData.get(originalData.size()-1)))
			heatMapThreshold = originalData.get(0);
		map.setThreshold(heatMapThreshold);
	}
	
	/**
	 * Returns the colour of the bin containing the value.
	 * 
	 * @param value See above.
	 * @return See above.
	 */
	public int findColour(double value)
	{
		return chart.findColour(value);
	}
	
	/**
	 * Get the pixel position in the heat map.
	 * @param x The x coord.
	 * @param y The y coord.
	 * @return return the pixels position, or return <code>null</code>.
	 */
	public PVector getHeatMapPosition(int x, int y)
	{
		int mapX, mapY;
		
		mapX = x;
		mapY = y;
		if(mapX>0&&mapX<width)
			if(mapY>0&&mapY<height)
			{
				return new PVector(mapX, mapY);
			}
		return null;
	}
	
	/**
	 * Return true if the heat map has been clicked.
	 * @param x The x coordinate on the dialog.
	 * @param y The y coordinate on the dialog.
	 * @return See above.
	 */
	public boolean heatMapClicked(int x, int y)
	{
		int mapX, mapY;
		if(!displayHeatMap)
			return false;
		
		mapX = x;
		mapY = y;
		if(mapX>0&&mapX<map.getWidth())
			if(mapY>0&&mapY<map.getHeight())
				return true;
		return false;
	}
	
	/**
	 * Return true if the chart has been clicked.
	 * @param x The x coordinate on the dialog.
	 * @param y The y coordinate on the dialog.
	 * @return See above.
	 */
	public boolean chartClicked(int x, int y)
	{
		int mapX, mapY;
		Rectangle chartBounds = chart.getBounds();
		if(displayHeatMap)
		{
			mapX = x-data.getWidth()-borderWidth*2;
			mapY = y;
			
			if(mapX>0&&mapX<width-2-data.getWidth()-borderWidth)
				if(mapY>0&&mapY< height-2)
					return true;
			return false;
		}
		else
		{
			return chartBounds.contains(x, y); 
		
		}
	}
	
	/**
	 * Get the value of the Chart at position (x,y)
	 * @param x The x coordinate on the dialog.
	 * @param y The y coordinate on the dialog.
	 * @return See above.
	 */
	public PVector getChartValue(int x, int y)
	{
		int mapX, mapY;
		if(displayHeatMap)
		{
			mapX = x-data.getWidth();
			mapY = y;
		}
		else
		{
			mapX = x;
			mapY = y;
		}
		
		return chart.getScreenToData(new PVector(mapX, mapY));
	}
	
	/** 
	 * Overridden to build the chart. 
	 * 
	 * @see {@link PApplet#setup()}
	 */
	public void setup()
	{
		Dimension d = new Dimension(800, 300);
		setSize(d);
		smooth();
		textFont(createFont("Helvetica", 10));
		textSize(10);
		chart.transposeAxes(false);
		chart.setXAxisLabel("Bins");
		chart.setYAxisLabel("Frequency");
		chart.setLineColour(Color.black.getRGB());
		chart.setLineWidth(2);
		//Scale line graph to use same space as bar graph.
		chart.setMinY(chart.getMinX()); 
		chart.setMaxY(chart.getMaxY());  
		chart.setPointSize(4);
		chart.drawBackground(true);
		//chart.setRGB(true, -1.1, 1.0);
		chart.showXAxis(true);
		chart.showYAxis(true);
	}

	/** Draws the histogram.
	 *
	 * @see {@link PApplet#draw()}
	 */
	public void draw()
	{
		if (chart == null) return;

		background(windowsBackground.getRGB());
		// Draw the bar chart first, then overlay the line chart.
		if (displayHeatMap && map != null) {
			Color c = new Color(102, 102, 102);
			pushMatrix();
				translate(borderWidth, 0);
				pushMatrix();
					translate(0, heatMapOffsetY);
					map.draw();
				popMatrix();
			translate(data.getWidth()+borderWidth, 0);
			chart.draw(1, 1, width-2-data.getWidth()-borderWidth, height-2);
			popMatrix();
		}
		else if(map!= null)
		{
			pushMatrix();
			translate(borderWidth,0);
			chart.draw(1, 1, width-2-borderWidth, height-2);
			popMatrix();
		}
	}
	
	
	/**
	 * The mouse has been released, pick the point.
	 */
	public void mouseReleased()
	{
		pick(mouseX, mouseY);
	}

	/**
	 * Get the stats of the bin.
	 * @param bin The bin.
	 * @return The stats as a map.
	 */
	public Map<String, Double> getBinStats(int bin)
	{
		return chart.getBinStats(bin);
	}
	
	/**
	 * Show the value at position x as picked.
	 * @param point See above.
	 */
	public void pick(int x, int y)
	{
		if(chartClicked(x,y))
		{
			int chartX, chartY;
			if(displayHeatMap)
			{
				chartX = x-data.getWidth()-borderWidth*2;
				chartY = y;
			}
			else
			{
				chartX = x;
				chartY = y;
			}
			int binPicked = chart.pick(new PVector(chartX,chartY));
			if(binPicked==-1)
				return;
			firePropertyChange(CHARTSELECTED_PROPERTY, null, binPicked);
		}
		if(heatMapClicked(x, y))
		{
			
		}
		
	}
	
	/**
	 * Get the bin containing the mean. 
	 * @return See above.
	 */
	public double getMean()
	{
		return chart.getMean();
	}

	/**
	 * Get the bin containing the mean. 
	 * @return See above.
	 */
	public double getMedian()
	{
		return chart.getMedian();
	}

	/**
	 * Get the bins that are one stddev.
	 * @return See above.
	 */
	public int getStd()
	{
		return chart.findBin(chart.getStd());
	}
	
	/**
	 * Set the RGB values of the colourmap.
	 * @param isRGB is the colour map RGB.
	 * @param red The red limit.
	 * @param blue The blue lower limit.
	 */
	public void setRGB(boolean isRGB, int red, int blue)
	{
		chart.setRGB(isRGB, red, blue);
		map.recalculate();
		this.redraw();
	}
	
	/**
	 * Get the stats for the values in the bins [start,end]
	 * @param start See above.
	 * @param end See above.
	 * @return See above.
	 */
	public Map<String, Double> getRangeStats(int start, int end)
	{
		return chart.getRangeStats(start,end);
	}
	
}
