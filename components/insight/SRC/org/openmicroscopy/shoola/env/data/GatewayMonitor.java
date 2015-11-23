/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import omero.gateway.Gateway;

/**
 * A simple {@link PropertyChangeListener} to monitor what happens in
 * the {@link Gateway}
 */
public class GatewayMonitor implements PropertyChangeListener {

    /** Simple format to display timestamps */
    private static final DateFormat df = new SimpleDateFormat("hh:mm ss.SSS");
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(df.format(new Date())+" - "+evt);
    }

}
