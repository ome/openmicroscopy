/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.model;

import java.util.Arrays;
import java.util.List;

public class SmartPointI extends omero.model.PointI implements SmartShape {
    public List<Point> asPath() {
        return Arrays.<Point> asList(this);
    }
}