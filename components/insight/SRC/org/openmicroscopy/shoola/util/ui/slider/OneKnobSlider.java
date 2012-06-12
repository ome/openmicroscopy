/*
 * org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider
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
import javax.swing.ImageIcon;
import javax.swing.JSlider;

//Third-party libraries

//Application-internal dependencies

/** 
* OneKnobSlider is an extension of the {@link JSlider}, 
* it has a more <code>Aqua look and feel</code>, 
* plus the addition of arrow buttons at the ends of the track which can 
* increment the slider by one.
* <p>
* When the track is selected, the thumb will move to the point clicked, which
* is different to the original.
* </p>
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME2.2
*/
public class OneKnobSlider
	extends JSlider
{
	
	/** Bound property indicating that the knob has been released.*/
	public static final String ONE_KNOB_RELEASED_PROPERTY =
		"oneKnobReleasedProperty";
	
	/** Slider UI for new L&F. */
	private OneKnobSliderUI	sliderUI;	
	
	/** This is set to <code>true</code> if the slider has tooltipString. */
	private boolean 		hasLabel;
	
	/** Tool-tip text which is shown when slider is dragged, changed value. */
	private String 			endLabel;
	
	/** This value is set to true if the tip label will be displayed. */
	private	boolean			showTipLabel;
	
	/** This value is set to true if the end label will be displayed. */
	private boolean 		showEndLabel;
	
	/** The original size of a minor tick spacing. */
	private int				minorTickSpacing;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orientation  Orientation of slider.
	 * @param min          Minimum value for slider. 
	 * @param max          Maximum value for slider. 
	 * @param value        Value of slider. 
	 */
	public OneKnobSlider(int orientation, int min, int max, int value)
	{
		super();
		sliderUI = new OneKnobSliderUI(this);
		setUI(sliderUI);
		setOrientation(orientation);
		setMinimum(min);
		setMaximum(max);
		setValue(value);
		hasLabel = false;
		setSnapToTicks(false);
		minorTickSpacing = getMinorTickSpacing();
	}
	
	/** Creates a default slider.*/
	public OneKnobSlider()
	{
		this(OneKnobSlider.HORIZONTAL, 0, 1, 0);
	}
	
	/** Fires a property indicating that the knob has been released.*/
	void onMouseReleased()
	{
		firePropertyChange(ONE_KNOB_RELEASED_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Returns <code>true</code> if the  arrows on the track, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isShowArrows() { return sliderUI.isShowArrows(); }
	
	/**
	 * Shows the arrows on the track if the passed value is <code>true</code>,
	 * hides otherwise.
	 * 
	 * @param isShow See above.
	 */
	public void setShowArrows(boolean isShow)
	{
		sliderUI.setShowArrows(isShow);
	}
	
	/**
	 * Replaces the arrows icons by the specified one.
	 * 
	 * @param up	The icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param down  The icon displayed at the bottom of the slider if
	 * 				vertical, at the left of the slider if horizontal.
	 */
	public void setArrowsImageIcon(ImageIcon up, ImageIcon down)
	{
		if (up == null || down == null) return;
		sliderUI.setArrowsImageIcon(up, down);
	}
	
	/**
	 * Replaces the arrows icons by the specified one.
	 * 
	 * @param up	The icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param down  The icon displayed at the bottom of the slider if
	 * 				vertical, at the left of the slider if horizontal.
	 * @param disabledUp The disabled icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param disabledDown The disabled icon displayed at the bottom of the 
	 * 				slider if vertical, at the left of the slider if horizontal.
	 */
	public void setArrowsImageIcon(ImageIcon up, ImageIcon down, 
			ImageIcon disabledUp, ImageIcon disabledDown)
	{
		if (up == null || down == null) return;
		sliderUI.setArrowsImageIcon(up, down, disabledUp, disabledDown);
	}
	
	/**
	 * Sets the string for the tooltip which is displayed when slider changes
	 * value, as well as the label shown at the end of the text. 
	 * 
	 * @param label prefix data for the string to display.
	 */
	public void setEndLabel(String label)
	{
		endLabel = label;
		hasLabel = true;
		sliderUI.setEndLabel(label);
	}
	
	/**
	 * Returns the text used in the end Label.
	 *  
	 * @return see above.
	 */
	public String getEndLabel() { return endLabel; }
	
	/**
	 * Returns <code>true</code> if the component has an <code>endLabel</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above. 
	 */
	public boolean hasEndLabel() { return hasLabel; }
	
	/**
	 * Shows the end label if set to <code>true</code>, hides it 
	 * <code>otherwise</code>.
	 *  
	 * @param show  Pass <code>true</code> to show the label, 
	 *              <code>false</code> otherwise.
	 */
	public void setShowEndLabel(boolean show)
	{
		showEndLabel = show;
		sliderUI.setShowEndLabel(show);
	}
	
	/**
	 * Shows the tip label if set to <code>true</code>, hides it 
	 * <code>otherwise</code>.
	 *  
	 * @param show Pass <code>true</code> to show the tip label, 
	 *              <code>false</code> otherwise.
	 */
	public void setShowTipLabel(boolean show)
	{
		showTipLabel = show;
		sliderUI.setShowTipLabel(show);
	}
	
	/**
	 * Returns <code>true</code> if the tip label will be displayed,
   * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean showTipLabel() { return showTipLabel; }
	
	/**
	 * Returns <code>true</code> if the end label will be displayed,
   * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean showEndLabel() { return showEndLabel; }
	
	/**
	 * Returns <code>true</code> if the user is dragging the slider's knob,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDragging() { return sliderUI.isDragging; }
	
	/**
	 * Sets the space between the minor ticks. Passes <code>true</code>
	 * to set the value to <code>0</code>, <code>false</code> otherwise
	 * 
	 * @param b Pass <code>true</code> to paint the minor ticks,
	 * 			<code>false</code> otherwise.
	 */
	public void setPaintMinorTicks(boolean b)
	{
		if (b) setMinorTickSpacing(minorTickSpacing);
		else super.setMinorTickSpacing(0);
	}
	
	/**
	 * Overridden to reset the space between minor ticks.
	 * @see JSlider#setMinorTickSpacing(int)
	 */
	public void setMinorTickSpacing(int s)
	{
		minorTickSpacing = s;
		super.setMinorTickSpacing(s);
	}
	
	/**
	 * Overridden to enable the slider and the icons displayed on each
	 * side of the slider if any.
	 * @see JSlider#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		repaint();
	}

}
