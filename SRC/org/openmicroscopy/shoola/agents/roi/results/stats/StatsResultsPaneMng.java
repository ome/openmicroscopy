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

import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
class StatsResultsPaneMng
{
    
    /** Action ID to identify the fieldNames for the table. */
    static final int                ZFIELD = 0, TFIELD = 1, ZTFIELD = 2, 
                                    ZANDTFIELD = 3;
    
    private static final String[]   zFieldNames = {"Z", "Min", "Max", "Mean",
                                                    "Sigma"};

    private static final String[]   tFieldNames = {"T", "Min", "Max", "Mean",
                                                    "Sigma"};

    private static final String[]   zAndtFieldNames = {"Z", "T", "Min", "Max", 
                                                    "Mean", "Sigma"};

    private static final String[]   ztFieldNames =  {"Min", "Max", "Mean", 
                                                    "Sigma"};

    private StatsResultsPane        view;
    
    private int                     aggregationIndex;
    
    private ROIResultsMng           mng;
    
    StatsResultsPaneMng(StatsResultsPane view, ROIResultsMng mng)
    {
        this.view = view;
        this.mng = mng;
        aggregationIndex = ZANDTFIELD;
    }
    
    int getAggregationIndex() { return aggregationIndex; }
    
    /** Aggregate results on Z. */
    void aggregateOnZ()
    {
        aggregationIndex = ZFIELD;
        resetTable();
    }
    
    /** Aggregate results on T. */
    void aggregateOnT()
    {
        aggregationIndex = TFIELD; 
        resetTable();
    }
    
    /** Aggregate results on Z and T. */
    void aggregateOnZAndT()
    {
        aggregationIndex = ZTFIELD; 
        resetTable();
        
    }
    
    void displayZandT()
    {
        aggregationIndex = ZANDTFIELD;
        resetTable();
    }
    
    private void resetTable()
    {
        //need to retrieve the data
        view.table.initTable(setDefault(), getFieldsNames());
    }
    
    String[] getFieldsNames()
    {
        String[] names = zAndtFieldNames;
        switch (aggregationIndex) {
            case ZFIELD:
                names = zFieldNames;
                break;
            case TFIELD:
                names = tFieldNames;
                break;
            case ZTFIELD:
                names = ztFieldNames; 
            break;
            case ZANDTFIELD:
                names = zAndtFieldNames; 
        }
        return names;
    }
    
    /** Reset the table in the viewPort. */
    private String[][] setDefault()
    {
        String[][] data =  new String[2][10];
        for (int i = 0; i < data.length; i++) {
            data[0][i] = ""+100*(i+1);
            data[1][i] = ""+50*(i+1);
        }
        return data;                        
    }

}
