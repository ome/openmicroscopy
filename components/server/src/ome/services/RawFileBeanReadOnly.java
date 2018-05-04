/*
 * Copyright (C) 2006-2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services;

import ome.annotations.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Raw file gateway which provides access to the OMERO file repository.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO3.0
 * @see RawFileBean
 */
@Transactional(readOnly = true)
public class RawFileBeanReadOnly extends RawFileBean {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(RawFileBean.class);

    private static final long serialVersionUID = 3885987391741946793L;

    /**
     * default constructor
     */
    public RawFileBeanReadOnly() {}

    /**
     * overridden to allow Spring to set boolean
     * @param checking
     */
    public RawFileBeanReadOnly(boolean checking) {
        super(checking);
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.StatefulServiceInterface#close()
     */
    @RolesAllowed("user")
    @Override
    public synchronized void close() {
        /* omits save() */
        clean();
    }
}
