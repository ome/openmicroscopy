/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.BottomBar
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

package org.openmicroscopy.shoola.agents.roi.results.stats;

//Java imports
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
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
class BottomBar
    extends JPanel
{
    
    JButton         save;
    
    BottomBar(StatsResultsPaneMng mng, Registry reg)
    {
        initComponents(IconManager.getInstance(reg));
        new BottomBarMng(this, mng);
        buildGUI();
    }
    
    /** Initializes the components. */
    void initComponents(IconManager im)
    {
        save = new JButton();
        save = new JButton(im.getIcon(IconManager.SAVE));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the result as a " +
                                            "Text file."));
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        JPanel p = new JPanel();
        p.add(buildBar());
        add(p); 
    }
    
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(save);
        return bar;
    }
}
