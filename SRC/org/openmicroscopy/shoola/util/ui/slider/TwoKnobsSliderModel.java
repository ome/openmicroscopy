/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobSliderModel
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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


    /** The default minimum value. */
    static final int    DEFAULT_MIN = 0;
    
    /** The default maximum value. */
    static final int    DEFAULT_MAX = 100;
    
    /** The maximum value. */
    private int maximum;
    
    /** The minimum value. */
    private int minimum;
    
    /** The value of the start knob, the default value is {@link #minimum}. */
    private int startValue;
    
    /** The value of the end knob, the default value is {@link #maximum}. */
    private int endValue;
    
    /** <code>true</code> if knobs' motions are allowed. */
    private boolean enabled;
    
    /** Indicates if the labels are painted or not, */
    private boolean paintLabels;
    
    /** Indicates if the ticks are painted or not. */
    private boolean paintTicks;
    
    /** Indicates if the end labels are painted or not. */
    private boolean paintEndLabels;
    
    /** The space between the major ticks. */
    private int     majorTickSpacing;
    
    /** The space between the minor ticks. */
    private int     minorTickSpacing;
    
    /** The ticks increment value. */
    private int     increment;
    
    /** The collection storing the labels. */
    private Map     labels;
    
    /**
     * Identifies the orientation of the slider either 
     * {@link TwoKnobsSlider#HORIZONTAL} or {@link TwoKnobsSlider#VERTICAL}
     */
    private int     orientation;

    /**
     * Creates labels for the minimum and maximum values.
     */
    private void createEndLabels()
    {
        labels.put(new Integer(minimum), render(minimum));
        labels.put(new Integer(maximum), render(maximum));
    }
    
    /**
     * Creates the labels.
     */
    private void createLabels()
    {
        for (int i = minimum; i <= maximum; i += increment)
            labels.put(new Integer(i), render(i));
    }
    
    /**
     * Initializes the controls with the default values.
     */
    private void installDefaults()
    {
        paintTicks = true;
        enabled = true;
        increment = (maximum-minimum)/10;
        minorTickSpacing = 1;
        majorTickSpacing = 10;
        labels = new HashMap();
        setPaintLabels(false);
        setPaintEndLabels(true);
        setOrientation(TwoKnobsSlider.HORIZONTAL);
    }

    /**
     * Creates a new instance. 
     * 
     * @param maximum       The maximum value.
     * @param minimum       The minimum value.
     * @param startValue    The value of the start knob.
     * @param endValue      The value of the end knob.
     */
    TwoKnobsSliderModel(int maximum, int minimum, int startValue, int endValue)
    {
        checkValues(maximum, minimum, startValue, endValue);
        installDefaults();
    }
    
    /**
     * Checks if the specified values are valid.
     * 
     * @param maximum       The maximum value.
     * @param minimum       The minimum value.
     * @param startValue    The value of the start knob.
     * @param endValue      The value of the end knob.
     */
    void checkValues(int maximum, int minimum, int startValue, int endValue)
    {
        if ((maximum >= minimum) && (startValue >= minimum) && 
                (startValue <= maximum) && (endValue >= minimum) &&
                (endValue <= maximum) && (startValue <= endValue)) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.minimum = minimum;
            this.maximum = maximum;
        } else
            throw new IllegalArgumentException("Invalid range properties");
        increment = (maximum-minimum)/10;
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
     * Sets the value of the start knob, value in the range
     * <code>[{@link #minimum}, {@link #endValue}[</code>.
     * 
     * @param startValue The value to set.
     */
    void setStartValue(int startValue)
    { 
        if (startValue < minimum || startValue >= endValue)
            throw new IllegalArgumentException("Start value not valid.");
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
        if (endValue <= startValue || endValue > maximum)
            throw new IllegalArgumentException("End value not valid.");
        this.endValue = endValue;
    }
    
    /**
     * Sets the maximum value. The value must be greater that {@link #minimum}.
     * 
     * @param maximum The value to set.
     */
    void setMaximum(int maximum)
    { 
        if (maximum <= minimum)
            throw new IllegalArgumentException("Maximum must be greater than " +
                    "minimum.");
        this.maximum = maximum;
    }
    
    /**
     * Sets the minimum value. The value must be less than {@link #maximum}.
     * 
     * @param minimum The value to set.
     */
    void setMinimum(int minimum)
    { 
        if (minimum >= maximum)
            throw new IllegalArgumentException("Minimum cannot be greater" +
                    "than maximum.");
        this.minimum = minimum;
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
    Map getLabels() { return labels; }
    
    /**
     * Formats the specified value.
     *  
     * @param value The value to format.
     * @return See above.
     */
    String render(double value)
    {
        DecimalFormat myF = new DecimalFormat();
        return myF.format(value);
    }
    
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
    
}
