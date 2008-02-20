/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.spw;

import ome.model.core.Image;
import ome.model.screen.Plate;
import ome.model.screen.Reagent;
import ome.model.screen.Screen;
import ome.model.screen.ScreenAcquisition;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

public class SpwTest extends AbstractManagedContextTest {

    Screen s;
    Plate p;
    Well w;
    Image i;

    ScreenAcquisition sa;

    @Test
    public void testMinimalSave() {
        // The trick is to save via the WellSample
        Screen s = new Screen("s");
        Plate p = new Plate("p");
        Well w = new Well();
        Reagent r = new Reagent("r");
        ScreenAcquisition sa = new ScreenAcquisition(s);
        WellSample ws = new WellSample(w, sa);

        s.linkPlate(p);
        p.addWell(w);

        s.linkReagent(r);
        r.addWell(w);

        ws.linkImage(new Image("i"));
        w.addWellSample(ws);
        sa.addWellSample(ws);

        ws = iUpdate.saveAndReturnObject(ws);
    }

    @Test
    public void testMultistageSave() {
        loginRoot();

        Screen s = new Screen("s");
        Plate p = new Plate("p");
        Well w = new Well();
        s.linkPlate(p);
        p.addWell(w);

        Reagent r = new Reagent("r");
        s.linkReagent(r);
        r.addWell(w);

        s = iUpdate.saveAndReturnObject(s);
        p = s.linkedPlateList().get(0);
        w = p.unmodifiableWells().iterator().next();

        w = iQuery
                .findByQuery(
                        "select w from Well w left outer join fetch w.wellSamples where w.id = :id",
                        new Parameters().addId(w.getId()));

        sa = new ScreenAcquisition(s);
        sa = iUpdate.saveAndReturnObject(sa);

        WellSample ws = new WellSample(w, sa);
        ws.linkImage(new Image("i"));
        w.addWellSample(ws);
        sa.addWellSample(ws);
        ws = iUpdate.saveAndReturnObject(ws);

        iQuery.findAllByQuery("select w from Well w where w.column is null",
                null);
    }
}
