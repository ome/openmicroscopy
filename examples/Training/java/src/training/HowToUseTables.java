/*
 * training.HowToUseTables 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;



//Java imports
import java.util.UUID;

//Third-party libraries

//Application-internal dependencies
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;

/** 
 * Follow samples code indicating how to use OMERO.tables
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class HowToUseTables
	extends ConnectToOMERO
{
	
    /**
     * Creates a number of empty rows.
     * 
     * @param rows The number of rows.
     * @return See above.
     */
    private Column[] createColumns(int rows) 
    {
        Column[] newColumns = new Column[2];
        newColumns[0] = new LongColumn("Uid", "", new long[rows]);
        newColumns[1] = new LongColumn("MyLongColumn", "", 
        		new long[rows]);
        return newColumns;
    }
    
	private void createTable()
		throws Exception
	{
		int rows = 1;
		String name = UUID.randomUUID().toString();
		Column[] columns = createColumns(rows);

		//create a new table.
		TablePrx table = entryUnencrypted.sharedResources().newTable(1, name);

		//initialize the table
		table.initialize(columns);
		//add data to the table.
		rows = 2;
		Column[] newRow = createColumns(rows);

    	LongColumn uids = (LongColumn) newRow[0];
    	LongColumn myLongs = (LongColumn) newRow[1];
    	for (int i = 0; i < rows; i++) {
    		uids.values[i] = i;
        	myLongs.values[i] = i;
		}
    	
		table.addData(newRow);

		OriginalFile file = table.getOriginalFile(); // if you need to interact with the table
		
		table.close();
		
		file = new OriginalFileI(file.getId(), false);
		//Open the table again
		table = entryUnencrypted.sharedResources().openTable(file);

		//read headers
		Column[] cols = table.getHeaders();
		
		for (int i = 0; i < cols.length; i++) {
			String colName = cols[i].name;
			System.err.println("Column"+colName);
		}

		// Depending on size of table, you may only want to read some blocks.
		long[] columnsToRead = new long[cols.length];
		for (int i = 0; i < cols.length; i++) {
			columnsToRead[i] = i;
		} 
		
		// The number of columns we wish to read.
		long[] rowSubset = new long[(int) (table.getNumberOfRows()-1)];
		for (int j = 0; j < rowSubset.length; j++) {
			rowSubset[j] = j;
		}
		Data data = table.slice(columnsToRead, rowSubset); // read the data.
		cols = data.columns;
		for (int j = 0; j < cols.length; j++) {
			Column c = cols[j];
			//do something
		}
		table.close(); // Important to close when done.
	}

	/**
	 * Connects and invokes the various methods.
	 */
	HowToUseTables()
	{
		try {
			connect();
			createTable();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new HowToUseTables();
		System.exit(0);
	}

}
