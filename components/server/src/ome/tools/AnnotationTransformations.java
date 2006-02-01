/* ome.tools.HierarchyTransformations
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.tools;

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;

//Third-party libraries

//Application-internal dependencies

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
