/*
 * omeds.dbrows.ProjectDatasetRow
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
public class ProjectDatasetMapRow
	extends DBRow
{
	private static String	INSERT_STM, DELETE_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO project_dataset_map ");
		buf.append("(dataset_id, project_id)");
		buf.append(" VALUES (?, ?)");
		INSERT_STM = buf.toString();
		
		//DELETE_STM
		buf = new StringBuffer();
		buf.append("DELETE FROM project_dataset_map ");
		buf.append("WHERE dataset_id = ? AND  project_id = ?");
		DELETE_STM = buf.toString();
	}
	
	private ProjectRow		projectRow;
	private DatasetRow		datasetRow;
	
	public ProjectDatasetMapRow(ProjectRow projectRow, DatasetRow datasetRow)
	{
		this.projectRow = projectRow;
		this.datasetRow = datasetRow;
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "project_dataset_map";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getIDColumnName()
	 */
	public String getIDColumnName()
	{
		return "project_id";
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
		ps.setInt(1, datasetRow.getID());	
		ps.setInt(2, projectRow.getID());
		ps.execute();
		ps.close();	
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#update()
	 */
	public void update()
		throws Exception
	{
	}
	
	/** Overwrites {@link omedsDBRow#delete} method. */
	public void delete()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(DELETE_STM);
		ps.setInt(1, datasetRow.getID());	
		ps.setInt(2, projectRow.getID());
		ps.execute();
		ps.close();	
	}
	
}
