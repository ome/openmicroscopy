/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.PlateInfo
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Contains information about a particular plate-- such as its dimension
 * and names of individual rows and columns.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PlateInfo
{
    private List rowNames;
    private List colNames;
    
    private Comparator lexicalComparator = new Comparator()
    {
        public int compare(Object arg0, Object arg1)
        {
            if(arg0 == null || arg1 == null ||
               !(arg0 instanceof String) ||
               !(arg1 instanceof String))
            {
                return 0;
            }
    
            String s1 = (String)arg0;
            String s2 = (String)arg1;
        
            if(s1.length() < s2.length())
            {
                return -1;
            }
            else if(s1.length() > s2.length())
            {
                return 1;
            }
            else
            {
                return s1.compareTo(s2);
            }
        }
    };
    
    public PlateInfo()
    {
        rowNames = new ArrayList();
        colNames = new ArrayList();
    }
    
    /**
     * Adds a unique row name (such as 'A') to the plate.
     * @param name The row name to add.
     */
    public void addRowName(String name)
    {
        if(!rowNames.contains(name))
        {
            rowNames.add(name);
            Collections.sort(rowNames,lexicalComparator);
        }
    }
    
    /**
     * Adds a unique column name (such as '21') to the plate.
     * @param name The row name to add.
     */
    public void addColumnName(String name)
    {
        if(!colNames.contains(name))
        {
            colNames.add(name);
            Collections.sort(colNames,lexicalComparator);
        }
    }
    
    /**
     * Returns the number of rows in the plate.
     * @return See above.
     */
    public int getNumRows()
    {
        return rowNames.size();
    }
    
    /**
     * Returns the number of columns in the plate.
     * @return See above.
     */
    public int getNumCols()
    {
        return colNames.size();
    }
    
    /**
     * Returns the name of the row at the specified index.
     * @param index The index.
     * @return See above.
     */
    public String getRowName(int index)
    {
        return (String)rowNames.get(index);
    }
    
    /**
     * Returns the name of the column at the specified index.
     * @param index The index.
     * @return See above.
     */
    public String getColumnName(int index)
    {
        return (String)colNames.get(index);
    }
    
    /**
     * Gets the index of the specified row in the plate (alphabetically)
     * @param rowName The name of the row to retrieve the index of.
     * @return See above.
     */
    public int getRowIndex(String rowName)
    {
        return rowNames.indexOf(rowName);
    }
    
    /**
     * Gets the index of the specified column in the plate (lexicographically)
     * @param rowName The name of the row to retrieve the index of.
     * @return See above.
     */
    public int getColumnIndex(String colName)
    {
        return colNames.indexOf(colName);
    }
    
}
