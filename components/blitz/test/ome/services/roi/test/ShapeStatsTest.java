/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import omero.api.AMD_IRoi_getStats;
import omero.api.ShapeStats;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Pixels;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Shape;

import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = "integration")
public class ShapeStatsTest extends AbstractRoiITest {

    Image i;

    @Test
    public void testStatsOfRectangle() throws Exception {
        long pix = makePixels();
        Pixels p = (Pixels) assertFindByQuery(
                "select p from Pixels p where id = " + pix, null).get(0);
        Image i = new ImageI();
        i.addPixels(p);
        i.setName(rstring("statsOfRect"));
        i.setAcquisitionDate(rtime(0));
        Rect r = geomTool.rect(0, 0, 10, 10);
        Roi roi = createRoi(i, "statsOfRect", r);
        ShapeStats stats = assertStats(roi.getPrimaryShape());
    }

    protected ShapeStats assertStats(final Shape shape) throws Exception {
        final RV rv = new RV();
        user_roisvc.getStats_async(new AMD_IRoi_getStats() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(ShapeStats __ret) {
                rv.rv = __ret;
            }
        }, shape.getId().getValue(), null);

        rv.assertPassed();
        ShapeStats stats = (ShapeStats) rv.rv;
        assertNotNull(stats);
        assertNotNull(stats.min);
        assertNotNull(stats.max);
        assertNotNull(stats.sum);
        assertNotNull(stats.mean);
        assertNotNull(stats.stdDev);
        assertNotNull(stats.pointsCount);
        return stats;
    }

}
