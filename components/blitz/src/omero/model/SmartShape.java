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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.DefaultPointsHandler;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.PointsHandler;
import org.apache.batik.parser.PointsParser;
import org.apache.xerces.impl.xpath.regex.ParseException;

/**
 * Orthogonal interface hierarchy of types for working with the
 * {@link omero.model.Shape} hierarchy.
 * 
 * @since Beta4.1
 */
public interface SmartShape {

    /**
     * Utility class used as a mixin by all of the {@link SmartShape}
     * implementations. The inheritance hierarchy of Ice-generated objects
     * doesn't allow for simply subclassing .
     */
    public static class Util {

        /**
         * Used from assert statements of the form:
         * 
         * <pre>
         * assert Util.checkNonNull(points) : &quot;Null points in &quot; + this;
         * </pre>
         * 
         * in all the implementations of {@link SmartShape#asPoints()}.
         * 
         * @param points the points
         * @return false if iterating through the points list and dereferencing
         *         the x and y fields would cause a
         *         {@link NullPointerException}
         */
        public static boolean checkNonNull(List<Point> points) {
            if (points == null) {
                return false;
            }

            for (Point point : points) {
                if (point == null || point.x == null || point.y == null) {
                    return false;
                }
            }

            return true;
        }

        public static void appendDbPoint(StringBuilder sb, Point p) {
            appendDbPoint(sb, p.x.getValue(), p.y.getValue());
        }

        public static void appendDbPoint(StringBuilder sb, double x, double y) {
            sb.append("(");
            sb.append(x);
            sb.append(",");
            sb.append(y);
            sb.append(")");
        }

        public static void appendSvgPoint(StringBuilder sb, Point p) {
            appendSvgPoint(sb, p.x.getValue(), p.y.getValue());
        }

        public static void appendSvgPoint(StringBuilder sb, double x, double y) {
            sb.append(x);
            sb.append(",");
            sb.append(y);
            sb.append(" ");
        }

        public static boolean appendSegment(StringBuilder sb, boolean first,
                double x, double y) {
            if (first) {
                sb.append("M ");
                first = false;
            } else {
                sb.append("L ");
            }
            sb.append(x);
            sb.append(" ");
            sb.append(y);
            sb.append(" ");
            return first;
        }

        public static String pointsToPath(List<Point> points, boolean close) {
            StringBuilder sb = new StringBuilder(points.size() * 16);
            boolean first = true;
            for (Point point : points) {
                double x = point.getX().getValue();
                double y = point.getY().getValue();
                first = appendSegment(sb, first, x, y);
            }
            if (close) {
                sb.append("Z");
            }
            String path = sb.toString();
            return path;
        }

        public static String parsePointsToPath(String str, boolean close) {
            final StringBuilder sb = new StringBuilder();
            final boolean[] first = new boolean[] { true };
            PointsParser pp = new PointsParser();
            PointsHandler ph = new DefaultPointsHandler() {
                public void point(float x, float y) throws ParseException {
                    first[0] = appendSegment(sb, first[0], x, y);
                }
            };
            pp.setPointsHandler(ph);
            pp.parse(str);
            return sb.toString();
        }

        public static Shape parseAwtPath(String str) {
            PathParser pp = new PathParser();
            AWTPathProducer ph = new AWTPathProducer();
            pp.setPathHandler(ph);
            pp.parse(str);
            return ph.getShape();
        }

        public static List<Point> parsePoints(String str) {
            final List<Point> points = new ArrayList<Point>();
            PointsParser pp = new PointsParser();
            PointsHandler ph = new DefaultPointsHandler() {
                public void point(float x, float y) throws ParseException {
                    SmartPointI sp = new SmartPointI();
                    sp.setX(rdouble(x));
                    sp.setY(rdouble(y));
                    points.add(sp);
                }
            };
            pp.setPointsHandler(ph);
            pp.parse(str);
            return points;
        }

