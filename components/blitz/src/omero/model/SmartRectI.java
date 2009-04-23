/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.model;

import static omero.rtypes.rdouble;

import java.util.ArrayList;
import java.util.List;

public class SmartRectI extends omero.model.RectI implements SmartShape {
    public List<Point> asPath() {

        double x = getX().getValue();
        double y = getY().getValue();
        double w = getWidth().getValue();
        double h = getHeight().getValue();

        omero.RDouble x0 = getX();
        omero.RDouble y0 = getY();
        omero.RDouble x1 = rdouble(x + w);
        omero.RDouble y1 = rdouble(y + h);

        List<Point> points = new ArrayList<Point>();
        SmartPointI tl = new SmartPointI();
        tl.setCx(x0);
        tl.setCy(y0);
        points.add(tl);

        SmartPointI tr = new SmartPointI();
        tr.setCx(x1);
        tr.setCy(y0);
        points.add(tr);

        SmartPointI br = new SmartPointI();
        br.setCx(x1);
        br.setCy(y1);
        points.add(br);

        SmartPointI bl = new SmartPointI();
        bl.setCx(x0);
        bl.setCy(y1);
        points.add(bl);

        return points;
    }
}