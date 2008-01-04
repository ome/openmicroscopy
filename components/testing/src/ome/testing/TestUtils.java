/*
 * ome.testing.TestUtils
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.testing;

import java.util.HashSet;
import java.util.Set;

public class TestUtils {
    public static Set getSetFromInt(int[] ids) {
        Set set = new HashSet();
        for (int i = 0; i < ids.length; i++) {
            set.add(new Integer(ids[i]));
        }
        return set;
    }
}
