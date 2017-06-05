/*
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


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXBusyLabel;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.util.ui.MagnificationComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * The tool bar of {@link DataBrowser} displaying wells. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class DataBrowserWellToolBar
	extends JPanel
	implements ActionListener
{

	/** ID to bring up the add thumbnail view to the node.. */
	private static final int	ROLL_OVER = 0;
	/** The factor to use to set the magnification factor. */
    private static final int FACTOR = 10;
	
	/** Reference to the control. */
	private DataBrowserControl 	controller;

	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Button to refresh the display. */
	private JButton				refreshButton;
	
	/** 
	 * Button to display a magnified thumbnail if selected when 
	 * the user mouses over a node.
	 */
	private JToggleButton 		rollOverButton;
	
	/** Displays the possible fields per well. */
	private JComboBox			fields;
	
	/** The fields indicating the loading state of the field. */
	private JXBusyLabel			busyLabel;
	
	/** The component displaying the magnification factor. */
    private MagnificationComponent mag;

    /** Slider to zoom the fields . */
    private OneKnobSlider zoomSlider;
    
    /** Changelistener for the zoom slider */
    private ChangeListener zoomListener;
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		rollOverButton = new JToggleButton();
		rollOverButton.setIcon(icons.getIcon(IconManager.ROLL_OVER));
		rollOverButton.setToolTipText("Turn on/off the magnification " +
				"of a thumbnail while mousing over it.");
		rollOverButton.addActionListener(this);
		rollOverButton.setActionCommand(""+ROLL_OVER);
		busyLabel = new JXBusyLabel(new Dimension(
				UIUtilities.DEFAULT_ICON_WIDTH, 
				UIUtilities.DEFAULT_ICON_HEIGHT));
		busyLabel.setVisible(false);
		refreshButton = new JButton(controller.getAction(
				DataBrowserControl.REFRESH));
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		int f = view.getFieldsNumber();
		if (f > 1) { 
			String[] values = new String[f];
 			for (int i = 0; i < f; i++) 
				values[i] = "Field #"+(i+1);
 			fields = new JComboBox(values);
 			fields.setSelectedIndex(view.getSelectedField());
 			fields.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					controller.viewField(fields.getSelectedIndex());
				}
			
			});
		}

        double scale = DataBrowserFactory.getThumbnailScaleFactor();

        mag = new MagnificationComponent(Thumbnail.MIN_SCALING_FACTOR,
                Thumbnail.MAX_SCALING_FACTOR, scale);
        mag.addPropertyChangeListener(
                MagnificationComponent.MAGNIFICATION_PROPERTY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String name = evt.getPropertyName();
                        if (MagnificationComponent.MAGNIFICATION_PROPERTY
                                .equals(name)) {
                            double v = (Double) evt.getNewValue();
                            view.setMagnificationFactor(v);
                            int value = (int) (v * FACTOR);
                            zoomSlider.removeChangeListener(zoomListener);
                            zoomSlider.setValue(value);
                            zoomSlider.addChangeListener(zoomListener);
                        }
                    }
                });

        zoomSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL,
                (int) (Thumbnail.MIN_SCALING_FACTOR * FACTOR),
                (int) (Thumbnail.MAX_SCALING_FACTOR * FACTOR),
                (int) (scale * FACTOR));
        zoomListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Object src = e.getSource();
                if (src == zoomSlider) {
                    int v = zoomSlider.getValue();
                    double f = (double) v / FACTOR;
                    view.setMagnificationFactor(f);
                    firePropertyChange(
                            MagnificationComponent.MAGNIFICATION_UPDATE_PROPERTY,
                            null, f);
                    DataBrowserFactory.setThumbnailScaleFactor(f);
                }
            }
        };
        zoomSlider.addChangeListener(zoomListener);
        zoomSlider.setToolTipText("Magnifies the thumbnails.");
	}
	
	/**
	 * Builds the tool bar with the various control for the view.
	 * 
	 * @return See above.
	 */
	private JToolBar buildViewsBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setRollover(true);
		bar.add(refreshButton);
		bar.add(rollOverButton);
		if (fields != null) { 
			bar.add(fields);
			bar.add(Box.createHorizontalStrut(5));
            bar.add(mag);
            bar.add(Box.createHorizontalStrut(5));
            bar.add(zoomSlider);
			bar.add(Box.createHorizontalStrut(5));
			bar.add(busyLabel);
		}
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.add(buildViewsBar());
		content.add(p);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(content);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 			Reference to the view. Mustn't be <code>null</code>.
	 * @param controller 	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	DataBrowserWellToolBar(DataBrowserUI view, DataBrowserControl controller)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.controller = controller;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Indicates the status of the fields loading.
	 * 
	 * @param busy  Pass <code>true</code> when the fields are loading,
	 * 				<code>false</code> when they are loaded.
	 */
	void setStatus(boolean busy)
	{
		busyLabel.setVisible(busy);
		busyLabel.setBusy(busy);
	}
	
	/**
	 * Shows or hides the options only available in the fields view.
	 * 
	 * @param show  Pass <code>true</code> to show the options, 
	 * 				<code>false</code> to hide them.
	 */
	void displayFieldsOptions(boolean show)
	{
		refreshButton.setEnabled(!show);
		if (fields != null) 
		    fields.setEnabled(show);
	}
	
	/** 
	 * Sets the specified view.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case ROLL_OVER:
				//view.setRollOver(rollOverItem.isSelected());
				view.setRollOver(rollOverButton.isSelected());
				break;
		}
	}
	
}
