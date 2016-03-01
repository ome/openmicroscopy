/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobSliderModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.slider;


//Java imports
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
* The Model sub-component of the {@link TwoKnobsSlider}.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class TwoKnobsSliderModel
{

	/** The absolute minimum. */
	private double							absoluteMin;

	/** The absolute maximum. */
	private double							absoluteMax;

	/** The maximum value. */
	private double         				maximum;

	/** The minimum value. */
	private double         				minimum;

	/** The value of the start knob, the default value is {@link #minimum}. (one based) */
	private double         				startValue;

	/** The value of the end knob, the default value is {@link #maximum}. (one based) */
	private double         				endValue;

	/** Flag indicating if we can move the knobs. */
	private boolean     				enabled;

	/** Indicates if the labels are painted or not, */
	private boolean     				paintLabels;

	/** Indicates if the ticks are painted or not. */
	private boolean     				paintTicks;

	/** Indicates to paint the {@link #startValue} and {@link #endValue}. */
	private boolean						paintCurrentValues;
	
	/** Indicates if the end labels are painted or not. */
	private boolean     				paintEndLabels;

	/** The space between the major ticks. */
	private double         				majorTickSpacing;

	/** The space between the minor ticks. */
	private double         				minorTickSpacing;

	/** The ticks increment value. */
	private double         				increment;

	/** The collection storing the labels. */
	private Map<Double, String>		labels;

	/**
	 * Identifies the orientation of the slider either 
	 * {@link TwoKnobsSlider#HORIZONTAL} or {@link TwoKnobsSlider#VERTICAL}
	 */
	private int         				orientation;

	/** The partial min is for example the minimum value minus the increment. */
	private double							partialMin;

	/** The partial max is for example the maximum value plus the increment. */
	private double							partialMax;

	/** Indicates that overlap is allowed.*/
	private boolean						overlap;
	
	/** Creates labels for the minimum and maximum values. */
	private void createEndLabels()
	{
		labels.put(Double.valueOf(minimum), render(minimum));
		labels.put(Double.valueOf(maximum), render(maximum));
	}

	/** Creates the labels. */
	private void createLabels()
	{
		for (double i = minimum; i <= maximum; i += increment)
			labels.put(Double.valueOf(i), render(i));
	}

	/** Initializes the controls with the default values. */
	private void installDefaults()
	{
		paintTicks = true;
		enabled = true;
		increment = (maximum-minimum)/10;
		minorTickSpacing = 1;
		majorTickSpacing = 10;
		labels = new HashMap<Double, String>();
		setPaintLabels(false);
		setPaintEndLabels(true);
		setOrientation(TwoKnobsSlider.HORIZONTAL);
	}

	/**
	 * Creates a new instance. 
	 * 
	 * @param absoluteMax	The absolute maximum value.
	 * @param absoluteMin	The absolute minimum value.
	 * @param maximum       The maximum value.
	 * @param minimum       The minimum value.
	 * @param startValue    The value of the start knob. (one based)
	 * @param endValue      The value of the end knob.  (one based)
	 */
	TwoKnobsSliderModel(double absoluteMax, double absoluteMin, double maximum, 
	        double minimum, double startValue, double endValue)
	{
		checkValues(absoluteMax, absoluteMin, maximum, minimum, startValue, 
				endValue);
		installDefaults();
	}

        
	/**
	 * Checks if the specified values are valid.
	 * 
	 * @param absoluteMax	The absolute maximum value.
	 * @param absoluteMin	The absolute minimum value.
	 * @param maximum       The maximum value.
	 * @param minimum       The minimum value.
	 * @param startValue    The value of the start knob. (one based)
	 * @param endValue      The value of the end knob. (one based)
	 */
	void checkValues(double absoluteMax, double absoluteMin, double maximum, double minimum,
	        double startValue, double endValue)
	{
		if (maximum >= minimum && startValue <= endValue &&
				absoluteMax >= absoluteMin && maximum <= absoluteMax &&
				minimum >= absoluteMin) {
			this.startValue = startValue;
			this.endValue = endValue;
			this.minimum = minimum;
			this.maximum = maximum;
			this.absoluteMax = absoluteMax;
			this.absoluteMin = absoluteMin;
		}
		if (startValue < absoluteMin) startValue = minimum;
		if (endValue > absoluteMax) endValue = absoluteMax;
		increment = (maximum-minimum)/10;
		if (startValue < minimum) partialMin = startValue;
		else partialMin = minimum;
		if (endValue > maximum) partialMax = endValue;
		else partialMax = maximum;
	}

	/**
	 * Returns <code>true</code> if the knobs' motions are allowed.
	 * 
	 * @return See above.
	 */
	boolean isEnabled() { return enabled; }

	/**
	 * Allows knobs' motions if the passed value is <code>true</code>.
	 * 
	 * @param enabled Passed <code>true</code> to allow knobs' motions. 
	 */
	void setEnabled(boolean enabled) { this.enabled = enabled; }

	/**
	 * Returns the value of the end knob. (one based)
	 * 
	 * @return See above.
	 */
	double getEndValue() { return endValue; }

	/**
	 * Returns the value of the start knob. (one based)
	 * 
	 * @return See above.
	 */
	double getStartValue() { return startValue; }

	/**
	 * Returns the maximum value of the slider.
	 * 
	 * @return See above.
	 */
	double getMaximum() { return maximum; }

	/**
	 * Returns the minimum value of the slider.
	 * 
	 * @return See above.
	 */
	double getMinimum() { return minimum; }

	/**
	 * Returns the maximum value of the slider.
	 * 
	 * @return See above.
	 */
	double getAbsoluteMaximum() { return absoluteMax; }

	/**
	 * Returns the minimum value of the slider.
	 * 
	 * @return See above.
	 */
	double getAbsoluteMinimum() { return absoluteMin; }

	/**
	 * Returns the partial minimum.
	 * 
	 * @return See above.
	 */
	double getPartialMinimum() { return absoluteMin; }

	/**
	 * Returns the partial maximum.
	 * 
	 * @return See above.
	 */
	double getPartialMaximum() { return absoluteMax; }

	/**
	 * Sets the value of the start knob, value in the range
	 * <code>[{@link #minimum}, {@link #endValue}[  (one based) </code>.
	 * 
	 * @param startValue The value to set.
	 */
	void setStartValue(double startValue)
	{ 
		if (overlap) {
			if (startValue > endValue) return;
		} else {
			if (startValue >= endValue) return;
		}
		
			//throw new IllegalArgumentException("Start value not valid.");
		if (startValue < absoluteMin) startValue = absoluteMin;
		if (startValue <= partialMin) partialMin = startValue;
		this.startValue = startValue;
	}

	/**
	 * Sets the value of the end knob, value in the range
	 * <code>]{@link #startValue}, {@link #maximum}]  (one based) </code>.
	 * 
	 * @param endValue The value to set.
	 */
	void setEndValue(double endValue)
	{ 
		if (overlap) {
			if (endValue < startValue) return;
		} else {
			if (endValue <= startValue) return;
		}
			//throw new IllegalArgumentException("End value not valid.");
		if (endValue > absoluteMax) endValue = absoluteMax;
		if (endValue >= partialMax) partialMax = endValue;
		this.endValue = endValue;
	}

	/**
	 * Sets the start and end values of the slider.
	 * 
	 * @param start	The value to set. (one based)
	 * @param end	The value to set. (one based)
	 */
	void setInterval(double start, double end)
	{
		if (end > absoluteMax) end = absoluteMax;
		if (end >= partialMax) partialMax = end;
		if (start < absoluteMin) start = absoluteMin;
		if (start <= partialMin) partialMin = start;
		this.startValue = start;
		this.endValue = end;
	}
	
	/**
	 * Paints the labels if the passed flag is <code>true</code>.
	 * 
	 * @param paintCurrentValues Passed <code>true</code> to paint the values.
	 */
	void setPaintCurrentValues(boolean paintCurrentValues)
	{ 
		this.paintCurrentValues = paintCurrentValues;
	}
	
	/**
	 * Paints the labels if the passed flag is <code>true</code>.
	 * 
	 * @param paintLabels Passed <code>true</code> to paint the labels.
	 */
	void setPaintLabels(boolean paintLabels)
	{ 
		this.paintLabels = paintLabels;
		if (paintLabels) createLabels();
	}

	/**
	 * Paints the ticks if the passed value is <code>true</code>.
	 * 
	 * @param paintTicks Passed <code>true</code> to paint the ticks.
	 */
	void setPaintTicks(boolean paintTicks) { this.paintTicks = paintTicks; }

	/**
	 * Paints the minimum and maximum labels if the passed flag
	 * is <code>true</code>.
	 * 
	 * @param b Passed <code>true</code> to paint the labels.
	 */
	void setPaintEndLabels(boolean b)
	{
		paintEndLabels = b;
		if (paintEndLabels) createEndLabels();
	}

	/**
	 * Returns <code>true</code> the labels are painted, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPaintLabels() { return paintLabels; }

	/**
	 * Returns <code>true</code> to paint the current values, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPaintCurrentValues() { return paintCurrentValues; }
	
	/**
	 * Returns <code>true</code> the ticks are painted, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPaintTicks() { return paintTicks; }

	/**
	 * Returns <code>true</code> the minimum and maximum labels are painted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPaintEndLabels() { return paintEndLabels; }

	/**
	 * Returns the majorTickSpacing value.
	 * 
	 * @return See above. 
	 */
	double getMajorTickSpacing() { return majorTickSpacing; }

	/**
	 * Sets the majorTickSpacing value.  
	 * 
	 * @param v The value to set.
	 */
	void setMajorTickSpacing(int v) { this.majorTickSpacing = v; }

	/**
	 * Returns the minorTickSpacing value.
	 * 
	 * @return See above. 
	 */
	double getMinorTickSpacing() { return minorTickSpacing; }

	/**
	 * Sets the minorTickSpacing value.  
	 * 
	 * @param v The value to set.
	 */
	void setMinorTickSpacing(double v) { minorTickSpacing = v; }

	/**
	 * Returns the increment value.
	 * 
	 * @return See above.
	 */
	double getIncrement() { return increment; }

	/**
	 * Returns the map with the labels.
	 * 
	 * @return See above. 
	 */
	Map<Double, String> getLabels() { return labels; }

	/**
	 * Formats the specified value.
	 *  
	 * @param value The value to format.
	 * @return See above.
	 */
	String render(double value) { return (new DecimalFormat()).format(value); }

	/**
	 * Returns the orientation of the slider.
	 * 
	 * @return See above.
	 */
	int getOrientation() { return orientation; }

	/** 
	 * Sets the orientation of the slider either
	 * {@link TwoKnobsSlider#HORIZONTAL} or {@link TwoKnobsSlider#VERTICAL}.
	 * 
	 * @param v The value to set.
	 */
	void setOrientation(int v) { orientation = v; }
  
	/**
	 * Returns <code>true</code> if we allow to have <code>start == end</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean allowOverlap() { return overlap; }
	
	/**
	 * Sets the flag indicating that we allow to have <code>start == end</code>.
	 * 
	 * @param overlap Pass <code>true</code> to allow <code>start == end</code>,
	 * <code>false</code> otherwise.
	 */
	void setOverlap(boolean overlap) { this.overlap = overlap; }

}
