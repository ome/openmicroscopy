/*
 * org.openmicroscopy.shoola.env.data.OmeroSessionService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data;

/**
 * Provides access to OMERO sessions functionality using the current user's
 * login credentials.
 */
public interface OmeroSessionService {

    /**
     * Creates a new session with the specified timeout.
     * @param timeout the number of seconds of inactivity after which OMERO will
     *                automatically close the session.
     * @return the ID of the newly created session.
     * @throws Exception if an error occurs.
     */
    String create(int timeout) throws Exception;

    /**
     * Creates a new session to use for an offline import.
     * @return the ID of the newly created session.
     * @throws Exception if an error occurs.
     */
    String createOfflineImportSession() throws Exception;

}
