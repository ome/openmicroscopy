/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ome.model.meta.EventLog;
import ome.services.messages.ShapeChangeMessage;
import ome.tools.hibernate.SessionFactory;
import ome.util.ShallowCopy;
import omero.RBool;
import omero.RInt;
import omero.api.ShapePoints;
import omero.api.ShapeStats;
import omero.model.Ellipse;
import omero.model.Line;
import omero.model.Point;
import omero.model.Rect;
import omero.model.Shape;
import omero.model.SmartEllipseI;
import omero.model.SmartLineI;
import omero.model.SmartPointI;
import omero.model.SmartRectI;
import omero.model.SmartShape;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * Strategy for handling the conversion between {@link Shape shapes} and
 * database-specific geometries.
 * 
 * Implements {@link ApplicationListener} in order to keep the strategy-specific
 * geometry columns in sync when a {@link ShapeChangeMessage} is published.
 * 
 * @since Beta4.1
 */
public class GeomTool implements ApplicationListener {

    protected Log log = LogFactory.getLog(GeomTool.class);

    protected final SimpleJdbcOperations jdbc;

    protected final SessionFactory factory;

    public GeomTool(SimpleJdbcOperations jdbc, SessionFactory factory) {
        this.jdbc = jdbc;
        this.factory = factory;
        try {
            jdbc.update("alter table shape add column pg_geom polygon");
            log.info("Configured Shape.pg_geom");
        } catch (Exception e) {
            log.info("Shape.pg_geom already configured");
        }
    }

