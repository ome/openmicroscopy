/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorStatusBar 
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
package org.openmicroscopy.shoola.agents.editor.view;


//Java imports
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;
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
 * @since 3.0-Beta3
 */
class EditorStatusBar
	extends JPanel
{

	/** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;
    
    /** Displays the status message. */
    private JLabel              status;
    
    /** Initializes the components. */
    private void initComponents()
    {
    	IconManager icons = IconManager.getInstance();
    	progressBar = new JProgressBar();
    	progressBar.setIndeterminate(true);
        status = new JLabel(icons.getIcon(IconManager.STATUS_INFO));
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEtchedBorder());
        add(status);
        add(UIUtilities.buildComponentPanelRight(progressBar));
        add(Box.createHorizontalStrut(10));
    }
    
    /** Creates a new instance. */
	EditorStatusBar()
	{
		initComponents();
		buildGUI();
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
