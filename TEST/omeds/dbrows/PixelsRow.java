/*
 * omeds.dbrows.PixelsRow
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
public class PixelsRow
	extends DBRow
{
	private static String	INSERT_STM, UPDATE_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO image_pixels ");
		buf.append("(attribute_id, image_id, size_x, size_y, size_z, size_c, ");
		buf.append("size_t, bits_per_pixel, repository)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
		
		//UPDATE_STM
		buf = new StringBuffer();
	   	buf.append("UPDATE image_pixels ");
	   	buf.append("SET image_id = ?, size_x = ?, size_y = ?, size_z = ?, ");
	   	buf.append("size_c = ?, size_t = ?, bits_per_pixel = ?, ");
		buf.append("repositoty = ?");
	   	buf.append("WHERE attribute_id = ?");
		UPDATE_STM = buf.toString();
	}
	
	private ImageRow		imageRow;
	private RepositoryRow	repRow;
	private Integer			sizeX;
	private Integer			sizeY;	
	private Integer			sizeZ;
	private Integer			sizeC;
	private Integer			sizeT;
	private Integer			bitsPerPixel;
	
	public PixelsRow(ImageRow imageRow, Integer sizeX, Integer sizeY, 
					Integer sizeZ, Integer sizeC, Integer sizeT, 
					Integer bitsPerPixel, RepositoryRow repRow)
	{
		this.imageRow = imageRow;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.sizeC = sizeC;
		this.sizeT = sizeT;
		this.bitsPerPixel = bitsPerPixel;
		this.repRow = repRow;
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "image_pixels";
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
		ps.setInt(2, imageRow.getID());	
		if (sizeX == null) ps.setNull(3, Types.INTEGER);
		else ps.setInt(3, sizeX.intValue());
		if (sizeY == null) ps.setNull(4, Types.INTEGER);
		else ps.setInt(4, sizeY.intValue());
		if (sizeZ == null) ps.setNull(5, Types.INTEGER);
		else ps.setInt(5, sizeZ.intValue());
		if (sizeC == null) ps.setNull(6, Types.INTEGER);
		else ps.setInt(6, sizeC.intValue());
		if (sizeT == null) ps.setNull(7, Types.INTEGER);
		else ps.setInt(7, sizeT.intValue());
		if (bitsPerPixel == null) ps.setNull(8, Types.INTEGER);
		else ps.setInt(8, bitsPerPixel.intValue());
		if (repRow == null)	ps.setNull(9, Types.INTEGER);
		else ps.setInt(9, repRow.getID());
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
		ps.setInt(1, imageRow.getID());	
		if (sizeX == null) ps.setNull(2, Types.INTEGER);
		else ps.setInt(2, sizeX.intValue());
		if (sizeY == null) ps.setNull(3, Types.INTEGER);
		else ps.setInt(3, sizeY.intValue());
		if (sizeZ == null) ps.setNull(4, Types.INTEGER);
		else ps.setInt(4, sizeZ.intValue());
		if (sizeC == null) ps.setNull(5, Types.INTEGER);
		else ps.setInt(5, sizeC.intValue());
		if (sizeT == null) ps.setNull(6, Types.INTEGER);
		else ps.setInt(6, sizeT.intValue());
		if (bitsPerPixel == null) ps.setNull(7, Types.INTEGER);
		else ps.setInt(7, bitsPerPixel.intValue());
		if (repRow == null)	ps.setNull(8, Types.INTEGER);
		else ps.setInt(8, repRow.getID());
		ps.setInt(9, getID());
		ps.execute();
		ps.close();
	}

	public Integer getBitsPerPixel()
	{
		return bitsPerPixel;
	}
	
	public ImageRow getImageRow()
	{
		return imageRow;
	}

	public Integer getSizeC()
	{
		return sizeC;
	}

	public Integer getSizeT()
	{
		return sizeT;
	}

	public Integer getSizeX()
	{
		return sizeX;
	}

	public Integer getSizeY()
	{
		return sizeY;
	}

	public Integer getSizeZ()
	{
		return sizeZ;
	}

	public RepositoryRow getRepositoryRow()
	{
		return repRow;
	}
	
	public void setBitsPerPixel(Integer bitsPerPixel)
	{
		this.bitsPerPixel = bitsPerPixel;
	}

	public void setImageRow(ImageRow imageRow)
	{
		this.imageRow = imageRow;
	}

	public void setSizeC(Integer sizeC)
	{
		this.sizeC = sizeC;
	}

	public void setSizeT(Integer sizeT)
	{
		this.sizeT = sizeT;
	}

	public void setSizeX(Integer sizeX)
	{
		this.sizeX = sizeX;
	}

	public void setSizeY(Integer sizeY)
	{
		this.sizeY = sizeY;
	}
	
	public void setSizeZ(Integer sizeZ)
	{
		this.sizeZ = sizeZ;
	}
	
	public  void setRepositoryRow(RepositoryRow repRow)
	{
		this.repRow = repRow;
	}
}
