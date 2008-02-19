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
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
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

	/** The maximum number of value. */
	public static final int MAX = 5;
	
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
	
	/** The canvas hosting the rating starts. */
	private RatingCanvas		canvas;
	
	/** Fills the {@link #plus} and {@link #minus} lists. */
	private void fillLists()
	{
		plus.clear();
		minus.clear();
		int n = MAX-currentValue;
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
		selected = icons.getImageIcon(IconManager.START_SELECTED).getImage();
		unselected = 
				icons.getImageIcon(IconManager.START_UNSELECTED).getImage();
		int w = selected.getWidth(null);
		int h = selected.getHeight(null);
		canvas.setPreferredSize(
				new Dimension(w*MAX+(MAX-1)*RatingCanvas.SPACE, 
						h+2*RatingCanvas.SPACE));
		fillLists();
	}
	
	/** 
	 * Builds and lays out the UI.
	 * 
	 * @param showSpinner	Pass <code>true</code> if the spinner is shown
	 * 						<code>false</code> otherwise. 
	 */
	private void buildGUI(boolean showSpinner)
	{
		add(canvas);
		add(Box.createHorizontalStrut(5));
		if (!showSpinner) canvas.uninstallListeners();
	}
	
	/** Creates a default instance. */
	public RatingComponent()
	{
		this(0, true);
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param selected	The number of stars.
	 */
	public RatingComponent(int selected)
	{
		this(selected, true);
	}
	/**
	 * 
	 * @param showSpinner
	 */
	public RatingComponent(boolean showSpinner)
	{
		this(0, showSpinner);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param selected		The number of stars.
	 * @param showSpinner	Pass <code>true</code> if the spinner is shown
	 * 						<code>false</code> otherwise.
	 */
	public RatingComponent(int selected, boolean showSpinner)
	{
		if (selected < 0) selected = 0;
		if (selected > MAX) selected = MAX;
		currentValue = selected;
		initialize();
		buildGUI(showSpinner);
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

	/**
	 * Sets the value.
	 * 
	 * @param value The current value.
	 */
	void setValue(int value)
	{
		if (value < 0 || value > MAX) return;
		currentValue = value;
		fillLists();
		canvas.repaint();
	}
	
	/** Decreases the number of selected stars. */
	void decrease()
	{
		int v = currentValue;
		v--;
		if (v < 0) return;
		currentValue = v;
		fillLists();
		canvas.repaint();
	}
	
	/** Increases the number of selected stars. */
	void increase()
	{
		int v = currentValue;
		v++;
		if (v > MAX) return;
		currentValue = v;
		fillLists();
		canvas.repaint();
	}
	
}
