/*
 * ome.formats.enums.EnumHandlerFactory
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
import java.util.Map;

import omero.model.IObject;

/**
 * Factory for all available enumeration handlers.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class EnumHandlerFactory
{
    /** A list of enumeration handlers for specific enumeration types. */
    private static Map<Class<? extends IObject>, EnumerationHandler> handlers =
	new HashMap<Class<? extends IObject>, EnumerationHandler>();

    static
    {
	handlers.put(CorrectionEnumHandler.HANDLER_FOR,
			     new CorrectionEnumHandler());
	handlers.put(ImmersionEnumHandler.HANDLER_FOR,
	            new ImmersionEnumHandler());
	handlers.put(OriginalFileFormatEnumHandler.HANDLER_FOR,
	        new OriginalFileFormatEnumHandler());
	handlers.put(DetectorTypeEnumHandler.HANDLER_FOR,
	        new DetectorTypeEnumHandler());
    }

    /** Our fall through no-op enumeration handler. */
    private EnumerationHandler noopHandler = new NoopEnumHandler();

    /**
     * Returns an enumeration handler for a specific enumeration type.
     * @param klass Enumeration type to retrieve a handler for.
     * @return See above.
     */
    public EnumerationHandler getHandler(Class<? extends IObject> klass)
    {
	return handlers.containsKey(klass)? handlers.get(klass) : noopHandler;
    }
}
