/*
 * omeds.dbrows.OwnerRow
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
public class ExperimenterRow
	extends DBRow
{

	private static String	INSERT_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO experimenters ");
		buf.append("(attribute_id, ome_name, firstname, data_dir, lastname,");
		buf.append(" email, group_id, institution)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
	}
	
	
	private String		omeName;
	private String		firstName;
	private String		lastName;
	private String		email;
	private String		institution;
	private String		dataDirectory;
	private Integer		groupID;
	
	public ExperimenterRow(String omeName, String firstName, 
							String dataDirectory, String lastName, String email,
							Integer groupID, String institution)
	{
		this.omeName = omeName;
		this.firstName = firstName;
		this.dataDirectory = dataDirectory;
		this.lastName = lastName;
		this.email = email;
		this.groupID = groupID;
		this.institution = institution;		
	}
	
	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "experimenters";
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
		ps.setString(2, omeName);
		ps.setString(3, firstName);
		ps.setString(4, dataDirectory);
		ps.setString(5, lastName);
		ps.setString(6, email);
		if (groupID == null) ps.setNull(7, Types.INTEGER);
		else ps.setInt(7, groupID.intValue());
		ps.setString(8, institution);
		ps.execute();
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#update(java.sql.Connection)
	 */
	public void update()
		throws Exception
	{
		// TODO Auto-generated method stub
		
	}

}
