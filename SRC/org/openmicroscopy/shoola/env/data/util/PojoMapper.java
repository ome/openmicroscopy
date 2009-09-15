/*
 * org.openmicroscopy.shoola.env.data.util.PojoMapper
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.BooleanAnnotation;
import omero.model.CommentAnnotation;
import omero.model.Dataset;
import omero.model.DoubleAnnotation;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LongAnnotation;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.TagAnnotation;
import omero.model.TimestampAnnotation;
import omero.model.UriAnnotation;
import omero.model.Well;
import omero.model.WellSample;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DoubleAnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.TimeAnnotationData;
import pojos.URLAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

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
        else if (object instanceof UriAnnotation)
        	return new URLAnnotationData((UriAnnotation) object);
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
        } else if (object instanceof DoubleAnnotation) {
        	return new DoubleAnnotationData((DoubleAnnotation) object);
        } else if (object instanceof FileAnnotation) 
        	return new FileAnnotationData((FileAnnotation) object);
        else if (object instanceof BooleanAnnotation) {
        	return new BooleanAnnotationData((BooleanAnnotation) object);
        }  else if (object instanceof TimestampAnnotation) 
        	return new TimeAnnotationData((TimestampAnnotation) object);
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
        else if (object instanceof Well)
        	return new WellData((Well) object);
        else if (object instanceof WellSample)
        	return new WellSampleData((WellSample) object);
        else if (object instanceof Roi)
        	return new ROIData((Roi) object);
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
    
}
