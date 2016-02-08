/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import ome.model.IObject;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.Microscope;
import ome.model.enums.MicroscopeType;

import org.testng.annotations.Test;

/**
 * Test of the model objects as changed for 4.2.
 */
@Test(groups = { "4.2", "integration" })
public class Model42Test extends AbstractUpdateTest {

    @Test
    public void testLightPath() {

        Instrument instrument = new Instrument();

        Microscope microscope = new Microscope();
        microscope.setManufacturer("Acme");
        microscope.setModel("Whizbaz");
        microscope.setSerialNumber("123");
        microscope.setType(new MicroscopeType("Other"));
        instrument.setMicroscope(microscope);

        FilterSet filterSet = new FilterSet();
        filterSet.setInstrument(instrument);

        LightPath lightPath = new LightPath();

        Dichroic dichroic = new Dichroic();
        dichroic.setInstrument(instrument);
        filterSet.setDichroic(dichroic); // On both, filterSet
        lightPath.setDichroic(dichroic); // and lightPath

        Filter filter1 = new_Filter(instrument);
        Filter filter2 = new_Filter(instrument);
        Filter filter3 = new_Filter(instrument);
        Filter filter4 = new_Filter(instrument);

        filterSet.linkExcitationFilter(filter1);
        filterSet.linkExcitationFilter(filter2);
        filterSet.linkEmissionFilter(filter3);
        filterSet.linkEmissionFilter(filter4);

        lightPath.linkExcitationFilter(filter1); // These are ordered
        lightPath.linkExcitationFilter(filter2);
        lightPath.linkEmissionFilter(filter3); // These aren't
        lightPath.linkEmissionFilter(filter4);

        iUpdate.saveAndReturnArray(new IObject[] { instrument, lightPath });

    }

    static int count = 0;

    Filter new_Filter(Instrument i) {
        Filter f = new Filter();
        f.setInstrument(i);
        f.setManufacturer("Acme");
        f.setLotNumber("" + count++);
        f.setModel("test");
        return f;
    }
}
