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
import java.awt.Container;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.agents.roi.results.controls.ControlsBar;
import org.openmicroscopy.shoola.agents.roi.results.pane.ResultsPerROIPane;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane;
import org.openmicroscopy.shoola.env.rnd.roi.ROIStats;
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
    
    public static final int             WIDTH = 500, HEIGHT = 500;
    
    Map                                 paneMap;
    
    String[]                            listROIs;

    JTabbedPane                         tabs;
    
    private ROIResultsMng               manager;
    
    public ROIResults(ROIAgtCtrl control, int sizeT, int sizeZ)
    {
        super(control.getReferenceFrame(), "ROI Results");
        init();
        manager = new ROIResultsMng(this, control);
        IconManager im = IconManager.getInstance(control.getRegistry());
        JPanel p = buildTabbedPanel(control, im, sizeT, sizeZ);
        buildGUI(im, p);
        setSize(WIDTH, HEIGHT);
    }
    
    private void init()
    {
        paneMap = new HashMap();
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(IconManager im, JPanel tablePanel)
    {
        TitlePanel tp = new TitlePanel("ROI stats", 
                                "Result of the ROI analysis.", 
                                im.getIcon(IconManager.ROISHEET_BIG));
        //set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(tablePanel, BorderLayout.CENTER);
        c.add(new ControlsBar(manager, im), BorderLayout.SOUTH);
    }

    /** Build the main component. */
    private JPanel buildTabbedPanel(ROIAgtCtrl control, IconManager im, 
                                    int sizeT, int sizeZ)
    {
        ResultsPerROIPane roiPane;
        StatsResultsPane pane;
        int c = 0;
        Icon up = im.getIcon(IconManager.UP), 
            down = im.getIcon(IconManager.DOWN);
        List analysedROI = control.getAnalyzedROI();
        Iterator i = analysedROI.iterator();
        ScreenROI roi;
        ROIStats roiStats;
        Map results = control.getROIResults();
        String[] channels = control.getAnalyzedChannels();
        int channel = control.getAnalyzedChannel(0);
        listROIs = new String[analysedROI.size()];
        int roiIndex;
        int length = analysedROI.size();
        while (i.hasNext()) {
            roi = (ScreenROI) i.next();
            roiIndex = roi.getIndex();
            roiStats = (ROIStats) results.get(roi.getActualROI());
            roiPane = new ResultsPerROIPane(c, manager, channels, 
                                roi.getName(), roi.getAnnotation());
            pane =  new StatsResultsPane(manager, roiStats, sizeT, sizeZ, 
                                        channel, channels.length, up, down, 
                                        roiIndex, length);
            roiPane.addToContainer(pane);
            paneMap.put(new Integer(c), pane);
            tabs.insertTab("ROI #"+roiIndex, null, roiPane, null, c);
            listROIs[c] = "ROI #"+roiIndex;
            c++;
        }
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }
    
}
