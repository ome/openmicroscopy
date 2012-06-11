/*
 * org.openmicroscopy.shoola.util.ui
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * The rating component. 
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
public class RatingComponent 
	extends JPanel
{

	/** Index corresponding to 16x16 icons. */
	public static final int		HIGH_SIZE = 0;
	
	/** Index corresponding to 12x12 icons. */
	public static final int		MEDIUM_SIZE = 1;
	
	/** Index corresponding to 8x8 icons. */
	public static final int		SMALL_SIZE = 2;
	
	/** Bound property indicating that the value has changed. */
	public static final String	RATE_PROPERTY = "rate";
	
	/**
	 * Bound property indicating that the modification of the value has ended.
	 */
	public static final String	RATE_END_PROPERTY = "rateEnd";
	
	/** The maximum number of value. */
	public static final int 	MAX_VALUE = 5;
	
	/** The maximum number of value. */
	public static final int 	MIN_VALUE = 0;
	
	/** The collection of selected stars. */
	private List<Image>			plus;
	
	/** The collection of unselected stars. */
	private List<Image>			minus;
	
	/** The currently selected value. */
	private int					currentValue;
	
	/** The image used when the rate increases. */
	private Image 				selected;
	
	/** The image used when the rate decreases. */
	private Image 				unselected;
	
	/** The canvas hosting the rating stars. */
	private RatingCanvas		canvas;
	
	/** The selected size. */
	private int					size;
	
	/** Fills the {@link #plus} and {@link #minus} lists. */
	private void fillLists()
	{
		plus.clear();
		minus.clear();
		int n = MAX_VALUE-currentValue;
		for (int i = 0; i < n; i++)
			minus.add(unselected);
		for (int i = 0; i < currentValue; i++)
			plus.add(selected);
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		canvas = new RatingCanvas(this);
		plus = new ArrayList<Image>();
		minus = new ArrayList<Image>();
		IconManager icons = IconManager.getInstance();
		switch (size) {
			case HIGH_SIZE:
				selected = icons.getImageIcon(
							IconManager.START_SELECTED).getImage();
				unselected = icons.getImageIcon(
								IconManager.START_UNSELECTED).getImage();
				break;
			case MEDIUM_SIZE:
				selected = icons.getImageIcon(
						IconManager.START_SELECTED_12).getImage();
				unselected = icons.getImageIcon(
								IconManager.START_UNSELECTED_12).getImage();
				break;
			case SMALL_SIZE:
				selected = icons.getImageIcon(
								IconManager.START_SELECTED_8).getImage();
				unselected = icons.getImageIcon(
						IconManager.START_UNSELECTED_8).getImage();
		}
		int w = selected.getWidth(null);
		int h = selected.getHeight(null);
		canvas.setPreferredSize(
				new Dimension(w*MAX_VALUE+(MAX_VALUE-1)*RatingCanvas.SPACE, 
						h+2*RatingCanvas.SPACE));
		fillLists();
	}
	
	/** 
	 * Builds and lays out the UI.
	 * 
	 * @param hasListeners 	Pass <code>true</code> to install the listeners,
	 * 						<code>false</code> otherwise. 
	 */
	private void buildGUI(boolean hasListeners)
	{
		setBorder(null);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(canvas);
		//add(Box.createHorizontalStrut(5));
		if (!hasListeners) canvas.setEnabled(false);
	}
	
	/** Creates a default instance. */
	public RatingComponent()
	{
		this(0, HIGH_SIZE, true);
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param size The size of the stars. One of the constants
	 * 					defined by this class.
	 */
	public RatingComponent(int size)
	{
		this(0, size, true);
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param selected	The number of stars.
	 * @param size		The size of the stars. One of the constants
	 * 					defined by this class.
	 */
	public RatingComponent(int selected, int size)
	{
		this(selected, size, true);
	}
	
	/**
	 * Creates a new instance with default value equals to <code>0</code>
	 * 
	 * @param size			The size of the stars. One of the constants
	 * 						defined by this class.
	 * @param hasListeners 	Pass <code>true</code> to install the listeners,
	 * 						<code>false</code> otherwise.
	 */
	public RatingComponent(int size, boolean hasListeners)
	{
		this(0, size, hasListeners);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param selected		The number of stars.
	 * @param size			The size of the stars. One of the constants
	 * 						defined by this class.
	 * @param hasListeners 	Pass <code>true</code> to install the listeners,
	 * 						<code>false</code> otherwise.
	 */
	public RatingComponent(int selected, int size, boolean hasListeners)
	{
		if (selected < MIN_VALUE) selected = MIN_VALUE;
		if (selected > MAX_VALUE) selected = MAX_VALUE;
		switch (size) {
			case HIGH_SIZE:
			case MEDIUM_SIZE:
			case SMALL_SIZE:
				this.size = size;
				break;
			default:
				this.size = SMALL_SIZE;
		}
		currentValue = selected;
		initialize();
		buildGUI(hasListeners);
	}
	
	/** 
	 * Returns the collection of selected stars.
	 * 
	 * @return See above.
	 */
	List<Image> getPlus() { return plus; }

	/** 
	 * Returns the collection of unselected stars.
	 * 
	 * @return See above.
	 */
	List<Image> getMinus() { return minus; }
	
	/** Invokes when the mouse is released i.e. the dragging has ended.*/
	void onMouseReleased()
	{
		firePropertyChange(RATE_END_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Sets the value.
	 * 
	 * @param value The current value.
	 */
	public void setValue(int value)
	{
		if (value < MIN_VALUE || value > MAX_VALUE) return;
		int oldValue = currentValue;
		currentValue = value;
		fillLists();
		canvas.repaint();
		firePropertyChange(RATE_PROPERTY, oldValue, currentValue);
	}
	
	/** 
	 * Returns the currently selected value.
	 * 
	 * @return See above.
	 */
	public int getCurrentValue()
	{
		if (currentValue < MIN_VALUE) return MIN_VALUE;
		if (currentValue > MAX_VALUE) return MAX_VALUE;
		return currentValue;
	}
	
	/**
	 * Overridden to set the background of the canvas.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color bg)
	{
		super.setBackground(bg);
		if (canvas != null) canvas.setBackground(bg);
	}
	
	/** 
	 * Overridden to add or remove the listeners
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		canvas.setEnabled(enabled);
	}
	
}
