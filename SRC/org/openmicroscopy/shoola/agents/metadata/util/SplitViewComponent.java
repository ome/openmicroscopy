/*
 * org.openmicroscopy.shoola.agents.metadata.util.SplitViewComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.util;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.ColourIcon;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component displaying a given channel.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class SplitViewComponent
	extends JPanel
	implements ChangeListener
{

	/** The size of the button. */
	private static final Dimension BUTTON_SIZE = new Dimension(16, 16);
	
	/** Reference to the canvas displaying the image. */
	private SplitViewCanvas canvas;
	
	/** 
	 * The component displaying the label of the channel so that
	 * the use can edit it.
	 */
	private JTextField		field;
	
	/** The box to remove the channel from the split. */
	private JCheckBox	  	box;
	
	/** The color associated to the channel. */
	private JLabel			colorLabel;
	
	/** The image associated to that channel. */
	private BufferedImage 	image;
	
	/** The image associated to that channel. */
	private BufferedImage 	displayedImage;
	
	/** The color associated to the channel. */
	private Color			color;
	
	/** 
	 * Initializes the components composing the display.
	 *
	 * @param name The name to give to the channel.
	 */
	private void initComponents(String name)
	{
		field = new JTextField(name);
		Font f = field.getFont();
		field.setFont(f.deriveFont(f.getStyle(), ChannelButton.MIN_FONT_SIZE));
		field.setColumns(8);
		canvas = new SplitViewCanvas();
		ColourIcon icon = new ColourIcon(color);
		icon.paintLineBorder(true);
		box = new JCheckBox();
		colorLabel = new JLabel(icon);
		box.setSelected(true);
		box.addChangeListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.add(colorLabel);
		p.add(box);
		p.add(field);
		setLayout(new BorderLayout(0, 0));
		add(p, BorderLayout.NORTH);
		add(UIUtilities.buildComponentPanelCenter(canvas), BorderLayout.CENTER);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param color  The color associated to the channel the channel.
	 * @param name	 The name to give to the channel.
	 */
	SplitViewComponent(Color color, String name)
	{
		this.color = color;
		initComponents(name);
		buildGUI();
	}
	
	/**
	 * Returns the label associated to the channel.
	 * 
	 * @return See above.
	 */
	String getChannelLabel() { return field.getText(); }
	
	/**
	 * Resets the image.
	 * 
	 * @param grey  Pass <code>true</code> to reset the image as grey,
	 * 				<code>false</code> otherwise.
	 */
	void resetImage(boolean grey)
	{
		if (grey) {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
			int mask = -1;
			//red
			if (r == 255 && g == 0 && b == 0) mask = Factory.RED_MASK;
			else if (r == 0 && g == 255 && b == 0) mask = Factory.GREEN_MASK;
			else if (r == 0 && g == 0 && b == 255) mask = Factory.BLUE_MASK;
			if (mask != -1) {
				DataBuffer buf = image.getRaster().getDataBuffer();
				displayedImage = Factory.createBandImage(buf, image.getWidth(), 
						image.getHeight(), mask, mask, mask);
			}
		} else 
			displayedImage = image;
		canvas.setImage(displayedImage);
	}
	
	/**
	 * Sets the image. The original image should always be a colored image.
	 * 
	 * @param image The value to set.
	 */
	void setOriginalImage(BufferedImage image)
	{
		this.image = image;
		displayedImage = image;
		canvas.setImage(image);
		if (image != null) {
			Dimension d = new Dimension(image.getWidth(), image.getHeight());
			canvas.setPreferredSize(d);
		}
	}

	/**
	 * Listens to the check box selection.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (!(e.getSource() instanceof JCheckBox)) return;
		if (box.isSelected()) canvas.setImage(displayedImage);
		else canvas.setImage(null);
	}

}
