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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmartPointI extends omero.model.PointI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        try {
            double point_x = x.getValue();
            double point_y = y.getValue();
            if (transform != null) {
                final AffineTransform t = Util.getAwtTransform(transform);
                
                final Point2D p = 
                    t.transform(new Point2D.Double(point_x, point_y), null);
                point_x = p.getX();
                point_y = p.getY();
            }
            cb.handle((int) point_x, (int) point_y);
        } catch (NullPointerException npe) {
            return;
        }
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
        if (x == null || y == null) {
            return null; // Could also pass self and let NPE happen later.
        }
        List<Point> points = Arrays.<Point> asList(this);
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            x = rdouble(random.nextInt(100));
            y = rdouble(random.nextInt(100));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }
}