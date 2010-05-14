/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.StatusBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Presents the progress of the data retrieval.
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
class StatusBar
    extends JPanel
{

    /** Bounds property indicating that data loading is cancelled. */
    static final String         	CANCEL_PROPERTY = "cancel";
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** Dimension of the progress bar. */
    private static final Dimension PROGRESSBAR_SIZE = new Dimension(30, 8);
    
    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;

    /** Displays the status message. */
    private JLabel              status;
    
    /** The label displaying the progress icon. */
    private JLabel              progressLabel;
    
    /** Displays the status icon. */
    private JButton             statusButton;
    
    /** Flag to indicate if the button is enabled. */
    private boolean             buttonEnabled;
    
    /** Reference to the control. */
    private TreeViewerControl   controller;
    
    /** Helper reference. */
    private IconManager 		icons;
    
    /** Initializes the components. */
    private void initComponents()
    {
        icons = IconManager.getInstance();
        progressLabel = new JLabel(icons.getIcon(IconManager.TRANSPARENT));
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        status = new JLabel();
        statusButton = new JButton(icons.getIcon(IconManager.CANCEL));
        statusButton.setContentAreaFilled(false);
        statusButton.setBorder(null);
        UIUtilities.unifiedButtonLookAndFeel(statusButton);
        //statusButton.setOpaque(false);
        statusButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                if (buttonEnabled) controller.cancel();
            }
        
        });
    }
    
    /** Builds and lays out the UI. */
    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        //p.add(statusButton);
        //p.add(Box.createRigidArea(H_SPACER_SIZE));
        p.add(status);
        add(UIUtilities.buildComponentPanel(p));
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));  
        progressPanel.add(progressBar);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        add(UIUtilities.buildComponentPanelRight(progressPanel));
        progressBar.setPreferredSize(PROGRESSBAR_SIZE);
        progressBar.setSize(PROGRESSBAR_SIZE);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the control. Mustn't be 
     *                      <code>null</code>.
     */
    StatusBar(TreeViewerControl controller)
    {
        if (controller == null)
            throw new IllegalArgumentException("No control.");
        this.controller = controller;
        initComponents();
        buildUI();
    }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    void setStatus(String s) { status.setText(s); }
    
    /** 
     * Enables the button and shows/hides the {@link #statusButton}
     * depending on the passed parameter.
     * 
     * @param b The value to set.
     */
    void setStatusIcon(boolean b)
    { 
    	if (b) statusButton.setIcon(icons.getIcon(IconManager.CANCEL));
    	else statusButton.setIcon(icons.getIcon(IconManager.TRANSPARENT));
        //statusButton.setVisible(b);
        buttonEnabled = b;
    }
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>
     */
    void setProgress(boolean hide)
    {
    	progressBar.setVisible(!hide);
        progressLabel.setEnabled(!hide);
        if (hide) progressLabel.setIcon(icons.getIcon(IconManager.TRANSPARENT));
        else progressLabel.setIcon(icons.getIcon(IconManager.PROGRESS));
    }
    
}
