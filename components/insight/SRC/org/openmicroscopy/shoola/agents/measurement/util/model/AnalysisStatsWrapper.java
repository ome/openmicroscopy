/*
 * org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper 
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
package org.openmicroscopy.shoola.agents.measurement.util.model;

//Java imports
import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class AnalysisStatsWrapper
{	
	/** enumeration of the type of stats in the map. */
	public enum StatsType 
	{
		MIN,
		MAX, 
		MEAN,
		STDDEV,
		PIXELDATA,
		SUM,
		PIXEL_PLANEPOINT2D
	};

	/**
	 * Converts the statistics.
	 * 
	 * @param shapeStats The statistics to convert.
	 * @return See above.
	 */
	public static Map<StatsType, Map> convertStats(Map shapeStats)
	{
		if (shapeStats == null || shapeStats.size() == 0) return null;
		ROIShapeStats stats;
		Map<Integer, Double> channelMin = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelSum = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelMax = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelMean = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelStdDev = new TreeMap<Integer, Double>();
		Map<Integer, double[]> channelData = new TreeMap<Integer, double[]>();
		Map<Integer, Map<Point, Double>> channelPixel = 
			new TreeMap<Integer, Map<Point, Double>>();
		Iterator<Double> 	pixelIterator;
		Map<Point, Double> pixels;
		
		double[] pixelData;
		int cnt;
		int channel;
		Iterator channelIterator = shapeStats.keySet().iterator();
		while (channelIterator.hasNext())
		{
			channel = (Integer) channelIterator.next();
			stats = (ROIShapeStats) shapeStats.get(channel);
			channelSum.put(channel, stats.getSum());
			channelMin.put(channel, stats.getMin());
			channelMax.put(channel, stats.getMax());
			channelMean.put(channel, UIUtilities.roundTwoDecimals(
					stats.getMean()));
			channelStdDev.put(channel, UIUtilities.roundTwoDecimals(
					stats.getStandardDeviation()));
			pixels = stats.getPixelsValue();
				
			channelPixel.put(channel, pixels);
			pixelIterator = pixels.values().iterator();
			pixelData = new double[pixels.size()];
			cnt = 0;
			while (pixelIterator.hasNext())
			{
				pixelData[cnt] = pixelIterator.next();
				cnt++;
			}
			
			channelData.put(channel, pixelData);
			//pixels.clear();
		}
		Map<StatsType, Map> 
			statsMap = new HashMap<StatsType, Map>(StatsType.values().length);
		statsMap.put(StatsType.SUM, channelSum);
		statsMap.put(StatsType.MIN, channelMin);
		statsMap.put(StatsType.MAX, channelMax);
		statsMap.put(StatsType.MEAN, channelMean);
		statsMap.put(StatsType.STDDEV, channelStdDev);
		statsMap.put(StatsType.PIXELDATA, channelData);
		statsMap.put(StatsType.PIXEL_PLANEPOINT2D, channelPixel);
		shapeStats.clear();
		return statsMap;
	}
	
}
