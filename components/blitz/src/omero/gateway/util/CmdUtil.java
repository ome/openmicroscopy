/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package omero.gateway.util;

import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.WellData;

public abstract class CmdUtil {

    /** Identifies the fileset as root. */
    public static final String REF_FILESET = "/Fileset";

    /** Identifies the image as root. */
    public static final String REF_IMAGE = "/Image";

    /** Identifies the dataset as root. */
    public static final String REF_DATASET = "/Dataset";

    /** Identifies the project as root. */
    public static final String REF_PROJECT = "/Project";

    /** Identifies the screen as root. */
    public static final String REF_SCREEN = "/Screen";

    /** Identifies the plate as root. */
    public static final String REF_PLATE = "/Plate";

    /** Identifies the ROI as root. */
    public static final String REF_ROI = "/Roi";

    /** Identifies the PlateAcquisition as root. */
    public static final String REF_PLATE_ACQUISITION = "/PlateAcquisition";

    /** Identifies the PlateAcquisition as root. */
    public static final String REF_WELL = "/Well";

    /** Identifies the Tag. */
    public static final String REF_ANNOTATION = "/Annotation";
    
    /** Identifies the group. */
    public static final String REF_GROUP = "/ExperimenterGroup";
    
    /**
     * Creates the string corresponding to the object to delete.
    *
    * @param data The object to handle.
    * @return See above.
    */
    public static String createDeleteCommand(String data)
   {
           if (ImageData.class.getName().equals(data)) return REF_IMAGE;
           else if (DatasetData.class.getName().equals(data)) return REF_DATASET;
           else if (ProjectData.class.getName().equals(data)) return REF_PROJECT;
           else if (ScreenData.class.getName().equals(data)) return REF_SCREEN;
           else if (PlateData.class.getName().equals(data)) return REF_PLATE;
           else if (ROIData.class.getName().equals(data)) return REF_ROI;
           else if (PlateAcquisitionData.class.getName().equals(data))
                   return REF_PLATE_ACQUISITION;
           else if (FilesetData.class.getName().equals(data)) return REF_FILESET;
           else if (WellData.class.getName().equals(data))
                   return REF_WELL;
           else if (PlateAcquisitionData.class.getName().equals(data))
                   return REF_PLATE_ACQUISITION;
           else if (TagAnnotationData.class.getName().equals(data) ||
                           TermAnnotationData.class.getName().equals(data) ||
                           FileAnnotationData.class.getName().equals(data) ||
                           TextualAnnotationData.class.getName().equals(data))
                   return REF_ANNOTATION;
           throw new IllegalArgumentException("Cannot delete the speficied type.");
   }
    
}
