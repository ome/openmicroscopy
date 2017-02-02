/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
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
package ome.formats.utests;

import java.lang.reflect.Method;
import java.util.HashMap;

import ome.formats.enums.EnumerationProvider;
import omero.RString;
import omero.model.IObject;


public class TestEnumerationProvider implements EnumerationProvider
{

    public <T extends IObject> T getEnumeration(Class<T> klass, String value,
                                                boolean loaded)
    {

        try {
            Class concreteClass = Class.forName(klass.getName() + "I");
            IObject enumeration = (IObject) concreteClass.newInstance();
            enumeration.setId(omero.rtypes.rlong(-1L));
            Method setValue = concreteClass.getMethod(
                    "setValue", new Class[] { RString.class });
            setValue.invoke(enumeration, omero.rtypes.rstring("Unknown"));
            return (T) enumeration;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T extends IObject> HashMap<String, T> getEnumerations(Class<T> klass)
    {
        throw new RuntimeException("Not implemented yet.");
    }

}
