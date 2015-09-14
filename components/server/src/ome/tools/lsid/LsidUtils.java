/*
 * ome.tools.lsid.LsidUtils
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.tools.lsid;

public abstract class LsidUtils {

    /**
     * takes a field identifier as code-generated in each IObject class and
     * produces a back-end useable type name.
     */
    public static String parseType(String lsidProperty) {
        return lsidProperty.substring(0, lsidProperty.indexOf("_"));
    }

    /**
     * takes a field identifier as code-generated in each IObject class and
     * produces a back-end useable name.
     * 
     * TODO should change those fields from Strings to LSIDs with proper getters
     * to avoid this parsing overhead. TODO throw exceptions on invalid.
     * 
     * TODO possibly unused.
     */
    public static String parseField(String lsidProperty) {
        return lsidProperty.substring(lsidProperty.indexOf("_") + 1);
    }

}
