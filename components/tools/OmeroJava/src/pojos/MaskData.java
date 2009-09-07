/*
 * pojos.MaskData
 *
Ê*------------------------------------------------------------------------------
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
import java.awt.image.BufferedImage;


//Third-party libraries

//Application-internal dependencies
import omero.RDouble;
import omero.model.Mask;
import omero.model.Shape;


/**
 * Represents an Mask in the Euclidean space <b>R</b><sup>2</sup>.
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
public class MaskData 
	extends ShapeData
{

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape this object represents.
	 */
	public MaskData(Shape shape)
	{
		super(shape);
	}
	
	/**
	 * Returns the x-coordinate of the top left corner of the mask.
	 * 
	 * @return See above.
	 */
	public double getX()
	{
		Mask shape = (Mask) asIObject();
		RDouble value = shape.getX();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the y-coordinate of the top left corner of the mask.
	 * 
	 * @return See above.
	 */
	public double getY()
	{
		Mask shape = (Mask) asIObject();
		RDouble value = shape.getY();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the width of the mask.
	 * 
	 * @return See above.
	 */
	public double getWidth()
	{
		Mask shape = (Mask) asIObject();
		RDouble value = shape.getWidth();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the height of the mask.
	 * 
	 * @return See above.
	 */
	public double getHeight()
	{
		Mask shape = (Mask) asIObject();
		RDouble value = shape.getHeight();
		if (value == null) return 0;
		return value.getValue();
	}

	/**
	 * Returns the mask image.
	 * 
	 * @return See above.
	 */
	public BufferedImage getMask()
	{
		Mask shape = (Mask) asIObject();
		return null;
	}
	
}
