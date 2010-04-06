/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.ChannelSlider 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
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
	static final Color	GRADIENT_COLOR = Color.BLACK;
	
	/** Reference to the model. */
	private RendererModel 		model;
	
	/** Reference to the control. */
	private RendererControl 	controller;
	
	/** The reference to the parent hosting the component. */
	private GraphicsPane 		uiParent;
	
	/** Reference to the channel. */
	private ChannelData 		channel;
	
	/** Selection slider. */
	private TextualTwoKnobsSlider 	slider;

	/** Initializes the component composing the display. */
	private void initComponents()
	{
		int index = channel.getIndex();
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
        double range = (max-min)*GraphicsPane.RATIO;
        int lowestBound = (int) (min-range);
        if (lowestBound < absMin) lowestBound = absMin;
        int highestBound = (int) (max+range);
        if (highestBound > absMax) highestBound = absMax;
        //domainSlider.setValues(highestBound, lowestBound, max, min, s, e);
        slider.setValues(max, min, highestBound, lowestBound,
        		max, min, s, e, f);
        
        slider.getSlider().setPaintLabels(false);
        slider.getSlider().setPaintEndLabels(false);
        slider.getSlider().setPaintTicks(false);
        slider.addPropertyChangeListener(this);
        slider.setColourGradients(GRADIENT_COLOR, model.getChannelColor(index));
        
        
        
        Font font = slider.getFont();
        slider.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
        List<String> list = new ArrayList<String>();
        list.add(channel.getChannelLabeling());
        list.add("min: "+min);
        list.add("max: "+max);
        slider.getSlider().setToolTipText(UIUtilities.formatToolTipText(list));
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(slider);
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
	
	/** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
    	 slider.setColourGradients(GRADIENT_COLOR, 
    			 model.getChannelColor(getIndex()));
    }
    
    /** Modifies the color of the channel. */
    void setChannelColor()
    {
    	slider.setColourGradients(GRADIENT_COLOR, 
   			 model.getChannelColor(getIndex()));
    }
    
	/**
	 * Reacts to property changes fired by the {@link #slider}.
	 * 
	 * @param evt The event to handle.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (uiParent.isPreviewSelected()) {
			if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)
					|| TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
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
