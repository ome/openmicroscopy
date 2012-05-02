/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.spw;

import ome.model.core.Image;
import ome.model.spw.Plate;
import ome.model.spw.Reagent;
import ome.model.spw.Screen;
import ome.model.spw.PlateAcquisition;
import ome.model.spw.Well;
import ome.model.spw.WellSample;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

public class SpwTest extends AbstractManagedContextTest {

    Screen s;
    Plate p;
    Well w;
    Image i;

    PlateAcquisition pa;

    @Test
    public void testMinimalSave() {

        Screen s = new Screen("s");
        Plate p = new Plate("p");
        Well w = new Well();
        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        Image i = new Image();
        i.setName("i");
        i.setAcquisitionDate(testTimestamp);
        Reagent r = new Reagent();
        r.setName("r");
        PlateAcquisition pa = new PlateAcquisition();
        p.addPlateAcquisition(pa);
        WellSample ws = new WellSample();
        pa.addWellSample(ws);

        s.linkPlate(p);
        p.addWell(w);

        s.addReagent(r);
        r.setWells(w);

        w.addWellSample(ws);
        i.setWellSamples(ws);
        pa.addWellSample(ws);

        s = iUpdate.saveAndReturnObject(s);
    }

    @Test
    public void testMultistageSave() {
        loginRoot();

        Screen s = new Screen("s");
        Plate p = new Plate("p");
        Well w = new Well();
        s.linkPlate(p);
        p.addWell(w);

        Reagent r = new Reagent();
        r.setName("r");
        s.addReagent(r);
        r.setWells(w);

        s = iUpdate.saveAndReturnObject(s);
        p = s.linkedPlateList().get(0);
        w = p.unmodifiableWells().iterator().next();

        w = iQuery.findByQuery("select w from Well w "
                + "left outer join fetch w.wellSamples " + "where w.id = :id",
                new Parameters().addId(w.getId()));

        pa = new PlateAcquisition();
        p.addPlateAcquisition(pa);
        pa = iUpdate.saveAndReturnObject(pa);

        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

        Image i = new Image();
        i.setName("i");
        i.setAcquisitionDate(testTimestamp);

        WellSample ws = new WellSample();
        pa.addWellSample(ws);
        i.setWellSamples(ws);
        w.addWellSample(ws);
        ws = iUpdate.saveAndReturnObject(ws);

        iQuery.findAllByQuery("select w from Well w where w.column is null",
                null);
    }

    @Test
    public void testRetrievingWellSamples() {
        loginRoot();

        Plate plate = new Plate("plate");
        Well well = new Well();
        WellSample sample = new WellSample();
        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        Image image = new Image();
        image.setName("image");
        image.setAcquisitionDate(testTimestamp);
        plate.addWell(well);
        well.addWellSample(sample);
        sample.setImage(image);
        plate = iUpdate.saveAndReturnObject(plate);
        well = plate.unmodifiableWells().iterator().next();

        StringBuilder sb = new StringBuilder();
        sb.append("select well from Well well ");
        sb.append("left outer join fetch well.wellSamples ws ");
        sb.append("left outer join fetch ws.image wsi ");
        sb.append("where well.plate.id = :plateID");

        well = iQuery.findByQuery(sb.toString(), new Parameters().addLong(
                "plateID", well.getPlate().getId()));
        assertNotNull(well);
        assertTrue(well.sizeOfWellSamples() > 0);
    }
}
