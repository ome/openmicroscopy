/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.ChannelSlider 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import pojos.ChannelData;

/** 
 * Component uses to select the pixels intensity interval for the hosted
 * channel.
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
class ChannelSlider 
	extends JPanel
	implements PropertyChangeListener
{

	/** The default color. */
	static final Color		GRADIENT_COLOR = Color.BLACK;
	
	/** The default size of the button. */
	private static final Dimension	DEFAULT_SIZE = new Dimension(20, 20);
	
	/** Reference to the model. */
	private RendererModel 			model;
	
	/** Reference to the control. */
	private RendererControl 		controller;
	
	/** The reference to the parent hosting the component. */
	private GraphicsPane 			uiParent;
	
	/** Reference to the channel. */
	private ChannelData 			channel;
	
	/** Selection slider. */
	private TextualTwoKnobsSlider 	slider;

	/** Turn on/off the channel, when used in the viewer. */
	private ChannelButton			channelSelection;
	
	/** Button for opening the color picker */
	private JButton colorPicker;
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		final int index = channel.getIndex();
		int f = model.getRoundFactor(index);
    	int s = (int) (model.getWindowStart(index)*f);
        int e = (int) (model.getWindowEnd(index)*f);
        int min = (int) (channel.getGlobalMin()*f);
        int max = (int) (channel.getGlobalMax()*f);
        slider = new TextualTwoKnobsSlider();
        slider.layoutComponents(
        		TextualTwoKnobsSlider.LAYOUT_SLIDER_FIELDS_X_AXIS);
        slider.setBackground(UIUtilities.BACKGROUND_COLOR);

        int absMin = (int) (model.getLowestValue(index)*f);
        int absMax = (int) (model.getHighestValue(index)*f);
        if (!channel.hasStats()) {
        	min = absMin;
        	max = absMax;
        }
        double range = (max-min)*GraphicsPane.RATIO;
        int lowestBound = (int) (min-range);
        //if (lowestBound < absMin) lowestBound = absMin;
        lowestBound = absMin;
        int highestBound = (int) (max+range);
        //if (highestBound > absMax) highestBound = absMax;
        highestBound = absMax;
        slider.setValues(max, min, highestBound, lowestBound,
        		max, min, s, e, f);
        
        slider.getSlider().setPaintLabels(false);
        slider.getSlider().setPaintEndLabels(false);
        slider.getSlider().setPaintTicks(false);
        slider.addPropertyChangeListener(this);
        Color c = model.getChannelColor(index);
        slider.setColourGradients(GRADIENT_COLOR, c);
 
        Font font = slider.getFont();
        slider.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
        List<String> list = new ArrayList<String>();
        list.add(channel.getChannelLabeling());
        list.add("min: "+min);
        list.add("max: "+max);
        slider.getSlider().setToolTipText(UIUtilities.formatToolTipText(list));
        
    	channelSelection = new ChannelButton(""+channel.getChannelLabeling(), c, index);
    	channelSelection.setPreferredSize(new Dimension(40,25));
    	channelSelection.setSelected(model.isChannelActive(index));
    	channelSelection.setRightClickSupported(false);
    	channelSelection.addPropertyChangeListener(controller);
        
    	colorPicker = new JButton(IconManager.getInstance().getIcon(IconManager.COLOR_PICKER));
    	colorPicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                controller.showColorPicker(index);
            }
        });
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		int w = 230;
                JPanel p = new JPanel();
                p.setBackground(UIUtilities.BACKGROUND_COLOR);
                p.setBorder(null);
                double size[][] = { { w }, // Columns
                        { TableLayout.PREFERRED } }; // Rows
                p.setLayout(new TableLayout(size));
                p.add(slider, "0, 0");
                setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
                add(channelSelection);
                add(p);
                add(colorPicker);
                setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param uiParent		Reference to the parent hosting the component.
	 * 						Mustn't be <code>null</code>.
	 * @param model   		Reference to the model.
	 * 						Mustn't be <code>null</code>.
	 * @param controller    Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 * @param channel		The channel this component is for.
	 */
	ChannelSlider(GraphicsPane uiParent, RendererModel model, 
			RendererControl controller, ChannelData channel)
	{
		if (uiParent == null)
			throw new IllegalArgumentException("UI cannot be null.");
		if (model == null)
			throw new IllegalArgumentException("Model cannot be null.");
		if (controller == null)
			throw new IllegalArgumentException("Control cannot be null.");
		this.uiParent = uiParent;
		this.model = model;
		this.controller = controller;
		this.channel = channel;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the number of columns.
	 * 
	 * @return See above.
	 */
	int getColumns() { return slider.getColumns(); }
	
	/**
	 * Sets the number of columns.
	 * 
	 * @param columns The value to set.
	 */
	void setColumns(int columns) { slider.setColumns(columns); }
	
	/**
	 * Returns the index of the channel.
	 * 
	 * @return See above.
	 */
	int getIndex() { return channel.getIndex(); }
	
	/**
	 * Sets the interval values.
	 * 
	 * @param s The lowest bound of the interval.
	 * @param e The upper bound of the interval.
	 */
	void setInterval(int s, int e)
	{
		slider.setInterval(s, e);
	}
	
    /** 
     * Modifies the input range of the channel sliders. 
     * 
     *  @param absolute Pass <code>true</code> to set it to the absolute value,
	 *  				<code>false</code> to the minimum and maximum.
	 */
	void setInputRange(boolean absolute)
	{
		int index = channel.getIndex();
		int f = model.getRoundFactor(index);
    	int s = (int) (model.getWindowStart(index)*f);
        int e = (int) (model.getWindowEnd(index)*f);
        int min = (int) (channel.getGlobalMin()*f);
        int max = (int) (channel.getGlobalMax()*f);
       
        int absMin = (int) (model.getLowestValue(index)*f);
        int absMax = (int) (model.getHighestValue(index)*f);
        if (absolute)
        	slider.getSlider().setValues(absMax, absMin, absMax, absMin, s, e);
        else 
        	slider.getSlider().setValues(max, min, max, min, s, e);
	}
	
	
	/** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
    	 slider.setColourGradients(GRADIENT_COLOR, 
    			 model.getChannelColor(getIndex()));
    	 setSelectedChannel();
    }
    
    /** Modifies the color of the channel. */
    void setChannelColor()
    {
    	Color c = model.getChannelColor(getIndex());
    	slider.setColourGradients(GRADIENT_COLOR, c);
    	if (channelSelection != null) channelSelection.setColor(c);
    }
    
    /** Indicates that the channel is selected. */
    void setSelectedChannel()
    {
    	if (channelSelection == null) return;
    	channelSelection.setSelected(model.isChannelActive(getIndex()));
    	channelSelection.setColor(model.getChannelColor(getIndex()));
    	channelSelection.setGrayedOut(
				 Renderer.GREY_SCALE_MODEL.equals(model.getColorModel()));
    }
    
	/**
	 * Reacts to property changes fired by the {@link #slider}.
	 * 
	 * @param evt The event to handle.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (uiParent.isLiveUpdate()) {
			if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)
					|| TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name) ||
					TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
				controller.setInputInterval(slider.getStartValue(),
						slider.getEndValue(), channel.getIndex());
			}
		} else {
			if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
				controller.setInputInterval(slider.getStartValue(),
						slider.getEndValue(), channel.getIndex());
			} 
		}
	}

}
