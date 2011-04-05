/*
 * org.openmicroscopy.shoola.util.processing.chart.HistogramChart
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
import java.awt.Color;
import java.util.List;

//Third-party libraries
import processing.core.PApplet;
import processing.core.PVector;

//Application-internal dependencies

/**
 * Displays the chart.
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
public class HistogramChart
	extends XYChart
{

	/** The histogram of the data. */
	private Histogram histogram;
	
	/** The frequency data. */
	private float[] data;
	
	/** The number of bins in the histogram. */
	private int bins;
	
	/** The starting location for the chart. */
	private float xOrigin, yOrigin;
	
	/** The width and height of the chart. */
	private float width, height;
	
	/** Flag indicating if the background should be draw as RGB. */
	private boolean rgb;
	
	/** The values of the data where the r, g, b gradients change.*/
	private double red, blue;
	
	/** Flag indicating to draw gradients in background. */
	private boolean drawRGB;
	
	/** The colors for red, green, blue areas. */
	private int redColour, greenColour, blueColour;
	
	/** Should the colours in the background be a 
	 * gradientFill
	 */
	private FillType fillType;
	
	/** Gradient step. */
	private double gradientStep;

	/**
	 * Returns the bins values for the X axis of the plot.
	 * 
	 * @return See above.
	 */
	private float[] getBins()
	{
		float[] b = new float[bins];
		for(int i = 0 ; i < bins ; i++)
			b[i] = i;
		return b;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param parent Reference to the <code>PApplet</code>.
	 * @param orderedData The data to display.
	 * @param bins The number of bins.s
	 */
	public HistogramChart(PApplet parent, List<Double> orderedData, int bins)
	{
		super(parent);
		if (bins <= 0) bins = 1;
		this.bins = bins;
		setHistogramData(orderedData);
		this.rgb = true;
		this.drawRGB = false;
		setPrimaryColours();
		fillType = FillType.NONE;
		gradientStep = 1;
	}

	/** Sets the colour map to have primary Colours. */
	public void setPrimaryColours()
	{
		redColour = Color.red.getRGB();
		greenColour = Color.green.getRGB();
		blueColour = Color.blue.getRGB();
	}

	/** Sets the colour map to have pastel colours. */
	public void setPastelColours()
	{
		redColour = new Color(255, 102,102).getRGB();
		greenColour = new Color(102, 255, 102).getRGB();
		blueColour = new Color(102, 204, 255).getRGB();
	}

	/**
	 * Overridden to draw colors.
	 * @see XYChart#draw(float, float, float, float)
	 */
	public void draw(float xOrigin, float yOrigin, float width, float height)
	{
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.width = width;
		this.height = height;
		parent.pushMatrix();
		super.draw(xOrigin, yOrigin, width, height);
		if (drawRGB)
		{
			this.paintRGB();
			super.draw(xOrigin, yOrigin, width, height);
		}
		parent.popMatrix();
	}

	/**
	 * Sets the data of the histogram to the new orderedData.
	 * 
	 * @param orderedData The data in ascending order.
	 */
	public void setHistogramData(List<Double> orderedData)
	{
		histogram = new Histogram(orderedData, bins);
		data = histogram.getHistogram();
		setData(getBins(), data);
	}
	
	/**
	 * Reshapes the histogram, retaining the same binning to the lower and 
	 * upper bounds.
	 * 
	 * @param lower The lower bound.
	 * @param upper The upper bound.
	 */
	public void reshape(double lower, double upper)
	{
		data = histogram.reshapeOn(lower, upper);
		setData(getBins(), data);
	}
	
	/** 
	 * Passes <code>true</code> to draw the RGB gradient background,
	 * <code>false</code> otherwise.
	 * 
	 * @param drawRGB See above.
	 */
	public void setDrawRGB(boolean drawRGB)
	{
		this.drawRGB = drawRGB;
	}
	
	/**
	 * Sets the RGB values of the background, if RGB is <code>rgb</code>
	 * then painting is in RGB order.
	 * 
	 * @param rgb Pass <code>true</code> to draw in RGB order, 
	 * 			  <code>false</code> otherwise.
	 * @param red The red component of the color.
	 * @param blue The blue component of the color.
	 */
	public void setRGB(boolean rgb, double red, double blue)
	{
		this.rgb = rgb;
		this.red = red;
		this.blue = blue;
	}
	
	/**
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColours(int red, int green, int blue)
	{
		this.redColour = red;
		this.greenColour = green;
		this.blueColour = blue;
	}
	
	/**
	 * Returns the colour of the bin containing the value.
	 * 
	 * @param value See above.
	 * @return See above.
	 */
	public int findColour(double value)
	{
		switch(fillType) {
			case NONE:
			
			if(rgb) {
				if (value < red) return redColour;
				if (value >= blue) return blueColour;
				return greenColour;
			}
			else
			{
				if (value < red) return blueColour;
				if (value >= blue) return redColour;
				return greenColour;	
			}
			case GRADIENT:
			default:
				return binToColour(histogram.findBin(value));
		}
	}

	/** Paints the RGB gradient on the background. */
	public void paintRGB()
	{
		float offset = 1;
		int redBin = histogram.findBin(red);
		PVector pRed = this.getDataToScreen(new PVector(redBin, 0));
		int blueBin = histogram.findBin(blue);
		PVector pBlue = this.getDataToScreen(new PVector(blueBin, 0));
		parent.pushStyle();
		parent.noStroke();
		if (rgb)
		{
			switch(fillType)
			{
				case NONE:
					parent.fill(redColour);
					parent.rect(xOrigin, yOrigin, pRed.x, height, offset, 
							offset);	
					parent.fill(greenColour);
					parent.rect(pRed.x, yOrigin, pBlue.x-pRed.x, height,
							offset, offset);	
					parent.fill(blueColour);
					parent.rect(pBlue.x, yOrigin, width-pBlue.x, height,
							offset, offset);
					break;
				case GRADIENT:
				case JET:
					for(double x = left ; x < right ; x += gradientStep)
					{
						parent.fill(gradientColour(x));
						parent.rect((float) x, (float) top, 
								(float) gradientStep, bottom-top+1, 
								offset, offset);
					}
			}
		}
		else
		{
			switch(fillType)
			{
				case NONE:
					parent.fill(blueColour);
					parent.rect(xOrigin, yOrigin, pRed.x, height, offset, 
							offset);	
					parent.fill(greenColour);
					parent.rect(pRed.x, yOrigin, pBlue.x-pRed.x, height,
							offset, offset);	
					parent.fill(redColour);
					parent.rect(pBlue.x, yOrigin, width-pBlue.x, height, 
							offset, offset);	
				case GRADIENT:
				case JET:
					for(double x = left ; x < right ; x += gradientStep)
					{
						parent.fill(gradientColour(x));
						parent.rect((float) x, (float) top,(float) gradientStep,
								bottom-top+1, offset, offset);
					}
			}
		}
		parent.popStyle();
	}

	/**
	 * Returns the mixture of the colours col1, and col2 under the percentages
	 * percent1 and percent2.
	 * 
	 * @param col1 See above.
	 * @param col2 See above. 
	 * @param percent1 See above.
	 * @param percent2 See above.
	 * @return The new mixture of the colours.
	 */
	private int mixColour(int col1, int col2, double percent1, double percent2)
	{
		Color c1 = new Color(col1);
		Color c2 = new Color(col2);
		int red = Math.min((int) ((double) c1.getRed()*percent1+
				(double) c2.getRed()*percent2), 255);
		int green = Math.min((int)((double) c1.getGreen()*percent1+
				(double) c2.getGreen()*percent2), 255);
		int blue = Math.min((int)((double) c1.getBlue()*percent1+
				(double) c2.getBlue()*percent2), 255);
		Color c3 = new Color(red, green, blue);
		return c3.getRGB();
	}

	/**
	 * Returns the jet gradient map for the current percent in the map.
	 * 
	 * @param percent See above.
	 * @return See above.
	 */
	private int getJetColour(double percent)
	{
		return Color.white.getRGB();
	}
	
	/**
	 * Returns the colour of the bin for the current fillType.
	 * @param bin See above.
	 * @return See above.
	 */
	private int binToColour(int bin)
	{
		switch(fillType)
		{
			case GRADIENT:
				double midPoint = bins/2;
				if (bin < midPoint)
					return mixColour(redColour, greenColour,
							1.0-(double) bin/midPoint, (double) bin/midPoint); 
				return mixColour(greenColour, blueColour, 
						1.0-((bin-midPoint)/midPoint),
						((bin-midPoint)/midPoint)); 
			default:
				return getJetColour((double) bin/bins);
			}
	}

	/**
	 * Returns the gradient colour of the value x.
	 * 
	 * @param x See above.
	 * @return See above.
	 */
	private int gradientColour(double x)
	{
		PVector value = getScreenToData(new PVector((float) x, (bottom-top)/2));
		if (value == null) return Color.white.getRGB();
		switch(fillType)
		{
			case GRADIENT:
				return binToColour((int) value.x);
			case JET:
				return binToColour((int) value.x);
		}
		return Color.white.getRGB();
	}
	
	/**
	 * Returns the mean value of the bin picked, at point x,y if none returns
	 * <code>null</code>.
	 * 
	 * @param x See above.
	 * @param y See above.
	 * @return See above.
	 */
	public Double pickedValue(float x, float y)
	{
		PVector point = getScreenToData(new PVector(x,y));
		if (point == null) return null;
		return histogram.findValue((int)point.x);
	}
	
	/**
	 * Sets the filling type for the graph.
	 * 
	 * @param fill See above.
	 */
	public void setGradientFill(FillType fill) { fillType = fill; }
	
	/**
	 * Returns the vector corresponding to the screen point.
	 * 
	 * @param screenPoint The point to handle.
	 * @return See above.
	 */
	public PVector getScreenToData(PVector screenPoint)
	{
		float hRange = right-left;
		float vRange = bottom-top;

		if ((vRange <= 0) || (hRange <=0))
		{
			return null;
		}

		if ((screenPoint.x < left) || (screenPoint.x >= right) || 
				(screenPoint.y <= top) || (screenPoint.y > bottom))
		{
			return null;
		}

		// Scale the screen coordinates between 0-1.
		float x,y;
		if (transposeAxes)
		{
			y = (screenPoint.x - left)/(hRange);
			x = (bottom - screenPoint.y)/vRange;
		}
		else
		{   
			x = (screenPoint.x - left)/(hRange);
			y = (bottom - screenPoint.y)/vRange;
		}

		x = (int)(x*super.data[0].length);

		if (getIsLogScale(1))
		{
			y = convertFromLog(y, getMinLog(1), getMaxLog(1));
		}
		else
		{
			y = y*(getMax(1)-getMin(1)) + getMin(1);
		}
		return new PVector(x,y);
	}
	
}
