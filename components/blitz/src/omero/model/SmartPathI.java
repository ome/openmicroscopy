/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

import static omero.rtypes.rdouble;
import static omero.rtypes.rstring;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.batik.parser.PathHandler;
import org.apache.batik.parser.PathParser;

public class SmartPathI extends omero.model.PathI implements SmartShape {

    public void areaPoints(PointCallback cb) {
        throw new UnsupportedOperationException();
    }
    
    public Shape asAwtShape() {
        String str = d.getValue();
        if (str == null) {
            return null;
        }
        return Util.parseAwtPath(str);
    }
    
    public List<Point> asPoints() {
        String str = d.getValue();
        if (str == null) {
            return null;
        }
        final List<Point> points = new ArrayList<Point>();
        PathParser pp = new PathParser();
        PathHandler ph = new SmartPathHandler(points);
        pp.setPathHandler(ph);
        pp.parse(str);
        assert Util.checkNonNull(points) : "Null points in " + this;
        return points;
    }

    public void randomize(Random random) {
        if (roi == null) {
            int x1 = random.nextInt(100);
            int y1 = random.nextInt(100);
            int x2 = random.nextInt(100);
            int y2 = random.nextInt(100);
            int x3 = random.nextInt(100);
            int y3 = random.nextInt(100);
            d = rstring(String.format("M %s %s L %s %s L %s %s z", x1, y1, x2,
                    y2, x3, y3));
        } else {
            throw new UnsupportedOperationException(
                    "Roi-based values unsupported");
        }
    }
}

class SmartPathHandler implements PathHandler {

    private final List<Point> points;

    private boolean started = false;

    private boolean finished = false;

    SmartPathHandler(List<Point> points) {
        this.points = points;
    }

    public void arcAbs(float arg0, float arg1, float arg2, boolean arg3,
            boolean arg4, float arg5, float arg6)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void arcRel(float arg0, float arg1, float arg2, boolean arg3,
            boolean arg4, float arg5, float arg6)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void closePath() throws org.apache.batik.parser.ParseException {
        addPoint(points.get(0).cx.getValue(), points.get(0).cy.getValue());
    }

    public void curvetoCubicAbs(float arg0, float arg1, float arg2, float arg3,
            float arg4, float arg5)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoCubicRel(float arg0, float arg1, float arg2, float arg3,
            float arg4, float arg5)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoCubicSmoothAbs(float arg0, float arg1, float arg2,
            float arg3) throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoCubicSmoothRel(float arg0, float arg1, float arg2,
            float arg3) throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoQuadraticAbs(float arg0, float arg1, float arg2,
            float arg3) throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoQuadraticRel(float arg0, float arg1, float arg2,
            float arg3) throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoQuadraticSmoothAbs(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void curvetoQuadraticSmoothRel(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void endPath() throws org.apache.batik.parser.ParseException {
        finished = true;
    }

    public void linetoAbs(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        addPoint(arg0, arg1);
    }

    public void linetoHorizontalAbs(float arg0)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void linetoHorizontalRel(float arg0)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void linetoRel(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void linetoVerticalAbs(float arg0)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void linetoVerticalRel(float arg0)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void movetoAbs(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        addPoint(arg0, arg1);
    }

    public void movetoRel(float arg0, float arg1)
            throws org.apache.batik.parser.ParseException {
        throw new UnsupportedOperationException();

    }

    public void startPath() throws org.apache.batik.parser.ParseException {
        started = true;
    }

    private void addPoint(double arg0, double arg1) {
        SmartPointI pt = new SmartPointI();
        pt.cx = rdouble(arg0);
        pt.cy = rdouble(arg1);
        points.add(pt);
    }

}
