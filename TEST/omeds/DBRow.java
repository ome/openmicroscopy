/*
 * omeds.DBRow
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
import java.sql.PreparedStatement;

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
public abstract class DBRow
{
	private String DELETE_STM;
	
	private int 			id;
	
	public int getID()
	{
		return id;
	}

	void setID(int id)
	{
		this.id = id;
	}
	
	void delete()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		DELETE_STM = "DELETE FROM "+getTableName()+" WHERE "+getIDColumnName()+
						"="+id;
		PreparedStatement ps = dbm.getPreparedStatement(DELETE_STM);
		ps.execute();
		ps.close();
	}
	
	public abstract String getTableName();
	public abstract String getIDColumnName();
	public abstract void fillFromDB(int id) throws Exception;
	public abstract void insert() throws Exception;
	public abstract void update() throws Exception;
	
}
