/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration.delete;

import integration.AbstractServerTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.xml.model.OME;
import omero.cmd.Delete2;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
import omero.model.Experiment;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Screen;
import omero.model.WellSample;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;
import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;

/**
 * Tests for deleting screen/plate/wells
 *
 * @since 4.2.1
 */
@Test(groups = "ticket:2615")
public class SpwDeleteTest extends AbstractServerTest {

    @Test(groups = {"ticket12601", "ticket:3102", "ticket:11540"})
    public void testScreen() throws Exception {

        newUserAndGroup("rw----");

        List<Pixels> pixels = createScreen();

        Experiment exp = null;
        Screen screen = null;
        List<Plate> plates = new ArrayList<Plate>();
        List<WellSample> samples = new ArrayList<WellSample>();

        for (Pixels p : pixels) {

            Experiment e = getExperiment(p);
            if (exp == null) {
                exp = e;
            } else {
                assertEquals(exp.getId().getValue(), e.getId().getValue());
            }

            WellSample ws = getWellSample(p);
            Plate plate = ws.getWell().getPlate();
            Screen s = plate.copyScreenLinks().get(0).getParent();
            if (screen == null) {
                screen = s;
            } else {
                assertEquals(screen.getId().getValue(), s.getId().getValue());
            }
        }

        // In order to avoid omero.LockTimeout
        // see XMLMockObjects.createScreen()
        scalingFactor *= 1*2*2*2*2;

        final Delete2 dc = Requests.delete("Screen", screen.getId().getValue());
        callback(true, client, dc);

        assertDoesNotExist(screen);
        assertNoneExist(plates.toArray(new Plate[0]));
        assertNoneExist(samples.toArray(new WellSample[0]));
        assertNoneExist(pixels.toArray(new Pixels[0]));

    }

    /**
     * Tests to delete a screen and keep the plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testScreenKeepPlates() throws Exception {

        newUserAndGroup("rw----");

        List<Pixels> pixels = createScreen();

        Pixels p = pixels.get(0);
        WellSample ws = getWellSample(p);
        Plate plate = ws.getWell().getPlate();
        Screen screen = plate.copyScreenLinks().get(0).getParent();
        long sid = screen.getId().getValue();

        final Delete2 dc = Requests.delete("Screen", sid);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList(Plate.class.getSimpleName());
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        assertDoesNotExist(screen);
        assertExists(plate);

    }

    @Test(groups = {"ticket12601", "ticket:3890"})
    public void testImportMultiplePlates() throws Exception {
        create(new Creator() {
            public OME create(XMLMockObjects xml) {
                return xml.createPopulatedScreen(2, 2, 2, 2, 2);
            }
        });
    }

    //
    // Helpers
    //

    interface Creator {
        OME create(XMLMockObjects xml);
    }

    private List<Pixels> createScreen() throws IOException, Exception {
        return create(new Creator() {
            public OME create(XMLMockObjects xml) {
                return xml.createPopulatedScreen();
            }
        });
    }

    private List<Pixels> create(Creator creator) throws Exception {

        File f = File.createTempFile("testImportPlate", ".ome.xml");
        f.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = creator.create(xml);
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, "ome.xml");
        } catch (Throwable e) {
            throw new Exception("cannot import the plate", e);
        }
        return pixels;
    }

}
