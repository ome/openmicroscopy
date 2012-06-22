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

package ome.formats.model.handler;

import java.util.HashMap;
import java.util.Map;

import ome.formats.enums.EnumerationProvider;
import omero.model.IObject;

/**
 * Factory for all available model handlers.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ModelObjectHandlerFactory
{
    /** A list of model object handlers for specific types. */
    private static Map<Class<? extends IObject>, ModelObjectHandler> handlers =
	new HashMap<Class<? extends IObject>, ModelObjectHandler>();

    /**
     * Default constructor.
     * @param enumProvider Enumeration provider for this factory to use.
     */
    public ModelObjectHandlerFactory(EnumerationProvider enumProvider)
    {
	handlers.put(ObjectiveHandler.HANDLER_FOR,
			     new ObjectiveHandler(enumProvider));
	handlers.put(DetectorHandler.HANDLER_FOR,
			     new DetectorHandler(enumProvider));
	handlers.put(ArcHandler.HANDLER_FOR,
			     new ArcHandler(enumProvider));
	handlers.put(FilamentHandler.HANDLER_FOR,
			     new FilamentHandler(enumProvider));
	handlers.put(LaserHandler.HANDLER_FOR,
			     new LaserHandler(enumProvider));
	handlers.put(LogicalChannelHandler.HANDLER_FOR,
	        new LogicalChannelHandler(enumProvider));
        handlers.put(MicroscopeHandler.HANDLER_FOR,
                new MicroscopeHandler(enumProvider));
        handlers.put(ExperimentHandler.HANDLER_FOR,
                new ExperimentHandler(enumProvider));
    }

    /** Our fall through no-op enumeration handler. */
    private ModelObjectHandler noopHandler = new NoopModelObjectHandler();

    /**
     * Returns an enumeration handler for a specific enumeration type.
     * @param klass Enumeration type to retrieve a handler for.
     * @return See above.
     */
    public ModelObjectHandler getHandler(Class<? extends IObject> klass)
    {
	return handlers.containsKey(klass)? handlers.get(klass) : noopHandler;
    }
}
