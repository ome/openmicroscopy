/*
 * ome.formats.Index
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package ome.formats;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public enum Index implements StringEnumeration
{
    // Index values
    BOOLEAN_ANNOTATION_INDEX("booleanAnnotationIndex"),
    CHANNEL_INDEX("channelIndex"),
    DATASET_INDEX("datasetIndex"),
    DETECTOR_INDEX("detectorIndex"),
    DICHROIC_INDEX("dichroicIndex"),
    DOUBLE_ANNOTATION_INDEX("doubleAnnotationIndex"),
    EXPERIMENT_INDEX("experimentIndex"),
    EXPERIMENTER_INDEX("experimenterIndex"),
    FILE_ANNOTATION_INDEX("fileAnnotationIndex"),
    FILTER_INDEX("filterIndex"),
    FILTER_SET_INDEX("filterSetIndex"),
    FOLDER_INDEX("folderIndex"),
    GROUP_INDEX("groupIndex"),
    IMAGE_INDEX("imageIndex"),
    INSTRUMENT_INDEX("instrumentIndex"),
    LIGHT_SOURCE_INDEX("lightSourceIndex"),
    LIGHT_SOURCE_SETTINGS_INDEX("lightSourceSettingsIndex"),
    LIST_ANNOTATION_INDEX("listAnnotationIndex"),
    LONG_ANNOTATION_INDEX("longAnnotationIndex"),
    MAP_ANNOTATION_INDEX("mapAnnotationIndex"),
    MICROBEAM_MANIPULATION_INDEX("microbeamManipulationIndex"),
    OBJECTIVE_INDEX("objectiveIndex"),
    ORIGINAL_FILE_INDEX("originalFileIndex"),
    PLANE_INDEX("planeIndex"),
    PLATE_ACQUISITION_INDEX("plateAcquisitionIndex"),
    PLATE_INDEX("plateIndex"),
    PROJECT_INDEX("projectIndex"),
    REAGENT_INDEX("reagentIndex"),
    ROI_INDEX("roiIndex"),
    SCREEN_INDEX("screenIndex"),
    SHAPE_INDEX("shapeIndex"),
    TAG_ANNOTATION_INDEX("tagAnnotationIndex"),
    TERM_ANNOTATION_INDEX("termAnnotationIndex"),
    COMMENT_ANNOTATION_INDEX("commentAnnotationIndex"),
    TIMESTAMP_ANNOTATION_INDEX("timestampAnnotationIndex"),
    WELL_INDEX("wellIndex"),
    WELL_SAMPLE_INDEX("wellSampleIndex"),
    XML_ANNOTATION_INDEX("xmlAnnotationIndex");

    /** The string-wise "value" of the Index. */
    private String value;

    /** Reverse lookup map. */
    private static final Map<String, Index> lookup =
        new HashMap<String, Index>();

    /** Initialize the reverse lookup map. */
    static
    {
        for (Index v : EnumSet.allOf(Index.class))
        {
            lookup.put(v.getValue(), v);
        }
    }

    /**
     * Default constructor.
     * @param value The index value. (For example: "imageIndex" for IMAGE_INDEX).
     */
    private Index(String value)
    {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see ome.formats.StringEnumeration#getValue()
     */
    public String getValue()
    {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return value;
    }

    /**
     * Retrieves an Index by reverse lookup of its stringwise "index".
     * @param value The stringwise "index" to look up.
     * @return The <code>Index</code> instance for the <code>value</code>.
     * @throws IllegalArgumentException If <code>value</code> cannot be
     * found in the reverse lookup table.
     */
    static Index get(String value)
    {
        Index toReturn = lookup.get(value);
        if (toReturn == null)
        {
            throw new IllegalArgumentException(
                    "Unable to find Index with value: " + value);
        }
        return toReturn;
    }
}
