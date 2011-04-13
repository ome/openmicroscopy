/*
 * ome.formats.EnumerationException
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.enums;

import omero.model.IObject;

/**
 * @author "Brian W. Loranger"
 */
public class EnumerationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /** The enumeration class that was used in a failed enumeration lookup. */
    private Class<? extends IObject>  failureClass;

    /** The enumeration value that was used in a failed enumeration lookup. */
    private String value;

    public EnumerationException(String message, Class<? extends IObject> klass,
                                String value)
    {
        super(message);
        this.failureClass = klass;
        this.value = value;
    }

    public Class<? extends IObject> getFailureClass()
    {
        return failureClass;
    }

    public String getValue()
    {
        return value;
    }

    public String toString()
    {
        return getMessage() + "'" + value + "' in '" + failureClass + "'.";
    }
}
