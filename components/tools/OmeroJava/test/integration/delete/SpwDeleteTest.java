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
import java.util.List;

import ome.xml.model.OME;
import omero.api.delete.DeleteCommand;
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

        Pixels p = pixels.get(0);
        WellSample ws = getWellSample(p);
        Plate plate = ws.getWell().getPlate();
        Screen screen = plate.copyScreenLinks().get(0).getParent();

        delete(client, new DeleteCommand(DeleteServiceTest.REF_SCREEN,
                screen.getId().getValue(), null));

        assertDoesNotExist(screen);
        assertDoesNotExist(plate);
        assertDoesNotExist(ws);
        assertDoesNotExist(p);

    }

}
