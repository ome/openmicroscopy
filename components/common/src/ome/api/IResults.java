/*
 * ome.api.IResults
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IObject;

// Third-party libraries

// Application-internal dependencies

/**
 * Provides methods for storing, quering, and retrieving analysis results.
 * 
 * Note: Primitive operations will most likely be moved out to a separate
 * PrimitiveResults interface.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @author <br>
 *         Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @author <br>
 *         Sheldon Change &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:sheldons@mit.edu"> sheldons@mit.edu</a>
 * @author <br>
 *         Tony Sceflo &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:tonys@mit.edu">
 *         tonys@mit.edu</a>
 * 
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OMERO3.0
 * 
 */
public interface IResults extends ServiceInterface {

    /* Definiftions to make this compile */
    static class PixelSet {
    } // also known as RoiSets

    static interface Result extends IObject {
    } // marker for results

    /**
     * server-side parsing of data maps
     * 
     * @param Map
     *            of data. Keys are of the form: "ClassName:fieldName" ClassName
     *            must be unique within the map.
     * @DEV.TODO is this restriction necessary
     * @return created Result objects
     */
    Result[] parseData(Map data);

    /**
     * storing results.
     * 
     * @return PixelSet created by attachment
     */
    PixelSet store(int moduleId, Class type, int typeId, Result data);

    int storePrimitive(int moduleId, String type, int typeId, Map data); // for
                                                                            // Matlab,
                                                                            // etc.

    // bulk operations
    // Result[] can only contain one of each type
    PixelSet storeSingleBulk(int moduleId, Class type, int typeId, Result[] data);

    int storeSingleBulkPrimitive(int moduleId, Class type, int typeId,
            Map[] data);

    // possibility; array parameters must be of the same size
    PixelSet[] storeMultiBulk(int moduleId, Class[] types, int[] typeIds,
            Result[] data);

    int[] storeMultiBulkPrimitive(int moduleId, String[] types, int[] typeIds,
            Map[] data);

    /**
     * finding PixelSets. This users the type and id paramters to create a
     * temporary PixelSet and searches for a similar, existing PixelSet
     * 
     * In the case of Image,Pixel, or Roi5d, this should work fairly exactly.
     * (unless DefaultPixel has changed for Image). For Project, Dataset, and
     * other hierarchies, your mileage may vary.
     * 
     * @param id
     *            of the module which created the PixelSet
     * @param type
     *            to use as the <code>source</code> for this PixelSet
     * @param id
     *            of that type
     * @param Map
     *            of options. resultType=String or Class filtering on desired
     *            Result.class experimenter=id filtering on desired original
     *            result creator exactness=double percentage identical that two
     *            PixelSets must be (Whew!)
     * @return Set of PixelSet ids.
     */
    Set findPixelSets(int moduleId, Class type, int id, Map options);

    int[] findPixelSetsPrimitive(int moduleId, String type, int id, Map options);

    // bulk operations omitted

    /**
     * retrieving data for PixelSet
     * 
     * @param id
     *            of a PixelSet
     * @param list
     *            of keys; in format similar to
     *            {@link #store(int, Class, int, Result) store*} methods
     * @return a set of Result implementation
     */
    Set retrieveResult(int pixelSetId, List keys); // for performance! or
                                                    // List[]?

    Map retrieveResultPrimitive(int pixelSetId, String[] keys); // or
                                                                // String[][]/Map[]?
                                                                // hrm...

    Set[] retrieveResultsBulk(int[] pixelSetIds, List[] keys);

    Map[] retrieveResultsBulkPrimitive(int[] pixelSetIds, String[][] keys);

    /**
     * querying for IResults
     * 
     * @param resultType
     *            determines the table to be queried; could be the interface
     *            <code>Result</code> but then careful with orderBy
     * @param pixelSetIds
     *            restricts the search to certain PixelSets pixel_set_id in
     *            (1,2,3,....); can be null
     * @param orderBy
     *            field names on the table <code>resultType</code>; can be
     *            null
     * @param ascending
     *            true="asc", false="desc"; must be the same size as orderBy;
     *            can be byll
     * @param limit
     *            as in SQL; negative implies disabled
     * @param offset
     *            as in SQL; negative implies disabled
     * @param keys
     *            fields to be transported; currently unavailable
     * @param where
     *            THIS FIELD NEEDS SOME MORE THOUGHT; currently unavailable
     * @result an array of Result implementations which match the given
     *         criteria.
     */
    Result[] queryResults(Class resultType, Set pixelSetIds, String[] orderBy,
            boolean[] ascending, int limit, int offset);

    Map[] queryResultsPrimitive(String resultType, int[] pixelSetIds,
            String[] orderBy, boolean[] ascending, int limit, int offset);

}
