/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.util.VersionCompare;

import omero.RType;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.Dataset;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.sys.Parameters;
import omero.sys.ParametersI;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class Connect {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String version = "5.3.7";
        boolean b = VersionCompare.compare(version, "5.4.8") >= 0;
        System.err.println(b);
        String name = "test.tif";
        String ex = name.substring(name.lastIndexOf(".")+1);
        System.err.println(ex);
    }

}
