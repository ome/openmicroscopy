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
import omero.model.DetectorType;
import omero.model.IObject;

/**
 * An enumeration handler that handles enumerations of type DetectorType.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
class DetectorTypeEnumHandler implements EnumerationHandler
{
	/** Class we're a handler for. */
	static final Class<? extends IObject> HANDLER_FOR = DetectorType.class;

	/** Array of enumeration patterns this handler uses for searching. */
	private static final PatternSet[] searchPatterns = new PatternSet[]
        {
			new PatternSet(".*EM.*CCD.*", "EM-CCD"),
			new PatternSet(".*CCD.*", "CCD")
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
