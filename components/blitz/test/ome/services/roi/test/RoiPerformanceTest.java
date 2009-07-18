/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.Random;

import omero.model.Image;
import omero.model.ImageI;
import omero.model.Roi;
import omero.model.Shape;

import org.perf4j.commonslog.CommonsLogStopWatch;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "integration", "rois" })
public class RoiPerformanceTest extends AbstractRoiITest {

    Image i;

    @BeforeClass
    public void setup() throws Exception {

        Random r = new Random();
        CommonsLogStopWatch watch = new CommonsLogStopWatch();

        int count = 10;

        while (count > 0) {

            i = new ImageI();
            i.setName(rstring("RoiPerformanceTest"));
            i.setAcquisitionDate(rtime(0));
            i = assertSaveAndReturn(i);
            i.unload();

            watch.lap("create.image");

            while (true) {
                Roi roi = createRoi(i, "RoiPerformanceTest", geomTool
                        .random(10).toArray(new Shape[] {}));
                watch.lap("create.roi." + roi.copyShapes().size());
                if (r.nextDouble() < 0.1) {
                    break;
                }

            }
        }

    }

    @Test
    public void testMakeLots() {
        // pass
    }

}
