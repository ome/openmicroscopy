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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates a histogram from a sorted list of numbers, as the list is sorted it
 * means we only need to access each elemeent in the list once to calcuate the 
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
	
	/** The original data. */
	List<Double> originalData;
	
	/** The number of bins in the histogram. */
	double bins;
	
	/** The frequency count. */
	float[] freq;
	
	/** The Range of the data. */
	Range range;
		
	/** The width of a bin. */
	double binWidth;
	
	/** Has the histogram been reshaped. */
	boolean reshaped;
	
	/** The bounds of the reshaped histogram. */
	double lower, upper;
	
	/**
	 * Instantiate the Historgram, the histogram expects a sorted list of numbers.
	 * @param data The raw data. 
	 * @param bins The number of bins in the histogram.
	 */
	public Histogram(List<Double> data, int bins)
	{
		this.originalData = data;
		this.bins = bins;
		calculateHistogram();
	}
	
	/**
	 * Calculate the histogram. 
	 */
	private void calculateHistogram()
	{
		this.freq = new float[(int)bins];
		calculateRange();
		populateBins();
	}
	
	/**
	 * Calculate the range of the values in the originalData.
	 */
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
	 * [(n-1)/bins, 1]
	 * @param value See above.
	 * @return See above.
	 */
	private int calculateIndex(double value)
	{
		double norm = (value-range.getMin())/range.getRange();
		double dBin = norm*bins;
		if(dBin > Math.floor(bins-1/bins))
			return (int)bins-1;
		else
			return (int)Math.floor(dBin);
	}
	
	/**
	 * Populate the frequency data with the bin information
	 */
	private void populateBins()
	{
		int index;
		for(double dataPt : originalData)
		{
			index = calculateIndex(dataPt);
			freq[index] = freq[index]+1;
		}
	}
	
	/**
	 * Return the calculated histogram.
	 * @return See above.
	 */
	public float[] getHistogram()
	{
		return freq;
	}
	
	public void setBinning(double bins)
	{
		this.bins = bins;
		this.calculateHistogram();
	}
	
	/**
	 * Get the bin for data value.
	 * @param data See above.
	 * @return
	 */
	public int findBin(double data)
	{
		return calculateIndex(data);
	}
	
	/**
	 * Reshape the histogram on the lower and upper bounds supplied.
	 * @param lower See above.
	 * @param upper See above.
	 */
	public float[] reshapeOn(double lower, double upper)
	{
		int lowerBin = findBin(lower);
		int upperBin = findBin(upper);
		float [] reshaped = new float[upperBin-lowerBin];
		for(int i = 0 ; i < upperBin-lowerBin; i++)
			reshaped[i] = freq[lowerBin+i];
		return reshaped;
	}
	
	/**
	 * Return the mean value of the bin
	 * @param bin See above.
	 * @return See above.
	 */
	public double findValue(int bin)
	{
		return bin*binWidth+lower+binWidth/2;
	}
	
	/**
	 * Revert the reshaped histogram. 
	 */
	public void revert()
	{
		
	}
}
