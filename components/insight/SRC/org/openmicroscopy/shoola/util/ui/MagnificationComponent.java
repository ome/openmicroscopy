/*
 * org.openmicroscopy.shoola.util.ui.MagnificationComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * Component offering a zoom in/zoom out, actual size. controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class MagnificationComponent
	extends JPanel
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating that the magnification has been changed. */
	public static final String MAGNIFICATION_PROPERTY = "magnification";
	
	/** Bound property indicating that the magnification has been changed. */
    public static final String MAGNIFICATION_UPDATE_PROPERTY =
            "magnificationUpdate";
    
	/** Default minimum value. */
	public static final double MINIMUM = 0.1;
	
	/** Default maximum value. */
	public static final double MAXIMUM = 2.0;
	
	/** The default magnification factor. */
	public static final double DEFAULT = 1.0;
	
	/** The tool tip of the zoom out action. */
	private static final String ZOOM_OUT_TEXT = "Zoom out";
	
	/** The tool tip of the zoom in action. */
	private static final String ZOOM_IN_TEXT = "Zoom in";
	
	/** The tool tip of the zoom out action. */
	private static final String ZOOM_FIT_TEXT = "Actual size";
	
	/** Action ID to reduce the magnification factor. */
	public static final int ZOOM_OUT = 0;
	
	/** Action ID to increase the magnification factor. */
	public static final int ZOOM_IN = 1;
	
	/** Action ID to increase the magnification factor. */
	public static final int ZOOM_ACTUAL_SIZE = 2;
	
	/** The factor by which the magnification is incremented. */
	private static final double INCREMENT = 0.10;
	
	/** The zoom out button. */
	private JButton zoomOut;
	
	/** The zoom in button. */
	private JButton zoomIn;
	
	/** The actual size button. */
	private JButton	actualSize;
	
	/** The minimum magnification factor. */
	private double min;
	
	/** The magnification magnification factor. */
	private double max;
	
	/** The original value. */
	private double originalValue;
	
	/** The current magnification value. */
	private double currentValue;
	
	/**
	 * Modifies the magnification factor according to the passed value.
	 * 
	 * @param increment Pass <code>true</code> to increment the value,
	 * 					<code>false</code> otherwise.
	 */
	private void setMagnification(boolean increment)
	{
		double v = currentValue;
		if (increment) {
			currentValue += INCREMENT;
			if (currentValue > max) currentValue = max;
		} else {
			currentValue -= INCREMENT;
			if (currentValue < min) currentValue = min;
		}
		firePropertyChange(MAGNIFICATION_PROPERTY, v, currentValue);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		zoomOut = new JButton(icons.getIcon(IconManager.ZOOM_OUT));
		zoomOut.setToolTipText(ZOOM_OUT_TEXT);
		zoomOut.setActionCommand(""+ZOOM_OUT);
		zoomOut.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(zoomOut);
		zoomIn = new JButton(icons.getIcon(IconManager.ZOOM_IN));
		zoomIn.setToolTipText(ZOOM_IN_TEXT);
		zoomIn.setActionCommand(""+ZOOM_IN);
		zoomIn.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(zoomIn);
		actualSize = new JButton(icons.getIcon(IconManager.ACTUAL_SIZE));
		actualSize.setToolTipText(ZOOM_FIT_TEXT);
		actualSize.setActionCommand(""+ZOOM_ACTUAL_SIZE);
		actualSize.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(actualSize);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JToolBar bar = new JToolBar();
		bar.setBorder(null);
		bar.setFloatable(false);
		bar.add(zoomOut);
		bar.add(zoomIn);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(actualSize);

		add(bar);
	}

	/** Creates a default instance. */
	public MagnificationComponent()
	{
		this(MINIMUM, MAXIMUM, DEFAULT);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param min The minimum value.
	 * @param max The maximum value.
	 */
	public MagnificationComponent(double min, double max)
	{
		this(min, max, DEFAULT);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @param value The original magnification.
	 */
	public MagnificationComponent(double min, double max, double value)
	{
		if (min <= 0) min = MINIMUM;
		if (max < DEFAULT) max = DEFAULT;
		this.max = max;
		this.min = min;
		setOriginal(value);
		initComponents();
		buildGUI();
	}

	/**
	 * Sets the original value.
	 * 
	 * @param value The value to set.
	 */
	public void setOriginal(double value)
	{
		if (value < min) value = min;
		if (value > max) value = max;
		originalValue = value;
		currentValue = originalValue;
	}

	/** 
	 * Returns the current magnification factor.
	 * 
	 * @return See above.
	 */
	public double getMagnification() { return currentValue; }
	
	/**
	 * Sets the icon for the specified button.
	 * 
	 * @param index The index corresponding to the button. One of the constants
	 * 				defined by this class.
	 * @param icon  The icon to set.
	 */
	public void setButtonIcon(int index, Icon icon)
	{
		if (icon == null) return;
		switch (index) {
			case ZOOM_ACTUAL_SIZE:
				actualSize.setIcon(icon);
				break;
			case ZOOM_IN:
				zoomIn.setIcon(icon);
				break;
			case ZOOM_OUT:
				zoomOut.setIcon(icon);
		}
	}

	/** 
	 * Sets the magnification factor.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case ZOOM_IN:
				setMagnification(true);
				break;
			case ZOOM_OUT:
				setMagnification(false);
				break;
			case ZOOM_ACTUAL_SIZE:
				double v = currentValue;
				currentValue = originalValue;
				firePropertyChange(MAGNIFICATION_PROPERTY, v, currentValue);
		}
	}

    /**
     * Updates the current value if modified by another component.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (MAGNIFICATION_UPDATE_PROPERTY.equals(name)) {
            currentValue = (Double) evt.getNewValue();
        }
    }

}
