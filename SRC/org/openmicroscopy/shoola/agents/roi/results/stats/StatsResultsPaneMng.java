/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng
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
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
import org.openmicroscopy.shoola.agents.roi.results.stats.saver.ROISaver;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.roi.ROIPlaneStats;
import org.openmicroscopy.shoola.env.rnd.roi.ROIStats;
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
public class StatsResultsPaneMng
{
 
    private StatsResultsPane        view;
    
    private ROIResultsMng           mng;
    
    private ROIStats                stats;
    
    private int                     sizeT, sizeZ;

    private int                     lengthStats;
    
    public StatsResultsPaneMng(StatsResultsPane view, ROIResultsMng mng, 
                                int sizeT, int sizeZ, ROIStats stats, 
                                int numChannel)
    {
        this.view = view;
        this.sizeZ = sizeZ;
        this.sizeT = sizeT;
        this.mng = mng;
        this.stats = stats;
        lengthStats = stats.getSize()/numChannel;
    }

    public AbstractTableModel getTableModel() { return view.getTableModel(); }
    
    public Registry getRegistry() { return mng.getRegistry(); }
    
    public ROIAgtUIF getReferenceFrame() { return mng.getReferenceFrame(); }
    
    void saveTable() 
    {
        UIUtilities.centerAndShow(new ROISaver(this));
    }
    
    Object[][] getData(int channel)
    {
        Object[][] data = 
           new Object[lengthStats][StatsResultsPane.zAndtFieldNames.length];
        ROIPlaneStats p;
        int c = 0;
        for (int t = 0; t < sizeT; t++) {
            for (int z = 0; z < sizeZ; z++) {
                p = stats.getPlaneStats(z, channel, t);
                if (p != null) {
                    data[c][0] = new Integer(z);
                    data[c][1] = new Integer(t);
                    data[c][2] = new Double(p.getMin());
                    data[c][3] = new Double(p.getMax());
                    data[c][4] = new Double(p.getMean());
                    data[c][5] = new Double(p.getStandardDeviation());
                    data[c][6] = new Integer(p.getPointsCount());
                    data[c][7] = new Double(p.getSum());
                    c++;
                }
            }
        }
        return data;
    }

}
