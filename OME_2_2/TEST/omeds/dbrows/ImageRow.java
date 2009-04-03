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
import java.sql.Timestamp;
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

	private static String	INSERT_STM, UPDATE_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO images ");
		buf.append("(image_id, created, group_id, inserted, name,");
		buf.append(" experimenter_id, image_guid, description)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
		
		buf = new StringBuffer();
		buf.append("UPDATE images ");
		buf.append("SET created = ?, group_id = ?, inserted = ?, name = ?, ");
		buf.append("experimenter_id = ?, image_guid = ?, description = ?, ");
		buf.append("pixels_id = ? ");
		buf.append("WHERE image_id = ?");
		UPDATE_STM = buf.toString();	
	}
	
	
	private Timestamp		created;
	private Timestamp		inserted;
	private String			name;
	private String			description;
	private String			imageGuid;
	private ExperimenterRow	expRow;
	private GroupRow		groupRow;
	private Integer			pixelID;
	
	public ImageRow(Timestamp created, GroupRow groupRow,
					Timestamp inserted, String name, ExperimenterRow expRow, 
					String imageGuid, String description)
	{
		this.created = created;
		this.groupRow = groupRow;
		this.inserted = inserted;
		this.name = name;
		this.expRow = expRow;
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
		ps.setTimestamp(2, created);
		if (groupRow == null) ps.setNull(3, Types.INTEGER);
		else ps.setInt(3, groupRow.getID());
		ps.setTimestamp(4, inserted);
		ps.setString(5, name);
		if (expRow == null) ps.setNull(6, Types.INTEGER);
		else ps.setInt(6, expRow.getID());
		ps.setString(7, imageGuid);
		ps.setString(8, description);
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
		ps.setTimestamp(1, created);
		if (groupRow == null) ps.setNull(2, Types.INTEGER);
		else ps.setInt(2, groupRow.getID());
		ps.setTimestamp(3, inserted);
		ps.setString(4, name);
		if (expRow == null) ps.setNull(5, Types.INTEGER);
		else ps.setInt(5, expRow.getID());
		ps.setString(6, imageGuid);
		ps.setString(7, description);
		if (pixelID == null) ps.setNull(8, Types.INTEGER);
		else ps.setInt(8, pixelID.intValue());
		ps.setInt(9, getID());
		ps.execute();
		ps.close();
	}
	
	public String getCreatedtoString()
	{
		return created.toString();
	}
	
	public Timestamp getCreated()
	{
		return created;
	}
	
	public String getDescription()
	{
		return description;
	}

	public ExperimenterRow getExperimenterRow()
	{
		return expRow;
	}

	public GroupRow getGroupRow()
	{
		return groupRow;
	}

	public String getImageGuid()
	{
		return imageGuid;
	}

	public String getInsertedtoString()
	{
		return inserted.toString();
	}
	
	public Timestamp getInserted()
	{
		return inserted;
	}
		
	public String getName() {
		return name;
	}

	public void setCreated(Timestamp created)
	{
		this.created = created;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setExperimenterRow(ExperimenterRow expRow)
	{
		this.expRow = expRow;
	}

	public void setGroupRow(GroupRow groupRow)
	{
		this.groupRow = groupRow;
	}

	public void setImageGuid(String imageGuid)
	{
		this.imageGuid = imageGuid;
	}

	public void setInserted(Timestamp inserted)
	{
		this.inserted = inserted;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getPixelID()
	{
		return pixelID;
	}

	public void setPixelID(Integer pixelID)
	{
		this.pixelID = pixelID;
	}

}
