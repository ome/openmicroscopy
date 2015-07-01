/*
 * org.openmicroscopy.shoola.env.data.util.PojoMapper
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.util;

//Java imports
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

//Third-party libraries
















//Application-internal dependencies
import omero.RString;
import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DoubleAnnotation;
import omero.model.Ellipse;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Label;
import omero.model.LongAnnotation;
import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.Mask;
import omero.model.Namespace;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;
import omero.model.PlateI;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.TimestampAnnotation;
import omero.model.TimestampAnnotationI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.XmlAnnotation;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DoubleAnnotationData;
import pojos.EllipseData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.MapAnnotationData;
import pojos.MaskData;
import pojos.PixelsData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.PointData;
import pojos.PolygonData;
import pojos.PolylineData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.RatingAnnotationData;
import pojos.RectangleData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextData;
import pojos.TextualAnnotationData;
import pojos.TimeAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;
import pojos.WorkflowData;
import pojos.XMLAnnotationData;

/** 
 * Helper methods to convert {@link IObject}s into their corresponding
 * {@link DataObject}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class PojoMapper
{

    /**
     * Helper method to convert the specified object into its corresponding
     * {@link DataObject} or collection of {@link DataObject}s.
     * 
     * @param value The object to convert.
     * @return      See above.
     */
    private static Object convert(Object value)
    {
        if (value instanceof IObject) return asDataObject((IObject) value);
        else if (value instanceof Collection) 
        	return asDataObjects((Collection) value);
        else if (value instanceof Map) return asDataObjects((Map) value);
        else return null;
    }
    
    /**
     * Converts the specified {@link IObject} into its corresponding 
     * {@link DataObject}.
     * 
     * @param object    The object to convert.
     * @return          See above.
     * @throws IllegalArgumentException If the object is null or 
     * if the type {@link IObject} is unknown.
     */
    public static DataObject asDataObject(IObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("IObject cannot be null.");
        if (object instanceof Project) 
            return new ProjectData((Project) object);
        else if (object instanceof Dataset) 
            return new DatasetData((Dataset) object);
        else if (object instanceof Image) 
        	return new ImageData((Image) object);
        else if (object instanceof TermAnnotation)
        	return new TermAnnotationData((TermAnnotation) object);
        else if (object instanceof TagAnnotation)
        	return new TagAnnotationData((TagAnnotation) object);
        else if (object instanceof CommentAnnotation) 
        	return new TextualAnnotationData((CommentAnnotation) object);
        else if (object instanceof LongAnnotation) {
        	LongAnnotation ann = (LongAnnotation) object;
        	RString ns = ann.getNs();
        	if (ns != null) {
        		if (RatingAnnotationData.INSIGHT_RATING_NS.equals(
        			ns.getValue()))
             		return new RatingAnnotationData(ann);	
        		return new LongAnnotationData(ann);
        	}
        	return new LongAnnotationData(ann);
        } else if (object instanceof DoubleAnnotation)
        	return new DoubleAnnotationData((DoubleAnnotation) object);
        else if (object instanceof FileAnnotation) 
        	return new FileAnnotationData((FileAnnotation) object);
        else if (object instanceof BooleanAnnotation)
        	return new BooleanAnnotationData((BooleanAnnotation) object);
        else if (object instanceof TimestampAnnotation) 
        	return new TimeAnnotationData((TimestampAnnotation) object);
        else if (object instanceof XmlAnnotation)
        	return new XMLAnnotationData((XmlAnnotation) object);
        else if (object instanceof Pixels) 
            return new PixelsData((Pixels) object);
        else if (object instanceof Experimenter) 
        	return new ExperimenterData((Experimenter) object); 
        else if (object instanceof ExperimenterGroup) 
            return new GroupData((ExperimenterGroup) object); 
        else if (object instanceof Screen)
        	return new ScreenData((Screen) object);
        else if (object instanceof Plate)
        	return new PlateData((Plate) object);
        else if (object instanceof PlateAcquisition)
            return new PlateAcquisitionData((PlateAcquisition) object);
        else if (object instanceof Well)
        	return new WellData((Well) object);
        else if (object instanceof WellSample)
        	return new WellSampleData((WellSample) object);
        else if (object instanceof Roi)
        	return new ROIData((Roi) object);
        else if (object instanceof Namespace) 
        	return new WorkflowData((Namespace) object);
        else if (object instanceof Fileset) 
        	return new FilesetData((Fileset) object);
        else if (object instanceof MapAnnotation)
        	return new MapAnnotationData((MapAnnotation) object);
        else if (object instanceof Rect)
            return new RectangleData((Rect) object);
        else if (object instanceof Ellipse)
            return new EllipseData((Ellipse) object);
        else if (object instanceof Point)
            return new PointData((Point) object);
        else if (object instanceof Polyline)
            return new PolylineData((Polyline) object);
        else if (object instanceof Polygon)
            return new PolygonData((Polygon) object);
        else if (object instanceof Mask)
            return new MaskData((Mask) object);
        else if (object instanceof Label)
            return new TextData((Label) object);
        return null;
    }
    
    /**
     * Converts each {@link IObject element} of the collection into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(Collection objects)
    {
    	if (objects == null) return new HashSet<DataObject>();
        Set<DataObject> set = new HashSet<DataObject>(objects.size());
        Iterator i = objects.iterator();
        DataObject data;
        while (i.hasNext()) {
        	data = asDataObject((IObject) i.next());
        	if (data != null) set.add(data);
        }
        return set;
    }
    
    /**
     * Converts each {@link IObject element} of the collection into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static List asDataObjectsAsList(Collection objects)
    {
    	if (objects == null) return new ArrayList<DataObject>();
        List<DataObject> set = new ArrayList<DataObject>(objects.size());
        Iterator i = objects.iterator();
        DataObject data;
        while (i.hasNext()) {
        	data = asDataObject((IObject) i.next());
        	if (data != null) set.add(data);
        }
        return set;
    }
    
    /**
     * Converts each {@link IObject element} of the collection into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(List objects)
    {
        if (objects == null) return new HashSet<DataObject>();
        Set<DataObject> set = new HashSet<DataObject>(objects.size());
        Iterator i = objects.iterator();
        DataObject data;
        while (i.hasNext()) {
        	data = asDataObject((IObject) i.next());
        	if (data != null) set.add(data);
        }
        return set;
    }
    
    /**
     * Converts each {@link IObject element} of the array into its 
     * corresponding {@link DataObject}.
     * 
     * @param objects   The set of objects to convert.
     * @return          A set of {@link DataObject}s.
     * @throws IllegalArgumentException If the set is <code>null</code>, doesn't
     * contain {@link IObject} or if the type {@link IObject} is unknown.
     */
    public static Set asDataObjects(IObject[] objects)
    {
    	Set<DataObject> set = new HashSet<DataObject>();
    	if (objects == null) return set;
    	DataObject data;
        for (int i = 0; i < objects.length; i++) {
        	data = asDataObject(objects[i]);
        	set.add(data);
        }
        return set;
    }

    /**
     * Converts each element of the list to a pair (key, value) in the map. 
     * 
     * The object in the list must be a IObject subclass and the key is the 
     * ID of the object.
     * 
     * @param keyClass The class that will be the key for the map
     * @param valueClass The class that will be the value for the map
     * @param method The method name as a string that, using reflection, 
     * 					will be used to get the key from the object.
     * @param objects   The map of objects to convert.
     * @return          A map of converted objects.
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws IllegalArgumentException If the map is <code>null</code> 
     * or if the type {@link IObject} is unknown.
     */
    public static <K, V extends DataObject>  Map<K, V> 
    								asDataObjectMap(Class<K> keyKlass, 
    										Class<V> valueKlass, 
    								String method, List objects) throws 
    								SecurityException, 
    								NoSuchMethodException, 
    								IllegalArgumentException, 
    								IllegalAccessException, 
    								InvocationTargetException
    {
    	Map<K, V> map = new TreeMap<K, V>();
    	V value;
    	Method meth;
    	K keyValue;
    	for (Object obj: objects)
    	{
    		value = (V) asDataObject((IObject)obj);
     		meth = (value.getClass()).getMethod(method);
    		keyValue = (K) meth.invoke(value, (Object[]) null);
    		map.put(keyValue, value);
    	}
   		return map;
    }
    
    /**
     * Converts each pair (key, value) of the map. If the key (resp. value) is
     * an {@link IObject}, the element is converted into its corresponding
     * {@link DataObject}.
     * 
     * @param objects   The map of objects to convert.
     * @return          A map of converted objects.
     * @throws IllegalArgumentException If the map is <code>null</code> 
     * or if the type {@link IObject} is unknown.
     */
    public static Map asDataObjects(Map objects)
    {
        if (objects == null) 
            throw new IllegalArgumentException("The map cannot be null.");
        Map<Object, Object> 
        	map = new HashMap<Object, Object>(objects.size());
        Set set = objects.entrySet();
        Entry entry;
        Iterator i = set.iterator();
        Object key, value;
        Object convertedKey = null;
        Object convertedValue = null;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
            key = entry.getKey();
            value = entry.getValue();
            convertedKey = convert(key);
            convertedValue = convert(value);
            map.put(convertedKey == null ? key : convertedKey, 
                    convertedValue == null ? value : convertedValue);
        }
        return map;
    }
    
    /**
     * Converts the specified type to its corresponding type for search by HQL query
     *
     * @param nodeType The type to convert.
     * @return See above.
     */
    public static String convertTypeForSearchByQuery(Class nodeType) {
        if (nodeType.equals(Image.class) || nodeType.equals(ImageData.class))
            return Image.class.getSimpleName();
        else if (nodeType.equals(Dataset.class)
                || nodeType.equals(DatasetData.class))
            return Dataset.class.getSimpleName();
        else if (nodeType.equals(Project.class)
                || nodeType.equals(ProjectData.class))
            return Project.class.getSimpleName();
        else if (nodeType.equals(Screen.class)
                || nodeType.equals(ScreenData.class))
            return Screen.class.getSimpleName();
        else if (nodeType.equals(Well.class) || nodeType.equals(WellData.class))
            return Well.class.getSimpleName();
        else if (nodeType.equals(Plate.class)
                || nodeType.equals(PlateData.class))
            return Plate.class.getSimpleName();
        throw new IllegalArgumentException("type not supported");
    }
    
    /**
     * Returns the name of the data type which has to used for Graph actions,
     * see {@link Requests}
     * 
     * @param dataType
     * @return See above
     */
    public static String getGraphType(Class<? extends DataObject> dataType) {

        // containers
        if (dataType.equals(DatasetData.class))
            return Dataset.class.getSimpleName();
        if (dataType.equals(ProjectData.class))
            return Project.class.getSimpleName();
        if (dataType.equals(ScreenData.class))
            return Screen.class.getSimpleName();
        if (dataType.equals(WellData.class))
            return Well.class.getSimpleName();
        if (dataType.equals(PlateData.class))
            return Plate.class.getSimpleName();
        if (dataType.equals(PlateAcquisitionData.class))
            return PlateAcquisition.class.getSimpleName();

        // annotations
        if (dataType.equals(AnnotationData.class))
            return Annotation.class.getSimpleName();
        if (dataType.equals(TagAnnotationData.class))
            return TagAnnotation.class.getSimpleName();
        if (dataType.equals(BooleanAnnotationData.class))
            return BooleanAnnotation.class.getSimpleName();
        if (dataType.equals(TermAnnotationData.class))
            return TermAnnotation.class.getSimpleName();
        if (dataType.equals(FileAnnotationData.class))
            return FileAnnotation.class.getSimpleName();
        if (dataType.equals(TextualAnnotationData.class))
            return CommentAnnotation.class.getSimpleName();
        if (dataType.equals(MapAnnotationData.class))
            return MapAnnotation.class.getSimpleName();
        if (dataType.equals(TimeAnnotationData.class))
            return TimestampAnnotation.class.getSimpleName();
        if (dataType.equals(XMLAnnotationData.class))
            return XmlAnnotation.class.getSimpleName();

        // other
        if (dataType.equals(ImageData.class))
            return Image.class.getSimpleName();
        if (dataType.equals(ROIData.class))
            return Roi.class.getSimpleName();

        throw new IllegalArgumentException("type not supported");
    }
    
    /**
     * Converts the specified type to its corresponding type for search.
     *
     * @param nodeType The type to convert.
     * @return See above.
     */
    public static String convertTypeForSearch(Class nodeType)
    {
            if (nodeType.equals(Image.class) || nodeType.equals(ImageData.class))
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
            else if (nodeType.equals(MapAnnotation.class) ||
                    nodeType.equals(MapAnnotationData.class))
            return MapAnnotationI.class.getName();
            else if (nodeType.equals(TimestampAnnotation.class) ||
                            nodeType.equals(TimeAnnotationData.class))
                    return TimestampAnnotationI.class.getName();
            else if (nodeType.equals(Dataset.class) ||
                    nodeType.equals(DatasetData.class))
                return DatasetI.class.getName();
            else if (nodeType.equals(Project.class) ||
                    nodeType.equals(ProjectData.class))
                return ProjectI.class.getName();
            else if (nodeType.equals(Screen.class) ||
                    nodeType.equals(ScreenData.class))
                return ScreenI.class.getName();
            else if (nodeType.equals(Well.class) ||
                    nodeType.equals(WellData.class))
                return WellI.class.getName();
            else if (nodeType.equals(Plate.class) ||
                    nodeType.equals(PlateData.class))
                return PlateI.class.getName();
            else if (nodeType.equals(PlateAcquisition.class) ||
                    nodeType.equals(PlateAcquisitionData.class))
                return PlateAcquisitionI.class.getName();
            throw new IllegalArgumentException("type not supported");
    }
}
