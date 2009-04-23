/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.model;

import java.util.ArrayList;
import java.util.List;

public class SmartLineI extends omero.model.LineI implements SmartShape {
    public List<Point> asPath() {
        List<Point> points = new ArrayList<Point>();
        SmartPointI start = new SmartPointI();
        start.setCx(getX1());
        start.setCy(getY1());
        SmartPointI end = new SmartPointI();
        end.setCx(getX2());
        end.setCy(getY2());
        points.addAll(start.asPath());
        points.addAll(end.asPath());
        return points;
    }
}