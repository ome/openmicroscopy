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
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

public class SmartRectI extends omero.model.RectangleI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        Shape s = asAwtShape();
        if (s == null) {
            return;
        }
        Rectangle2D r = s.getBounds2D();
        Util.pointsByBoundingBox(s, r, cb);
    }

    public Shape asAwtShape() {
        double[] d = data();
        if (d == null) {
            return null;
        }
        Rectangle2D.Double rect = new Rectangle2D.Double(d[0], d[1], d[2], d[3]);
        return rect;
    }

    public List<Point> asPoints() {
        double[] d = data();
        if (d == null) {
            return null;
        }
        List<Point> points = Util.points(d[0], d[1], d[2], d[3]);
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            x = rdouble(random.nextInt(100));
            y = rdouble(random.nextInt(100));
            width = rdouble(random.nextInt(100));
            height = rdouble(random.nextInt(100));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }

    private double[] data() {
        try {
            double x = getX().getValue();
            double y = getY().getValue();
            double w = getWidth().getValue();
            double h = getHeight().getValue();
            return new double[] { x, y, w, h };
        } catch (NullPointerException npe) {
            return null;
        }
    }
}