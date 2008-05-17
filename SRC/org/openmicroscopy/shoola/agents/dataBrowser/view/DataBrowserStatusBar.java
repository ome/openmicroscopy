/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserStatusBar 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * Component used as a tool bar.
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
class DataBrowserStatusBar 
	extends JPanel
	implements ChangeListener
{

	/** The factor to use to set the magnification factor. */
	private static final int	FACTOR = 10;

	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Slider to zoom the nodes. */
	private OneKnobSlider		zoomSlider;
	
    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;

    /** Displays the status message. */
    private JLabel              status;
    
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		
		zoomSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 
				(int) (Thumbnail.MIN_SCALING_FACTOR*FACTOR), 
				(int) (Thumbnail.MAX_SCALING_FACTOR*FACTOR), 
				(int) (Thumbnail.SCALING_FACTOR*FACTOR));
		zoomSlider.setEnabled(true);
		zoomSlider.setShowArrows(true);
		zoomSlider.setToolTipText("Magnifies all thumbnails.");
		zoomSlider.setArrowsImageIcon(
				icons.getImageIcon(IconManager.ZOOM_IN), 
				icons.getImageIcon(IconManager.ZOOM_OUT));
		zoomSlider.addChangeListener(this);
		progressBar = new JProgressBar();
        status = new JLabel();
		
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
        left.add(zoomSlider);
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
	 */
	void setSelectedViewIndex(int index)
	{
		zoomSlider.setEnabled(index == DataBrowserUI.THUMB_VIEW);
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
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>.
     * @param perc  The value to set.
     */
    void setProgress(boolean hide, int perc)
    {
        progressBar.setVisible(!hide);
        if (perc < 0) progressBar.setIndeterminate(true);
        else {
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
    }
    
	/** 
	 * Zooms in or out the thumbnails.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (src == zoomSlider) {
			int v = zoomSlider.getValue();
			view.setMagnificationFactor((double) v/FACTOR);
		}
	}
	
}
