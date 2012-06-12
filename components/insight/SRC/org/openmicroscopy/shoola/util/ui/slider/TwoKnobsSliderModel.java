/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobSliderModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
	private int							absoluteMin;

	/** The absolute maximum. */
	private int							absoluteMax;

	/** The maximum value. */
	private int         				maximum;

	/** The minimum value. */
	private int         				minimum;

	/** The value of the start knob, the default value is {@link #minimum}. */
	private int         				startValue;

	/** The value of the end knob, the default value is {@link #maximum}. */
	private int         				endValue;

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
	private int         				majorTickSpacing;

	/** The space between the minor ticks. */
	private int         				minorTickSpacing;

	/** The ticks increment value. */
	private int         				increment;

	/** The collection storing the labels. */
	private Map<Integer, String>		labels;

	/**
	 * Identifies the orientation of the slider either 
	 * {@link TwoKnobsSlider#HORIZONTAL} or {@link TwoKnobsSlider#VERTICAL}
	 */
	private int         				orientation;

	/** The partial min is for example the minimum value minus the increment. */
	private int							partialMin;

	/** The partial max is for example the maximum value plus the increment. */
	private int							partialMax;

	/** Indicates that overlap is allowed.*/
	private boolean						overlap;
	
	/** Creates labels for the minimum and maximum values. */
	private void createEndLabels()
	{
		labels.put(Integer.valueOf(minimum), render(minimum));
		labels.put(Integer.valueOf(maximum), render(maximum));
	}

	/** Creates the labels. */
	private void createLabels()
	{
		for (int i = minimum; i <= maximum; i += increment)
			labels.put(Integer.valueOf(i), render(i));
	}

	/** Initializes the controls with the default values. */
	private void installDefaults()
	{
		paintTicks = true;
		enabled = true;
		increment = (maximum-minimum)/10;
		minorTickSpacing = 1;
		majorTickSpacing = 10;
		labels = new HashMap<Integer, String>();
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
	 * @param startValue    The value of the start knob.
	 * @param endValue      The value of the end knob.
	 */
	TwoKnobsSliderModel(int absoluteMax, int absoluteMin, int maximum, 
			int minimum, int startValue, int endValue)
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
	 * @param startValue    The value of the start knob.
	 * @param endValue      The value of the end knob.
	 */
	void checkValues(int absoluteMax, int absoluteMin, int maximum, int minimum,
			int startValue, int endValue)
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
	 * Returns the value of the end knob.
	 * 
	 * @return See above.
	 */
	int getEndValue() { return endValue; }

	/**
	 * Returns the value of the start knob.
	 * 
	 * @return See above.
	 */
	int getStartValue() { return startValue; }

	/**
	 * Returns the maximum value of the slider.
	 * 
	 * @return See above.
	 */
	int getMaximum() { return maximum; }

	/**
	 * Returns the minimum value of the slider.
	 * 
	 * @return See above.
	 */
	int getMinimum() { return minimum; }

	/**
	 * Returns the maximum value of the slider.
	 * 
	 * @return See above.
	 */
	int getAbsoluteMaximum() { return absoluteMax; }

	/**
	 * Returns the minimum value of the slider.
	 * 
	 * @return See above.
	 */
	int getAbsoluteMinimum() { return absoluteMin; }

	/**
	 * Returns the partial minimum.
	 * 
	 * @return See above.
	 */
	int getPartialMinimum() { return absoluteMin; }

	/**
	 * Returns the partial maximum.
	 * 
	 * @return See above.
	 */
	int getPartialMaximum() { return absoluteMax; }

	/**
	 * Sets the value of the start knob, value in the range
	 * <code>[{@link #minimum}, {@link #endValue}[</code>.
	 * 
	 * @param startValue The value to set.
	 */
	void setStartValue(int startValue)
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
	 * <code>]{@link #startValue}, {@link #maximum}]</code>.
	 * 
	 * @param endValue The value to set.
	 */
	void setEndValue(int endValue)
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
	 * @param start	The value to set.
	 * @param end	The value to set.
	 */
	void setInterval(int start, int end)
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
	int getMajorTickSpacing() { return majorTickSpacing; }

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
	int getMinorTickSpacing() { return minorTickSpacing; }

	/**
	 * Sets the minorTickSpacing value.  
	 * 
	 * @param v The value to set.
	 */
	void setMinorTickSpacing(int v) { minorTickSpacing = v; }

	/**
	 * Returns the increment value.
	 * 
	 * @return See above.
	 */
	int getIncrement() { return increment; }

	/**
	 * Returns the map with the labels.
	 * 
	 * @return See above. 
	 */
	Map<Integer, String> getLabels() { return labels; }

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
