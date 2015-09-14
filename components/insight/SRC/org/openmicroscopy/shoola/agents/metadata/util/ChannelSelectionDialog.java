/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openmicroscopy.shoola.util.ui.OptionsDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ChannelData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ChannelSelectionDialog 
	extends OptionsDialog
{
	
	/** Bound property indicating to collect the parameters. */
	public static final String CHANNEL_ANALYSIS_SELECTION_PROPERTY = 
		"channelAnalysisSelection";
	
	/** Holds the components displaying the various channels. */
	private Map<JRadioButton, ChannelData> channelSelection;
	
	/** The analysis to perform. */
	private int index;
	
	/**
	 * Initializes the components composing the display.
	 * 
	 * @param channels
	 */
	private void initialize(List<ChannelData> channels)
	{
		setYesText("Analyse");
		hideNoButton();
		addCancelButton();
		setResizable(true);
		channelSelection = new LinkedHashMap<JRadioButton, ChannelData>();
		Iterator<ChannelData> i = channels.iterator();
		ChannelData data;
		JRadioButton box;
		int index = 0;
		ButtonGroup group = new ButtonGroup();
		while (i.hasNext()) {
			data = i.next();
			box = new JRadioButton(data.getChannelLabeling());
			channelSelection.put(box, data);
			if (index == 0) box.setSelected(true);
			index++;
			group.add(box);
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		JPanel p = new JPanel();
		//p.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		Entry entry;
		Iterator i = channelSelection.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			p.add((JRadioButton) entry.getKey());
		}
		addBodyComponent(UIUtilities.buildComponentPanel(p));
	}
	
	/**
	 * Overridden to fire a property with the selected channel.
	 * @see OptionsDialog#onYesSelection()
	 */
	protected void onYesSelection()
	{
		Entry entry;
		Iterator i = channelSelection.entrySet().iterator();
		ChannelData data = null;
		JRadioButton b;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			b = (JRadioButton) entry.getKey();
			if (b.isSelected()) {
				data = (ChannelData) entry.getValue();
				break;
			}
		}
		if (data == null) return;
		List<Object> l = new ArrayList<Object>(2);
		l.add(data);
		l.add(index);
		firePropertyChange(CHANNEL_ANALYSIS_SELECTION_PROPERTY, null, l);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The owner of this dialog
	 * @param icon	 	The icon displayed in the header.
	 * @param channels  The collection of channels.
	 * @param index		The type of analysis to perform.
	 */
	public ChannelSelectionDialog(JFrame parent, Icon icon, 
			List<ChannelData> channels, int index)
	{
		super(parent, "Channel Selection", "Select the channel to analyze.", 
				icon);
		this.index = index;
		initialize(channels);
		buildGUI();
		setSize(450, 200);
	}
	
}
