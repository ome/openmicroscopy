/*
 * org.openmicroscopy.shoola.util.ui.Spinner 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Spinner 
	extends JPanel
	implements ActionListener
{

	/** Bound property indicating to increase the value. */
	public static final String INCREASE_PROPERTY = "increase";
	
	/** Bound property indicating to decrease the value. */
	public static final String DECREASE_PROPERTY = "decrease";
	
	/** Action command to increase the value. */
	private static final int INCREASE = 0;
	
	/** Action command to decrease the value. */
	private static final int DECREASE = 1;
	
	/** The button to decrease the value. */
	private JButton minus;
	
	/** The button to increase the value. */
	private JButton plus;
	
	/** Initializes the components. */
	private void initiliaze()
	{
		IconManager icons = IconManager.getInstance();
		minus = new JButton();
		minus.setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
		minus.setMargin(new Insets(0, 0, 0, 0));//Just to make sure button sz=icon sz.
		minus.setOpaque(false);  //B/c button=icon.
		minus.setFocusPainted(false);  //Don't paint focus box on top of icon.
		minus.setRolloverEnabled(true);
		minus.setIcon(icons.getIcon(IconManager.MINUS_9));
		minus.setRolloverIcon(icons.getIcon(IconManager.MINUS_OVER_9));
		minus.setToolTipText("Decrease the value");
		minus.addActionListener(this);
		minus.setActionCommand(""+DECREASE);
		plus = new JButton();
		plus.setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
		plus.setMargin(new Insets(0, 0, 0, 0));//Just to make sure button sz=icon sz.
		plus.setOpaque(false);  //B/c button=icon.
		plus.setFocusPainted(false);  //Don't paint focus box on top of icon.
		plus.setRolloverEnabled(true);
		plus.setIcon(icons.getIcon(IconManager.PLUS_9));
		plus.setRolloverIcon(icons.getIcon(IconManager.PLUS_OVER_9));
		plus.setToolTipText("Increase the value");
		plus.addActionListener(this);
		plus.setActionCommand(""+INCREASE);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder()); 
		add(plus);
		add(minus);
	}

	/** Creates a new instance, */
	public Spinner()
	{
		initiliaze();
		buildGUI();
	}
	
	/**
	 * Fires property change to increase or decrease the value. 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case INCREASE:
				firePropertyChange(INCREASE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
				break;
			case DECREASE:
				firePropertyChange(DECREASE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
		}
		
	}
}
