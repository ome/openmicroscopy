/*
 * omeds.dbrows.UserStateRow
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
import java.sql.ResultSet;
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
public class UserStateRow
	extends DBRow
{
	private static String	INSERT_STM, UPDATE_STM, FILL_FROM_DB_STM;
	private static String	EXP_ID = "experimenter_id";
	
	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO ome_sessions ");
		buf.append("(session_id, feature_view, dataset_id, host, ");
		buf.append("last_access, started, experimenter_id, image_view, ");
		buf.append("project_id)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
	
		//UPDATE_STM
		buf = new StringBuffer();
		buf.append("UPDATE ome_sessions ");
		buf.append("SET feature_view = ?, dataset_id = ?, host = ?, ");
		buf.append("last_access = ?, started = ?, experimenter_id = ?, ");
		buf.append("image_view = ?, project_id = ? ");
		buf.append("WHERE session_id = ?");
		UPDATE_STM = buf.toString();
		
		//FILL_FROM_DB_STM
		buf = new StringBuffer();
		buf.append("SELECT * FROM ome_sessions ");
		buf.append("WHERE session_id = ?");
		FILL_FROM_DB_STM = buf.toString();
	}
	
	private ExperimenterRow		expRow;
	private Integer				datasetID;
	private Integer				projectID;
	private Timestamp			lastAccess;
	private Timestamp			started;
	private String				host;
	private String				featureView;
	private String				imageView;
	
	public UserStateRow(ExperimenterRow	expRow, Timestamp lastAccess, 
						Timestamp started)
	{
		this.expRow = expRow;
		this.lastAccess = lastAccess;
		this.started = started;
		host = null;
		featureView = null;
		imageView = null;
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "ome_sessions";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getIDColumnName()
	 */
	public String getIDColumnName()
	{
		return "session_id";
	}
	
	/* (non-Javadoc)
	 * @see omeds.DBRow#fillFromDB(java.sql.Connection, int)
	 */
	public void fillFromDB(int id)
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(FILL_FROM_DB_STM);
		ps.setInt(1, id);
		ResultSet   rs = ps.executeQuery();
		
		while( rs.next() ) {
			expRow.setID(rs.getInt(EXP_ID));	
		}
		ps.close();
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
		ps.setString(2, featureView);
		if (datasetID == null) ps.setNull(3, Types.INTEGER);
		else ps.setInt(3, datasetID.intValue());
		ps.setString(4, host);
		ps.setTimestamp(5, lastAccess);
		ps.setTimestamp(6, started);
		ps.setInt(7, expRow.getID());
		ps.setString(8, imageView);
		if (projectID == null) ps.setNull(9, Types.INTEGER);
		else ps.setInt(9, projectID.intValue());
		ps.execute();
		ps.close();
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#update()
	 */
	public void update()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(UPDATE_STM);
		ps.setString(1, featureView);
		if (datasetID == null) ps.setNull(2, Types.INTEGER);
		else ps.setInt(2, datasetID.intValue());
		ps.setString(3, host);
		ps.setTimestamp(4, lastAccess);
		ps.setTimestamp(5, started);
		ps.setInt(6, expRow.getID());
		ps.setString(7, imageView);
		if (projectID == null) ps.setNull(8, Types.INTEGER);
		else ps.setInt(8, projectID.intValue());
		ps.setInt(9, getID());
		ps.execute();
		ps.close();
	}

	public Integer getDatasetID()
	{
		return datasetID;
	}

	public ExperimenterRow getExperimenterRow()
	{
		return expRow;
	}

	public String getFeatureView()
	{
		return featureView;
	}

	public String getHost()
	{
		return host;
	}

	public String getImageView()
	{
		return imageView;
	}

	public Timestamp getLastAccess()
	{
		return lastAccess;
	}
	
	public String getLastAccessToString()
	{
		return lastAccess.toString();
	}
	
	public Integer getProjectID()
	{
		return projectID;
	}

	public Timestamp getStarted()
	{
		return started;
	}
	public String getStartedToString()
	{
		return started.toString();
	}
	
	public void setDatasetID(Integer datasetID)
	{
		this.datasetID = datasetID;
	}

	public void setExperimenterRow(ExperimenterRow expRow)
	{
		this.expRow = expRow;
	}

	public void setFeatureView(String featureView)
	{
		this.featureView = featureView;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setImageView(String imageView)
	{
		this.imageView = imageView;
	}

	public void setLastAccess(Timestamp lastAccess)
	{
		this.lastAccess = lastAccess;
	}
	
	public void setProjectID(Integer projectID)
	{
		this.projectID = projectID;
	}

	public void setStarted(Timestamp started)
	{
		this.started = started;
	}

}
