/*
 * omeds.DBFixture
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

package omeds;


//Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
public class DBFixture
{

	/** Queue to order the tasks to be executed. */
	private List				processingQueue;

	/** The tasks that have currently been executed. */
	private Stack				doneCommands;

	private Map					idGenerationMap;
	
	/**
	 * Creates a new instance.
	 */
	public DBFixture()
	{
		processingQueue = new ArrayList();
		doneCommands = new Stack(); 
		idGenerationMap = new HashMap();
	}

	public void enlist(SQLCommand command)
	{
		if (command == null) throw new NullPointerException();
		processingQueue.add(command);
	}
	
	/** Inserts data in DB. */
	final void load()
		throws Exception
	{
		Connection c = DBManager.getInstance().getConnection();
		Iterator i = processingQueue.iterator();
		SQLCommand command;
		try {
			while (i.hasNext()) {
				command = (SQLCommand) i.next();
				command.execute();
				doneCommands.push(command);
			}
			c.commit();
		} catch(Exception e) {
			c.rollback();
			throw e;
		} 
	}
	
	/** Removes data inserted for testing. */
	final void unload()
		throws Exception
	{
		Connection c = DBManager.getInstance().getConnection();
		SQLCommand command;
		try {
			while (!doneCommands.isEmpty()) {
				command = (SQLCommand) doneCommands.pop();	
				command.undo();
			}
			c.commit();
		} catch(Exception e) {
			c.rollback();
			throw e;
		}
	}
	
	/** 
	 * Retrieves the highest value of a given column in a given table. 
	 * 
	 * @param row	DBRow object.
	 * @return int	Max+1;
	 * @throws an Exception If the ID cannot be retrieved.
	 */
	public int generateID(DBRow row)
		throws Exception
	{
		Integer id = (Integer) idGenerationMap.get(row.getClass());
		if (id == null) {
			DBManager dbm = DBManager.getInstance();
			String MAX_ID_QUERY = "SELECT MAX("+row.getIDColumnName()+ 
							") FROM "+row.getTableName();
			PreparedStatement ps = dbm.getPreparedStatement(MAX_ID_QUERY);
			ResultSet rs = ps.executeQuery();
			rs.next();
			id = new Integer(rs.getInt(1));	
			ps.close();	
		}
		id = new Integer(id.intValue()+1);
		idGenerationMap.put(row.getClass(), id);
		return id.intValue();
	}
	
}
