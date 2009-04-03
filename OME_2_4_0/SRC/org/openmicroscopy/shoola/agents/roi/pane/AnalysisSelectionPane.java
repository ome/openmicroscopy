/*
 * org.openmicroscopy.shoola.agents.roi.pane.AnalysisSelectionPane
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

package org.openmicroscopy.shoola.agents.roi.pane;


//Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
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
class AnalysisSelectionPane
    extends JPanel
{

    private static final Dimension      HBOX = new Dimension(5, 0);
    
    JButton                             analyse;
    
    AnalysisSelectionPane(Registry registry)
    {
        initComponents(IconManager.getInstance(registry));
        buildGUI();
    }
    
    private void initComponents(IconManager im)
    {
        analyse = new JButton(im.getIcon(IconManager.ANALYSE));
        analyse.setToolTipText(
                UIUtilities.formatToolTipText("Analyze data."));
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        add(buildButtonsBar());
    }

    /** Build a toolBar with buttons. */
    private JToolBar buildButtonsBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(analyse);
        return bar;
    }
    
}
