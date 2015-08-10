/*
 * training.HowToUseTables 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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




import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
//Application-internal dependencies
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.LongColumn;
import omero.grid.SharedResourcesPrx;
import omero.grid.TablePrx;
import omero.log.SimpleLogger;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import pojos.ExperimenterData;

/** 
 * Follow samples code indicating how to use OMERO.tables
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class HowToUseTables
{
	
	//Edit the information below
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	//end edit
	
	private Gateway gateway;
    
    private SecurityContext ctx;

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

	/** 
	 * Creates a table.
	 * @throws Exception
	 */
	private void createTable()
		throws Exception
	{
		int rows = 1;
		String name = UUID.randomUUID().toString();
		Column[] columns = createColumns(rows);

		//create a new table.
		SharedResourcesPrx store = null;
		TablePrx table = null;
		TablePrx table2 = null;
		try {
			store = gateway.getSharedResources(ctx);
			table = store.newTable(1, name);

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

			file = new OriginalFileI(file.getId(), false);
			//Open the table again
			table2 = store.openTable(file);

			//read headers
			Column[] cols = table2.getHeaders();
			
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
			long[] rowSubset = new long[(int) (table2.getNumberOfRows()-1)];
			for (int j = 0; j < rowSubset.length; j++) {
				rowSubset[j] = j;
			}
			Data data = table2.slice(columnsToRead, rowSubset); // read the data.
			cols = data.columns;
			for (int j = 0; j < cols.length; j++) {
				Column c = cols[j];
				//do something
			}
			
		} catch (Exception e) {
			throw new Exception("Cannot open table", e);
		} finally {
			if (table != null) table.close();
			if (table2 != null) table2.close();
		}
		
	}

	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	HowToUseTables(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
		}
		
		LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());
        
		try {
		    ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
			createTable();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			    gateway.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new HowToUseTables(null);
		System.exit(0);
	}

}
