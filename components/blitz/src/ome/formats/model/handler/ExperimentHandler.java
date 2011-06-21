/*
 * ome.formats.model.handler.ExperimentHandler
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2010 University of Dundee. All rights reserved.
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
import omero.model.Experiment;
import omero.model.ExperimentType;
import omero.model.IObject;

/**
 * A model object handler that handles objects of type Experiment.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
class ExperimentHandler implements ModelObjectHandler
{
    /** Our enumeration provider. */
    private EnumerationProvider enumProvider;

    /** The class we're a handler for. */
    static final Class<? extends IObject> HANDLER_FOR = Experiment.class;

    /**
     * Default constructor.
     * @param enumHandler Enumeration provider we are to use.
     */
    ExperimentHandler(EnumerationProvider enumProvider)
    {
        this.enumProvider = enumProvider;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.handler.ModelObjectHandler#handle(omero.model.IObject)
     */
    public IObject handle(IObject object)
    {
        Experiment o = (Experiment) object;
        o.setType(enumProvider.getEnumeration(
                ExperimentType.class, "Unknown", false));
        return object;
    }

}
