/*
 * ChannelToggleButton.java
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
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
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
	public static final String		CHANNEL_PICKED_PROPERTY = 
		                                "channelPicked";

    /** The channel index. */
	private final int    index;
	
	/**
     * Creates a new instance.
     * 
	 * @param text      The text to display.
	 * @param color     The button's color.
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
    
}
