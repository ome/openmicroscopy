/*
 * org.openmicroscopy.shoola.env.data.util.PojoMapper
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.util;




//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;

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
        else if (value instanceof Set) return asDataObjects((Set) value);
        else if (value instanceof Map) return asDataObjects((Map) value);
        else return null;
    }
    
    /**
     * Converts the speficied {@link IObject} into its corresponding 
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
        else if (object instanceof CategoryGroup) 
            return new CategoryGroupData((CategoryGroup) object);
        else if (object instanceof Category) 
            return new CategoryData((Category) object);
        else if (object instanceof Image) 
            return new ImageData((Image) object);
        else if (object instanceof ImageAnnotation) 
            return new AnnotationData((ImageAnnotation) object);
        else if (object instanceof DatasetAnnotation) 
            return new AnnotationData((DatasetAnnotation) object);
        else if (object instanceof Pixels) 
            return new PixelsData((Pixels) object);
        else if (object instanceof Experimenter) 
            return new ExperimenterData((Experimenter) object); 
        else if (object instanceof ExperimenterGroup) 
            return new GroupData((ExperimenterGroup) object); 
        throw new IllegalArgumentException("Unknown IObject type: "+
                object.getClass().getName());
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
    public static Set asDataObjects(Set objects)
    {
        if (objects == null) 
            throw new IllegalArgumentException("The set cannot be null.");
        try {
            objects.toArray(new IObject[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException("The set only contains " +
                    "IObject.");
        }  
        HashSet set = new HashSet(objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext())
            set.add(asDataObject((IObject) i.next()));
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
        HashMap map = new HashMap(objects.size());
        Iterator i = objects.keySet().iterator();
        Object key, value;
        Object convertedKey = null;
        Object convertedValue = null;
        while (i.hasNext()) {
            key = i.next();
            value = objects.get(key);
            convertedKey = convert(key);
            convertedValue = convert(value);
            map.put(convertedKey == null ? key : convertedKey, 
                    convertedValue == null ? value : convertedValue);
        }
        return map;
    }
    
}
