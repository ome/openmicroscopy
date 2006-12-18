/*
 * org.openmicroscopy.shoola.util.ui.ChannelToggleButton.java
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColouredButton;

/** 
 * Customized button to select the channel whose rendering settings are
 * currently displayed.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
public class ChannelToggleButton 
    extends ColouredButton
{

    /** Bound property indicating that the channel is selected. */
	public static final String		CHANNEL_PICKED_PROPERTY = "channelPicked";

    /** The channel index. */
	private final int    index;
	
	/**
     * Creates a new instance.
     * 
	 * @param text      The text to display.
	 * @param color     The button's color. Mustn't be <code>null</code>.
     * @param i         The channel's index.
	 */
	public ChannelToggleButton(String text, Color color, int i)
    {
		super(text, color);
		index = i;
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            { 
                firePropertyChange(CHANNEL_PICKED_PROPERTY,new Integer(-1),
                       new Integer(index));
            }
        });
	}
    
    /**
     * Returns the index of the channel.
     * 
     * @return See above.
     */
    public int getChannelIndex() { return index; }
    
}
