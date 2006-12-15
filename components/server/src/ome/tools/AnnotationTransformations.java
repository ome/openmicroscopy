/*
 * ome.tools.HierarchyTransformations
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools;

// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;

// Third-party libraries

// Application-internal dependencies

public class AnnotationTransformations {

    public static Map sortDatasetAnnotatiosn(Set result) {

        Map map = new HashMap();

        // SORT
        Iterator i = result.iterator();
        while (i.hasNext()) {
            DatasetAnnotation ann = (DatasetAnnotation) i.next();
            Long ds_id = ann.getDataset().getId();
            if (!map.containsKey(ds_id)) {
                map.put(ds_id, new HashSet());
            }
            ((Set) map.get(ds_id)).add(ann);
        }

        return map;
    }

    public static Map sortImageAnnotatiosn(Set result) {

        Map map = new HashMap();

        // SORT
        Iterator i = result.iterator();
        while (i.hasNext()) {
            ImageAnnotation ann = (ImageAnnotation) i.next();
            Long img_id = ann.getImage().getId();
            if (!map.containsKey(img_id)) {
                map.put(img_id, new HashSet());
            }
            ((Set) map.get(img_id)).add(ann);
        }

        return map;
    }
}
