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
}
