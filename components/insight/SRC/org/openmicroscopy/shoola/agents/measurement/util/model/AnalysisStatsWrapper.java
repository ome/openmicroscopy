/*
 * org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.env.rnd.roi.AbstractROIShapeStats;
import org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats;
import org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStatsSimple;
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
		AbstractROIShapeStats stats;
		Map<Integer, Double> channelMin = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelSum = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelMax = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelMean = new TreeMap<Integer, Double>();
		Map<Integer, Double> channelStdDev = new TreeMap<Integer, Double>();
		Map<Integer, double[]> channelData = new TreeMap<Integer, double[]>();
		double[] pixels;
		
		int channel;
		Iterator channelIterator = shapeStats.keySet().iterator();
		while (channelIterator.hasNext())
		{
			channel = (Integer) channelIterator.next();
			stats = (AbstractROIShapeStats) shapeStats.get(channel);
			channelSum.put(channel, stats.getSum());
			channelMin.put(channel, stats.getMin());
			channelMax.put(channel, stats.getMax());
			channelMean.put(channel, UIUtilities.roundTwoDecimals(
					stats.getMean()));
			channelStdDev.put(channel, UIUtilities.roundTwoDecimals(
					stats.getStandardDeviation()));
			
            if (stats instanceof ROIShapeStatsSimple)
                pixels = ((ROIShapeStatsSimple) stats).getPixelsValue();
            else {
                Map<Point, Double> pixelsMap = ((ROIShapeStats) stats)
                        .getPixelsValue();
                Iterator<Double> pixelIterator = pixelsMap.values().iterator();
                pixels = new double[pixelsMap.size()];
                int cnt = 0;
                while (pixelIterator.hasNext()) {
                    pixels[cnt] = pixelIterator.next();
                    cnt++;
                }
            }
			
			channelData.put(channel, pixels);
		}
		Map<StatsType, Map> 
			statsMap = new HashMap<StatsType, Map>(StatsType.values().length);
		statsMap.put(StatsType.SUM, channelSum);
		statsMap.put(StatsType.MIN, channelMin);
		statsMap.put(StatsType.MAX, channelMax);
		statsMap.put(StatsType.MEAN, channelMean);
		statsMap.put(StatsType.STDDEV, channelStdDev);
		statsMap.put(StatsType.PIXELDATA, channelData);
		statsMap.put(StatsType.PIXEL_PLANEPOINT2D, channelData);
		//shapeStats.clear();
		return statsMap;
	}
	
}
