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
	private static String		MAX_ID_QUERY = "SELECT MAX(project_id) FROM projects";
	
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
	
	public void add(DBRow row)
	{
		if (row == null) throw new NullPointerException();
		processingQueue.add(row);
	}
	
	final void load()
		throws Exception
	{
		Connection c = DBManager.getInstance().getConnection();
		Iterator i = processingQueue.iterator();
		DBRow row;
		try {
			while (i.hasNext()) {
				row = (DBRow) i.next();
				row.setID(generateID(row));
				row.insert();
				doneCommands.add(row);
			}
			c.commit();
		} catch(Exception e) {
			c.rollback();
			throw e;
		} 
	}
	
	final void unload()
		throws Exception
	{
		Connection c = DBManager.getInstance().getConnection();
		Iterator i = doneCommands.iterator();
		DBRow row;
		try {
			while (i.hasNext()) {
				row = (DBRow) i.next();	
				row.delete();
			}
			c.commit();
		} catch(Exception e) {
			c.rollback();
			throw e;
		}
	}

	private int generateID(DBRow row)
		throws Exception
	{
		Integer id = (Integer) idGenerationMap.get(row.getClass());
		if (id == null) {
			DBManager dbm = DBManager.getInstance();
			PreparedStatement ps = dbm.getPreparedStatement(MAX_ID_QUERY);
			//ps.setString(1, row.getIDColumnName());
			//ps.setString(2, row.getTableName());
			ResultSet rs = ps.executeQuery();
			rs.next();
			id = new Integer(rs.getInt(1));	
			rs.close();	
		}
		id = new Integer(id.intValue()+1);
		idGenerationMap.put(row.getClass(), id);
		return id.intValue();
	}
}
