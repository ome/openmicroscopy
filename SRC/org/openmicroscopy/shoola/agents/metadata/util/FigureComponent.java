/*
 * org.openmicroscopy.shoola.agents.metadata.util.FigureComponent 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.util.image.geom.Factory;
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
class FigureComponent
	extends JPanel
	implements PropertyChangeListener
{

	/** The default size of the button. */
	static final Dimension	DEFAULT_SIZE = new Dimension(16, 16);

	/** Reference to the canvas displaying the image. */
	private FigureCanvas 			canvas;
	
	/** The layered pane hosting the canvas. */
	private JLayeredPane			pane;
	
	/** 
	 * The component displaying the label of the channel so that
	 * the use can edit it.
	 */
	private JTextField				field;
	
	/** The component used instead of the text field. */
	private JCheckBox				box;
	
	/** The channel buttons. */
	private List<ChannelButton>		buttons;
	
	/** The image associated to that channel. */
	private BufferedImage 			image;
	
	/** Reference to the grey version of the image. */
	private BufferedImage			greyImage;
	
	/** The image associated to that channel. */
	private BufferedImage 			displayedImage;

	/** Reference to the model. */
	private FigureDialog 			model;
	
	/** 
	 * Flag indicating that the component is for a single channel
	 * if set to <code>true</code>, <code>false</code> otherwise.
	 */
	private boolean					single;
	
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
		canvas = new FigureCanvas();
		pane = new JLayeredPane();
		pane.add(canvas, Integer.valueOf(0));
		box = new JCheckBox("Channel names");
		box.setToolTipText("Label the merged panel with channel names " +
				"if selected. Otherwise label with 'Merged'.");
		box.setBorder(null);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		Iterator<ChannelButton> i = buttons.iterator();
		while (i.hasNext()) {
			p.add(i.next());
			p.add(Box.createHorizontalStrut(2));
		}
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		if (!single) {
			//JPanel pBox = UIUtilities.buildComponentPanel(box);
			//pBox.setBorder(null);
			controls.add(UIUtilities.buildComponentPanel(box, 5, 4));
		} else controls.add(field);
		controls.add(UIUtilities.buildComponentPanel(p));
		//controls.add(UIUtilities.buildComponentPanelCenter(pane));
		setLayout(new BorderLayout(0, 0));
		add(UIUtilities.buildComponentPanel(controls, 0, 0),
				BorderLayout.NORTH);
		add(UIUtilities.buildComponentPanelCenter(pane), BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model  Reference to the model.
	 * @param color  The color associated to the channel.
	 * @param name	 The name to give to the channel.
	 * @param index  The index of the channel.
	 */
	FigureComponent(FigureDialog model, Color color, String name,
			int index)
	{
		this.model = model;
		single = true;
		ChannelButton b = new ChannelButton("", color, index);
		b.setPreferredSize(DEFAULT_SIZE);
		b.addPropertyChangeListener(this);
		buttons = new ArrayList<ChannelButton>();
		buttons.add(b);
		initComponents(name);
		buildGUI();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model  	Reference to the model.
	 * @param channels  The channels to handle.
	 */
	FigureComponent(FigureDialog model, List<ChannelButton> channels)
	{
		this.model = model;
		single = false;
		buttons = channels;
		initComponents(FigureParam.MERGED_TEXT);
		buildGUI();
	}
	
	/**
	 * Adds the passed component to the layer.
	 * 
	 * @param component The component to add.
	 */
	void addToView(JComponent component)
	{
		if (component != null) {
			Dimension d = pane.getPreferredSize();
			component.setPreferredSize(d);
			component.setSize(d);
			pane.add(component, Integer.valueOf(1));
		}
	}
	
	/**
	 * Returns the label associated to the component.
	 * 
	 * @return See above.
	 */
	String getLabel()
	{ 
		String value = field.getText(); 
		if (value == null || value.trim().length() == 0) {
			if (single) {
				ChannelButton b = buttons.get(0);
				return ""+b.getChannelIndex();
			}
			return FigureParam.MERGED_TEXT;
		}
			
		return value.trim(); 
	}
	
	/**
	 * Returns <code>true</code> if the names of the channels are merged
	 * and displayed next to the merged image, <code>false</code> to
	 * display the default text i.e. {@link FigureParam#MERGED_TEXT}.
	 * 
	 * @return See above.
	 */
	boolean isChannelsName() 
	{
		if (single) return false;
		return box.isSelected();
 	}
	
	/**
	 * Resets the image.
	 * 
	 * @param grey  Pass <code>true</code> to reset the image as grey,
	 * 				<code>false</code> otherwise.
	 */
	void resetImage(boolean grey)
	{
		if (image == null || !single) return;
		ChannelButton button = buttons.get(0);
		Color color = button.getColor();
		if (grey) {
			if (greyImage == null) {
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				int mask = -1;
				//red
				if (r == 255 && g == 0 && b == 0) 
					mask = Factory.RED_MASK;
				else if (r == 0 && g == 255 && b == 0) 
					mask = Factory.GREEN_MASK;
				else if (r == 0 && g == 0 && b == 255) 
					mask = Factory.BLUE_MASK;
				if (mask != -1) { //not primary color
					DataBuffer buf = image.getRaster().getDataBuffer();
					greyImage = Factory.createBandImage(buf, 
							image.getWidth(), 
							image.getHeight(), mask, mask, mask);
				} else {
					greyImage = model.createSingleGreyScaleImage(
							button.getChannelIndex());
				}
			}
			displayedImage = greyImage;
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
		greyImage = null;
		canvas.setImage(image);
	}

	/**
	 * Sets the size of the canvas.
	 * 
	 * @param width	 The width to set.
	 * @param height The height to set.
	 */
	void setCanvasSize(int width, int height)
	{
		Dimension d = new Dimension(width, height);
		pane.setPreferredSize(d);
	    pane.setSize(d);
		//canvas.setPreferredSize(d);
		//canvas.setSize(d);
		Component[] comps = pane.getComponents();
		for (int i = 0; i < comps.length; i++) {
			comps[i].setPreferredSize(d);
			comps[i].setSize(d);
		}
	}
	
	/**
	 * Returns <code>true</code> if the component is selected, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSelected()
	{ 
		if (buttons.size() != 1) return false;
		ChannelButton b = buttons.get(0);
		return b.isSelected();
	}
	
	/**
	 * Selects or not the selection box.
	 * 
	 * @param selected The value to set.
	 */
	void setSelected(boolean selected)
	{ 
		if (buttons.size() != 1) return;
		ChannelButton b = buttons.get(0);
		b.setSelected(selected);
		b.repaint();
	}
	
	/**
	 * Returns the collection of channels associated to this component.
	 * 
	 * @return See above.
	 */
	List<ChannelButton> getChannels() { return buttons; }
	
	/**
	 * Returns the displayed image.
	 * 
	 * @return See above.
	 */
	BufferedImage getDisplayedImage() { return displayedImage; }
	
	/**
	 * Overridden to set the enabled flag of the selection box.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		field.setEnabled(enabled);
	}
	
	/**
	 * Selects the channel and updates the canvas.
	 * @see PropertyChangeListener#propertyChange(ChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(name)) {
			if (single) {
				Map m = (Map) evt.getNewValue();
				ChannelButton button = buttons.get(0);
				Boolean b = (Boolean) m.get(button.getChannelIndex());
				if (b != null) {
					button.setSelected(b);
					model.setChannelSelection(button.getChannelIndex(), b, 
							false);
					canvas.setImageVisible(b);
				}
			}
		}
	}

}
