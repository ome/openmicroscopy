/*
 * ome.tools.StringUtils
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools;

// Java imports

// Third-party libraries

// Application-internal dependencies

public class StringUtils {

    public static String getClassName(final Class arg0) {

        if (arg0 == null)
            throw new IllegalArgumentException("Class argument cannot be null.");

        String klass = arg0.getName();
        int last = klass.lastIndexOf(".");
        if (last == -1)
            return klass;

        return klass.substring(last + 1);
    }

}
