/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.PlateInfoParser
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses various plate information.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PlateInfoParser
{
    /**
     * Builds plate information from a list of well names.  The names are
     * parsed as follows:
     * 
     * Well names are parsed into alpha and numeric.  Rows are determined to
     * be alpha, columns numeric.  Each distinct alpha prefix will be
     * considered a different row, and each distinct numeric suffix will be
     * considered a different column.  Rows and columns in the ensuing
     * PlateInfo will be ordered in lexicographic order-- AA after B, 50 after
     * 6, etc. (although that is an invariant maintained by the PlateInfo
     * object itself, not the Parser)
     *
     * @param wellNames The array of well names
     * @return A PlateInfo with the dimensions and list of row & column names
     *         contained in the list of well names.
     */
    public static PlateInfo buildPlateInfo(String[] wellNames)
    {
        PlateInfo info = new PlateInfo();
        for(int i=0;i<wellNames.length;i++)
        {
            String[] vector = parseName(wellNames[i]);
            info.addRowName(vector[0]);
            info.addColumnName(vector[1]);
        }
        return info;
    }
    
    /**
     * Gets the row from a well name.
     * @param wellName The well name.
     * @return The row name (alpha sequence at the beginning)
     */
    public static String getRow(String wellName)
    {
        Pattern alphaPattern = Pattern.compile("\\p{Alpha}+");
        Matcher alphaMatch = alphaPattern.matcher(wellName);
        alphaMatch.find();
        return alphaMatch.group();
    }
    
    /**
     * Gets the column from a well name.
     * @param wellName The well name.
     * @return The column name (number sequence at the end)
     */
    public static String getColumn(String wellName)
    {
        Pattern numericPattern = Pattern.compile("\\d+");
        Matcher numericMatch = numericPattern.matcher(wellName);
        numericMatch.find();
        return numericMatch.group();
    }
    
    // the function that extracts a row and column name.  Boy, this would be
    // easier with a regex.
    private static String[] parseName(String name)
    {
        return new String[] { getRow(name), getColumn(name) };
    }
}
