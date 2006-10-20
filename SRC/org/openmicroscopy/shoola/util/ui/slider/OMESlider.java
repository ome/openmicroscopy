/*
 * OMESlider.java
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.slider;

//Java imports
import javax.swing.JSlider;

//Third-party libraries

//Application-internal dependencies

/** 
 * OMESlider is an extension of the JSlider, it has a moew Aqua look and feel, 
 * plus the addition of arrow buttons at the ends of the track which can 
 * increment the slider by one.
 * 
 * When the track is selected, the thumb will move to the point clicked, which
 * is different to the original 
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
public class OMESlider
	extends JSlider
{

	/** Show the arrows on the track if true. */
	private boolean		showArrows;
	
	/** Slider UI for new laf. */
	private OMESliderUI	sliderUI;	
	
	/**
	 * Constructor for OMESlider.
	 * 
	 * @param orientation Orientation of slider.
	 * @param min Minimum value for slider. 
	 * @param max Maximum value for slider. 
	 * @param value Value of slider. 
	 */
	public OMESlider(int orientation, int min, int max, int value)
	{
		super();
		sliderUI = new OMESliderUI(this);
		this.setUI(sliderUI);
		this.setOrientation(orientation);
		this.setMinimum(min);
		this.setMaximum(max);
		this.setValue(value);
	}
	
	/**
	 * OMESlider contructor. 
	 */
	public OMESlider()
	{
		this(OMESlider.HORIZONTAL, 0, 1, 0);
	}
	
	/**
	 * Returns true if the  arrows on the track.
	 * 
	 * @return See above.
	 */
	public boolean isShowArrows()
	{
		return showArrows;
	}
	
	/**
	 * Show or hide the arrows on the track.
	 * 
	 * @param isShow See above.
	 */
	public void setShowArrows(boolean isShow)
	{
		showArrows = isShow;
		sliderUI.setShowArrows(showArrows);
	}
}
