/*
 * omeds.dbrows.ImageRow
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
public class ImageRow
	extends DBRow
{

	private static String	INSERT_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO images ");
		buf.append("(image_id, pixels_id, created, group_id, inserted, name,");
		buf.append(" experimenter_id, image-guid, description)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
	}
	
	
	private Integer		pixelsID;
	private String		created;
	private String		inserted;
	private String		name;
	private String		description;
	private String		imageGuid;
	private	int			ownerID;
	private Integer		groupID;
	
	
	public ImageRow(Integer pixelsID, String created, Integer groupID,
					String inserted, String name, int ownerID, 
					String imageGuid, String description)
	{
		this.pixelsID = pixelsID;
		this.created = created;
		this.groupID = groupID;
		this.inserted = inserted;
		this.name = name;
		this.ownerID = ownerID;
		this.imageGuid = imageGuid;
		this.description = description;
		
	}
	
	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "images";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getIDColumnName()
	 */
	public String getIDColumnName()
	{
		return "image_id";
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
		if (pixelsID == null) ps.setNull(2, Types.INTEGER);
		else ps.setInt(2, groupID.intValue());
		ps.setString(3, created);
		if (groupID == null) ps.setNull(4, Types.INTEGER);
		else ps.setInt(4, groupID.intValue());
		ps.setString(5, inserted);
		ps.setString(6, name);
		ps.setInt(7, ownerID);
		ps.setString(8, imageGuid);
		ps.setString(9, description);
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
