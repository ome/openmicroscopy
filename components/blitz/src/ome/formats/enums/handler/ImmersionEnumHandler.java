/*
 * ome.formats.enums.CorrectionEnumHandler
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

package ome.formats.enums.handler;

import java.util.HashMap;

import ome.formats.enums.EnumerationException;
import omero.model.IObject;
import omero.model.Immersion;

/**
 * An enumeration handler that handles enumerations of type Correction.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
class ImmersionEnumHandler implements EnumerationHandler
{
    /** Class we're a handler for. */
    static final Class<? extends IObject> HANDLER_FOR = Immersion.class;

    /** Array of enumeration patterns this handler uses for searching. */
    private static final PatternSet[] searchPatterns = new PatternSet[]
        {
            new PatternSet("^\\s*oil.*$", "Oil"),
            new PatternSet("^\\s*OI.*$", "Oil"),
            new PatternSet("^\\s*W", "Water"),
            new PatternSet("^\\s*UV", "Unknown"),
            new PatternSet("^\\s*Plan.*$", "Unknown"), // TODO: Remove when .nd2 bug which puts correction into immersion is fixed
            new PatternSet("^\\s*DRY", "Air") //TODO: This needs to be changed to "Air" when immersion enum bug is fixed in 4.1
        };

    /* (non-Javadoc)
     * @see ome.formats.enums.handler.EnumerationHandler#findEnumeration(java.util.HashMap, java.lang.String)
     */
    public IObject findEnumeration(HashMap<String, IObject> enumerations,
                                 String value)
    {
        for (PatternSet x : searchPatterns)
        {
            if (x.pattern.matcher(value).matches())
            {
                IObject enumeration = enumerations.get(x.value);
                if (enumeration == null)
                {
                    throw new EnumerationException(String.format(
                            "Matched value %s with regex %s. Could not " +
                            "find resulting value in enumerations.",
                            x.pattern.pattern(), x.value), HANDLER_FOR, value);
                }
                return enumeration;
            }
        }
        return null;
    }
}
