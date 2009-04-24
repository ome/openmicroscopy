/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.roi;

import java.util.HashMap;
import java.util.Map;

import omero.model.Ellipse;
import omero.model.Line;
import omero.model.Mask;
import omero.model.Path;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Shape;
import omero.model.SmartEllipseI;
import omero.model.SmartLineI;
import omero.model.SmartMaskI;
import omero.model.SmartPathI;
import omero.model.SmartPointI;
import omero.model.SmartPolygonI;
import omero.model.SmartPolylineI;
import omero.model.SmartRectI;

/**
 * Intelligent server-side representations of the {@link Roi} and {@link Shape}
 * types. Registered manually in blitz-config.xml. This class manages the
 * ObjectFactories for the various types. Similar to java.util.Arrays.
 */
public abstract class RoiTypes {

    // Object factories
    // =========================================================================

    public final static Map<Class, ObjectFactory> ObjectFactories;

    public static abstract class ObjectFactory implements Ice.ObjectFactory {

        private final String id;

        public ObjectFactory(String id) {
            this.id = id;
        }

        public abstract Shape shape();

        public void register(Ice.Communicator ic) {
            ic.addObjectFactory(this, id);
        }

        public Ice.Object create(String arg0) {
            return shape();
        }

        public void destroy() {
            // noop
        }

    }

    static {
        Map<Class, ObjectFactory> factories = new HashMap<Class, ObjectFactory>();

        factories.put(SmartEllipseI.class, new ObjectFactory(Ellipse.ice_staticId()) {

            @Override
            public Ellipse shape() {
                return new SmartEllipseI();
            }

        });

        factories.put(SmartLineI.class, new ObjectFactory(Line.ice_staticId()) {

            @Override
            public Line shape() {
                return new SmartLineI();
            }

        });

        factories.put(SmartMaskI.class, new ObjectFactory(Mask.ice_staticId()) {

            @Override
            public Mask shape() {
                return new SmartMaskI();
            }

        });

        factories.put(SmartPathI.class, new ObjectFactory(Path.ice_staticId()) {

            @Override
            public Path shape() {
                return new SmartPathI();
            }

        });

        factories.put(SmartPointI.class, new ObjectFactory(Point.ice_staticId()) {

            @Override
            public Point shape() {
                return new SmartPointI();
            }

        });

        factories.put(SmartPolygonI.class, new ObjectFactory(Polygon.ice_staticId()) {

            @Override
            public Polygon shape() {
                return new SmartPolygonI();
            }

        });

        factories.put(SmartPolylineI.class,
                new ObjectFactory(Polyline.ice_staticId()) {

                    @Override
                    public Polyline shape() {
                        return new SmartPolylineI();
                    }

                });

        factories.put(SmartRectI.class, new ObjectFactory(Rect.ice_staticId()) {

            @Override
            public Rect shape() {
                return new SmartRectI();
            }

        });

        ObjectFactories = factories;
    }

}
