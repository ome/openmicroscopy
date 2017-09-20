/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.model;

import static omero.rtypes.rstring;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SmartPolylineI extends omero.model.PolylineI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        Shape s = asAwtShape();
        if (s == null) {
            return;
        }
        final PathIterator it = s.getPathIterator(Util.getAwtTransform(transform));
        double [] vals = new double[] {0, 0, 0, 0, 0, 0};
        double [] last_point = null;
        while (!it.isDone()) {
            it.currentSegment(vals);
            double [] new_point = new double[] {vals[0], vals[1]};
            if (last_point != null) {
                final Set<Point2D> points = Util.getQuantizedLinePoints(
                    new Line2D.Double(last_point[0], last_point[1], new_point[0], new_point[1]), null);
                for (Point2D p : points) cb.handle((int) p.getX(), (int) p.getY());
            }
            last_point = new_point;
            it.next();
        }
    }
    
    public Shape asAwtShape() {
        String str = this.points.getValue();
        if (str == null) {
            return null;
        }
        String path = Util.parsePointsToPath(str, false);
        return Util.parseAwtPath(path);
    }

    public List<Point> asPoints() {
        String str = this.points.getValue();
        if (str == null) {
            return null;
        }
        List<Point> points = Util.parsePoints(str);
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            StringBuilder sb = new StringBuilder();
            int sz = random.nextInt(20) + 1;
            for (int i = 0; i < sz; i++) {
                int x = random.nextInt(100);
                int y = random.nextInt(100);
                if (i > 0) {
                    sb.append(",");
                }
                Util.appendSvgPoint(sb, x, y);
            }
            this.points = rstring(sb.toString());
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }

}