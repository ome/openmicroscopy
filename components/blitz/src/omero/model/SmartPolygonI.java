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
import java.util.List;
import java.util.Random;

public class SmartPolygonI extends omero.model.PolygonI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        throw new UnsupportedOperationException();
    }
    
    public Shape asAwtShape() {
        String str = points.getValue();
        if (str == null) {
            return null;
        }
        String path = Util.parsePointsToPath(str, true);
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