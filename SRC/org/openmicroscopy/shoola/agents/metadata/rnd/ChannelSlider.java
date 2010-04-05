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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColouredButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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

	/** The dimension of the icon. */
	private static final Dimension ICON_DIMENSION = new Dimension(14, 14);
	
	/** Reference to the model. */
	private RendererModel 	model;
	
	/** Reference to the control. */
	private RendererControl controller;
	
	/** Reference to the channel. */
	private ChannelData 	channel;
	
	/** Selection slider. */
	private TwoKnobsSlider 	slider;
	
	/** Component displaying the color associated to the channel. */
	private ColouredButton 	icon;
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		int index = channel.getIndex();
		icon = new ColouredButton("", model.getChannelColor(index));
		icon.setPreferredSize(ICON_DIMENSION);
		int f = model.getRoundFactor(index);
    	int s = (int) (model.getWindowStart(index)*f);
        int e = (int) (model.getWindowEnd(index)*f);
        int min = (int) (channel.getGlobalMin()*f);
        int max = (int) (channel.getGlobalMax()*f);
        slider = new TwoKnobsSlider(min, max, min, max, s, e);
        Font font = slider.getFont();
        slider.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
        slider.setBackground(UIUtilities.BACKGROUND_COLOR);
        slider.setPaintLabels(false);
       // slider.setPaintEndLabels(false);
        slider.setPaintTicks(false);
        slider.addPropertyChangeListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel p = new JPanel();
		setBorder(null);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(icon);
		p.add(slider);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(p);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model   		Reference to the model. Mustn't be <code>null</code>.
	 * @param controller    Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 * @param channel		The channel this component is for.
	 */
	ChannelSlider(RendererModel model, RendererControl controller, 
			ChannelData channel)
	{
		if (model == null)
			throw new IllegalArgumentException("Model cannot be null.");
		if (controller == null)
			throw new IllegalArgumentException("Control cannot be null.");
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
		slider.removePropertyChangeListener(this);
		slider.setStartValue(s);
		slider.setEndValue(e);
		slider.addPropertyChangeListener(this);
	}
	
	/** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
    	icon.setColor(model.getChannelColor(getIndex()));
    	icon.setGrayedOut(model.isGreyScale());
    }
    
    /** Modifies the color of the channel. */
    void setChannelColor()
    {
    	boolean gs = model.isGreyScale();
    	icon.setColor(model.getChannelColor(getIndex()));
		 if (gs) icon.setGrayedOut(gs);
    }
    
	/**
	 * Reacts to property changes fired by the {@link #slider}.
	 * 
	 * @param evt The event to handle.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
			controller.setInputInterval(slider.getStartValue(),
					slider.getEndValue(), channel.getIndex());
		}
	}
	
}
