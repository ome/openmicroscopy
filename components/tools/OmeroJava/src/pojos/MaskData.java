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
import java.awt.Color;


//Third-party libraries

//Application-internal dependencies
import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Line;
import omero.model.LineI;
import omero.model.Mask;
import omero.model.MaskI;
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
	 * Create a new instance of MaskData, creating a new MaskI Object.
	 */
	public MaskData()
	{
		this(0.0, 0.0, 0.0, 0.0, null);
	}
	
	/**
	 * Create a new instance of the MaskData, 
	 * @param x x-coordinate of the shape.
	 * @param y y-coordinate of the shape.
	 * @param widht width of the shape.
	 * @param height height of the shape.
	 * @param mask The mask image.
	 */
	public MaskData(double x, double y, double width, double height,
			BufferedImage mask)
	{
		super(new MaskI(), true);
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
		this.setMask(mask);
	}
	
	/**
	 * Returns the text of the shape.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		Mask shape = (Mask) asIObject();
		RString value = shape.getTextValue();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Sets the text of the shape.
	 * 
	 * @param text See above.
	 */
	public void setText(String text)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Mask shape = (Mask) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
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
	 * set the x-coordinate top-left corner of an untransformed mask.
	 * 
	 * @param x See above.
	 */
	public void setX(double x)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Mask shape = (Mask) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setX(rtypes.rdouble(x));
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
	 * set the y-coordinate top-left corner of an untransformed mask.
	 * 
	 * @param y See above.
	 */
	public void setY(double y)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Mask shape = (Mask) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setY(rtypes.rdouble(y));
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
	 * set width of an untransformed mask.
	 * 
	 * @param width See above.
	 */
	public void setWidth(double width)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Mask shape = (Mask) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setWidth(rtypes.rdouble(width));
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
	 * set height of an untransformed mask.
	 * 
	 * @param height See above.
	 */
	public void setHeight(double height)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Mask shape = (Mask) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setHeight(rtypes.rdouble(height));
	}
	
	/**
	 * Set the mask image.
	 * @param mask See above.
	 */
	public void setMask(BufferedImage mask)
	{
		
	}
	
	/**
	 * Returns the mask image.
	 * 
	 * @return See above.
	 */
	public BufferedImage getMask()
	{
/*		Mask shape = (Mask) asIObject();
		Byte[] data = shape.getBytes();
		if(data == null) return null;
		double width = getWidth();
		if(width==0) return null;
		double height = getHeight();
		if(height==0) return null;
		BufferedImage bufferedImage = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
		int bx, by;
		byte val;
     	for(int x = 0 ; x < (int)width ; x++)
			for(int y = 0 ; y < (int)height ; y++)
			{
				bx = x % 8 + 1;
				by = y * (int)height;
				byte byteval = (byte)(data[by] & bx);
				if(byteval==1)
					bufferedImage.setRGB(x, y, Color.white.getRGB());
			}
		return bufferedImage;*/
		return null;
	}

	
}
