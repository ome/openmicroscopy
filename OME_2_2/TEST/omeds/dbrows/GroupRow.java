/*
 * omeds.dbrows.GroupRow
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

package omeds.dbrows;

//Java imports
import java.sql.PreparedStatement;
import java.sql.Types;

//Third-party libraries

//Application-internal dependencies
import omeds.DBManager;
import omeds.DBRow;

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
public class GroupRow
	extends DBRow
{

	private static String	INSERT_STM, UPDATE_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO groups ");
		buf.append("(attribute_id, leader, name, contact)");
		buf.append(" VALUES (?, ?, ?, ?)");
		INSERT_STM = buf.toString();
		
		//UPDATE_STM
		buf = new StringBuffer();
		buf.append("UPDATE experimenters ");
		buf.append("SET leader = ?, name = ?, contact = ?, group_id = ? ");
		buf.append("WHERE attribute_id = ? ");
		UPDATE_STM = buf.toString();
	}
	
	
	private String			name;
	private Integer			leader;
	private Integer			contact;
	private ExperimenterRow expRow;
	
	public GroupRow(String name, ExperimenterRow expRow)
	{
		this.name = name;
		this.expRow = expRow;
		
	}
	
	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "groups";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getIDColumnName()
	 */
	public String getIDColumnName()
	{
		return "attribute_id";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#fillFromDB(java.sql.Connection, int)
	 */
	public void fillFromDB(int id)
		throws Exception
	{
		
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#insert(java.sql.Connection)
	 */
	public void insert()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(INSERT_STM);
		ps.setInt(1, getID());
		if (expRow != null) {
			ps.setInt(2, expRow.getID());
			ps.setInt(4, expRow.getID());
			Integer i = new Integer(expRow.getID());
			leader = i;
			contact = i;
		} else {
			ps.setNull(2, Types.INTEGER);
			ps.setNull(4, Types.INTEGER);
		} 
		ps.setString(3, name);
		ps.execute();
		ps.close();
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#update(java.sql.Connection)
	 */
	public void update()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(UPDATE_STM);
		if (expRow != null) {
			ps.setInt(1, expRow.getID());
			ps.setInt(3, expRow.getID());
		} else {
			ps.setNull(1, Types.INTEGER);
			ps.setNull(3, Types.INTEGER);
		} 
		ps.setString(4, name);
		ps.setInt(5, getID());
		ps.execute();
		ps.close();
	}
	
	public ExperimenterRow getExperimenterRow()
	{
		return expRow;
	}
	
	public Integer getContact()
	{
		return contact;
	}

	public Integer getLeader()
	{
		return leader;
	}

	public String getName() 
	{
		return name;
	}

	public void setExperimenterRow(ExperimenterRow expRow)
	{
		this.expRow = expRow;
	}
	
	public void setContact(Integer contact)
	{
		this.contact = contact;
	}

	public void setLeader(Integer leader)
	{
		this.leader = leader;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
