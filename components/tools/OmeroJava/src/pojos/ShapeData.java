/*
 * pojos.ShapeData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
	private ShapeSettingsData settings;
	
	/** Has this shape been created client side. */
	private boolean clientObject;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape to host.
	 */
	protected ShapeData(Shape shape)
	{
		super();
		setClientObject(false);
		setValue(shape);
		settings = new ShapeSettingsData(shape);
	}
	
	/** 
	 * Create a new instance of shapeData, this is a client side instance, 
	 * and so has to have the shape, shapeSettings set by the client.
	 */
	protected ShapeData()
	{
		super();
		setClientObject(true);
		setValue(null);
		settings = null;
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
	protected boolean isReadOnly()
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
	protected void setReadOnly(boolean readOnly)
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
	protected boolean isClientObject()
	{
		return clientObject;
	}
	
	/**
	 * Set the object to be a client side object. If so the id will be null
	 * or invalid.
	 * @param clientSideObject See above.
	 */
	protected void setClientObject(boolean clientSideObject)
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
		if (value == null) return -1;
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
		if (value == null) return -1;
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
	}

	
	
}
