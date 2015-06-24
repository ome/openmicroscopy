/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.enums.UnitsTime;
import ome.model.units.Time;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DetachedPixelsGraphTest extends AbstractUpdateTest {

    /** the "original" pixels from which p will be the detached version */
    Pixels example;

    /**
     * the test object; a detached pixels object which exactly matches example
     * but has never been associated with a session (this simulates
     * serialization!)
     */
    Pixels p;

    int channelsSizeBefore;

    @BeforeClass
    protected void setup() throws Exception {

        example = ObjectFactory.createPixelGraph(null);

        example = iUpdate.saveAndReturnObject(example.getImage()).getPixels(0);

        p = ObjectFactory.createPixelGraph(example);
        assertTrue("Starting off empty", p.sizeOfChannels() >= 0);
        channelsSizeBefore = p.sizeOfChannels();
        assertTrue("Starting off empty", channelsSizeBefore > 0);

    }

    @Test
    public void testNewRecursiveEntityFieldOnDetachedPixels() throws Exception {
        // PREPARE ----------------------------------------------
        p.setRelatedTo(ObjectFactory.createPixelGraph(null));
        p = iUpdate.saveAndReturnObject(p.getImage()).getPixels(0);

        // TEST -------------------------------------------------
        assertTrue("Related-to is null", p.getRelatedTo() != null);
        assertTrue("or it has no id", p.getRelatedTo().getId().longValue() > 0);

        long id = (Long) iQuery.projection(
                "select relatedto from Pixels where id = :id",
                new Parameters().addId(p.getId())).get(0)[0];
        assertTrue("Id *really* has to be there.", p.getRelatedTo().getId()
                .longValue() == id);

    }

    @Test
    public void testDetachedRecursiveEntityFieldOnDetachedPixels()
            throws Exception {
        // PREPARE ----------------------------------------------
        // Make field entry; we have to re-do what is done in setup above.
        Pixels example2 = ObjectFactory.createPixelGraph(null);
        example2 = iUpdate.saveAndReturnObject(example2.getImage()).getPixels(0);

        Pixels p2 = ObjectFactory.createPixelGraph(example2);

        p.setRelatedTo(p2);
        p = iUpdate.saveAndReturnObject(p);

        // TEST -------------------------------------------------
        assertTrue("Related-to is null", p.getRelatedTo() != null);
        assertTrue("and it has no id", p.getRelatedTo().getId().equals(
                p2.getId()));

    }

/*
	// This test is now out of date
    @Test
    public void testNewEntityFieldOnDetachedPixels() throws Exception {
        // PREPARE ----------------------------------------------
        PixelsDimensions pd = new PixelsDimensions();
        pd.setSizeX(new Double(1));
        pd.setSizeY(new Double(2));
        pd.setSizeZ(new Double(3));

        p.setPixelsDimensions(pd);
        p = iUpdate.saveAndReturnObject(p);

        // TEST -------------------------------------------------
        assertTrue("Dimensions is valid.", p.getPixelsDimensions() != null
                && p.getPixelsDimensions().getId().longValue() > 0);

    }
*/

/*
	// This test is now out of date
    @Test
    public void testUnloadedEntityFieldOnDetachedPixels() throws Exception {
        // PREPARE -------------------------------------------------
        // TODO or bool flag?
        PixelsDimensions dims = new PixelsDimensions(example
                .getPixelsDimensions().getId(), false);

        p.setPixelsDimensions(dims);
        p = iUpdate.saveAndReturnObject(p);

        // TEST -------------------------------------------------
        assertNotNull("should be back.", p.getPixelsDimensions());
        assertTrue("and it should have a valid id.", p.getPixelsDimensions()
                .getId().longValue() > 0);
    }
*/

    @Test
    public void testNulledCollectionFieldOnDetachedPixels() throws Exception {
        // PREPARE -------------------------------------------------
        p.putAt(Pixels.CHANNELS, null);
        p = iUpdate.saveAndReturnObject(p);

        // TEST -------------------------------------------------
        assertTrue("Didn't get re-filled", p.sizeOfChannels() >= 0);
        assertTrue("channel ids aren't the same", equalCollections(example
                .unmodifiableChannels(), p.unmodifiableChannels()));
    }

    @Test
    public void testFilteredCollectionFieldOnDetachedPixels() throws Exception {
        // PREPARE -------------------------------------------------
        Channel first = p.unmodifiableChannels().iterator().next();
        p.removeChannel(first);
        p.getDetails().addFiltered(Pixels.CHANNELS);

        // Save and it should be back
        p = iUpdate.saveAndReturnObject(p);

        // TEST -------------------------------------------------
        int channelsSizeAfter = p.sizeOfChannels();
        assertTrue("Filtered channels not refilled",
                channelsSizeAfter == channelsSizeBefore);
        for (Channel c : p.unmodifiableChannels()) {
            assertTrue("Channel missing a valid id.", c.getId().longValue() > 0);
        }

    }

    @Test
    public void testNewCollectionFieldOnDetachedPixels() throws Exception {
        // PREPARE -------------------------------------------------
        PlaneInfo pi1 = new PlaneInfo(), pi2 = new PlaneInfo();

        Time exposureTime = new Time(10, UnitsTime.SECOND);
        Time deltaT = new Time(-1, UnitsTime.SECOND);
        Time deltaTB = new Time(-193, UnitsTime.SECOND);

        pi1.setTheC(new Integer(1));
        pi1.setTheT(new Integer(1));
        pi1.setTheZ(new Integer(1));
        pi1.setPixels(p);
        pi1.setExposureTime(exposureTime);
        pi1.setDeltaT(deltaT);

        pi2.setTheC(new Integer(1));
        pi2.setTheT(new Integer(1));
        pi2.setTheZ(new Integer(1));
        pi2.setPixels(p);
        pi2.setExposureTime(exposureTime);
        pi2.setDeltaT(deltaTB);

        p.addPlaneInfo(pi1);
        p.addPlaneInfo(pi2);
        p = iUpdate.saveAndReturnObject(p);

        // TEST ----------------------------------------------------
        // ObjectFactory now creations PlaneInfos, so this p already has one.
        assertTrue("Need at least two pixInfos, please.", p.collectPlaneInfo(
                null).size() >= 2);
        for (PlaneInfo pi : p.unmodifiablePlaneInfo()) {
            assertTrue("Need an id, please.", pi.getId().longValue() > 0);
        }
    }

    // TODO need to check that for detached that the version is not
    // incremented unless we really change something!

    // TODO assumptions about Experimenter.version increasing, security, etc.
}
