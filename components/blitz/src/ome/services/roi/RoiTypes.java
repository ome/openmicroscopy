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
import omero.model.Label;
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
import omero.model.SmartTextI;
import omero.util.ObjectFactoryRegistry;
import omero.util.ObjectFactoryRegistry.ObjectFactory;

/**
 * Intelligent server-side representations of the {@link Roi} and {@link Shape}
 * types. Registered manually in blitz-config.xml. This class manages the
 * ObjectFactories for the various types. Similar to java.util.Arrays.
 */
public abstract class RoiTypes {

    // Object factories
    // =========================================================================

    public static class RoiTypesObjectFactoryRegistry extends ObjectFactoryRegistry {

        @Override
        public Map<String, ObjectFactory> createFactories(Ice.Communicator ic) {
            Map<String, ObjectFactory> factories = new HashMap<String, ObjectFactory>();

            factories.put(SmartEllipseI.ice_staticId(), new ObjectFactory(Ellipse.ice_staticId()) {

                @Override
                public Ellipse create(String name) {
                    return new SmartEllipseI();
                }

            });

            factories.put(SmartLineI.ice_staticId(), new ObjectFactory(Line.ice_staticId()) {

                @Override
                public Line create(String name) {
                    return new SmartLineI();
                }

            });

            factories.put(SmartMaskI.ice_staticId(), new ObjectFactory(Mask.ice_staticId()) {

                @Override
                public Mask create(String name) {
                    return new SmartMaskI();
                }

            });

            factories.put(SmartPathI.ice_staticId(), new ObjectFactory(Path.ice_staticId()) {

                @Override
                public Path create(String name) {
                    return new SmartPathI();
                }

            });

            factories.put(SmartPointI.ice_staticId(), new ObjectFactory(Point.ice_staticId()) {

                @Override
                public Point create(String name) {
                    return new SmartPointI();
                }

            });

            factories.put(SmartPolygonI.ice_staticId(), new ObjectFactory(Polygon.ice_staticId()) {

                @Override
                public Polygon create(String name) {
                    return new SmartPolygonI();
                }

            });

            factories.put(SmartPolylineI.ice_staticId(),
                    new ObjectFactory(Polyline.ice_staticId()) {

                        @Override
                        public Polyline create(String name){
                            return new SmartPolylineI();
                        }

                    });

            factories.put(SmartRectI.ice_staticId(), new ObjectFactory(Rect.ice_staticId()) {

                @Override
                public Rect create(String name) {
                    return new SmartRectI();
                }

            });

            factories.put(SmartTextI.ice_staticId(), new ObjectFactory(Label.ice_staticId()) {

                @Override
                public Label create(String name) {
                    return new SmartTextI();
                }

            });

            return factories;
        }

    }

}
