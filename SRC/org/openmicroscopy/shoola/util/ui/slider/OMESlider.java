/*
 * org.openmicroscopy.shoola.util.ui.slider.OMESlider
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
 * OMESlider is an extension of the {@link JSlider}, 
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
public class OMESlider
	extends JSlider
{

	/** Show the arrows on the track if true. */
	private boolean		showArrows;
	
	/** Slider UI for new laf. */
	private OMESliderUI	sliderUI;	
	
	private boolean hasTip;
	private String 	tipString;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orientation  Orientation of slider.
	 * @param min          Minimum value for slider. 
	 * @param max          Maximum value for slider. 
	 * @param value        Value of slider. 
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
	
	/** Creates a default slider.  */
	public OMESlider()
	{
		this(OMESlider.HORIZONTAL, 0, 1, 0);
	}
	
	/**
	 * Returns <code>true</code> if the  arrows on the track, 
     * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isShowArrows() { return showArrows; }
	
	/**
	 * Shows the arrows on the track if the passed value is <code>true</code>,
     * hides otherwise.
	 * 
	 * @param isShow See above.
	 */
	public void setShowArrows(boolean isShow)
	{
		showArrows = isShow;
		sliderUI.setShowArrows(showArrows);
	}
	
	public void setTipString(String tip)
	{
		tipString = tip;
		hasTip = true;
		sliderUI.hasTipString(true);
	}
	
	public String getTipString()
	{
		return tipString;
	}
	
	public boolean hasTipString()
	{
		return hasTip;
	}
    
}