    //
    // geometry creation
    //

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ShapeChangeMessage) {
            ShapeChangeMessage scm = (ShapeChangeMessage) event;
            List<Long> shapeIds = new ArrayList<Long>();
            for (EventLog log : scm) {
                shapeIds.add(log.getEntityId());
            }
            synchronizeShapeGeometries(shapeIds);
        }
    }

    public void synchronizeShapeGeometries(List<Long> shapeIds) {
        for (Long shapeId : shapeIds) {
            synchronizeShapeGeometry(shapeId);
        }
    }

    public void synchronizeShapeGeometry(long shapeId) {
        Session session = factory.getSession();
        Shape s = shapeById(shapeId, session);
        String path = dbPath(s);
        jdbc.update(String.format("update shape set pg_geom = %s::polygon "
                + "where id = ?", path), shapeId);
    }

    public Shape shapeById(long shapeId, Session session) {
        ome.model.roi.Shape shape = (ome.model.roi.Shape) session.get(
                ome.model.roi.Shape.class, shapeId);
        Hibernate.initialize(shape);
        session.evict(shape);
        shape = new ShallowCopy().copy(shape);
        IceMapper mapper = new IceMapper();
        Shape s = (Shape) new IceMapper().map(shape);
        return s;
    }

    //
    // Factory methods
    //

    public List<Shape> random(int count) {
        if (count < 1 || count > 100000) {
            throw new RuntimeException("Count out of bounds: " + count);
        }

        Map<Class, RoiTypes.ObjectFactory> map = RoiTypes.ObjectFactories;
        List<Class> types = new ArrayList<Class>(map.keySet());
        List<Shape> shapes = new ArrayList<Shape>();
        Random r = new Random();

        try {
            while (shapes.size() < count) {
                int which = r.nextInt(types.size());
                Class type = types.get(which);
                Method m = type.getMethod("randomize", Random.class);
                RoiTypes.ObjectFactory of = map.get(type);
                SmartShape s = (SmartShape) of.create("");
                m.invoke(s, r);
                shapes.add((Shape) s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failure on creating shape "
                    + shapes.size(), e);
        }

        return shapes;
    }

    public Line ln(double x1, double y1, double x2, double y2) {
        SmartLineI rect = new SmartLineI();
        rect.setX1(rdouble(x1));
        rect.setY1(rdouble(y1));
        rect.setX2(rdouble(x2));
        rect.setY2(rdouble(y2));
        return rect;
    }

    public Rect rect(double x, double y, double w, double h) {
        SmartRectI rect = new SmartRectI();
        rect.setX(rdouble(x));
        rect.setY(rdouble(y));
        rect.setWidth(rdouble(w));
        rect.setHeight(rdouble(h));
        return rect;
    }

    public Point pt(double x, double y) {
        SmartPointI pt = new SmartPointI();
        pt.setCx(rdouble(x));
        pt.setCy(rdouble(y));
        return pt;
    }

    public Ellipse ellipse(double cx, double cy, double rx, double ry) {
        SmartEllipseI ellipse = new SmartEllipseI();
        ellipse.setCx(rdouble(cx));
        ellipse.setCy(rdouble(cy));
        ellipse.setRx(rdouble(rx));
        ellipse.setRy(rdouble(ry));
        return ellipse;
    }

    public Ellipse ellipse(double cx, double cy, double rx, double ry, int t,
            int z) {
        Ellipse ellipse = ellipse(cx, cy, rx, ry);
        ellipse.setTheT(rint(t));
        ellipse.setTheZ(rint(z));
        return ellipse;
    }

    //
    // Conversion methods
    //

    public String dbPath(Shape shape) {

        if (shape == null) {
            return null;
        }

        SmartShape ss = assertSmart(shape);
        List<Point> points = ss.asPoints();
        StringBuilder sb = new StringBuilder();
        sb.append("'(");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            SmartShape.Util.appendDbPoint(sb, points.get(i));
        }
        sb.append(")'");
        return sb.toString();
    }

    //
    // Database access methods
    //

    static final String FIND_INTERESECTING_QUERY = "select distinct s.roi "
            + "from Shape s, Roi r where r.image = %s and r.id  = s.roi and "
            + "pg_geom && %s::polygon";

    public List<Long> findIntersectingRois(long imageId, Shape shape) {

        if (shape == null) {
            return null; // EARLY EXIT
        }

        RInt z = shape.getTheZ();
        RInt t = shape.getTheT();
        RBool v = shape.getVisibility();
        RBool l = shape.getLocked();

        StringBuilder sb = new StringBuilder();
        sb.append(FIND_INTERESECTING_QUERY);
        if (z != null) {
            sb.append(" and theZ = ");
            sb.append(z.getValue());
        }
        if (t != null) {
            sb.append(" and theT = ");
            sb.append(t.getValue());
        }
        if (v != null) {
            sb.append(" and visibility = ");
            sb.append(v.getValue());
        }
        if (l != null) {
            sb.append(" and locked = ");
            sb.append(l.getValue());
        }

        String path = dbPath(shape);
        List<Long> ids = jdbc.query(
                String.format(sb.toString(), imageId, path),
                new ParameterizedRowMapper<Long>() {
                    public Long mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return (Long) rs.getLong(1);
                    }
                });
        return ids;
    }

    public ShapePoints getPoints(Shape shape) {
        SmartShape smart = assertSmart(shape);
        int[][] pts = smart.areaPoints();
        ShapePoints sp = new ShapePoints();
        sp.x = pts[0];
        sp.y = pts[1];
        return sp;
    }

    // UNFINISHED
    public Object getStats(Shape shape, ShapePoints points) {

        int sz = points.x.length;
        ShapeStats stats = new ShapeStats();
        stats.pointsCount = sz;
        double sumOfSquares = 0;

        for (int i = 0; i < sz; i++) {
            double value = 0.0; // EMPTY
            stats.min = Math.min(value, stats.min);
            stats.max = Math.max(value, stats.max);
            stats.sum += value;
            sumOfSquares += value * value;
        }

        stats.mean = stats.sum / stats.pointsCount;
        if (stats.pointsCount > 1) {
            double sigmaSquare = (sumOfSquares - stats.sum * stats.sum
                    / stats.pointsCount)
                    / (stats.pointsCount - 1);
            if (sigmaSquare > 0) {
                stats.stdDev = Math.sqrt(sigmaSquare);
            }
        }

        return stats;
    }

    //
    // helpers
    //

    private SmartShape assertSmart(Shape shape) {
        if (!SmartShape.class.isAssignableFrom(shape.getClass())) {
            throw new RuntimeException(
                    "Internally only SmartShapes should be used! not "
                            + shape.getClass());
        }

        SmartShape ss = (SmartShape) shape;
        return ss;
    }

}
