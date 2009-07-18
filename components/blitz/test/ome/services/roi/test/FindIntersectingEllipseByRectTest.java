/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rbool;
import static omero.rtypes.rint;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.model.Image;
import omero.model.ImageI;
import omero.model.Roi;
import omero.model.Shape;

import org.testng.annotations.Test;

/**
 * Tests searching for ellipses with rectangles
 */
@Test(groups = "integration")
public class FindIntersectingEllipseByRectTest extends AbstractRoiITest {

    class Fixture {

        List<Shape> shapes = new ArrayList<Shape>();
        Map<Shape, Integer> tests = new HashMap<Shape, Integer>();

        Fixture withPoint(double cx, double cy) {
            shapes.add(geomTool.pt(cx, cy));
            return this;
        }
        
        Fixture withEllipse(double cx, double cy, double rx, double ry) {
            shapes.add(geomTool.ellipse(cx, cy, rx, ry));
            return this;
        }

        Fixture withRect(double x, double y, double w, double h) {
            shapes.add(geomTool.rect(x, y, w, h));
            return this;
        }

        Fixture searchedByPoint(double cx, double cy, int overlaps) {
            Shape s = geomTool.pt(cx, cy);
            tests.put(s, overlaps);
            return this;
        }
        
        Fixture searchedByRectangle(double x, double y, double w, double h,
                int overlaps) {
            Shape s = geomTool.rect(x, y, w, h);
            tests.put(s, overlaps);
            return this;
        }

        void clear(Shape shape) {
            shape.setTheT(null);
            shape.setTheZ(null);
            shape.setVisibility(null);
            shape.setLocked(null);
        }

        public void test() throws Exception {

            //
            // Basic
            //

            for (Shape s : shapes) {
                clear(s);
            }
            Roi roi = createRoi("test basicc", shapes.toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                // All fields on t should be null here.
                assertIntersection(roi, t, tests.get(t));
            }

            //
            // Visibility
            //

            for (Shape s : shapes) {
                clear(s);
                s.setVisibility(rbool(true));
            }
            Roi roi_visible = createRoi("test with shapes visible", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_visible, t, tests.get(t));
                t.setVisibility(rbool(true));
                assertIntersection(roi_visible, t, tests.get(t));
                t.setVisibility(rbool(false));
                assertIntersection(roi_visible, t, 0);
            }

            for (Shape s : shapes) {
                clear(s);
                s.setVisibility(rbool(false));
            }
            Roi roi_invisible = createRoi("test with shapes visible", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_invisible, t, tests.get(t));
                t.setVisibility(rbool(false));
                assertIntersection(roi_invisible, t, tests.get(t));
                t.setVisibility(rbool(true));
                assertIntersection(roi_invisible, t, 0);
            }

            //
            // Locked
            //

            for (Shape s : shapes) {
                clear(s);
                s.setLocked(rbool(true));
            }
            Roi roi_locked = createRoi("test with shapes locked", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_locked, t, tests.get(t));
                t.setLocked(rbool(true));
                assertIntersection(roi_locked, t, tests.get(t));
                t.setLocked(rbool(false));
                assertIntersection(roi_locked, t, 0);
            }

            for (Shape s : shapes) {
                clear(s);
                s.setLocked(rbool(false));
            }
            Roi roi_unlocked = createRoi("test with shapes unlocked", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_unlocked, t, tests.get(t));
                t.setLocked(rbool(false));
                assertIntersection(roi_unlocked, t, tests.get(t));
                t.setLocked(rbool(true));
                assertIntersection(roi_unlocked, t, 0);
            }

            //
            // Z
            //

            for (Shape s : shapes) {
                clear(s);
                s.setTheZ(rint(1));
            }
            Roi roi_z1 = createRoi("test with shapes on z1", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_z1, t, tests.get(t));
                t.setTheZ(rint(1));
                assertIntersection(roi_z1, t, tests.get(t));
                t.setTheZ(rint(2));
                assertIntersection(roi_z1, t, 0);
            }

            //
            // T
            //

            for (Shape s : shapes) {
                clear(s);
                s.setTheT(rint(1));
            }
            Roi roi_t1 = createRoi("test with shapes on t1", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                assertIntersection(roi_t1, t, tests.get(t));
                t.setTheT(rint(1));
                assertIntersection(roi_t1, t, tests.get(t));
                t.setTheT(rint(2));
                assertIntersection(roi_t1, t, 0);
            }

            //
            // Z & T
            //

            for (Shape s : shapes) {
                clear(s);
                s.setTheT(rint(1));
                s.setTheZ(rint(2));
            }
            Roi roi_t1_z2 = createRoi("test with shapes on t1/z2", shapes
                    .toArray(new Shape[] {}));

            for (Shape t : tests.keySet()) {
                clear(t);
                // Matches
                assertIntersection(roi_t1_z2, t, tests.get(t));
                t.setTheT(rint(1));
                t.setTheZ(rint(2));
                assertIntersection(roi_t1_z2, t, tests.get(t));
                t.setTheT(rint(1));
                t.setTheZ(null);
                assertIntersection(roi_t1_z2, t, tests.get(t));
                t.setTheT(null);
                t.setTheZ(rint(2));
                assertIntersection(roi_t1_z2, t, tests.get(t));
                // Doesn't match
                t.setTheT(rint(0));
                t.setTheZ(rint(2)); // Correct
                assertIntersection(roi_t1_z2, t, 0);
                t.setTheT(rint(1)); // Correct
                t.setTheZ(rint(0));
                assertIntersection(roi_t1_z2, t, 0);
                t.setTheT(rint(0));
                t.setTheZ(rint(0));
                assertIntersection(roi_t1_z2, t, 0);
            }

        }

        private Roi createRoi(String name, Shape... shapes) throws Exception {
            Image i = new ImageI();
            i.setAcquisitionDate(rtime(0));
            i.setName(rstring(name));
            return createRoi(i, name, shapes);
        }

        private Roi createRoi(Image i, String name, Shape... shapes)
                throws Exception {
            Roi roi = new omero.model.RoiI();
            roi.setImage(i);
            roi.addAllShapeSet(Arrays.asList(shapes));
            roi = assertSaveAndReturn(roi);
            roi = (Roi) assertFindByQuery(
                    "select roi from Roi roi "
                            + "join fetch roi.shapes shapes join fetch shapes.roi "
                            + "join fetch roi.image image "
                            + "left outer join fetch image.pixels " // OUTER
                            + "where roi.id = " + roi.getId().getValue(), null)
                    .get(0);
            return roi;
        }

    }

    @Test
    public void testFindEllipseByRectangle() throws Exception {
        new Fixture().withEllipse(1, 1, .5, .5) //
                .searchedByRectangle(0.0, 0.0, 1.0, 1.0, 1) // 
                .searchedByRectangle(10.0, 10.0, 1.0, 1.0, 0) //
                .test();
    }
    
    @Test
    public void testFindEllipseByPoint() throws Exception {
        new Fixture().withEllipse(1, 1, .5, .5) //
                .searchedByPoint(0.75, 0.75, 1) // 
                .searchedByPoint(10.0, 10.0, 0) //
                .test();
    }
    
    @Test
    public void testFindPointByRectangle() throws Exception {
        new Fixture().withPoint(1,1)//
            .searchedByRectangle(0.0, 0.0, 2.0, 2.0, 1)//
            .searchedByRectangle(10.0, 10.0, 1.0, 1.0, 0)//
            .test();
    }

}
