/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.ROISelector
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
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;



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
class ROISelector
    extends JDialog
{
    
    JComboBox           box;
    JButton             compute;
    
    ROISelector(StatsResultsPaneMng mng, String s)
    {
        //super(mng.getReferenceFrame(), "ROI selector", true);
        initComponents(mng.getListROIs());
        new ROISelectorMng(this, mng);
        buildGUI(s);
        pack();
    }
    
    private void initComponents(String[] listROIs)
    {
        box = new JComboBox(listROIs);
        compute = new JButton("Compute");
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(String s)
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(buildPanel(s), BorderLayout.CENTER);
        c.add(buildBottom(), BorderLayout.SOUTH);
    }
    
    private JPanel buildPanel(String s)
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(
                new JLabel("Select the "+s+" ROI")));
        p.add(UIUtilities.buildComponentPanel(box));
        return UIUtilities.buildComponentPanel(p);

    }
    
    private JPanel buildBottom() 
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(compute);
        return p;
    }
    
}
