/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane
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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
import org.openmicroscopy.shoola.env.rnd.roi.ROIStats;

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
public class StatsResultsPane
    extends JPanel
{
    
    public static final String[]   zAndtFieldNames = {"Z", "T", "Min", "Max", 
                                                "Mean", "StD", "Area", "Sum"};
    
    StatsTable                      table;
    
    private StatsResultsPaneMng     manager;
    
    public StatsResultsPane(ROIResultsMng mng, ROIStats stats, int sizeT, 
                            int sizeZ, int channel, int l, Icon up, Icon down)
    {
        manager = new StatsResultsPaneMng(this, mng, sizeT, sizeZ, stats, l);
        table = new StatsTable(manager.getData(channel), up, down);
        buildGUI(new BottomBar(manager, mng.getRegistry()));
    }
    
    public void showResultsForChannel(int channel)
    {
        table.setTableData(manager.getData(channel));
    }
    
    AbstractTableModel getTableModel()
    { 
        return (AbstractTableModel) table.getModel();
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(BottomBar bar)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(table));
        add(bar);
    }
    
}
