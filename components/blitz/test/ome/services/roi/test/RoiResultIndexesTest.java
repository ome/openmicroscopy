/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import omero.api.RoiResult;
import omero.model.Ellipse;
import omero.model.Rect;
import omero.model.Roi;

import org.testng.annotations.Test;

/**
 * Checks that when a {@link RoiResult} is returned that its indexes are
 * properly setup.
 */
@Test(groups = {"integration", "rois"})
public class RoiResultIndexesTest extends AbstractRoiITest {

    @Test
    public void testBasic() throws Exception {

        Ellipse e1 = geomTool.ellipse(1, 1, .5, .5, 0, 0);
        Ellipse e2 = geomTool.ellipse(2, 2, 2, 2, 1, 0);
        Ellipse e3 = geomTool.ellipse(2, 2, 2, 2, 1, 1);
        Ellipse e4 = geomTool.ellipse(2, 2, 2, 2, 1, 2);
        Roi roi = createRoi("RoiResultIndexes.basic", e1, e2, e3, e4);

        Rect t = geomTool.rect(0.0, 0.0, 10, 10);
        RoiResult rr = assertIntersection(roi, t, 1);

        assertEquals(1, rr.byT.get(0).size());
        assertEquals(3, rr.byT.get(1).size());
        assertEquals(2, rr.byZ.get(0).size());
        assertEquals(1, rr.byZ.get(1).size());
        assertEquals(1, rr.byZ.get(2).size());

    }

}