        /**
         * Returns the four corner points of a rectangle
         * 
         * @param x
         *            the top-left corner's x coordinate (the lowest x)
         * @param y
         *            the top-left corner's y coordinate (the lowest y)
         * @param w
         *            width of the rectangle so that x+w gives the highest x
         * @param h
         *            height of the rectange so taht y+h gives the highest y
         * @return a list of points of the form: (x,y),(x+w,y),(x+w,y+h),(x,y+h)
         */
        public static List<Point> points(double x, double y, double w, double h) {
            omero.RDouble x0 = rdouble(x);
            omero.RDouble y0 = rdouble(y);
            omero.RDouble x1 = rdouble(x + w);
            omero.RDouble y1 = rdouble(y + h);

            List<Point> points = new ArrayList<Point>();
            SmartPointI tl = new SmartPointI();
            tl.setX(x0);
            tl.setY(y0);
            points.add(tl);

            SmartPointI tr = new SmartPointI();
            tr.setX(x1);
            tr.setY(y0);
            points.add(tr);

            SmartPointI br = new SmartPointI();
            br.setX(x1);
            br.setY(y1);
            points.add(br);

            SmartPointI bl = new SmartPointI();
            bl.setX(x0);
            bl.setY(y1);
            points.add(bl);

            return points;
        }

        public static void pointsByBoundingBox(Shape s, Rectangle2D r,
                PointCallback cb) {

            double xEnd = (r.getX() + r.getWidth());
            double yEnd = (r.getY() + r.getHeight());
            double startX = r.getX();
            double startY = r.getY();

            for (double y = startY; y < yEnd; ++y) {
                for (double x = startX; x < xEnd; ++x) {
                    if (s.intersects(x, y, 0.001, 0.001)) {
                        cb.handle((int) x, (int) y);
                    }
                }
            }
        }

        public static java.awt.geom.AffineTransform getAwtTransform(AffineTransform transform) {
            if (transform == null) return null;
            return 
                new java.awt.geom.AffineTransform(
                    transform.getA00().getValue(),
                    transform.getA10().getValue(),
                    transform.getA01().getValue(),
                    transform.getA11().getValue(),
                    transform.getA02().getValue(),
                    transform.getA12().getValue());
        }

        public static Shape transformAwtShape(Shape shape, AffineTransform transform) {
            if (transform == null) return shape;
            final java.awt.geom.AffineTransform t = 
                SmartShape.Util.getAwtTransform(transform);
            return t.createTransformedShape(shape);
        }
        
        public static Set<Point2D> getQuantizedLinePoints(Line2D line, Set<Point2D> points) {
            if (line == null) return null;

            final Set<Point2D> set =
                (points instanceof LinkedHashSet) ?
                    points : new LinkedHashSet<Point2D>();

            Point2D start = line.getP1();
            Point2D end = line.getP2();
            Point2D m = new Point2D.Double(
                end.getX()-start.getX(), end.getY()-start.getY());
            double lengthM = (Math.sqrt(m.getX()*m.getX()+m.getY()*m.getY()));
            if (lengthM == 0) {
                set.add(
                    new Point2D.Double(
                        Math.floor(start.getX()), Math.floor(start.getY())));
                return set;
            }
            Point2D mNorm = new Point2D.Double(m.getX()/lengthM,m.getY()/lengthM);
            
            for (double i = 0 ; i <= (lengthM + 0.1) ; i += 0.1) {
                final Point2D pt = 
                    new Point2D.Double(
                        start.getX()+i*mNorm.getX(),
                        start.getY()+i*mNorm.getY());
                set.add(
                    new Point2D.Double(Math.floor(pt.getX()), Math.floor(pt.getY())));
            }
            
            return set;
        }
    }

    /**
     * Callback interface passed every point which is within the area of this
     * shape. This prevents having all the points in memory at the same time. An
     * implementation that would like to collect all the points can do something
     * like:
     * 
     * <pre>
     * final List&lt;Integer&gt; xs = ...;
     * final List&lt;Integer&gt; ys = ...;
     * PointCallback cb = new PointCallback(){
     *   void handle(int x, int y) {
     *     xs.add(x);
     *     ys.add(y);
     *   };
     * };
     * </pre>
     */
    public interface PointCallback {
        void handle(int x, int y);
    }

    /**
     * Calls the {@link PointCallback} with all of the x/y coordinates which are
     * within the shape.
     * @param action the callback to call
     */
    void areaPoints(PointCallback action);

    /**
     * Converts the current {@link SmartShape} to a {@link java.awt.Shape}. This
     * is useful for determining paths and included points.
     * @return the AWT shape
     */
    java.awt.Shape asAwtShape();

    /**
     * Provides some, possibly lossy, bounding polygon of this
     * {@link SmartShape} via points.
     * @return the bounding polygon
     */
    List<Point> asPoints();

    /**
     * Initializes this shape with completely random data.
     * @param random a random number generator
     */
    void randomize(Random random);

}
