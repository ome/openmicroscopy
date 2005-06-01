/*
 * org.openmicroscopy.shoola.agents.datamng.editors.controls.CreateBar
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

package org.openmicroscopy.shoola.agents.datamng.editors.controls;






//Java imports
import java.awt.Cursor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class CreateBar
extends JToolBar
{

    private JButton     save;
    
    public CreateBar()
    {
        initButtons();
        buildGUI();
        setFloatable(false);
    }
    
    public JButton getSave() { return save; }

    /** Initializes the buttons. */
    private void initButtons()
    {
        save = new JButton("Save");
        save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        save.setToolTipText(
            UIUtilities.formatToolTipText("Save data."));
        save.setEnabled(false);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        add(UIUtilities.buildComponentPanelRight(buildButtonPanel()));
    }
    
    /** Build panel with buttons. */
    private JPanel buildButtonPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(save);
        p.add(Box.createRigidArea(DataManagerUIF.HBOX));
        p.setOpaque(false); //make panel transparent
        return p;
    }

}
