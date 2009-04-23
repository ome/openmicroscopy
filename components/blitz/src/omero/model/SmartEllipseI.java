/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

import static omero.rtypes.rdouble;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class SmartEllipseI extends omero.model.EllipseI implements
        SmartShape {
    public List<Point> asPath() {
        double cx = getCx().getValue();
        double cy = getCy().getValue();
        double rx = getRx().getValue();
        double ry = getRy().getValue();

        Ellipse2D.Double e2d = new Ellipse2D.Double(cx, cy, rx, ry);
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
}