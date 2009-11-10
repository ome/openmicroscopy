/*
 * org.openmicroscopy.shoola.agents.metadata.util.ChannelComponent 
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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies

/** 
 * Component to turn on or off a channel.
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
class ChannelComponent 
	extends JPanel
	implements ChangeListener
{

	/** Bound property indicating to turn on or off the channel. */
	static final String	CHANNEL_SELECTION_PROPERTY = "channelSelection";
	
	/** The default size of the panel. */
	private static final Dimension SIZE = new Dimension(22, 22);
	
	/** The index of the channel. */
	private int index;
	
	/** The color associated to the channel. */
	private JPanel colorLabel;
	
	/** Box to turn on/off the channel. */
	private JCheckBox activeBox;
	
	/**
	 * Initializes the components 
	 * 
	 * @param color The color associated to the channel.
	 * @param active Pass <code>true</code> if the channel is active,
	 * 				 <code>false</code> otherwise.
	 */
	private void initComponents(Color color, boolean active)
	{
		
		activeBox = new JCheckBox();
		//activeBox.setIcon(icon);
		activeBox.setBackground(color);
		activeBox.setSelected(active);
		activeBox.addChangeListener(this);
		colorLabel = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED}, //columns
				{TableLayout.PREFERRED}}; //rows
		colorLabel.setLayout(new TableLayout(tl));
		//colorLabel.setPreferredSize(SIZE);
		//p.add(colorLabel);
		colorLabel.add(activeBox, "0, 0, CENTER, CENTER");
		colorLabel.setBackground(color);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(colorLabel, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index  The index of the channel.
	 * @param color  The color associated to the channel.
	 * @param active Pass <code>true</code> if the channel is active,
	 * 				 <code>false</code> otherwise.
	 */
	ChannelComponent(int index, Color color, boolean active)
	{
		this.index = index;
		initComponents(color, active);
		buildGUI();
	}

	/**
	 * Returns <code>true</code> if the channel is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isActive() { return activeBox.isSelected(); }
	
	/**
	 * Returns the index of the channel.
	 * 
	 * @return See above.
	 */
	int getChannelIndex() { return index; }
	
	/**
	 * Turns on or off the channel.
	 * 
	 * @param active Pass <code>true</code> to turn the channel on,
	 * 				 <code>false</code> to turn it off.
	 */
	void setSelected(boolean active)
	{
		activeBox.removeChangeListener(this);
		activeBox.setSelected(active);
		activeBox.addChangeListener(this);
	}
	
	/**
	 * Turns on or off the channel.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		firePropertyChange(CHANNEL_SELECTION_PROPERTY, null, this);
	}
	
}
