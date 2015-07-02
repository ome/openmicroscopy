/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;


/** 
* A two knobs slider.
* This component extends {@link JPanel} and is composed of 
* two knobs to select a sub-interval of an interval defined 
* by a minimum and maximum value. 
* The slider behaves mostly like a {@link javax.swing.JSlider}.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
public class TwoKnobsSlider
  	extends JPanel
{
	
	/** The default minimum value. */
	public static final int    			DEFAULT_MIN = 0;

	/** The default maximum value. */
	public static final int    			DEFAULT_MAX = 100;
	
	/** Bound property name indicating if the dragged knob is released. */
	public final static String          KNOB_RELEASED_PROPERTY = 
		"knobReleased";

	/** Bound property name indicating if the left knob is moved. */
	public final static String          LEFT_MOVED_PROPERTY = "leftMoved";

	/** Bound property name indicating if the left knob is moved. */
	public final static String          RIGHT_MOVED_PROPERTY = "rightMoved";

	/** Bound property name indicating if the new start value is set. */
	public final static String          START_VALUE_PROPERTY = "startValue";

	/** Bound property name indicating if the new end value is set. */
	public final static String          END_VALUE_PROPERTY = "endValue";

	/** Bound property name indicating if the new max value is set. */
	public final static String          MAX_VALUE_PROPERTY = "maxValue";

	/** Bound property name indicating if the new min value is set. */
	public final static String          MIN_VALUE_PROPERTY = "minValue";

	/** Bound property name indicating if values of the slider are reset. */
	public final static String          SET_VALUES_PROPERTY = "setValues";

	/** Identifies an horizontal slider. */
	public static final int             HORIZONTAL = 100;

	/** Identifies a vertical slider. */
	public static final int             VERTICAL = 101;

	/** Initial value of the knob control. */
	public static final int             INITIAL = 0;

	/** Indicates that the left knob is moved. */
	public static final int             LEFT = 1;

	/** Indicates that the right know is moved. */
	public static final int             RIGHT = 2;

	/** The default dimension of an horizontal slider. */
	protected static final Dimension    MIN_HORIZONTAL = new Dimension(36, 21);

	/** The default dimension of a vertical slider. */
	protected static final Dimension    MIN_VERTICAL = new Dimension(21, 36);

	/** The preferred dimension of a vertical slider. */
	protected static final Dimension    PREFERRED_VERTICAL = new Dimension(21, 
			80);

	/** The preferred dimension of a horizontal slider. */
	protected static final Dimension    PREFERRED_HORIZONTAL =
		new Dimension(80, 21);

	/** The insets. */
	protected Insets            insetCache = null;

	/** The width of a knob. */
	private int                 knobWidth;

	/** The height of the knob. */
	private int                 knobHeight;

	/** The component's model. */
	private TwoKnobsSliderModel model;

	/** The View component that renders this slider. */
	private TwoKnobsSliderUI    uiDelegate;

	/** The colors for the gradient. */
	private Color[] 			gradients; 
	
	/** 
	 * Indicates which knob is moved.
	 * One of the following constants: {@link #INITIAL}, {@link #LEFT} or
	 * {@link #RIGHT}.
	 */
	private int                 knobControl;

	/** The preferred size of this component. */
	private Dimension           preferredSize_;

	/** The height of the font. */
	private int                 fontHeight;

	/** Flag indicating that the colors have been set. */
	private boolean 			colourGradient;

	/** Computes the preferred size of this component. */
	private void calculatePreferredSize()
	{
		int h = knobHeight;
		int w = 0;
		if (model.isPaintTicks()) h += knobHeight;
		if (model.isPaintLabels() || model.isPaintEndLabels())
			h += TwoKnobsSliderUI.EXTRA+fontHeight+2*TwoKnobsSliderUI.BUFFER;
		if (model.isPaintCurrentValues()) {
			FontMetrics fm = getFontMetrics(getFont());
			w += fm.stringWidth(model.render(model.getAbsoluteMinimum()));
			w += fm.stringWidth(model.render(model.getAbsoluteMaximum()));
		}
		if (model.getOrientation() == VERTICAL)
			preferredSize_ = PREFERRED_VERTICAL;
		else
			preferredSize_ = new Dimension(PREFERRED_HORIZONTAL.width+w, h);
	}

	/** Sets the default values. */
	private void setDefault()
	{
		insetCache = getInsets();
		fontHeight = getFontMetrics(getFont()).getHeight();
		knobControl = INITIAL;
		knobWidth = uiDelegate.getKnobWidth();
		knobHeight = uiDelegate.getKnobHeight();
		colourGradient = false;
		calculatePreferredSize();
	}

	/**
	 * Adds a <code>MouseListener</code> and a <code>MouseMotionListener</code>.
	 */
	private void attachListeners()
	{
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) { handleMouseEvent(evt); }
			public void mouseReleased(MouseEvent evt) { release(); }
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt) { handleMouseEvent(evt); }
		});
	}

	/** Fires a property indicating that the dragged knob is released. */
	private void release()
	{
		if (!model.isEnabled()) return;
		firePropertyChange(KNOB_RELEASED_PROPERTY, Integer.valueOf(INITIAL), 
				Integer.valueOf(knobControl));
		knobControl = INITIAL;
	}

	/**
	 * Handles the events according to the orientation selected.
	 * 
	 * @param me The event to handle.
	 */
	private void handleMouseEvent(MouseEvent me)
	{
		if (!model.isEnabled()) return;
		double oldStart = getStartValue();
		double oldEnd = getEndValue();
		if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL) {
			handleMouseEventForHorizSlider((int) me.getPoint().getX());
			switch (knobControl) {
				case LEFT:
					if (oldStart != getStartValue())
						firePropertyChange(LEFT_MOVED_PROPERTY, oldStart,
								getStartValue());
					break;
				case RIGHT:
					if (oldEnd != getEndValue())
						firePropertyChange(RIGHT_MOVED_PROPERTY, oldEnd, 
								getEndValue());
			} 
		} else {
			handleMouseEventForVertSlider((int) me.getPoint().getY());
			switch (knobControl) {
				case LEFT:
					if (oldEnd != getEndValue())
						firePropertyChange(LEFT_MOVED_PROPERTY, oldEnd, 
								getEndValue());
					break;
				case RIGHT:
					if (oldStart != getStartValue())
						firePropertyChange(RIGHT_MOVED_PROPERTY, oldStart,
								getStartValue());
			} 
		} 
	}

	/**
	 * Handles the event for an horizontal slider.
	 * 
	 * @param x The x-coordinate of the mouse.
	 */
	private void handleMouseEventForHorizSlider(int x)
	{
		int leftKnob = uiDelegate.xPositionForValue(model.getStartValue());
		int rightKnob = uiDelegate.xPositionForValue(model.getEndValue()); 
		int left = leftKnob, right = rightKnob; 
		int xmin = uiDelegate.xPositionForValue(model.getPartialMinimum());
		int xmax = uiDelegate.xPositionForValue(model.getPartialMaximum());
		//Identifies the closest knob
		int limit = leftKnob+(rightKnob-leftKnob)/2;
		if (x < limit && knobControl != RIGHT) {
			knobControl = LEFT;
			left = x;
		} else if (x > limit && knobControl != LEFT) {
			knobControl = RIGHT;
			right = x;
		}

		if (knobControl == LEFT) { //left knob moved.
			model.setStartValue(uiDelegate.xValueForPosition(left, true));
		} else if (knobControl == RIGHT) { //right knob moved.
			model.setEndValue(uiDelegate.xValueForPosition(x, false));
		}
		repaint();
	}

	/**
	 * Handles the event for a vertical slider.
	 * 
	 * @param y The y-coordinate of the mouse.
	 */
	private void handleMouseEventForVertSlider(int y)
	{
		int upKnob = uiDelegate.yPositionForValue(model.getEndValue());
		int downKnob = uiDelegate.yPositionForValue(model.getStartValue()); 
		int up = upKnob, down = downKnob; 
		int ymin = uiDelegate.yPositionForValue(model.getPartialMaximum());
		int ymax = uiDelegate.yPositionForValue(model.getPartialMinimum());
		//Identifies the closest knob 
		int limit = upKnob+(downKnob-upKnob)/2;
		if (y < limit && knobControl != RIGHT) {
			knobControl = LEFT; //corresponds to the up knob
			up = y;
		} else if (y > limit && knobControl != LEFT) {
			knobControl = RIGHT;
			down = y;
		}

		if (knobControl == LEFT) { //left knob moved.
			if (up < ymin) up = ymin;
			else if (up > (ymax-knobHeight)) up = ymax-knobHeight;
			else {
				if (up > down && down < ymax) up = down-1;
			}
			model.setEndValue(uiDelegate.yValueForPosition(up, true));
		} else if (knobControl == RIGHT) { //right knob moved.
			if (down > ymax) down = ymax;
			else if (down < (ymin+knobHeight)) down = ymin+knobHeight;
			else {
				if (down < up && up > ymin) down = up+1;
			}
			model.setStartValue(uiDelegate.yValueForPosition(down, false));
		}
		repaint();
	}

	/**
	 * Returns the preferred size of an horizontal slider.
	 * 
	 * @return See above.
	 */
	protected Dimension getPreferredHorizontalSize()
	{ 
		return PREFERRED_HORIZONTAL;
	}

	/**
	 * Returns the preferred size of a vertical slider.
	 * 
	 * @return See above.
	 */
	protected Dimension getPreferredVerticalSize()
	{
		return PREFERRED_VERTICAL;
	}

	/**
	 * Returns the minimum size of an horizontal slider.
	 * 
	 * @return See above.
	 */
	protected Dimension getMinimumHorizontalSize() { return MIN_HORIZONTAL; }

	/**
	 * Returns the minimum size of an horizontal slider.
	 * 
	 * @return See above.
	 */
	protected Dimension getMinimumVerticalSize() { return MIN_VERTICAL; }

	/**
	 * Returns the {@link #knobControl} i.e. either {@link #LEFT},
	 * {@link #RIGHT} of <code>-1</code> if not assigned.
	 * 
	 * @return See above.
	 */
	int getKnobControl() { return knobControl; }

	/**
	 * Creates a default slider with two knobs.
	 * The minimum and start values are {@link #DEFAULT_MIN}.
	 * The maximum and end values are {@link #DEFAULT_MAX}.
	 */
	public TwoKnobsSlider()
	{
		this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_MIN, DEFAULT_MAX);
	}

	/**
	 * Creates a slider with two knobs of passed minimum, maximum, start and
	 * end value.
	 * 
	 * @param min   The minimum value of the slider.
	 * @param max   The maximum value of the slider.
	 * @param start The start value.
	 * @param end   The end value.
	 */
	public TwoKnobsSlider(double min, double max, double start, double end)
	{
		this(min, max, min, max, start, end);
	}

	/**
	 * Creates a slider with two knobs of passed minimum, maximum, start and
	 * end value.
	 * 
	 * @param absoluteMin 	The absolute minimum value of the slider.
	 * @param absoluteMax 	The absolute minimum value of the slider.
	 * @param min   		The minimum value of the slider.
	 * @param max   		The maximum value of the slider.
	 * @param start 		The start value.
	 * @param end   		The end value.
	 */
	public TwoKnobsSlider(double absoluteMin, double absoluteMax, 
	        double min, double max, double start, double end)
	{
		model = new TwoKnobsSliderModel(absoluteMax, absoluteMin, max, min, 
				start, end);
		uiDelegate = new TwoKnobsSliderUI(this, model);
		attachListeners();
		setDefault();
	}

	/** 
	 * Returns the height of the knob.
	 * 
	 * @return See above.
	 */
	public int getKnobHeight() { return knobHeight; }

	/** 
	 * Returns the width of the knob.
	 * 
	 * @return See above.
	 */
	public int getKnobWidth() { return knobWidth; }

	/**
	 * Sets the color of the font.
	 * 
	 * @param c The font color.
	 */
	public void setFontColor(Color c)
	{
		if (c == null) return;
		uiDelegate.setFontColor(c);
	}

	/**
	 * Returns the value of the start knob i.e. value between
	 * {@link TwoKnobsSliderModel#getMinimum()} and {@link #getEndValue()}.
	 * 
	 * @return See above.
	 */
	public double getStartValue() { return model.getStartValue(); }
	
	/**
	 * Rounds and returns the start value as int
	 * (e. g. 2.49 will be rounded to 2, whereas 2.51 will
	 *  be rounded to 3)
	 * @return See above.
	 */
	public int getStartValueAsInt() {
	    return (int)(model.getStartValue()+0.5);
	}

	/**
	 * Returns the value of the end knob i.e. value between
	 * {@link #getStartValue()} and {@link TwoKnobsSliderModel#getMaximum()}.
	 * 
	 * @return See above.
	 */
	public double getEndValue() { return model.getEndValue(); }

	/**
         * Rounds and returns the end value as int
         * (e. g. 2.49 will be rounded to 2, whereas 2.51 will
         *  be rounded to 3)
         * @return See above.
         */
	public int getEndValueAsInt() {
            return (int)(model.getEndValue()+0.5);
        }
	
	/**
	 * Sets the start value. The value must be greater than the minimum and
	 * lower than the end value.
	 * 
	 * @param v The value to set.
	 */
	public void setStartValue(double v)
	{
	    double old = model.getStartValue();
		model.setStartValue(v);
		firePropertyChange(START_VALUE_PROPERTY, Double.valueOf(old), 
		        Double.valueOf(v));
		repaint();
	}
	
	/**
	 * Sets the end value. The value must be greater than the start value and
	 * lower than the maximum.
	 * 
	 * @param v The value to set.
	 */
	public void setEndValue(double v)
	{
	    double old = model.getStartValue();
		model.setEndValue(v);
		firePropertyChange(END_VALUE_PROPERTY, Double.valueOf(old), 
		        Double.valueOf(v));
		repaint();
		/*
		int max = model.getAbsoluteMaximum();
		if (v > max) return;
		if (v <= getStartValue()) return;
		int old = model.getEndValue();
		model.setEndValue(v);
		firePropertyChange(END_VALUE_PROPERTY, Integer.valueOf(old), 
				Integer.valueOf(v));
		repaint();*/
	}

	/**
	 * Resets the default value of the slider.
	 * 
	 * @param absoluteMax 	The absolute maximum value of the slider.
	 * @param absoluteMin 	The absolute minimum value of the slider.
	 * @param max       	The maximum value.
	 * @param min       	The minimum value.
	 * @param start     	The value of the start knob.
	 * @param end       	The value of the end knob.
	 */
	public void setValues(double absoluteMax, double absoluteMin, 
	        double max, double min, double start, double end)
	{
		model.checkValues(absoluteMax, absoluteMin, max, min, start, end);
		firePropertyChange(SET_VALUES_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
		repaint();
	}

	/**
	 * Sets the input interval.
	 * 
	 * @param start	The value of the start knob.
	 * @param end	The value of the end knob.
	 */
	public void setInterval(double start, double end)
	{
		if (start > end) return;
		double max = model.getAbsoluteMaximum();
		if (end > max) return;
		double oldEnd = model.getEndValue();
		double oldStart = model.getStartValue();
		model.setInterval(start, end);
		firePropertyChange(START_VALUE_PROPERTY, Double.valueOf(oldStart), 
		        Double.valueOf(start));
		firePropertyChange(END_VALUE_PROPERTY, Double.valueOf(oldEnd), 
		        Double.valueOf(end));
		repaint();
	}
	
	/**
	 * Paints the labels if the passed flag is <code>true</code>.
	 * 
	 * @param paintLabel Passed <code>true</code> to paint the labels.
	 */
	public void setPaintLabels(boolean paintLabel)
	{
		if (model.isPaintLabels() == paintLabel) return;
		model.setPaintLabels(paintLabel);
		calculatePreferredSize();
		repaint();
	}

	/**
	 * Paints the current values if the passed flag is <code>true</code>.
	 * 
	 * @param paintCurrentValues Passed <code>true</code> to paint the values.
	 */
	public void setPaintCurrentValues(boolean paintCurrentValues)
	{
		if (model.isPaintCurrentValues() == paintCurrentValues) return;
		model.setPaintCurrentValues(paintCurrentValues);
		calculatePreferredSize();
		repaint();
	}
	
	/**
	 * Paints the minimum and maximum labels if the passed flag
	 * is <code>true</code>.
	 * 
	 * @param paintLabel Passed <code>true</code> to paint the labels.
	 */
	public void setPaintEndLabels(boolean paintLabel)
	{
		if (model.isPaintEndLabels() == paintLabel) return;
		model.setPaintEndLabels(paintLabel);
		calculatePreferredSize();
		repaint();
	}

	/**
	 * Paints the ticks if the passed value is <code>true</code>.
	 * 
	 * @param paintTicks Passed <code>true</code> to paint the ticks.
	 */
	public void setPaintTicks(boolean paintTicks)
	{
		if (model.isPaintTicks() == paintTicks) return;
		model.setPaintTicks(paintTicks);
		calculatePreferredSize();
		repaint();
	}

	/**
	 * Passes <code>true</code> to allow knobs motions, <code>false</code>
	 * otherwise.
	 * 
	 * @param b The value to set.
	 */
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		model.setEnabled(b);
	}

	/**
	 * Returns the orientation of the slider either
	 * {@link #HORIZONTAL} or {@link #VERTICAL}.
	 * 
	 * @return See above.
	 */
	public int getOrientation() { return model.getOrientation(); }

	/** 
	 * Sets the orientation of the slider either
	 * {@link #HORIZONTAL} or {@link #VERTICAL}.
	 * 
	 * @param v The value to set.
	 */
	public void setOrientation(int v)
	{
		if (v == HORIZONTAL || v == VERTICAL) model.setOrientation(v);
		else throw new IllegalArgumentException("Orientation not supported.");
	}

	/**
	 * Sets the space between the minor ticks. Passes <code>true</code>
	 * to set the value to <code>0</code>, <code>false</code> otherwise
	 * 
	 * @param b 
	 */
	public void setPaintMinorTicks(boolean b)
	{
		if (b) {
			double m = model.getMinorTickSpacing();
			if (m == 0) m = 1;
			model.setMinorTickSpacing(m);
		} model.setMinorTickSpacing(0);
		repaint();
	}

	/**
	 * Sets the space between the minor ticks.
	 * 
	 * @param s The space between minor 
	 */
	public void setMinorTickSpacing(int s)
	{
		model.setMinorTickSpacing(s);
	}

	/**
	 * Returns the minimum value if the absolute min equals the minimum
	 * otherwise returns the minimum value.
	 * 
	 * @return See above.
	 */
	public double getPartialMinimum()
	{
		return model.getPartialMinimum();
	}

	/**
	 * Returns the maximum value if the absolute max equals the maximum
	 * otherwise returns the maximum value.
	 * 
	 * @return See above.
	 */
	public double getPartialMaximum()
	{
		return model.getPartialMaximum();
	}

	/**
	 * Sets the color gradient of the slider. This will replace the track with
	 * a gradient.
	 * 
	 * @param rgbStart Start color of the gradient.
	 * @param rgbEnd End color of the gradient.
	 */
	public void setColourGradients(Color rgbStart, Color rgbEnd)
	{
		if (rgbStart == null || rgbEnd == null) return;
		
		colourGradient = true;
		gradients = new Color[2];
		gradients[0] = rgbStart;
		gradients[1] = rgbEnd;
	}
	
	/**
	 * Sets the colors.
	 * 
	 * @param colors The colors to set.
	 */
	public void setColourGradients(Color[] colors)
	{
		if (colors == null || colors.length < 2) return;
		colourGradient = true;
		gradients = colors;
	}
	
	/**
	 * Returns the colors of the gradient.
	 *  
	 * @return See above.
	 */
	Color[] getGradientColors() { return gradients; }
	
	
	/**
	 * Returns <code>True</code> if the color gradient is set,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean getColourGradient() { return colourGradient; }
	
	/**
	 * Sets the flag indicating that we allow to have <code>start == end</code>.
	 * 
	 * @param overlap Pass <code>true</code> to allow <code>start == end</code>,
	 * <code>false</code> otherwise.
	 */
	public void setOverlap(boolean overlap)
	{ 
		model.setOverlap(overlap);
		uiDelegate.createDarkerImage();
	}
	
	/**
	 * Overrides method to return the <code>Preferred Size</code>.
	 * @see JPanel#getPreferredSize()
	 */
	public Dimension getPreferredSize()
	{ 
		if (getOrientation() == VERTICAL) 
			return getPreferredVerticalSize();
		int width = getPreferredHorizontalSize().width;
		int height = insetCache.top + insetCache.bottom;
		height += preferredSize_.height;
		return new Dimension(width, height);
	}

	/**
	 * Overrides method to return the <code>Minimum Size</code>.
	 * @see JPanel#getMinimumSize()
	 */
	public Dimension getMinimumSize() { return preferredSize_; }

	/**
	 * Overrides the {@link #update(Graphics)} method to avoid 
	 * flicking event.
	 * @see JPanel#update(Graphics)
	 */
	public void update(Graphics g) { paintComponent(g); }

	/**
	 * Method invoked at runtime when the slider is resized.
	 * @see JPanel#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		calculatePreferredSize();
		repaint();
	}

	/**
	 * Overrides the {@link #paintComponent(Graphics)} to paint the slider, 
	 * label, ticks if required.
	 * @see JPanel#paintComponent(Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		uiDelegate.paintComponent((Graphics2D) g, getSize());
	}
  
}
