/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.utests;

import junit.framework.TestCase;
import ome.model.core.Image;
import ome.model.screen.Plate;
import ome.model.screen.Reagent;
import ome.model.screen.Screen;
import ome.model.screen.ScreenAcquisition;
import ome.model.screen.Well;
import ome.model.screen.WellSample;

import org.testng.annotations.Test;

public class ScreenPlateWellTest extends TestCase {

    // Public since used by integration test for simple save

    public Screen screen = new Screen("s");

    public Plate plate1 = new Plate("p1"), plate2 = new Plate("p2");

    public Well well1_1 = new Well(), well1_2 = new Well(),
            well2_1 = new Well(), well2_2 = new Well();

    public WellSample ws1_1a = new WellSample(), ws1_1b = new WellSample(),
            ws1_2a = new WellSample(), ws1_2b = new WellSample(),
            ws2_1a = new WellSample(), ws2_1b = new WellSample(),
            ws2_2a = new WellSample(), ws2_2b = new WellSample();

    public ScreenAcquisition acq1 = new ScreenAcquisition(),
            acq2 = new ScreenAcquisition(), acq3 = new ScreenAcquisition(),
            acq4 = new ScreenAcquisition();

    public Reagent reagentA = new Reagent(), reagentB = new Reagent(),
            reagentC = new Reagent();
    {
        reagentA.setName("A");
        reagentB.setName("B");
        reagentC.setName("C");
    }

    public Image i1_1a = new Image("i1_1a");
    public Image i1_1b = new Image("i1_1b");
    public Image i1_2a = new Image("i1_2a");
    public Image i1_2b = new Image("i1_2b");
    public Image i2_1a = new Image("i2_1a");
    public Image i2_1b = new Image("i2_1b");
    public Image i2_2a = new Image("i2_2a");
    public Image i2_2b = new Image("i2_2b");

    @Test
    public void testBuildingAScreen() {

        screen.setName("screen-name");
        screen.setDescription("screen-description");

        screen.linkPlate(plate1);
        screen.linkPlate(plate2);

        plate1.addWell(well1_1);
        plate1.addWell(well1_2);

        plate2.addWell(well2_1);
        plate2.addWell(well2_2);

        well1_1.addWellSample(ws1_1a);
        well1_1.addWellSample(ws1_1b);

        well1_2.addWellSample(ws1_2a);
        well1_2.addWellSample(ws1_2b);

        well2_1.addWellSample(ws2_1a);
        well2_1.addWellSample(ws2_1b);

        well2_2.addWellSample(ws2_2a);
        well2_2.addWellSample(ws2_2b);

        ws1_1a.linkImage(i1_1a);
        ws1_1b.linkImage(i1_1b);
        ws1_2a.linkImage(i1_2a);
        ws1_2b.linkImage(i1_2b);
        ws2_1a.linkImage(i2_1a);
        ws2_1b.linkImage(i2_1b);
        ws2_2a.linkImage(i2_2a);
        ws2_2b.linkImage(i2_2b);

        screen.addScreenAcquisition(acq1);
        screen.addScreenAcquisition(acq2);
        screen.addScreenAcquisition(acq3);
        screen.addScreenAcquisition(acq4);

        acq1.linkWellSample(ws1_1a);
        acq1.linkWellSample(ws1_1b);
        acq1.linkWellSample(ws1_2a);
        acq1.linkWellSample(ws1_2b);
        acq2.linkWellSample(ws2_1a);
        acq2.linkWellSample(ws2_1b);
        acq3.linkWellSample(ws2_2a);
        acq4.linkWellSample(ws2_2b);

        reagentA.setScreen(screen);
        reagentB.setScreen(screen);
        reagentC.setScreen(screen);

        reagentA.linkWell(well1_1);
        reagentA.linkWell(well2_2);
        reagentB.linkWell(well2_1);
        reagentC.linkWell(well1_2);

    }
}
