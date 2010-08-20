/*
 * pojos.ShapeData
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.rtypes;
import omero.RInt;
import omero.RString;
import omero.model.Shape;

/**
 * Hosts a shape.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class ShapeData 
	extends DataObject
{

	/** Flag stating that the ROI is read only. */
	public static boolean READONLY_FLAG = true; 
	
	/** The representation of the shape. */
	protected ShapeSettingsData settings;
	
	/** Flag indicating that the shape been created client side. */
	private boolean clientObject;

	/**
	 * Converts the passed collection of points.
	 * 
	 * @param pts  The points to convert.
	 * @param type The value in the list to parse.
	 * @return See above.
	 */
	private String convertPoints(String pts, String type)
	{
		if (pts.length() == 0) return "";
		String exp = type+'[';
		int typeStr = pts.indexOf(exp, 0);
		int start = pts.indexOf('[', typeStr);
		int end = pts.indexOf(']', start);
		return pts.substring(start+1,end);
	}
	
	/**
	 * Parses out the type from the points string.
	 * 
	 * @param type The value in the list to parse.
	 * @return See above.
	 */
	protected String fromPoints(String type)
	{
		IObject o = asIObject();
		if (o instanceof Polygon) {
			Polygon shape = (Polygon) asIObject();
			return convertPoints(shape.getPoints().getValue(), type);
		} else if (o instanceof Polyline) {
			Polyline shape = (Polyline) asIObject();
			return convertPoints(shape.getPoints().getValue(), type);
		}
		throw new IllegalArgumentException("No shape specified.");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape to host.
	 */
	protected ShapeData(Shape shape)
	{
		this(shape, false);
	}
	
	/**
	 * Returns the settings associated to the shape.
	 * 
	 * @return See above.
	 */
	public ShapeSettingsData getShapeSettings()
	{
		return settings;
	}
	
	/**
	 * Set the settings associated to the shape.
	 * 
	 * @param shape See above.
	 */
	protected void setShapeSettings(Shape shape)
	{
		settings = new ShapeSettingsData(shape);
	}
	
	/**
	 * Is the object a read-only object.
	 * @return See above.
	 */
	public boolean isReadOnly()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		return (shape.getLocked().getValue()==READONLY_FLAG);
	}
	
	/**
	 * Set the Shape object to be readOnly
	 * @param readOnly See above.
	 */
	public void setReadOnly(boolean readOnly)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setLocked(rtypes.rbool(readOnly));
	}
	
	/**
	 * Is the object one that has been created client side. If so the id will
	 * be null, or invalid.
	 * @return See above.
	 */
	public boolean isClientObject()
	{
		return clientObject;
	}
	
	/**
	 * Set the object to be a client side object. If so the id will be null
	 * or invalid.
	 * @param clientSideObject See above.
	 */
	public void setClientObject(boolean clientSideObject)
	{
		clientObject = clientSideObject;
	}
	
	/**
	 * Returns the z-section.
	 * 
	 * @return See above.
	 */
	public int getZ()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RInt value = shape.getTheZ();
		if (value == null)
			return -1;
		return value.getValue();
	}

	/**
	 * Set the z-section.
	 * @param See above.
	 */
	public void setZ(int theZ)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTheZ(rtypes.rint(theZ));
		setDirty(true);
	}

	/**
	 * Returns the channel.
	 * 
	 * @return See above.
	 */
	public int getC()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RInt value = shape.getTheC();
		if (value == null)
			throw new IllegalArgumentException("No C Specified.");
		return value.getValue();
	}

	/**
	 * Set the channel
	 * @param See above.
	 */
	public void setC(int theC)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTheC(rtypes.rint(theC));
		setDirty(true);
	}

	
	/**
	 * Returns the timepoint.
	 * 
	 * @return See above.
	 */
	public int getT()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RInt value = shape.getTheT();
		if (value == null) 
			return -1;
		return value.getValue();
	}

	/**
	 * Set the timepoint.
	 * @param See above.
	 */
	public void setT(int theT)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTheT(rtypes.rint(theT));
		setDirty(true);
	}
	
	/** 
	 * Set the ROICoordinate for the ShapeData 
	 * @param roiCoordinate See above.
	 */
	public void setROICoordinate(ROICoordinate coord)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		setZ(coord.getZSection());
		setT(coord.getTimePoint());
		setDirty(true);
	}
	
	/** 
	 * Get the ROICoordinate for the ShapeData 
	 * @return See above.
	 */
	public ROICoordinate getROICoordinate()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		return new ROICoordinate(getZ(), getT());
	}
	
	
	/**
	 * Returns the transformation.
	 * 
	 * @return See above.
	 */
	public String getTransform()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RString value = shape.getTransform();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Set the Affine transform of the shape.
	 * 
	 * @param See above.
	 */
	public void setTransform(String transform)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTransform(rtypes.rstring(transform));
		setDirty(true);
	}

	/**
	 * Has the figure been changed from the server side version.
	 * @param dirty See above. 
	 */
	public void setDirty(boolean dirty)
	{
		super.setDirty(dirty);
	}
}
