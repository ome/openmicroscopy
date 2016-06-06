/*
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
package omero.gateway.model;

import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Ellipse;
import omero.model.EllipseI;
import omero.model.Shape;

/**
 * Represents an ellipse in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *    <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *    <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class EllipseData
    extends ShapeData
{

    /**
     * Creates a new instance of Ellipse data from an existing shape.
     *
     * @param shape The shape this object represents.
     */
    public EllipseData(Shape shape)
    {
        super(shape);
    }

    /**
     * Creates a new instance of EllipseData.
     */
    public EllipseData()
    {
        this(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Creates a new instance of the EllipseData.
     *
     * @param x The x-coordinate of the center of the Ellipse.
     * @param y The y-coordinate of the center of the Ellipse.
     * @param radiusx The radius along the X-axis.
     * @param radiusy The radius along the Y-axis.
     */
    public EllipseData(double x, double y, double radiusx, double radiusy)
    {
        super(new EllipseI(), true);
        setX(x);
        setY(y);
        setRadiusX(radiusx);
        setRadiusY(radiusy);
    }

    /**
     * Returns the text of the shape.
     *
     * @return See above.
     */
    public String getText()
    {
        Ellipse shape = (Ellipse) asIObject();
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
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setTextValue(rtypes.rstring(text));
        setDirty(true);
    }

    /**
     * Returns the x-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getX()
    {
        Ellipse shape = (Ellipse) asIObject();
        RDouble value = shape.getX();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the x-coordinate of the center of the ellipse.
     *
     * @param x See above.
     */
    public void setX(double x)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setX(rtypes.rdouble(x));
        setDirty(true);
    }

    /**
     * Returns the y-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getY()
    {
        Ellipse shape = (Ellipse) asIObject();
        RDouble value = shape.getY();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the y-coordinate of the center of the ellipse.
     *
     * @param y See above.
     */
    public void setY(double y)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setY(rtypes.rdouble(y));
        setDirty(true);
    }

    /**
     * Returns the radius along the X-axis.
     *
     * @return See above.
     */
    public double getRadiusX()
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        RDouble value = shape.getRadiusX();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the radius along the X-axis.
     *
     * @param x the value to set.
     */
    public void setRadiusX(double x)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setRadiusX(rtypes.rdouble(x));
        setDirty(true);
    }

    /**
     * Returns the radius along the Y-axis.
     *
     * @return See above.
     */
    public double getRadiusY()
    {
        Ellipse shape = (Ellipse) asIObject();
        RDouble value = shape.getRadiusY();
        if (value == null) return 0;
        return value.getValue();
    }

    /**
     * Sets the radius along the Y-axis.
     *
     * @param y The value to set.
     */
    public void setRadiusY(double y)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Ellipse shape = (Ellipse) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setRadiusY(rtypes.rdouble(y));
        setDirty(true);
    }

}
