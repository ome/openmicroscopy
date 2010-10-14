/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import integration.AbstractTest;
import integration.DeleteServiceTest;
import integration.XMLMockObjects;
import integration.XMLWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ome.xml.model.OME;
import omero.api.delete.DeleteCommand;
import omero.model.Experiment;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Screen;
import omero.model.WellSample;

import org.testng.annotations.Test;

/**
 * Tests for deleting screen/plate/wells
 *
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class SpwDeleteTest extends AbstractTest {

    @Test(groups = { "ticket:3102" })
    public void testScreen() throws Exception {

        newUserAndGroup("rw----");

        File f = File.createTempFile("testImportPlate", "."+OME_FORMAT);

        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        OME ome = xml.createPopulatedScreen();
        writer.writeFile(f, ome, true);
        List<Pixels> pixels = null;
        try {
                pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
                throw new Exception("cannot import the plate", e);
        }

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

        delete(client, new DeleteCommand(DeleteServiceTest.REF_SCREEN,
                screen.getId().getValue(), null));

        assertDoesNotExist(exp);
        assertDoesNotExist(screen);
        assertNoneExist(plates.toArray(new Plate[0]));
        assertNoneExist(samples.toArray(new WellSample[0]));
        assertNoneExist(pixels.toArray(new Pixels[0]));

    }

}
