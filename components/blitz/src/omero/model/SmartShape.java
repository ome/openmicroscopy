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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.DefaultPointsHandler;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.PointsHandler;
import org.apache.batik.parser.PointsParser;
import org.apache.xerces.impl.xpath.regex.ParseException;

public interface SmartShape {

    public static class Util {

        public static void appendDbPoint(StringBuilder sb, Point p) {
            appendDbPoint(sb, p.cx.getValue(), p.cy.getValue());
        }

        public static void appendDbPoint(StringBuilder sb, double cx, double cy) {
            sb.append("(");
            sb.append(cx);
            sb.append(",");
            sb.append(cy);
            sb.append(")");
        }

        public static void appendSvgPoint(StringBuilder sb, Point p) {
            appendSvgPoint(sb, p.cx.getValue(), p.cy.getValue());
        }

        public static void appendSvgPoint(StringBuilder sb, double cx, double cy) {
            sb.append(cx);
            sb.append(",");
            sb.append(cy);
            sb.append(" ");
        }
        
        public static boolean appendSegement(StringBuilder sb, boolean first,
                double cx, double cy) {
            if (first) {
                sb.append("M ");
                first = false;
            } else {
                sb.append("L ");
            }
            sb.append(cx);
            sb.append(" ");
            sb.append(cy);
            sb.append(" ");
            return first;
        }

        public static String pointsToPath(List<Point> points, boolean close) {
            StringBuilder sb = new StringBuilder(points.size() * 16);
            boolean first = true;
            for (Point point : points) {
                double cx = point.getCx().getValue();
                double cy = point.getCy().getValue();
                first = appendSegement(sb, first, cx, cy);
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
                    first[0] = appendSegement(sb, first[0], x, y);
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
                    sp.setCx(rdouble(x));
                    sp.setCy(rdouble(y));
                    points.add(sp);
                }
            };
            pp.setPointsHandler(ph);
            pp.parse(str);
            return points;
        }

        public static List<Point> points(double x, double y, double w, double h) {
            omero.RDouble x0 = rdouble(x);
            omero.RDouble y0 = rdouble(y);
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
        
        public static int[][] pointsByBoundingBox(Shape s, Rectangle2D r) {
            int size = ((int) r.getHeight() * (int) r.getWidth());
            List<Integer> xs = new ArrayList<Integer>(size);
            List<Integer> ys = new ArrayList<Integer>(size);

            double xEnd = (r.getX() + r.getWidth());
            double yEnd = (r.getY() + r.getHeight());
            double startX = r.getX();
            double startY = r.getY();

            for (double y = startY; y < yEnd; ++y) {
                for (double x = startX; x < xEnd; ++x) {
                    if (s.intersects(x, y, 0.001, 0.001)) {
                        xs.add((int) x);
                        ys.add((int) y);
                    }
                }
            }

            int[] xpts = new int[xs.size()];
            int[] ypts = new int[ys.size()];
            for (int i = 0; i < xpts.length; i++) {
                xpts[i] = xs.get(i);
                ypts[i] = ys.get(i);
            }
            return new int[][] { xpts, ypts };
        }
    }

    java.awt.Shape asAwtShape();

    List<Point> asPoints();

    int[][] areaPoints();
    
    // int[][] perimeterPoints();
    
    void randomize(Random random);

}