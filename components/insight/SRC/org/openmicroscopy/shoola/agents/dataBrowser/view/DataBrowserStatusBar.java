/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserStatusBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.util.ui.MagnificationComponent;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * Component used as a tool bar.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class DataBrowserStatusBar
	extends JPanel
	implements ChangeListener, PropertyChangeListener
{

	/** The factor to use to set the magnification factor. */
	private static final int FACTOR = 10;
    
	/** Reference to the view. */
	private DataBrowserUI view;
	
	/** Slider to zoom the fields . */
	private OneKnobSlider fieldsZoomSlider;

    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar progressBar;

    /** Displays the status message. */
    private JLabel status;

    /** The component displaying the magnification factor. */
    private MagnificationComponent mag;
    
	/** Initializes the components. */
	private void initComponents()
	{
	    double scale = view.getMagnificationFactor();
	    
		mag = new MagnificationComponent(Thumbnail.MIN_SCALING_FACTOR,
				Thumbnail.MAX_SCALING_FACTOR, scale);
		mag.addPropertyChangeListener(
				MagnificationComponent.MAGNIFICATION_PROPERTY, this);

		fieldsZoomSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL,
		        (int)(Thumbnail.MIN_SCALING_FACTOR*FACTOR),
                (int)(Thumbnail.MAX_SCALING_FACTOR*FACTOR),
                (int)(scale*FACTOR));
		
		fieldsZoomSlider.setToolTipText("Magnifies the thumbnails.");
	
		fieldsZoomSlider.addChangeListener(this);
		addPropertyChangeListener(
		        MagnificationComponent.MAGNIFICATION_UPDATE_PROPERTY, mag);
		
		progressBar = new JProgressBar();
        status = new JLabel();
		progressBar.setVisible(false);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel right = new JPanel();
		right.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(progressBar);
        JPanel left = new JPanel();
        left.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.add(mag);
        left.add(Box.createHorizontalStrut(5));
        left.add(fieldsZoomSlider);
        add(left);
		add(right);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 */
	DataBrowserStatusBar(DataBrowserUI view)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Sets the selected view index.
	 * 
	 * @param index The value to set.
	 * @param magnification The magnification.
	 */
	void setSelectedViewIndex(int index, double magnification)
	{
        mag.setOriginal(magnification);
        resetZoomSlider(magnification);
		removeAll();
		buildGUI();
	}

	/** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    void setStatus(String s) { status.setText(s); }

    /**
     * Sets the value of the progress bar.
     * 
     * @param hide Pass <code>true</code> to hide the progress bar,
     *              <code>false</otherwise>.
     * @param perc The value to set.
     */
    void setProgress(boolean hide, int perc)
    {
        progressBar.setVisible(!hide);
        if (perc < 0) { 
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
    }

    /**
     * Returns the magnification factor.
     * 
     * @return See above.
     */
    double getMagnificationFactor() { return mag.getMagnification(); }

	/** 
	 * Zooms in or out the thumbnails.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (src == fieldsZoomSlider) {
			int v = fieldsZoomSlider.getValue();
	    	double f = (double) v/FACTOR;
            if (view.wells()) {
                view.setFieldMagnificationFactor(f);
            } else {
                view.setMagnificationFactor(f);
                DataBrowserFactory.setThumbnailScaleFactor(f);
            }
		} 
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (MagnificationComponent.MAGNIFICATION_PROPERTY.equals(name)) {
            double v = (Double) evt.getNewValue();
            if (view.wells())
                view.setFieldMagnificationFactor(v);
            else
                view.setMagnificationFactor(v);
            resetZoomSlider(v);
        }
    }

    /**
     * Reset the zoom slider to the given value without triggering the
     * ChangeListener
     * 
     * @param value
     *            The value
     */
    private void resetZoomSlider(double value) {
        fieldsZoomSlider.removeChangeListener(this);
        fieldsZoomSlider.setValue((int) (value * FACTOR));
        fieldsZoomSlider.addChangeListener(this);
    }

}
