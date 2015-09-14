/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import omero.api.AMD_IRoi_getPoints;
import omero.api.ShapePoints;
import omero.model.Ellipse;
import omero.model.Image;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Shape;

import org.testng.annotations.Test;

@Test(groups = { "integration", "rois" })
public class ContainedPointsTest extends AbstractRoiITest {

    Image i;

    @Test
    public void testGeometryOfRectangle() throws Exception {
        Rect r = geomTool.rect(0, 0, 10, 10);
        Roi roi = createRoi("geoOfRect", r);
        ShapePoints pts = assertPoints(roi.getPrimaryShape());
        assertEquals(100, pts.x.length);
        assertEquals(100, pts.y.length);
    }

    @Test
    public void testGeometryOfCircle() throws Exception {
        Ellipse e = geomTool.ellipse(5, 5, 5, 5);
        Roi roi = createRoi("geoOfCircle - inside 0,0,10,10 rect", e);
        ShapePoints pts = assertPoints(roi.getPrimaryShape());
        assertEquals(16, pts.x.length);
        assertEquals(16, pts.y.length);
    }

    protected ShapePoints assertPoints(final Shape shape) throws Exception {
        final RV rv = new RV();
        user_roisvc.getPoints_async(new AMD_IRoi_getPoints() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(ShapePoints __ret) {
                rv.rv = __ret;
            }
        }, shape.getId().getValue(), null);

        rv.assertPassed();
        ShapePoints geom = (ShapePoints) rv.rv;
        assertNotNull(geom.x);
        assertNotNull(geom.y);
        return geom;
    }

}
