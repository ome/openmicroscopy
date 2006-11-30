/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.StatusBar
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
    static final String         CANCEL_PROPERTY = "cancel";
    
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
    
    /** Initializes the components. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        status = new JLabel();
        statusButton = new JButton(icons.getIcon(IconManager.STATUS_INFO));
        statusButton.setBorder(null);
        statusButton.setBorderPainted(false);
        statusButton.setFocusPainted(false);
        statusButton.setOpaque(false);
        statusButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                if (buttonEnabled) controller.cancel();
            }
        
        });
    }
    
    /** Build and lay out the UI. */
    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(statusButton);
        p.add(status);
        add(UIUtilities.buildComponentPanel(p));
        JPanel progress = new JPanel();
        progress.setLayout(new BoxLayout(progress, BoxLayout.X_AXIS));
        progress.add(progressBar);
        IconManager icons = IconManager.getInstance();
        progressLabel = new JLabel(icons.getIcon(IconManager.PROGRESS));
        progress.add(progressLabel);
        add(UIUtilities.buildComponentPanelRight(progress));
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
     * Sets the status icon and sets the enabled value.
     * 
     * @param statusIcon   The icon displayed in the left corner.
     * @param b            The value to set.
     */
    void setStatusIcon(Icon statusIcon, boolean b)
    { 
        statusButton.setIcon(statusIcon);
        buttonEnabled = b;
        //statusButton.setEnabled(b);
    }
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>
     * @param perc  The value to set.
     */
    void setProgress(boolean hide, int perc)
    {
        progressBar.setVisible(!hide);
        progressLabel.setVisible(!hide);
        /*
        if (perc < 0) progressBar.setIndeterminate(true);
        else {
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
        */
    }
    
}
