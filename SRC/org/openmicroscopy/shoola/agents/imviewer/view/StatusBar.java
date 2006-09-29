/*
 * org.openmicroscopy.shoola.agents.imviewer.view.StatusBar
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Presents the progress of the data retrieval.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
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

    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;

    /** Displays the status message. */
    private JLabel              status;
    
    /** Button to increase the size of the unit bar. */
    private JButton             plusUnitBar;
    
    /** Button to decrease the size of the unit bar. */
    private JButton             minusUnitBar;
    
    /** 
     * Initializes the components. 
     * 
     * @param controller Reference to the Control.
     */
    private void initComponents(ImViewerControl controller)
    {
        IconManager icons = IconManager.getInstance();
        progressBar = new JProgressBar();
        status = new JLabel(icons.getIcon(IconManager.STATUS_INFO));
        plusUnitBar = new JButton(
                controller.getAction(ImViewerControl.UNIT_BAR_PLUS));
        UIUtilities.unifiedButtonLookAndFeel(plusUnitBar);
        minusUnitBar = new JButton(
                controller.getAction(ImViewerControl.UNIT_BAR_MINUS));
        UIUtilities.unifiedButtonLookAndFeel(minusUnitBar);
    }
    
    /** Build and lay out the UI. */
    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(status);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(progressBar);
        p.add(minusUnitBar);
        p.add(plusUnitBar);
        add(UIUtilities.buildComponentPanelRight(p));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the Control. Mustn't be <code>null</code>.
     */
    StatusBar(ImViewerControl controller)
    {
        if (controller == null) 
            throw new IllegalArgumentException("No control.");
        initComponents(controller);
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
     *              <code>false</otherwise>
     * @param perc  The value to set.
     */
    void setProgress(boolean hide, int perc)
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
