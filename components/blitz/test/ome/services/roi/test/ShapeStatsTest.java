/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import static omero.rtypes.rint;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import omero.api.AMD_IRoi_getShapeStats;
import omero.api.ShapeStats;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.LogicalChannel;
import omero.model.Pixels;
import omero.model.Rectangle;
import omero.model.Roi;
import omero.model.Shape;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "integration", "rois" })
public class ShapeStatsTest extends AbstractRoiITest {

    Image i;

    @Test
    public void testStatsOfRectangle() throws Exception {
        Pixels p = makeAndLoadPixels();
        Image i = new ImageI();
        i.addPixels(p);
        i.setName(rstring("statsOfRect"));
        Rectangle r = geomTool.rect(0, 0, 10, 10);
        Roi roi = createRoi(i, "statsOfRect", r);
        ShapeStats stats = assertStats(roi.getPrimaryShape());
    }

    @Test
    public void testStatsWithImplicitChannels() throws Exception {

        Pixels p = makeAndLoadPixels();
        LogicalChannel[] lcs = collectLogicalChannels(p);

        Image i = new ImageI();
        i.addPixels(p);
        i.setName(rstring("statsOfRectImplicitChannels"));
        Rectangle r = geomTool.rect(0, 0, 10, 10);
        Roi roi = createRoi(i, "statsOfRect", r);

        ShapeStats stats = assertStats(roi.getPrimaryShape(), lcs);
    }

    @Test
    public void testStatsWithExplicitChannels() throws Exception {

        Pixels p = makeAndLoadPixels();
        LogicalChannel[] lcs = collectLogicalChannels(p);

        Image i = new ImageI();
        i.addPixels(p);
        i.setName(rstring("statsOfRectExplicitChannels"));
        Rectangle r = geomTool.rect(0, 0, 10, 10);
        // Now add one channel
        LogicalChannel lc = (LogicalChannel) assertFindByQuery(
                "select lc from LogicalChannel lc where id = :id",
                new ParametersI().addId(lcs[0].getId().getValue())).get(0);
        r.setTheC(rint(0)); // Link the same channel

        Roi roi = createRoi(i, "statsOfRect", r);

        ShapeStats stats = assertStats(roi.getPrimaryShape(), lcs[0]);
    }

    protected ShapeStats assertStats(final Shape shape,
            final LogicalChannel... lcs) throws Exception {
        final RV rv = new RV();
        user_roisvc.getShapeStats_async(new AMD_IRoi_getShapeStats() {

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
        assertEquals(shape.getId().getValue(), stats.shapeId);
        assertNotNull(stats.min);
        assertNotNull(stats.max);
        assertNotNull(stats.sum);
        assertNotNull(stats.mean);
        assertNotNull(stats.stdDev);
        assertNotNull(stats.pointsCount);
        assertNotNull(stats.channelIds);
        if (lcs.length > 0) {
            Set<Long> chosenIds = new HashSet<Long>();
            Set<Long> foundIds = new HashSet<Long>();
            for (LogicalChannel lc : lcs) {
                chosenIds.add(lc.getId().getValue());
            }
            for (long id : stats.channelIds) {
                foundIds.add(id);
            }
            assertTrue(chosenIds + " v. " + foundIds, chosenIds
                    .equals(foundIds));
        }
        return stats;
    }

    private Pixels makeAndLoadPixels() throws Exception, FileNotFoundException {
        long pix = makePixels();
        Pixels p = (Pixels) assertFindByQuery(
                "select p from Pixels p join fetch p.channels ch "
                        + "join fetch ch.logicalChannel lc where p.id = " + pix,
                null).get(0);
        return p;
    }

    private LogicalChannel[] collectLogicalChannels(Pixels p) {
        LogicalChannel[] lcs = new LogicalChannel[p.sizeOfChannels()];
        for (int j = 0; j < lcs.length; j++) {
            lcs[j] = p.getChannel(j).getLogicalChannel();
        }
        return lcs;
    }
}
