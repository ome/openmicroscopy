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
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
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
    
    JRadioButton zButton, tButton, ztButton, zAndtButton;
    
    BottomBar(StatsResultsPaneMng mng)
    {
        initComponents(mng.getAggregationIndex());
        new BottomBarMng(this, mng);
        buildGUI();
    }
    
    /** Initializes the components. */
    void initComponents(int index)
    {
        ButtonGroup group = new ButtonGroup();
        zAndtButton = new JRadioButton("display results for Z and T.");
        zAndtButton.setSelected(index == StatsResultsPaneMng.ZANDTFIELD);
        group.add(zAndtButton);
        zButton = new JRadioButton("on Z");
        zButton.setSelected(index == StatsResultsPaneMng.ZFIELD);
        group.add(zButton);
        tButton = new JRadioButton("on T");
        tButton.setSelected(index == StatsResultsPaneMng.TFIELD);
        group.add(tButton);
        ztButton = new JRadioButton("both Z and T");
        ztButton.setSelected(index == StatsResultsPaneMng.ZTFIELD);
        group.add(ztButton); 
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        JPanel radioPanel = new JPanel();
        radioPanel.add(zButton);
        radioPanel.add(tButton);
        radioPanel.add(ztButton);
        radioPanel.add(zAndtButton);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Aggregate Results")), 
                BorderLayout.NORTH);
        add(UIUtilities.buildComponentPanel(radioPanel), BorderLayout.CENTER); 
    }
    
}
