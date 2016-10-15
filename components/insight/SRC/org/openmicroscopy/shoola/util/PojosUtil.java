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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
import omero.model.PlateAcquisition;

/**
 * Provides some static utility methods for dealing the with Gateway
 * {@link DataObject}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 */
public class PojosUtil {

    /**
     * Checks if a given class is a container class, i. e. can contain multiple
     * images
     * 
     * @param type
     *            The class to check
     * @return See above.
     */
    public static boolean isContainerClass(Class<?> type) {
        return DatasetData.class.equals(type) || ProjectData.class.equals(type)
                || ScreenData.class.equals(type)
                || PlateData.class.equals(type)
                || PlateAcquisitionData.class.equals(type);
    }

    /**
     * Checks the permissions if the object (image, plate or well) can be
     * downloaded.
     * 
     * @param obj
     *            The object to check.
     *            
     * @return Returns <code>true</code> if the object can be downloaded,
     *         <code>false</code> otherwise.
     */
    public static boolean isDownloadable(DataObject obj) {
        if (obj instanceof ImageData) {
            ImageData img = (ImageData) obj;
            return img.isArchived()
                    && !img.asIObject()
                            .getDetails()
                            .getPermissions()
                            .isRestricted(
                                    omero.constants.permissions.BINARYACCESS.value);
        }

        if (obj instanceof PlateData) {
            PlateData p = (PlateData) obj;
            return !p
                    .asIObject()
                    .getDetails()
                    .getPermissions()
                    .isRestricted(
                            omero.constants.permissions.BINARYACCESS.value);
        }
        if (obj instanceof PlateAcquisitionData) {
            PlateAcquisitionData p = (PlateAcquisitionData) obj;
            PlateAcquisition pa = (PlateAcquisition) p.asIObject();
            return !pa
                    .getPlate()
                    .getDetails()
                    .getPermissions()
                    .isRestricted(
                            omero.constants.permissions.BINARYACCESS.value);
        }
        if (obj instanceof WellSampleData) {
            WellSampleData w = (WellSampleData) obj;
            return w.getImage().isArchived()
                    && !w.asWellSample()
                            .getWell()
                            .getPlate()
                            .getDetails()
                            .getPermissions()
                            .isRestricted(
                                    omero.constants.permissions.BINARYACCESS.value);
        }
        if (obj instanceof WellData) {
            WellData w = (WellData) obj;
            return w.getWellSamples().iterator().next().getImage().isArchived()
                    && !w.getPlate()
                            .asIObject()
                            .getDetails()
                            .getPermissions()
                            .isRestricted(
                                    omero.constants.permissions.BINARYACCESS.value);
        }

        return false;
    }
}
