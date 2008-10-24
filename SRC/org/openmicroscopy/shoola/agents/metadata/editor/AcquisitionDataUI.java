/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionDataUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component displaying the acquisition information.
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
class AcquisitionDataUI 
	extends JPanel
{

	/** The default text of the channel. */
	private static final String			DEFAULT_CHANNEL_TEXT = "Channel ";
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
		
	/** Reference to the Model. */
	private EditorUI					view;
	
	/** The component hosting the image acquisition data. */
	private ImageAcquisitionComponent	imageAcquisition;
	
	/** The collection of channel acquisition data. */
	private List<JXTaskPane> 			channelAcquisitionPanes;
	
	/** The component hosting the image info. */
	private JXTaskPane 					imagePane;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		channelAcquisitionPanes = new ArrayList<JXTaskPane>();
		imageAcquisition = new ImageAcquisitionComponent(model);
		imagePane = EditorUI.createTaskPane("Image");
		imagePane.add(imageAcquisition);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		removeAll();
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BorderLayout(0, 0));
		JPanel container = new JPanel();
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        Iterator<JXTaskPane> i = channelAcquisitionPanes.iterator();
        c.weightx = 1.0;
        container.add(imagePane, c);
        c.gridy = 1;
        while (i.hasNext()) {
            ++c.gridy;
            container.add(i.next(), c);  
        }
        add(container, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 */
	AcquisitionDataUI(EditorUI view, EditorModel model, 
				EditorControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.view = view;
		initComponents();
	}
	
	/** 
	 * Sets the data when data are loaded, builds the UI.
	 */
	void setChannelData()
	{
		List channels = model.getChannelData();
		channelAcquisitionPanes.clear();
		if (channels != null) {
			ChannelMetadata channel;
			Iterator i = channels.iterator();
			ChannelAcquisitionComponent comp;
			JXTaskPane p;
			while (i.hasNext()) {
				channel = (ChannelMetadata) i.next();
				comp = new ChannelAcquisitionComponent(channel);
				p = EditorUI.createTaskPane(DEFAULT_CHANNEL_TEXT+
						channel.getEmissionWavelength());
				p.add(comp);
				channelAcquisitionPanes.add(p);
			}
		}
		buildGUI();
	}
	
}
