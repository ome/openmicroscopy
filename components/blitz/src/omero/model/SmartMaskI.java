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
import java.util.List;
import java.util.Random;

public class SmartMaskI extends omero.model.MaskI implements SmartShape {

    public int[][] areaPoints() {
        throw new UnsupportedOperationException();
    }
    
    public Shape asAwtShape() {
        throw new UnsupportedOperationException();
    }

    public List<Point> asPoints() {
        double x = getX().getValue();
        double y = getY().getValue();
        double w = getWidth().getValue();
        double h = getHeight().getValue();
        return Util.points(x, y, w, h);
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

}