/*
 * org.openmicroscopy.shoola.util.file.TextWriter
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

package org.openmicroscopy.shoola.util.file;


//Java imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

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
    
    private static final String ROW = "row";
    private static final String COLUMN = "column";
    private static final String NUMBER = "num";
    private static final String VALUE = "value";
    
    /** Save the content of a table as a text file. */
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
                //if (table.getColumnName(j) != null)
                //    s += table.getColumnName(j)+": ";
                if (j < col-1) tail = ", ";
                s += table.getValueAt(i, j)+tail;
                tail = "";
            }
            //s += ";";
            output.write(s);
            output.newLine();
            s = "";
        } 
        output.close();
    }
    
    /** Save the content of a table as an XML file. */
    public static void writeTableAsXML(File f, AbstractTableModel table)
        throws Exception
    {
        if (table == null)
            throw new Exception("Table cannot be null.");
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        output.write(WriterTextCst.XML_HEADER);
        output.newLine();
        output.write(writeOpenTag(WriterTextCst.ROOT, 
                        WriterTextCst.ATTRIBUTES_ROOT, false));
        output.newLine();
        writeTable(output, table);
        output.write(writeCloseTag(WriterTextCst.ROOT));
        output.close();
    }
    
    /** Write the table in an XML format. */
    private static void writeTable(BufferedWriter output, 
                                    AbstractTableModel table)
        throws Exception
    {
        HashMap rowAttributes = new HashMap(), 
                columnAttributes = new HashMap();
        String key = VALUE;
        for (int i = 0; i < table.getRowCount(); i++) {
            rowAttributes.put(NUMBER, ""+i);
            output.write(writeOpenTag(ROW, rowAttributes, false));
            output.newLine();
            for (int j = 0; j < table.getColumnCount(); j++) {
                columnAttributes.put(NUMBER, ""+j);
                if (table.getColumnName(j) != null) 
                    key = table.getColumnName(j);
                columnAttributes.put(key, ""+table.getValueAt(i, j));
                output.write(writeOpenTag(COLUMN, columnAttributes, true));
                output.newLine();
                columnAttributes.remove(key);
            }
            output.write(writeCloseTag(ROW));
            output.newLine();
        } 
    }
    
    /** Follow basic methods to write the tag. NOT A XML editor. */
    private static String writeOpenTag(String tagName, HashMap attributes, 
                                        boolean emptyTag)
    {
        String tag;
        tag = "<"+tagName;
        if (attributes != null) {
            Iterator i = attributes.keySet().iterator();
            String key, value;
            while (i.hasNext()) {
                key = (String) i.next();
                value = (String) attributes.get(key);
                tag+= " "+key+"=\""+value+"\"";
            }
        }
        if (emptyTag) tag +="/>";
        else tag +=">";
        return tag;
    }
    
    private static String writeCloseTag(String tagName)
    {
        String tag;
        tag = "</"+tagName+">";
        return tag;
    }
    
}
