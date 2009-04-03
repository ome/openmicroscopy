/*
 * org.openmicroscopy.shoola.agents.hiviewer.controls.StatusBar
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

package org.openmicroscopy.shoola.agents.hiviewer.view;




//Java imports
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Presents the progress of the data retrieval.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class StatusBar
    extends JPanel
{
    
    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;

    /** Displays the status message. */
    private JLabel              status;
    
    /** 
     * Initializes the components. 
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    private void initComponents(Icon statusIcon)
    {
        progressBar = new JProgressBar();
        status = new JLabel(statusIcon);
    }
    
    /** Build and lay out the UI. */
    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(status);
        add(UIUtilities.buildComponentPanelRight(progressBar));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    public StatusBar(Icon statusIcon)
    {
        initComponents(statusIcon);
        buildUI();
    }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    public void setStatus(String s) { status.setText(s); }
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>
     * @param perc  The value to set.
     */
    public void setProgress(boolean hide, int perc)
    {
        progressBar.setVisible(!hide);
        if (perc < 0) progressBar.setIndeterminate(true);
        else {
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
    }
    
}
