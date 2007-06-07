/*
 * org.openmicroscopy.shoola.agents.measurement.view.StatusBar 
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

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays information related to the ROI handling.
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
class StatusBar
	extends JPanel
{
	
	/** Dimension of the horizontal space between UI components. */
	private static final		Dimension HBOX = new Dimension(5, 5);
	
	/** Displays the status message. */
	private JLabel              status;
	
	/** Button to display plane info. */
	private JButton				statusButton;
	
	/** Initializes the components. */
	private void initComponents()
	{
	    IconManager icons = IconManager.getInstance();
	    statusButton = new JButton(icons.getIcon(IconManager.STATUS_INFO));
	    statusButton.setContentAreaFilled(false);
	    statusButton.setBorder(null);
	    UIUtilities.unifiedButtonLookAndFeel(statusButton);
	    status = new JLabel();
	}
	
	/** Build and lay out the UI. */
	private void buildUI()
	{
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBorder(BorderFactory.createEtchedBorder());
	    add(statusButton);
	    add(Box.createRigidArea(HBOX));
	    add(status);
	}
	
	/** Creates a new instance. */
	StatusBar()
	{
	    initComponents();
	    buildUI();
	}
	
	/** 
	 * Sets the status message.
	 * 
	 * @param s The message to display.
	 */
	void setStatus(String s) { status.setText(s); }
	
}
