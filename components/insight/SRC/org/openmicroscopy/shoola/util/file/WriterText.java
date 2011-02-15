/*
 * org.openmicroscopy.shoola.util.file.TextWriter
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
import java.util.Map;
import java.util.Map.Entry;

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
    
    //TODO: MUST BE ASCII/CRLF= 0x0D 0x0A
    //private static final String RECORD_SEPARATOR = ""; 
    
    /** Save the content of a table as a text file. */
    public static void writeTableAsText(File f, AbstractTableModel table)
        throws Exception
    {
        if (table == null)
            throw new Exception("Table cannot be null.");
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        String tail = "";
        int col = table.getColumnCount();
        StringBuffer buffer = new StringBuffer();
        for (int k = 0; k < col; k++) {
            if (k < col-1) tail = ",";
            buffer.append(table.getColumnName(k));
            buffer.append(tail);
            tail = "";
        }
        output.write(buffer.toString());
        output.newLine();
       
        int i, j;
        for (i = 0; i < table.getRowCount(); i++) {
        	buffer = new StringBuffer();
        	for (j = 0; j < col; j++) {
        		if (j < col-1) tail = ",";
        		buffer.append(table.getValueAt(i, j));
        		buffer.append(tail);
        		tail = "";
        	}
            output.write(buffer.toString());
            output.newLine();
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
        Map rowAttributes = new HashMap(), 
                columnAttributes = new HashMap();
        String key = VALUE;
        int i, j;
        for (i = 0; i < table.getRowCount(); i++) {
            rowAttributes.put(NUMBER, ""+i);
            output.write(writeOpenTag(ROW, rowAttributes, false));
            output.newLine();
            for (j = 0; j < table.getColumnCount(); j++) {
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
    private static String writeOpenTag(String tagName, Map attributes, 
                                        boolean emptyTag)
    {
        String tag = "<"+tagName;
        if (attributes != null) {
        	Entry entry;
            Iterator i = attributes.entrySet().iterator();
            String key, value;
            while (i.hasNext()) {
            	entry = (Entry) i.next();
                key = (String) entry.getKey();
                value = (String)entry.getValue();
                tag+= " "+key+"=\""+value+"\"";
            }
        }
        if (emptyTag) tag += "/>";
        else tag += ">";
        return tag;
    }
    
    private static String writeCloseTag(String tagName)
    {
        return "</"+tagName+">";
    }
    
}
