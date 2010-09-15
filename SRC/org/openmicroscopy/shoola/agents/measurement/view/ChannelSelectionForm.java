/*
 * org.openmicroscopy.shoola.agents.measurement.view.ChannelSelectionForm 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Panel hosting check boxes containing the channels to save.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ChannelSelectionForm
	extends JPanel
{	
	
	/** Value for summary. */
	static int SUMMARYVALUE = -10;
	
	/** The map of channel number to name. */
	private Map<Integer, String>		 	channelNames;
	
	/** The list of checkboxes referring to channels.*/
	private List<JCheckBox> 		checkBox;

	/** 
	 * Finds the channel number in the channelNames Map based on name. 
	 * 
	 * @param comboBoxName The name of the combo box selected.
	 * @return See above.
	 */
	private int getSelectedChannel(String comboBoxName)
	{
		Iterator<Integer> channelIterator = channelNames.keySet().iterator();
		while(channelIterator.hasNext())
		{
			int channel = channelIterator.next();
			
			String channelString = channelNames.get(channel);
			if(channelString.equals(comboBoxName))
				return channel;
			
		}
		return -1;
	}
	
	/** Builds the components for the UI. */
	private void buildComponents()
	{
		checkBox = new ArrayList<JCheckBox>(channelNames.size()+1);
		Iterator<Integer> nameIterator = channelNames.keySet().iterator();
		JCheckBox cBox;
		cBox = new JCheckBox("Channel Summary");
		cBox.setSelected(true);
		checkBox.add(cBox);
		while (nameIterator.hasNext())
		{
			cBox = new JCheckBox(channelNames.get(nameIterator.next()));
			cBox.setSelected(false);
			checkBox.add(cBox);
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildUI()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JLabel l = new JLabel("Select Channels to Save: ");
		mainPanel.add(UIUtilities.buildComponentPanel(l));
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout());
		for (int i = 0 ; i < checkBox.size(); i++)
			panel.add(checkBox.get(i));
		mainPanel.add(UIUtilities.buildComponentPanel(panel));
		setLayout(new BorderLayout());
		add(UIUtilities.buildComponentPanel(mainPanel), BorderLayout.CENTER);
	}
	
	/**
	 * Creates the channel selection form from the map provided.
	 * 
	 * @param channelNames The Map to handle.
	 */
	ChannelSelectionForm(Map<Integer, String> channelNames)
	{
		super();
		this.channelNames = channelNames;
		buildComponents();
		buildUI();
	}
	
	/**
	 * Returns the user selection of the list of channels to output to the file.
	 * 
	 * @return See above.
	 */
	List<Integer> getUserSelection()
	{
		List<Integer> selection = new ArrayList<Integer>();
		int index;
		if(checkBox.get(0).isSelected())
		{
			selection.add(SUMMARYVALUE);
		}
		for (int i = 1 ; i < checkBox.size(); i++)
		{
			if (checkBox.get(i).isSelected()) {
				index = getSelectedChannel(checkBox.get(i).getText());
				if (index==-1)
					continue;
				selection.add(index);
			}		
		}
		return selection;
	}

	
	
}


