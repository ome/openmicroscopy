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

    public void areaPoints(PointCallback cb) {
        Shape s = asAwtShape();
        if (s == null) {
            return;
        }
        Rectangle2D r = s.getBounds2D();
        Util.pointsByBoundingBox(s, r, cb);
    }

    public Shape asAwtShape() {
        try {
            double x = getX().getValue();
            double y = getY().getValue();
            double radiusx = getRadiusX().getValue();
            double radiusy = getRadiusY().getValue();
            double height = radiusy * 2;
            double width = radiusx * 2;
            double cornerX = x - radiusx;
            double cornerY = y - radiusy;
            Ellipse2D.Double e = new Ellipse2D.Double(cornerX, cornerY, width,
                    height);
            return e;
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public List<Point> asPoints() {
        Ellipse2D.Double e2d = (Ellipse2D.Double) asAwtShape();
        if (e2d == null) {
            return null;
        }
        PathIterator it = e2d.getPathIterator(new AffineTransform(), 0.1f);
        List<Point> points = new ArrayList<Point>();
        final double[] coords = new double[6];
        while (!it.isDone()) {
            it.currentSegment(coords);
            SmartPointI pt = new SmartPointI();
            pt.setX(rdouble(coords[0]));
            pt.setY(rdouble(coords[1]));
            points.add(pt);
            it.next();
        }
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            x = rdouble(random.nextInt(100));
            y = rdouble(random.nextInt(100));
            radiusX = rdouble(random.nextInt(100));
            radiusY = rdouble(random.nextInt(100));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }
}