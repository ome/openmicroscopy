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
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartLineI extends omero.model.LineI implements SmartShape {

    public int[][] areaPoints() {
        throw new UnsupportedOperationException();
    }
    
    public Shape asAwtShape() {
        double x1 = getX1().getValue();
        double x2 = getX2().getValue();
        double y1 = getY1().getValue();
        double y2 = getY2().getValue();
        Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
        return line;
    }

    public List<Point> asPoints() {
        List<Point> points = new ArrayList<Point>();
        SmartPointI start = new SmartPointI();
        start.setCx(getX1());
        start.setCy(getY1());
        SmartPointI end = new SmartPointI();
        end.setCx(getX2());
        end.setCy(getY2());
        points.addAll(start.asPoints());
        points.addAll(end.asPoints());
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            x1 = rdouble(random.nextInt(100));
            x2 = rdouble(random.nextInt(100));
            y1 = rdouble(random.nextInt(100));
            y2 = rdouble(random.nextInt(100));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }

}