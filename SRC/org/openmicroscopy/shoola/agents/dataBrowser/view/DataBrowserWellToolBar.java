/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserWellToolBar 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The tool bar of {@link DataBrowser} displaying wells. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class DataBrowserWellToolBar
	extends JPanel
{

	/** Reference to the control. */
	private DataBrowserControl 	controller;

	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Button to refresh the display. */
	private JButton				refreshButton;
	
	/** Displays the possible fields per well. */
	private JComboBox			fields;
	
	/** Initializes the components. */
	private void initComponents()
	{
		refreshButton = new JButton(controller.getAction(
				DataBrowserControl.REFRESH));
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		int f = view.getFieldsNumber();
		if (f > 1) { 
			String[] values = new String[f];
 			for (int i = 0; i < f; i++) 
				values[i] = "Field #"+i;
 			fields = new JComboBox(values);
 			fields.setSelectedIndex(view.getSelectedField());
 			fields.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					controller.viewField(fields.getSelectedIndex());
				}
			
			});
		}
	}
	
	/**
	 * Builds the tool bar with the various control for the view.
	 * 
	 * @return See above.
	 */
	private JToolBar buildViewsBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setRollover(true);
		bar.add(refreshButton);
		if (fields != null) bar.add(fields);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.add(buildViewsBar());
		content.add(p);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(content);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 			Reference to the view. Mustn't be <code>null</code>.
	 * @param controller 	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	DataBrowserWellToolBar(DataBrowserUI view, DataBrowserControl controller)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.controller = controller;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
}
