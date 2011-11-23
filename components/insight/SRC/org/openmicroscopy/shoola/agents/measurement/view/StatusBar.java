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


//Java imports
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

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

    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** Dimension of the progress bar. */
    private static final Dimension PROGRESSBAR_SIZE = new Dimension(30, 8);
    
	/** Displays the status message. */
	private JLabel              status;
	
	/** Button to display plane info. */
	private JButton				statusButton;
	
	/** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;
    
    /** The label displaying the progress icon. */
    private JLabel              progressLabel;
    
    /** The label displaying the currently selected plane.*/
    private JLabel				planeLabel;
    
	/** Initializes the components. */
	private void initComponents()
	{
	    IconManager icons = IconManager.getInstance();
	    statusButton = new JButton(icons.getIcon(IconManager.STATUS_INFO));
	    statusButton.setContentAreaFilled(false);
	    statusButton.setBorder(null);
	    UIUtilities.unifiedButtonLookAndFeel(statusButton);
	    status = new JLabel();
	    planeLabel = new JLabel();
	    progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressLabel = new JLabel(icons.getIcon(IconManager.PROGRESS));
	}
	
	/** Build and lay out the UI. */
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        //p.add(statusButton);
        //p.add(Box.createRigidArea(H_SPACER_SIZE));
        p.add(planeLabel);
        p.add(Box.createRigidArea(H_SPACER_SIZE));
        p.add(status);
        add(UIUtilities.buildComponentPanel(p));
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
        
        progressPanel.add(progressBar);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        //add(UIUtilities.buildComponentPanelRight(progressPanel));
        
        progressBar.setPreferredSize(PROGRESSBAR_SIZE);
        progressBar.setSize(PROGRESSBAR_SIZE);
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
	public void setPlaneStatus(String s) { planeLabel.setText(s); }
	
	/** 
	 * Sets the status message.
	 * 
	 * @param s The message to display.
	 */
	public void setStatus(String s) { status.setText(s); }
	
}
