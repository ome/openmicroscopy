/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.DataElementType
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.datamodel;

/**
 * A typedef enum that indicates the type of an element in a DataAttribute
 * (or SemanticType, for that matter)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DataElementType
{
    // dummy definition
    private int value;

    // singleton constructor
    private DataElementType(int value)
    {
        this.value = value;
    }

    /**
     * Symbolic representation of an integer type.
     */
    public static final DataElementType INT = new DataElementType(0);

    /**
     * Symbolic representation of a long type.
     */
    public static final DataElementType LONG = new DataElementType(1);

    /**
     * Symbolic representation of a float type.
     */
    public static final DataElementType FLOAT = new DataElementType(2);

    /**
     * Symbolic representation of a double type.
     */
    public static final DataElementType DOUBLE = new DataElementType(3);

    /**
     * Symbolic representation of a boolean type.
     */
    public static final DataElementType BOOLEAN = new DataElementType(4);

    /**
     * Symbolic representation of a string type.
     */
    public static final DataElementType STRING = new DataElementType(5);

    /**
     * Symbolic representation of an attribute type.
     */
    public static final DataElementType ATTRIBUTE = new DataElementType(6);

    /**
     * Symbolic representation of an object type;
     */
    public static final DataElementType OBJECT = new DataElementType(7);

    /**
     * The canonical value of the type.  Makes switch statements easier.
     * @return The canonical value of the type.
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Overrides Object.equals() to force reference equality.
     */
    public boolean equals(Object o)
    {
        return o == this;
    }

    /**
     * Overrides the toString() so these values can be ordered in a Swing
     * view data model.
     */
    public String toString()
    {
        if (this == INT)
        {
            return "int";
        }
        else if (this == FLOAT)
        {
            return "float";
        }
        else if (this == LONG)
        {
            return "long";
        }
        else if (this == DOUBLE)
        {
            return "double";
        }
        else if (this == BOOLEAN)
        {
            return "boolean";
        }
        else if (this == STRING)
        {
            return "string";
        }
        else if (this == OBJECT)
        {
            return "object";
        }
        else if (this == ATTRIBUTE)
        {
            return "attribute";
        }
        else
        {
            return super.toString();
        }
    }

    /**
     * Maintains the equals()/hashCode() contract.
     */
    public int hashCode()
    {
        return value;
    }
}
