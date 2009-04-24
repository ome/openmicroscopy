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
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartEllipseI extends omero.model.EllipseI implements SmartShape {

    public int[][] areaPoints() {
        Shape s = asAwtShape();
        Rectangle2D r = s.getBounds2D();
        return Util.pointsByBoundingBox(s, r);
    }

    public Shape asAwtShape() {
        double[] d = data();
        Ellipse2D.Double e = new Ellipse2D.Double(d[0], d[1], d[2], d[3]);
        return e;
    }

    public List<Point> asPoints() {
        double[] d = data();
        Ellipse2D.Double e2d = new Ellipse2D.Double(d[0], d[1], d[2], d[3]);
        PathIterator it = e2d.getPathIterator(new AffineTransform(), 1.0f);
        List<Point> points = new ArrayList<Point>();
        final double[] coords = new double[6];
        long count = 0;
        while (!it.isDone()) {
            it.currentSegment(coords);
            SmartPointI pt = new SmartPointI();
            pt.setCx(rdouble(coords[0]));
            pt.setCy(rdouble(coords[1]));
            points.add(pt);
            it.next();
        }
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            cx = rdouble(random.nextInt(100));
            cy = rdouble(random.nextInt(100));
            rx = rdouble(random.nextInt(100));
            ry = rdouble(random.nextInt(100));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }

    double[] data() {
        double cx = getCx().getValue();
        double cy = getCy().getValue();
        double rx = getRx().getValue();
        double ry = getRy().getValue();
        return new double[] { cx, cy, rx, ry };
    }
}