/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.StatusBar 
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
package org.openmicroscopy.shoola.agents.util.classifier.view;



//Java imports
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Presents the progress of the data retrieval.
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
	
	/** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;

    /** Displays the status message. */
    private JLabel              status;
    
    /** The label displaying the progress icon. */
    private JLabel              progressLabel;
    
    /** Displays the status icon. */
    private JButton             statusButton;
    
    /** Initializes the components. */
    private void initComponents()
    {
    	IconManager icons = IconManager.getInstance();
    	progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        status = new JLabel();
        statusButton = new JButton(icons.getIcon(IconManager.INFO));
        statusButton.setBorder(null);
        statusButton.setBorderPainted(false);
        statusButton.setFocusPainted(false);
        statusButton.setOpaque(false);
    }
    
    /** Builds and lays out the UI. */
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
    
    /**Creates a new instance. */
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
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>.
     */
    void setProgress(boolean hide)
    {
        progressBar.setVisible(!hide);
    }
	
}
