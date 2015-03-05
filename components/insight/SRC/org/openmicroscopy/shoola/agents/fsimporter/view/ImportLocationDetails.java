/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.view;

import pojos.ExperimenterData;

/** 
 * Provides a transfer object for import location information
 *
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public class ImportLocationDetails {


    /** The datatype being loaded */
    private int dataType;

    /** The id of the user to load data for. */
    private ExperimenterData user;

    /**
     * Creates a new instance.
     *
     * @param dataType The type of data to retrieve.
     */
    public ImportLocationDetails(int dataType)
    {
        this(dataType, null);
    }

    /**
     * Creates a new instance.
     *
     * @param dataType The type of data to retrieve.
     * @param user The user to retrieve the data for or <code>null</code>.
     */
    public ImportLocationDetails(int dataType, ExperimenterData user)
    {
        this.dataType = dataType;
        this.user = user;
    }

    /**
     * Returns the data type to load.
     * @return see above.
     */
    public long getDataType() {
        return dataType;
    }

    /**
     * Returns the user to identify the data to load data for.
     * @return see above.
     */
    public ExperimenterData getUser() {
        return user;
    }
}
