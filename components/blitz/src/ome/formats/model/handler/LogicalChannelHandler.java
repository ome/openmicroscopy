/*
 * ome.formats.enums.EnumerationHandler
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

import ome.formats.enums.EnumerationProvider;
import omero.model.IObject;
import omero.model.LogicalChannel;
import omero.model.PhotometricInterpretation;

/**
 * A model object handler that handles objects of type Laser.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
class LogicalChannelHandler implements ModelObjectHandler
{
	/** Our enumeration provider. */
	private EnumerationProvider enumProvider;

	/** The class we're a handler for. */
	static final Class<? extends IObject> HANDLER_FOR = LogicalChannel.class;

	/**
	 * Default constructor.
	 * @param enumHandler Enumeration provider we are to use.
	 */
	LogicalChannelHandler(EnumerationProvider enumProvider)
	{
		this.enumProvider = enumProvider;
	}

	/* (non-Javadoc)
	 * @see ome.formats.model.handler.ModelObjectHandler#handle(omero.model.IObject)
	 */
	public IObject handle(IObject object)
	{
		LogicalChannel o = (LogicalChannel) object;
		o.setPhotometricInterpretation(enumProvider.getEnumeration(
				PhotometricInterpretation.class, "Monochrome", false));
		return object;
	}

}
