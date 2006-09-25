/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourMenuUI
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
package org.openmicroscopy.shoola.util.ui.colourpicker;


//Java imports
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a UI which represents the colours as a list in the menu, currently it
 * shows the colour as an icon and the colourname, all of which are added from 
 * the constructor. (this may change)
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ColourSwatchUI 
	extends JPanel
{
	
	/**
	 * Refernce to the Colour model.
	 */
	private RGBControl	control;
	
	/**
	 * List of colours.
	 */
	private JList		colourlist;	
	
	/**
	 * Scroll pane which contains the JList component.
	 */
	private JScrollPane	scrollpane;	
		
	/**
	 * Create the UI and attach the control c.
	 * @param c - control
	 */
	ColourSwatchUI(RGBControl c)
	{
		control = c;
		createUI();
	}
	
	/**
	 * Create the colours and add the renderer {@link ColourListRenderer} to 
	 * the JList object. 
	 */
	void createColours()
	{
		colourlist = new JList(
				new Object[] {	
				
						new Object[] {Color.black,"Black"},
						new Object[] {Color.gray,"Gray"},
						new Object[] {Color.white,"White"},
						new Object[] {Color.red,"Red"},
						new Object[] {Color.orange,"Orange"},
						new Object[] {Color.yellow,"Yellow"},
						new Object[] {Color.green,"Green"},
						new Object[] {Color.blue,"Blue"},
						new Object[] {new Color(75, 0, 130),"Indigo"},
						new Object[] {new Color(238,130,238),"Violet"},
							
				});
		colourlist.setCellRenderer(new ColourListRenderer());
		
	}
	
	/**
	 * Create the UI which includes adding the colour list (JList) to the 
	 * scrollpane.
	 */
	void createUI()
	{
		createColours();
		colourlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		colourlist.setLayoutOrientation(JList.VERTICAL);
		colourlist.setVisibleRowCount(-1);
		colourlist.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e)
				{
					Object []Obj = (Object[]) 
					colourlist.getModel().getElementAt(((JList)e.getSource()).
							getLeadSelectionIndex());
					control.setColour((Color)Obj[0]);	
		}

		});
		
		scrollpane = new JScrollPane(colourlist);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 80;
		gbc.weighty = 20;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(20,30,20,30);
		
		this.add(scrollpane, gbc);
	}
	
}


