/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import java.util.HashMap;
import java.util.Map;

import omero.RType;

public class PojoOptionsI {

    private final Map<String, RType> delegate = new HashMap<String, RType>();

    public Map<String, RType> map() {
        return delegate;
    }
}
