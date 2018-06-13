/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.CheckSumDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Dialog displaying the client and server checksums.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class CheckSumDialog
	extends JDialog
{

	/** The title of the dialog.*/
	private static final String TITLE = "Checksums";
	
	/** The text displayed in the header.*/
	private static final String TEXT = "Client and Server checksums.";
	
	/** Component used to close the dialog.*/
	private JButton closeButton;
	
	/** The table hosting checksums details.*/
	private JTable table;
	
	/**
	 * Initializes the components.
	 * 
	 * @param label The component hosting information about the checksums.
	 */
	private void initialize(Status label)
	{
		IconManager icons = IconManager.getInstance();
		ChecksumTableRenderer rnd = new ChecksumTableRenderer(
				icons.getIcon(IconManager.DELETE),
				icons.getIcon(IconManager.APPLY));
		
    	ChecksumTableModel model = new ChecksumTableModel(
    			label.getChecksumFiles(), label.getChecksums(),
    			label.getFailingChecksums());
    	table = new JTable(model);
    	TableColumnModel tcm = table.getColumnModel();
    	for (int i = 0; i < tcm.getColumnCount(); i++) {
    		tcm.getColumn(i).setCellRenderer(rnd);
		}
    	closeButton = new JButton("Close");
    	closeButton.addActionListener(new ActionListener() {
			
			/** Disposes of the dialog
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(closeButton);
		return UIUtilities.buildComponentPanelRight(bar);
	}

	/** Builds and lays out the UI.*/
	private void buildGUI()
	{
		JScrollPane scrollPane = new JScrollPane(table);
		//table.setFillsViewportHeight(true);
		StringBuffer buf = new StringBuffer();
		buf.append("Only the last ");
		buf.append(ChecksumTableRenderer.MAX_CHARACTERS);
		buf.append(" characters of the checksums are displayed.");
		TitlePanel tp = new TitlePanel(TEXT, buf.toString(), null);
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(tp, BorderLayout.NORTH);
		c.add(scrollPane, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param label The component hosting information about the checksums.
	 */
	public CheckSumDialog(JFrame owner, Status label)
	{
		super(owner);
		setTitle(TITLE);
		setModal(true);
		initialize(label);
		buildGUI();
		pack();
	}

}
