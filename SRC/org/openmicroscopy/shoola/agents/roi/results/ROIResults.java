/*
 * org.openmicroscopy.shoola.agents.roi.results.ROIResults
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

package org.openmicroscopy.shoola.agents.roi.results;

//Java imports
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.defs.ROIStats;
import org.openmicroscopy.shoola.agents.roi.results.controls.ControlsBar;
import org.openmicroscopy.shoola.agents.roi.results.pane.ResultsPerROIPane;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
public class ROIResults
    extends JDialog
{
    
    List                                roiPaneList;

    private ROIResultsMng               manager;
    
    public ROIResults(ROIAgtCtrl control, Registry reg)
    {
        super(control.getReferenceFrame(), "ROI Results", true);
        roiPaneList = new ArrayList();
        manager = new ROIResultsMng(this);
        IconManager im = IconManager.getInstance(reg);
        buildGUI(im);
        pack();  
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(IconManager im)
    {
        TitlePanel tp = new TitlePanel("ROI stats", 
                                "Result of the ROI analysis.", 
                                im.getIcon(IconManager.ROISHEET_BIG));
        //set layout and add components
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildTabbedPanel(getDefault(), im), 
                            BorderLayout.CENTER);
        getContentPane().add(new ControlsBar(manager, im), BorderLayout.SOUTH);
    }

    /** Build the main component. */
    private JPanel buildTabbedPanel(List l, IconManager im)
    {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
                                    JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        Iterator i = l.iterator();
        ROIStats stats;
        ResultsPerROIPane roiPane;
        StatsResultsPane pane;
        int c = 0;
        //TODO: refactor
        Icon    up = im.getIcon(IconManager.UP), 
                down = im.getIcon(IconManager.DOWN);
        while (i.hasNext()) {
            stats = (ROIStats) i.next();
            roiPane = new ResultsPerROIPane(c, manager, stats.getChannels(), 
                                "TEST", stats.getAnnotation());
            pane =  new StatsResultsPane(manager, stats.getData(), up, down);
            roiPane.addToContainer(pane);
            roiPaneList.add(roiPane);
            tabs.insertTab("ROI #"+stats.getIndex(), null, roiPane, null, c);
            c++;
        }
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }
    
    
    //TODO: TO BE REMOVED
    private List getDefault()
    {
        ArrayList l = new ArrayList();
        String[] channels = new String[2];
        channels[0] = "300";
        channels[1] = "400";
        ROIStats stats = new ROIStats(1, "JSKFSKFSFSKJ", channels);
        String[][] data = new String[2][6];
        for (int i = 0; i < 6; i++) {
            data[0][i] = ""+i;
            data[1][i] = ""+2*i;
        }
        stats.setData(data);
        l.add(stats);
        stats = new ROIStats(2, "", channels);
        stats.setData(data);
        l.add(stats);
        return l;
    }

}
