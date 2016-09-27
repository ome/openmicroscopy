/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ChannelColorMenuItem 
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
package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColorMenuItem;

/** 
 * Customized button used to display the color of the selected channel 
 * and bring up the <code>ColorPicker</code>.
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
public class ChannelColorMenuItem
	extends ColorMenuItem
	implements ActionListener
{

    /** 
     * Bound property name indicating that the channel is mapped to a new color. 
     */
    public static final String  CHANNEL_COLOR_PROPERTY = "channelColor";
    
    /** The index of the channel. */
    private final int	 index;
    
    /**
     * Creates a new instance.
     * 
     * @param text		The text of the button. The text should correspond to
     *                  the emission wavelength, fluor used or the index.
     * @param color     The background color of the button. Corresponds to the
     *                  color associated to the channel.
     * @param index     The channel index.
     */
	public ChannelColorMenuItem(String text, Color color, int index)
	{
		super(text, color);
		this.index = index;
		addActionListener(this);
	}

    /**
     * Creates a new instance.
     * 
     * @param text
     *            The text of the button. The text should correspond to the
     *            emission wavelength, fluor used or the index.
     * @param lut
     *            The lookup table of the button.
     * @param index
     *            The channel index.
     */
    public ChannelColorMenuItem(String text, String lut, int index) {
        super(text, lut);
        this.index = index;
        addActionListener(this);
    }
	
	/**
	 * Fires property change indicating to bring up the color picker.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		firePropertyChange(CHANNEL_COLOR_PROPERTY, null, 
				Integer.valueOf(index));
	}
	
}
