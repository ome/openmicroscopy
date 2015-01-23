/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.ChannelSlider 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
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
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		final int index = channel.getIndex();
		double s = model.getWindowStart(index);
		double e = model.getWindowEnd(index);
		double min = channel.getGlobalMin();
		double max = channel.getGlobalMax();
        
		boolean intMode = model.isIntegerPixelData();
		
		if (intMode) {
		        int absMin = (int) (model.getLowestValue(index));
		        int absMax = (int) (model.getHighestValue(index));
		        if (!channel.hasStats()) {
		                min = absMin;
		                max = absMax;
		        }
		        int lowestBound = absMin;
		        int highestBound = absMax;

		        slider = new TextualTwoKnobsSlider(0, 100);
		        
		        slider.layoutComponents(
                                TextualTwoKnobsSlider.LAYOUT_SLIDER_FIELDS_X_AXIS);
                        slider.setBackground(UIUtilities.BACKGROUND_COLOR);
                        
                        slider.setValues((int)max, (int)min, (int)highestBound, (int)lowestBound,
                                (int)max, (int)min, (int)s, (int)e);
                        
                        slider.layoutComponents(
                                TextualTwoKnobsSlider.LAYOUT_SLIDER_FIELDS_X_AXIS);
                        slider.setBackground(UIUtilities.BACKGROUND_COLOR);
		}
		else {
		    double absMin = model.getLowestValue(index);
		    double absMax = model.getHighestValue(index);
                    if (!channel.hasStats()) {
                            min = absMin;
                            max = absMax;
                    }
                    
                    double lowestBound = absMin;
                    double highestBound = absMax;

                    slider = new TextualTwoKnobsSlider(lowestBound, highestBound);
                    
                    slider.layoutComponents(
                            TextualTwoKnobsSlider.LAYOUT_SLIDER_FIELDS_X_AXIS);
                    slider.setBackground(UIUtilities.BACKGROUND_COLOR);
                    
                    slider.setValues(max, min, highestBound, lowestBound,
                            max, min, s, e);
                    
                    slider.layoutComponents(
                            TextualTwoKnobsSlider.LAYOUT_SLIDER_FIELDS_X_AXIS);
                    slider.setBackground(UIUtilities.BACKGROUND_COLOR);
		}
        
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
        
        if (!model.isGeneralIndex()) {
        	channelSelection = new ChannelButton("", c, index);
        	channelSelection.setPreferredSize(DEFAULT_SIZE);
        	channelSelection.setSelected(model.isChannelActive(index));
        	channelSelection.addPropertyChangeListener(controller);
        }
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		int w = 320;
		if (model.isGeneralIndex()) {
			double size[][] = {{w},{TableLayout.PREFERRED}}; // Rows
			setLayout(new TableLayout(size));
			add(slider, "0, 0");
		} else {
			JPanel p = new JPanel();
			p.setBorder(null);
			double size[][] = {{w},  // Columns
	    	{TableLayout.PREFERRED}}; // Rows
			p.setLayout(new TableLayout(size));
			p.add(slider, "0, 0");
			Dimension d = slider.getPreferredSize();
			channelSelection.setPreferredSize(new Dimension(d.height, d.height));
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			add(channelSelection);
			add(p);
			setBackground(p.getBackground());
		}
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
	void setInterval(double s, double e)
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
		double s = model.getWindowStart(index);
    	double e = model.getWindowEnd(index);
    	double min = channel.getGlobalMin();
    	double max = channel.getGlobalMax();
       
    	double absMin = model.getLowestValue(index);
    	double absMax = model.getHighestValue(index);
    	
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
