/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.util.Arrays;

import ome.model.IObject;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.Microscope;
import ome.model.core.Image;
import ome.model.enums.MicroscopeType;
import ome.model.meta.Namespace;
import ome.model.roi.Roi;

import org.testng.annotations.Test;

/**
 * Test of the model objects as changed for 4.2.
 */
@Test(groups = { "4.2", "integration" })
public class Model42Test extends AbstractUpdateTest {

    @Test
    public void testNamespace() {
        Namespace ns = new Namespace();
        ns.setName("openmicroscopy.org/test/foo");
        ns.setDescription("Namespace used for testing\n"
                + "If this were a real namespace \n"
                + "you could explain to the user how \n" + "interpret values.");
        ns.setKeywords(Arrays.asList("key1, key2"));
        ns.setDisplay(false); // This namespace is of interest to users
        ns.setMultivalued(false); // Users should pick one keyword

        loginRoot(); // System-type
        ns = iUpdate.saveAndReturnObject(ns);
    }

    @Test
    public void testRoiKeywords() {
        Image img = new_Image("model42");
        Roi roi = new Roi();
        roi.setImage(img);
        roi.setNamespaces(Arrays.asList("ns0", "ns0"));
        roi.setKeywords(Arrays.asList(new String[] { "ns0-0", "ns0-1" },
                new String[] { "ns1-0", "ns1-1" }));
        roi = iUpdate.saveAndReturnObject(roi);
        assertEquals("ns0-0", roi.getKeywords().get(0)[0]);
        assertEquals("ns0-1", roi.getKeywords().get(0)[1]);
        assertEquals("ns1-0", roi.getKeywords().get(1)[0]);
        assertEquals("ns1-1", roi.getKeywords().get(1)[1]);
    }

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
