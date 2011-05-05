/*
 * org.openmicroscopy.shoola.util.processing.chart.Histogram
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.util.processing.chart;

//Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a histogram from a sorted list of numbers, as the list is sorted it
 * means we only need to access each element in the list once to calculate the 
 * histogram.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Histogram
{
	/** The key for the minimum value in the map. */
	public final static String MIN = "MIN";
	
	/** The key for the maximum value in the map. */
	public final static String MAX = "MAX";

	/** The key for the mean value in the map. */
	public final static String MEAN = "MEAN";
	
	/** The key for the freq value in the map. */
	public final static String FREQ = "FREQ";
	
	/** The original data. */
	private List<Double> originalData;
	
	/** The number of bins in the histogram.*/
	private double bins;
	
	/** The frequency count.*/
	private float[] freq;
	
	/** The Range of the data.*/
	private Range range;
		
	/** The width of a bin.*/
	private double binWidth;
	
	/** Flag indicating if the histogram has been reshaped.*/
	private boolean reshaped;
	
	/** The bounds of the reshaped histogram.*/
	private double lower, upper;
	
	/** Calculates the histogram.*/
	private void calculateHistogram()
	{
		this.freq = new float[(int) bins];
		calculateRange();
		populateBins();
	}
	
	/** Calculate the range of the values in the originalData.*/
	private void calculateRange()
	{
		range = new Range(originalData.get(0), 
						originalData.get(originalData.size()-1));
		binWidth = range.getRange()/bins;
	}
	
	/**
	 * Calculate the bin of the current value, this is going to be
	 * [0, 1/bins)
	 * [1/bins, 2/bins)
	 * ....
	 * [(n-1)/bins, 1].
	 * 
	 * @param value The value to handle.
	 * @return See above.
	 */
	private int calculateIndex(double value)
	{
		double norm = (value-range.getMin())/range.getRange();
		if (norm < 0) norm = 0;
		double dBin = norm*bins;
		if (dBin > Math.floor(bins-1/bins))return (int) bins-1;
		return (int) Math.floor(dBin);
	}

	/** Populates the frequency data with the bin information. */
	private void populateBins()
	{
		int index;
		for (double dataPt : originalData)
		{
			index = calculateIndex(dataPt);
			freq[index] = freq[index]+1;
		}
	}

	/**
	 * Creates a new instance, the histogram expects a sorted list of numbers.
	 * 
	 * @param data The raw data. 
	 * @param bins The number of bins in the histogram.
	 */
	public Histogram(List<Double> data, int bins)
	{
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("Data not valid.");
		this.originalData = data;
		setBinning(bins);
	}


	/**
	 * Returns the calculated histogram.
	 * @return See above.
	 */
	public float[] getHistogram() { return freq; }
	
	/**
	 * Sets the number of bins.
	 * 
	 * @param bins The value to set.
	 */
	public void setBinning(double bins)
	{
		if (bins <= 0) bins = 1;
		this.bins = bins;
		this.calculateHistogram();
	}

	/**
	 * Returns the bin for data value.
	 * 
	 * @param data The value to handle.
	 * @return See above.
	 */
	public int findBin(double data) { return calculateIndex(data); }
	
	/**
	 * Reshapes the histogram on the lower and upper bounds supplied.
	 * 
	 * @param lower The lower bound.
	 * @param upper The upper bound.
	 */
	public float[] reshapeOn(double lower, double upper)
	{
		if (lower > upper) {
			double l = upper;
			upper = lower;
			lower = l;
		}
		int lowerBin = findBin(lower);
		int upperBin = findBin(upper);
		float [] reshaped = new float[upperBin-lowerBin];
		for (int i = 0 ; i < upperBin-lowerBin; i++)
			reshaped[i] = freq[lowerBin+i];
		return reshaped;
	}
	
	/**
	 * Returns the mean value of the bin.
	 * 
	 * @param bin The selected bin.
	 * @return See above.
	 */
	public double findValue(int bin)
	{
		if (bin < 0) bin = 0;
		return bin*binWidth+lower+binWidth/2;
	}
	
	/** Reverts the reshaped histogram.*/
	public void revert()
	{
		
	}
	
	/**
	 * Get the stats of the bin.
	 * @param bin See above.
	 * @return A map of the stats calculated.
	 */
	public Map<String, Double> getBinStats(int bin)
	{
		if(bin<0 || bin>bins)
			return null;
		Map<String, Double> binStats = new HashMap<String, Double>();
		binStats.put(MEAN, findValue(bin));
		binStats.put(MIN, lower+bin*binWidth);
		binStats.put(MAX, lower+bin*binWidth+binWidth);
		binStats.put(FREQ, (double)freq[bin]);
		
		return binStats;
	}
}
