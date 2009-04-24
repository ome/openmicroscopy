/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import ome.services.blitz.impl.RoiI;
import ome.services.blitz.test.AbstractServantTest;
import ome.services.roi.GeomTool;
import omero.api.AMD_IRoi_findByIntersection;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Roi;
import omero.model.Shape;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 *<pre>
 * // REMAINING TESTS:
 * // * CROSS-PRODUCT (and MULTI)
 * // * INDEXES
 * // * BOUNDARIES
 * // * LONG TABLES, BIG OBJECTS
 * </pre>
 */
@Test(groups = "integration")
public class AbstractRoiITest extends AbstractServantTest {

    protected RoiI user_roisvc, root_roisvc;
    protected GeomTool geomTool;
    protected Shape test;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        geomTool = (GeomTool) ctx.getBean("geomTool");
        user_roisvc = new RoiI(be, geomTool);
        user_roisvc.setServiceFactory(user_sf);
        user_roisvc.setServiceFactory(user_sf);

        root_roisvc = new RoiI(be, geomTool);
        root_roisvc.setServiceFactory(root_sf);
        root_roisvc.setServiceFactory(root_sf);
    }

    //
    // assertions
    //

    protected RoiResult assertFindIntersectingRois(long imageId, Shape shape,
            RoiOptions opts) throws Exception {
        final RV rv = new RV();
        user_roisvc.findByIntersection_async(new AMD_IRoi_findByIntersection() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(RoiResult __ret) {
                rv.rv = __ret;
            }
        }, imageId, shape, opts, null);

        rv.assertPassed();
        return (RoiResult) rv.rv;
    }

    protected RoiResult assertIntersection(Roi roi, Shape test, int size)
            throws Exception {
        long imageId = roi.getImage().getId().getValue();
        RoiResult rr = assertFindIntersectingRois(imageId, test, null);
        assertNotNull(rr);
        assertEquals(size, rr.rois.size());
        return rr;
    }

    //
    // helpers
    //

    protected Roi createRoi(String name, Shape... shapes) throws Exception {
        Image i = new ImageI();
        i.setAcquisitionDate(rtime(0));
        i.setName(rstring(name));
        return createRoi(i, name, shapes);
    }

    protected Roi createRoi(Image i, String name, Shape... shapes)
            throws Exception {
        Roi roi = new omero.model.RoiI();
        roi.setImage(i);
        roi.addAllShapeSet(Arrays.asList(shapes));
        roi = assertSaveAndReturn(roi);
        return roi;
    }

}
