/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

package omero.gateway.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;

import omero.gateway.model.DataObject;

/**
 * Utility methods for the Pojo {@link DataObject}s 
 * 
 *  @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class Pojos {

    /**
     * Extract the (distinct) ids from a collection of {@link DataObject}s
     * 
     * @param pojos
     *            The {@link DataObject}s
     * @return The ids
     */
    public static Collection<Long> extractIds(
            Collection<? extends DataObject> pojos) {
        Collection<Long> result = new HashSet<Long>(pojos.size());
        for (DataObject obj : pojos) {
            result.add(obj.getId());
        }
        return result;
    }

    /**
     * Get the relative complement of coll1 in coll2
     * 
     * @param coll1
     *            The collection
     * @param coll2
     *            The other collection
     * @return The elements of coll2 which are not part of coll1
     */
    public static <T extends DataObject> Collection<T> relativeComplement(
            Collection<T> coll1, Collection<T> coll2) {
        if (CollectionUtils.isEmpty(coll1))
            return coll2;
        if (CollectionUtils.isEmpty(coll2))
            return Collections.EMPTY_LIST;

        Collection<T> result = new ArrayList<T>();
        for (T t : coll2) {
            boolean found = false;
            for (T t2 : coll1) {
                if (t.getId() == t2.getId()) {
                    found = true;
                    break;
                }
            }

            if (!found)
                result.add(t);
        }
        return result;
    }
    
    /**
     * Checks if a DataObject Pojo is not null and has an ID
     * 
     * @param obj
     *            The DataObject
     * @return See above.
     */
    public static boolean hasID(DataObject obj) {
        return obj != null && obj.getId() >= 0;
    }
}
