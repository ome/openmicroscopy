/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider
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

//Third-party libraries

//Application-internal dependencies

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
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class TwoKnobsSlider
    extends JPanel
{
    
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
                                                                200);
    
    /** The preferred dimension of a vertical slider. */
    protected static final Dimension    PREFERRED_HORIZONTAL =
                                                    new Dimension(200, 21);
    /** The default width of a knob. */
    public static final int            KNOB_WIDTH = 16;
    
    /** The default height of the knob. */
    public static final int            KNOB_HEIGHT = 16;

    /** The default width of the horizontal slider. */
    private static final int            PREFERRED_HORIZONTAL_WIDTH = 200;

    /** The default width of the vertical slider. */
    private static final int            PREFERRED_VERTICAL_WIDTH = 21;

    /** Indicates that the right knob is pushed when moving the left knob. */
    private static final int            RIGHT_KNOB_PUSHED = 3;
    
    /** Indicates that the left knob is pushed when moving the right knob. */
    private static final int            LEFT_KNOB_PUSHED = 4;
    
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
    
    /** 
     * Indicates which knod is moved.
     * One of the following constants: {@link #INITIAL}, {@link #LEFT},
     * {@link #RIGHT}.
     */
    private int                 knobControl;
    
    /**
     * Control used to indicate if the right (resp. left) knob is pushed by the
     * left (resp. right) knob during the dragging process.
     * One of the following constants: {@link #INITIAL}, 
     * {@link #LEFT_KNOB_PUSHED}  or {@link #RIGHT_KNOB_PUSHED}.
     */
    private int                 pushKnobControl;
    
    /** The preferred size of this component. */
    private Dimension           preferredSize_;
    
    /** The width of this component. */
    private int                 width_;
    
    /** The height of the font. */
    private int                 fontHeight;

    /**
     * Computes the preferred size of this component.
     */
    private void calculatePreferredSize()
    {
        int h = knobHeight;
        if (model.isPaintTicks()) h += knobHeight;
        if (model.isPaintLabels() || model.isPaintEndLabels())
            h += TwoKnobsSliderUI.EXTRA+fontHeight+2*TwoKnobsSliderUI.BUFFER;
        if( model.getOrientation() == VERTICAL )
        {
        	width_ = calculateVerticalWidth();
        	System.err.println("Width : " + width_);
            preferredSize_ = new Dimension(PREFERRED_VERTICAL);
        }
        else
        	preferredSize_ = new Dimension(PREFERRED_HORIZONTAL_WIDTH,h);
    }
    
    /**
     * Calculates the vertical width of the slider. 
     * 
     * @return See above.
     */
    private int calculateVerticalWidth()
    {
    	int w = (KNOB_WIDTH+6)*2+6;
    	if( model.isPaintTicks() )
    		w += KNOB_WIDTH+6;
    	if( model.isPaintLabels() || model.isPaintEndLabels() )
    		w += 30;
    	return w;
    }
    
    /** Sets the default values. */
    private void setDefault()
    {
        FontMetrics metrics = getFontMetrics(getFont());
        insetCache = getInsets();
        fontHeight = metrics.getHeight();
        knobControl = INITIAL;
        pushKnobControl = INITIAL;
        knobWidth = KNOB_WIDTH;
        knobHeight = KNOB_HEIGHT;
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
        firePropertyChange(KNOB_RELEASED_PROPERTY, new Integer(INITIAL), 
                                new Integer(knobControl));
        knobControl = INITIAL;
        pushKnobControl = INITIAL;
    }
    
    /**
     * Handles the events according to the orientation selected.
     * 
     * @param me The event to handle.
     */
    private void handleMouseEvent(MouseEvent me)
    {
        if (!model.isEnabled()) return;
        int oldStart = getStartValue();
        int oldEnd = getEndValue();
        if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL) {
            handleMouseEventForHorizSlider((int) me.getPoint().getX());
            if (knobControl == LEFT || pushKnobControl == LEFT_KNOB_PUSHED) 
                firePropertyChange(LEFT_MOVED_PROPERTY, oldStart,
                                getStartValue());
            else if (knobControl == RIGHT || 
                    pushKnobControl == RIGHT_KNOB_PUSHED) {
                firePropertyChange(RIGHT_MOVED_PROPERTY, oldEnd, getEndValue());
            }
                
        } else {
            handleMouseEventForVertSlider((int) me.getPoint().getY());
            if (knobControl == LEFT || pushKnobControl == LEFT_KNOB_PUSHED) 
                firePropertyChange(LEFT_MOVED_PROPERTY, oldEnd, getEndValue());
            else if (knobControl == RIGHT || 
                    pushKnobControl == RIGHT_KNOB_PUSHED)
                firePropertyChange(RIGHT_MOVED_PROPERTY, oldStart,
                                    getStartValue());
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
        int xmin = uiDelegate.xPositionForValue(model.getMinimum());
        int xmax = uiDelegate.xPositionForValue(model.getMaximum());
        //Identifies the closest knob
        
        if (x < (leftKnob+(rightKnob-leftKnob)/2)) {
            knobControl = LEFT;
            left = x;
        } else {
            knobControl = RIGHT;
            right = x;
        }
        
        if (knobControl == LEFT) { //left knob moved.
            if (left < xmin) left = xmin;
            else if (left > (xmax-knobWidth)) left = xmax-knobWidth;
            else {
                if (left > (right-knobWidth) && right < xmax ) {
                    //push right
                    pushKnobControl = RIGHT_KNOB_PUSHED;
                    right = left+knobWidth;  
                    model.setEndValue(uiDelegate.xValueForPosition(right+1));
                }   
            }
            model.setStartValue(uiDelegate.xValueForPosition(left+1));
        } else if (knobControl == RIGHT) { //right knob moved.
            if (right > xmax) right = xmax;
            else if (right < (xmin+knobWidth)) right = xmin+knobWidth;
            else {
                if (right < (left+knobWidth) && left > xmin) {
                    //push left
                    pushKnobControl = LEFT_KNOB_PUSHED;
                    left = right-knobWidth;    
                    model.setStartValue(uiDelegate.xValueForPosition(left+1));
                }
            }
            model.setEndValue(uiDelegate.xValueForPosition(right+1));
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
        int ymin = uiDelegate.yPositionForValue(model.getMaximum());
        int ymax = uiDelegate.yPositionForValue(model.getMinimum());
        //Identifies the closest knob 
        if (y < (upKnob+(downKnob-upKnob)/2)) {
            knobControl = LEFT; //corresponds to the up knob
            up = y;
        } else {
            knobControl = RIGHT;
            down = y;
        }
        
        if (knobControl == LEFT) { //left knob moved.
            if (up < ymin) up = ymin;
            else if (up > (ymax-knobHeight)) up = ymax-knobHeight;
            else {
                if (up > (down-knobHeight) && down < ymax) {
                    //push down
                    pushKnobControl = RIGHT_KNOB_PUSHED;
                    down = up+knobHeight;  
                    model.setStartValue(uiDelegate.yValueForPosition(down));
                }    
            }
            model.setEndValue(uiDelegate.yValueForPosition(up));
        } else if (knobControl == RIGHT) { //right knob moved.
            if (down > ymax) down = ymax;
            else if (down < (ymin+knobHeight)) down = ymin+knobHeight;
            else {
                if (down < (up+knobHeight) && up > ymin) {
                    pushKnobControl = LEFT_KNOB_PUSHED;
                    up = down-knobHeight;  
                    model.setEndValue(uiDelegate.yValueForPosition(up));
                }    
            }
            model.setStartValue(uiDelegate.yValueForPosition(down));
        }
        //model.setEndValue(uiDelegate.yValueForPosition(up));
        //model.setStartValue(uiDelegate.yValueForPosition(down));
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
     * Creates a default slider with two knobs.
     * The minimum and start values are {@link TwoKnobsSliderModel#DEFAULT_MIN}.
     * The maximum and end values are {@link TwoKnobsSliderModel#DEFAULT_MAX}.
     */
    public TwoKnobsSlider()
    {
        this(TwoKnobsSliderModel.DEFAULT_MIN, TwoKnobsSliderModel.DEFAULT_MAX, 
             TwoKnobsSliderModel.DEFAULT_MIN, TwoKnobsSliderModel.DEFAULT_MAX);
    }
   
    /**
     * Creates a slider with two knobs of passed mininum, maximum, start and
     * end value.
     * 
     * @param min   The minimum value of the slider.
     * @param max   The maximum value of the slider.
     * @param start The start value.
     * @param end   The end value.
     */
    public TwoKnobsSlider(int min, int max, int start, int end)
    {
        model = new TwoKnobsSliderModel(max, min, start, end);
        uiDelegate = new TwoKnobsSliderUI(this, model);
        attachListeners();
        setDefault(); 
    }
    
    /** 
     * Returns the height of the knob.
     * 
     * @return See above.
     */
    int getKnobHeight() { return knobHeight; }
    
    /** 
     * Returns the width of the knob.
     * 
     * @return See above.
     */
    int getKnobWidth() { return knobWidth; }
    
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
     * Sets the color of the knob controlling the end value.
     * 
     * @param c The color of the knob.
     */
    public void setEndKnobColor(Color c)
    {
        if (c == null) return;
        uiDelegate.setEndKnobColor(c);
    }
    
    /**
     * Sets the color of the knob controlling the end value.
     * 
     * @param c The color of the knob.
     */
    public void setStartKnobColor(Color c)
    {
        if (c == null) return;
        uiDelegate.setStartKnobColor(c);
    }
    
    /**
     * Returns the value of the start knob i.e. value between
     * {@link #getMinimum()} and {@link #getEndValue()}.
     * 
     * @return See above.
     */
    public int getStartValue() { return model.getStartValue(); }
    
    /**
     * Returns the value of the end knob i.e. value between
     * {@link #getStartValue()} and {@link #getMaximum()}.
     * 
     * @return See above.
     */
    public int getEndValue() { return model.getEndValue(); }

    /**
     * Returns the maximum value of the slider.
     * 
     * @return See above.
     */
    public int getMaximum() { return model.getMaximum(); }
    
    /**
     * Returns the minimum value of the slider.
     * 
     * @return See above.
     */
    public int getMinimum() { return model.getMinimum(); }
    
    /**
     * Sets the start value. The value must be greater than the minimum and
     * lower than the end value.
     * 
     * @param v The value to set.
     */
    public void setStartValue(int v)
    {
        if (v < getMinimum()) 
            throw new IllegalArgumentException("Start cannot be < " +
                    ""+getMinimum());
        if (v >= getEndValue())
            throw new IllegalArgumentException("Start cannot be >= " +
                    ""+getEndValue());
        int old = model.getStartValue();
        model.setStartValue(v);
        firePropertyChange(START_VALUE_PROPERTY, new Integer(old), 
                            new Integer(v));
        repaint();
    }
    
    /**
     * Sets the end value. The value must be greater than the start value and
     * lower than the maximum.
     * 
     * @param v The value to set.
     */
    public void setEndValue(int v)
    {
        if (v > getMaximum()) 
            throw new IllegalArgumentException("End cannot be > " +
                    ""+getMaximum());
        if (v <= getStartValue())
            throw new IllegalArgumentException("End cannot be <= " +
                    ""+getStartValue());
        int old = model.getEndValue();
        model.setEndValue(v);
        firePropertyChange(END_VALUE_PROPERTY, new Integer(old), 
                            new Integer(v));
        repaint();
    }
    
    /**
     * Sets the maximum value of the slider.
     * 
     * @param v The value to set.
     */
    public void setMaximum(int v)
    {
        int o = model.getMaximum();
        model.setMaximum(v);
        if (model.getEndValue() > v) setEndValue(v);
        firePropertyChange(MAX_VALUE_PROPERTY, new Integer(o), new Integer(v));
        repaint();
    }
    
    /**
     * Sets the maximum value of the slider.
     * 
     * @param v The value to set.
     */
    public void setMinimum(int v)
    {
        int o = model.getMinimum();
        model.setMinimum(v);
        if (model.getStartValue() < v) setStartValue(v);
        firePropertyChange(MIN_VALUE_PROPERTY, new Integer(o), new Integer(v));
        repaint();
    }
    
    /**
     * Resets the default value of the slider.
     * 
     * @param max       The maximum value.
     * @param min       The minimum value.
     * @param start     The value of the start knob.
     * @param end       The value of the end knob.
     */
    public void setValues(int max, int min, int start, int end)
    {
        model.checkValues(max, min, start, end);
        firePropertyChange(SET_VALUES_PROPERTY, Boolean.FALSE, Boolean.TRUE);
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
        //if (model.isEnabled() == b) return;
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
     * Overrides method to return the <code>Preferred Size</code>.
     * @see JPanel#getPreferredSize()
     */
    public Dimension getPreferredSize()
    { 
        Dimension d;
        if (getOrientation() == VERTICAL ) {
            d = new Dimension(getPreferredVerticalSize());
        } else {
            d = new Dimension(getPreferredHorizontalSize());
            d.height = insetCache.top + insetCache.bottom;
            d.height += preferredSize_.height;
        }
        return d;
    }
    
    /**
     * Overrides method to return the <code>Minimum Size</code>.
     * @see JPanel#getMinimumSize()
     */
    public Dimension getMinimumSize() { return preferredSize_; }
    
    /**
     * Overrides the {@link #update(Graphics)} method to avoid 
     * flicking event.
     */
    public void update(Graphics g) { paintComponent(g); }
    
    /**
     * Method invoked at runtime when the slider is resized.
     * @see JPanel#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        width_ = width;
        calculatePreferredSize();
        repaint();
    }
    
    /**
     * Overrides the {@link #paintComponent(Graphics)} to paint the slider, 
     * label, ticks if required.
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        uiDelegate.paintComponent((Graphics2D) g, getSize());
    }

}
