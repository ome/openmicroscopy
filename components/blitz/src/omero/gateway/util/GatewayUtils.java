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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DoubleAnnotation;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.LongAnnotation;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.TimestampAnnotation;
import omero.model.TimestampAnnotationI;
import omero.model.Well;
import omero.model.WellSample;
import omero.model.XmlAnnotation;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DoubleAnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.FilesetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.MultiImageData;
import pojos.PixelsData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.TimeAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;
import pojos.XMLAnnotationData;

/**
 * Provides some utility/convenient methods for the {@link Gateway}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public abstract class GatewayUtils {

    /** Identifies the count property. */
    public static final String IMAGES_PROPERTY = "images";
    
    /** The collection of escaping characters we allow in the search. */
    public static final List<Character>    SUPPORTED_SPECIAL_CHAR;

    /** The collection of escaping characters we allow in the search. */
    public static final List<String>               WILD_CARDS;
    
    static {
        SUPPORTED_SPECIAL_CHAR = new ArrayList<Character>();
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('-'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('+'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('['));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(']'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(')'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('('));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(':'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('|'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('!'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('{'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('}'));
        SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('^'));
        WILD_CARDS = new ArrayList<String>();
        WILD_CARDS.add("*");
        WILD_CARDS.add("?");
        WILD_CARDS.add("~");
    }
    

    
    
    /**
     * Utility method to print an error message
     * 
     * @param e
     *            The exception to handle.
     * @return See above.
     */
    public static String printErrorText(Throwable e) {
        if (e == null)
            return "";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Determines the table name corresponding to the specified class.
     * 
     * @param klass
     *            The class to analyze.
     * @return See above.
     */
    public static String getTableForLink(Class klass) {
        String table = null;
        if (Dataset.class.equals(klass))
            table = "DatasetImageLink";
        else if (DatasetI.class.equals(klass))
            table = "DatasetImageLink";
        else if (Project.class.equals(klass))
            table = "ProjectDatasetLink";
        else if (ProjectI.class.equals(klass))
            table = "ProjectDatasetLink";
        else if (Screen.class.equals(klass))
            table = "ScreenPlateLink";
        else if (ScreenI.class.equals(klass))
            table = "ScreenPlateLink";
        else if (PlateAcquisition.class.equals(klass))
            table = "PlateAcquisitionWellSampleLink";
        else if (PlateAcquisitionI.class.equals(klass))
            table = "PlateAcquisitionWellSampleLink";
        else if (TagAnnotation.class.equals(klass))
            table = "AnnotationAnnotationLink";
        else if (TagAnnotationI.class.equals(klass))
            table = "AnnotationAnnotationLink";
        return table;
    }
    
    /**
     * Converts the specified type to its corresponding type for search.
     *
     * @param nodeType The type to convert.
     * @return See above.
     */
    public static String convertTypeForSearch(Class nodeType)
    {
            if (nodeType.equals(Image.class))
                    return ImageI.class.getName();
            else if (nodeType.equals(TagAnnotation.class) ||
                            nodeType.equals(TagAnnotationData.class))
                    return TagAnnotationI.class.getName();
            else if (nodeType.equals(BooleanAnnotation.class) ||
                            nodeType.equals(BooleanAnnotationData.class))
                    return BooleanAnnotationI.class.getName();
            else if (nodeType.equals(TermAnnotation.class) ||
                            nodeType.equals(TermAnnotationData.class))
                    return TermAnnotationI.class.getName();
            else if (nodeType.equals(FileAnnotation.class) ||
                            nodeType.equals(FileAnnotationData.class))
                    return FileAnnotationI.class.getName();
            else if (nodeType.equals(CommentAnnotation.class) ||
                            nodeType.equals(TextualAnnotationData.class))
                    return CommentAnnotationI.class.getName();
            else if (nodeType.equals(TimestampAnnotation.class) ||
                            nodeType.equals(TimeAnnotationData.class))
                    return TimestampAnnotationI.class.getName();
            throw new IllegalArgumentException("type not supported");
    }
    
    /**
     * Determines the table name corresponding to the specified class.
     *
     * @param klass The class to analyze.
     * @return See above.
     */
    public static String getTableForClass(Class klass)
    {
            if (DatasetData.class.equals(klass)) return "Dataset";
            else if (ProjectData.class.equals(klass)) return "Project";
            else if (ImageData.class.equals(klass)) return "Image";
            else if (ScreenData.class.equals(klass)) return "Screen";
            else if (PlateData.class.equals(klass)) return "Plate";
            else if (PlateAcquisitionData.class.equals(klass))
                    return "PlateAcquisition";
            return null;
    }

    /**
     * Converts the specified POJO into the corresponding model.
     * 
     * @param nodeType
     *            The POJO class.
     * @return The corresponding class.
     */
    public static Class convertPojos(DataObject node) {
        if (node instanceof FileData || node instanceof MultiImageData)
            return OriginalFile.class;
        return convertPojos(node.getClass());
    }

    /**
     * Converts the specified POJO into the corresponding model.
     * 
     * @param nodeType
     *            The POJO class.
     * @return The corresponding class.
     */
    public static Class convertPojos(Class nodeType) {
        if (ProjectData.class.equals(nodeType))
            return Project.class;
        else if (DatasetData.class.equals(nodeType))
            return Dataset.class;
        else if (ImageData.class.equals(nodeType))
            return Image.class;
        else if (BooleanAnnotationData.class.equals(nodeType))
            return BooleanAnnotation.class;
        else if (RatingAnnotationData.class.equals(nodeType)
                || LongAnnotationData.class.equals(nodeType))
            return LongAnnotation.class;
        else if (TagAnnotationData.class.equals(nodeType))
            return TagAnnotation.class;
        else if (TextualAnnotationData.class.equals(nodeType))
            return CommentAnnotation.class;
        else if (FileAnnotationData.class.equals(nodeType))
            return FileAnnotation.class;
        else if (TermAnnotationData.class.equals(nodeType))
            return TermAnnotation.class;
        else if (ScreenData.class.equals(nodeType))
            return Screen.class;
        else if (PlateData.class.equals(nodeType))
            return Plate.class;
        else if (WellData.class.equals(nodeType))
            return Well.class;
        else if (WellSampleData.class.equals(nodeType))
            return WellSample.class;
        else if (PlateAcquisitionData.class.equals(nodeType))
            return PlateAcquisition.class;
        else if (FileData.class.equals(nodeType)
                || MultiImageData.class.equals(nodeType))
            return OriginalFile.class;
        else if (GroupData.class.equals(nodeType))
            return ExperimenterGroup.class;
        else if (ExperimenterData.class.equals(nodeType))
            return Experimenter.class;
        else if (DoubleAnnotationData.class.equals(nodeType))
            return DoubleAnnotation.class;
        else if (XMLAnnotationData.class.equals(nodeType))
            return XmlAnnotation.class;
        else if (FilesetData.class.equals(nodeType))
            return Fileset.class;
        throw new IllegalArgumentException("NodeType not supported");
    }

    /**
     * Transforms the specified <code>property</code> into the corresponding
     * server value. The transformation depends on the specified class.
     * 
     * @param nodeType
     *            The type of node this property corresponds to.
     * @param property
     *            The name of the property.
     * @return See above.
     */
    public static String convertProperty(Class nodeType, String property) {
        if (nodeType.equals(DatasetData.class)) {
            if (property.equals(IMAGES_PROPERTY))
                return DatasetData.IMAGE_LINKS;
        } else
            throw new IllegalArgumentException("NodeType or "
                    + "property not supported");
        return null;
    }
    
    /**
     * Converts the class to the specified model string.
     *
     * @param pojo The class to convert.
     * @return See above.
     */
    public static String convertAnnotation(Class pojo)
    {
            if (TextualAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.CommentAnnotation";
            else if (TagAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.TagAnnotation";
            else if (RatingAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.LongAnnotation";
            else if (LongAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.LongAnnotation";
            else if (FileAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.FileAnnotation";
            else if (TermAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.UriAnnotation";
            else if (TimeAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.TimeAnnotation";
            else if (BooleanAnnotationData.class.equals(pojo))
                    return "ome.model.annotations.BooleanAnnotation";
            return null;
    }
    
    /**
     * Determines the table name corresponding to the specified class.
     *
     * @param klass The class to analyze.
     * @return See above.
     */
    public static String getTableForAnnotationLink(String klass)
    {
            String table = null;
            if (klass == null) return table;
            if (klass.equals(Dataset.class.getName()))
                    table = "DatasetAnnotationLink";
            else if (klass.equals(Project.class.getName()))
                    table = "ProjectAnnotationLink";
            else if (klass.equals(Image.class.getName()))
                    table = "ImageAnnotationLink";
            else if (klass.equals(Pixels.class.getName()))
                    table = "PixelAnnotationLink";
            else if (klass.equals(Annotation.class.getName()))
                    table = "AnnotationAnnotationLink";
            else if (klass.equals(DatasetData.class.getName()))
                    table = "DatasetAnnotationLink";
            else if (klass.equals(ProjectData.class.getName()))
                    table = "ProjectAnnotationLink";
            else if (klass.equals(ImageData.class.getName()))
                    table = "ImageAnnotationLink";
            else if (klass.equals(PixelsData.class.getName()))
                    table = "PixelAnnotationLink";
            else if (klass.equals(Screen.class.getName())) table =
                    "ScreenAnnotationLink";
            else if (klass.equals(Plate.class.getName()))
                    table = "PlateAnnotationLink";
            else if (klass.equals(ScreenData.class.getName()))
                    table = "ScreenAnnotationLink";
            else if (klass.equals(PlateData.class.getName()))
                    table = "PlateAnnotationLink";
            else if (klass.equals(DatasetI.class.getName()))
                    table = "DatasetAnnotationLink";
            else if (klass.equals(ProjectI.class.getName()))
                    table = "ProjectAnnotationLink";
            else if (klass.equals(ImageI.class.getName()))
                    table = "ImageAnnotationLink";
            else if (klass.equals(PixelsI.class.getName()))
                    table = "PixelAnnotationLink";
            else if (klass.equals(ScreenI.class.getName()))
                    table = "ScreenAnnotationLink";
            else if (klass.equals(PlateI.class.getName()))
                    table = "PlateAnnotationLink";
            else if (klass.equals(ScreenData.class.getName()))
                    table = "ScreenAnnotationLink";
            else if (klass.equals(PlateData.class.getName()))
                    table = "PlateAnnotationLink";
            else if (klass.equals(TagAnnotationData.class.getName()))
                    table = "AnnotationAnnotationLink";
            else if (klass.equals(PlateAcquisitionData.class.getName()))
                    table = "PlateAcquisitionAnnotationLink";
            else if (klass.equals(PlateAcquisitionI.class.getName()))
                    table = "PlateAcquisitionAnnotationLink";
            else if (klass.equals(PlateAcquisition.class.getName()))
                    table = "PlateAcquisitionAnnotationLink";
            return table;
    }
    
    
}
