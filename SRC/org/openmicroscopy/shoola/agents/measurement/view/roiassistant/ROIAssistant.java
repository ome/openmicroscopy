/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROIAssistant.ROIAssistant 
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
package org.openmicroscopy.shoola.agents.measurement.view.roiassistant;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class ROIAssistant
	extends JDialog
{	
	/** 
	 * The table showing the ROI and allowing the user to propagate the selected
	 * ROI through time and Z-section. 
	 */	
	private 	ROIAssistantTable	table;
	
	/**
	 * The model which will define the ROI's displayed in the table.
	 */
	private 	ROIAssistantModel 	model;
	
	/** The user has selected to cancel propagation of the ROI. */
	private 	JButton 			cancelButton;

	/** The user has selected to accept the current propagation of the ROI. */
	private 	JButton 			acceptButton;
	
	/** 
	 * The user has selected to revert to the original ROI time and 
	 * z-section. 
	 */
	private 	JButton 			resetButton;
	
	
	/**
	 * Construvtor for the ROIAssistant Dialog.
	 * @param numRow The number of z sections in the image. 
	 * @param numCol The numer of time points in the image. 
	 * @param selectedROI The ROI which will be propagated.
	 */
	public ROIAssistant(int numRow, int numCol, Coord3D currentPlane, 
						ROI selectedROI)
	{
		this.setAlwaysOnTop(true);
		this.setModal(true);
		createTable(numRow, numCol,currentPlane, selectedROI);
		buildUI();
	}
	
	/** Create the UI for the Assistant. */
	private void buildUI()
	{
		createButtons();
		layoutUI();
	}
	
	/**
	 * Create the table and model.
	 *  
	 * @param numRow The number of z sections in the image. 
	 * @param numCol The numer of time points in the image. 
	 * @param selectedROI The ROI which will be propagated.
	 */
	private void createTable(int numRow, int numCol, Coord3D currentPlane, ROI selectedROI)
	{
		model = new ROIAssistantModel(numRow, numCol, currentPlane, selectedROI);
		table = new ROIAssistantTable(model);
	}
	
	/** Create the accept, reset and cancel buttons including actions. */
	private void createButtons()
	{
		acceptButton = new JButton("Accept");
		cancelButton = new JButton("Cancel");
		resetButton = new JButton("Reset");

		resetButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						resetButtonClicked();
					}
				});
		cancelButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						cancelButtonClicked();
					}
				});
		acceptButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						acceptButtonClicked();
					}
				});
	}
	
	/**
	 * The info panel at the top the the dialog, showing a little text about the
	 * ROI Assistant. 
	 * @return the info panel.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		JLabel infoText = new JLabel
		("<html><body>This is ROI Assistant." +
		"It allows you to create an which ROI extends through " +
		"<p>time and z-sections.</body></html>");
		infoText.setIcon(IconManager.getInstance().getIcon(IconManager.WIZARD));
		infoPanel.add(Box.createHorizontalStrut(10));
		infoPanel.add(infoText, BorderLayout.CENTER);
		return infoPanel;
	}
	
	/** 
	 * Create the accept, cancel and reset buttons on the panel. 
	 * @return the panel with the JButtons. 
	 */
	private JPanel createButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(resetButton);
		buttonPanel.add(acceptButton);
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}
	
	/** Layout the UI, adding panels to the form. */
	private void layoutUI()
	{
		this.setSize(500,400);
		JPanel panel = new JPanel();
		JPanel infoPanel = createInfoPanel();
		JPanel buttonPanel = createButtonPanel();
		UIUtilities.setDefaultSize(infoPanel, new Dimension(500, 70));
		UIUtilities.setDefaultSize(table, new Dimension(500, 340));
		UIUtilities.setDefaultSize(buttonPanel, new Dimension(500, 70));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(infoPanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(table);
		panel.add(Box.createVerticalStrut(10));
		panel.add(buttonPanel);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel);
	}

	
	private void acceptButtonClicked()
	{
		
	}
	
	private void cancelButtonClicked()
	{
		
	}
	
	private void resetButtonClicked()
	{
		
	}

	
	
}


