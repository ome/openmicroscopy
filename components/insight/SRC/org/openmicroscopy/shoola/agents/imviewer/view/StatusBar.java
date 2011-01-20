/*
 * org.openmicroscopy.shoola.agents.imviewer.view.StatusBar
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays plane information.
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

    /** Displays the status message displayed on the left hand side. */
    private JLabel              leftStatus;
    
    /** Displays the status message displayed on the right side. */
    private JLabel              rigthStatus;
    
    /** Displays some of the plane information. */
    private JComponent			centerStatus;
    
    /** Button to display plane info. */
    private JButton				statusButton;

    /** Reference to the model. */
    private ImViewerModel		model;
    
    /** Initializes the components. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        statusButton = new JButton(icons.getIcon(IconManager.STATUS_INFO));
        statusButton.setToolTipText("Load the planes information.");
        statusButton.setContentAreaFilled(false);
        statusButton.setBorder(null);
        statusButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				model.firePlaneInfoRetrieval();
			}
		});
        UIUtilities.unifiedButtonLookAndFeel(statusButton);
        leftStatus = new JLabel();
        rigthStatus = new JLabel();
        centerStatus = new JPanel();
    }
    
    /** Build and lay out the UI. */
    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(statusButton);
        add(Box.createHorizontalStrut(10));
        add(leftStatus);
        add(Box.createHorizontalStrut(15));
        add(centerStatus);
        add(UIUtilities.buildComponentPanelRight(rigthStatus));
        add(Box.createRigidArea(new Dimension(20, 5)));
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param model Reference to the model.
     */
    StatusBar(ImViewerModel model)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	this.model = model;
        initComponents();
        buildUI();
    }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    void setLeftStatus(String s) { leftStatus.setText(s); }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    void setRigthStatus(String s) { rigthStatus.setText(s); }
    
    /** 
     * Sets the component displaying the plane information.
     * 
     * @param comp The message to display.
     */
    void setCenterStatus(JComponent comp)
    { 
    	//centerStatus.removeAll();
    	if (comp != null) {
    		Component[] comps = getComponents();
    		int index = -1;
    		for (int i = 0; i < comps.length; i++) {
				if (comps[i] == centerStatus) 
					index = i;
			}
    		centerStatus = comp;
    		remove(centerStatus);
    		if (index >= 0) add(centerStatus, index);
    		revalidate();
    		repaint();
    	}
    }
    
}
