/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

import static omero.rtypes.rdouble;

import java.awt.Shape;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * THIS CLASS IS CURRENTLY BROKEN. A Text needs a position (an x/y-offset within
 * the image) which it is currently lacking. Therefore this implementation
 * blindly sets the offset to (0,0) so that all text will be in the upper-right
 * corner.
 */
public class SmartTextI extends omero.model.TextI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        cb.handle(0, 0);
    }

    public Shape asAwtShape() {
        List<Point> points = asPoints();
        if (points == null) {
            return null;
        }
        String path = Util.pointsToPath(points, true);
        return Util.parseAwtPath(path);
    }

    public List<Point> asPoints() {
        Point pt = new PointI();
        pt.cx = rdouble(0);
        pt.cy = rdouble(0);
        List<Point> points = Arrays.<Point> asList(pt);
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            // DO NOTHING FOR NOW
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }
}