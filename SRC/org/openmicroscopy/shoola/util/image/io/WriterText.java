/*
 * org.openmicroscopy.shoola.util.image.io.TextWriter
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

package org.openmicroscopy.shoola.util.image.io;


//Java imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.swing.table.AbstractTableModel;

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
public class WriterText
{
    
    /** Write the content of a 2-dimension table in a file. */
    public static void writeTableAsText(File f, AbstractTableModel table)
        throws Exception
    {
        if (table == null)
            throw new Exception("Table cannot be null.");
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        String s = "", tail = "";
        int col = table.getColumnCount();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < col; j++) {
                if (table.getColumnName(j) != null)
                    s += table.getColumnName(j)+": ";
                if (j < col-1) tail = ", ";
                s += table.getValueAt(i, j)+tail;
                tail = "";
            }
            s += ";";
            output.write(s);
            output.newLine();
            s = "";
        } 
        output.close();
    }
    
}
