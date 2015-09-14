/*
 * ome.server.itests.PixelsServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import java.util.Collection;

import ome.api.IPixels;
import ome.api.RawPixelsStore;
import ome.io.nio.RomioPixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.RenderingModel;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
@Test(groups = "integration")
public class PixelsServiceTest extends AbstractManagedContextTest {

    private static Logger log = LoggerFactory.getLogger(PixelsServiceTest.class);

    private IPixels pix;


    // =========================================================================

    @BeforeClass
    protected void setup() throws Exception {
        // ome.security.Utils.setUserAuth();
        pix = factory.getPixelsService();
    }

    @Test
    public void testPix() {

        Parameters param = new Parameters(new Filter().unique().page(0, 1));
        Pixels p = (Pixels) iQuery.findByQuery("select p from Pixels p", param);
        assertNotNull(p);

        Pixels test = pix.retrievePixDescription(p.getId());
        assertNotNull(test);
    }

    @Test(groups = { "broken", "ticket:119" })
    public void testLetsSaveADefinition() throws Exception {
        Pixels p = pix.retrievePixDescription(1L);
        assertNotNull(p);
        RenderingDef r = makeRndDef(p);
        r = iUpdate.saveAndReturnObject(r);

        RenderingDef test = pix.retrieveRndSettings(1L);
        assertNotNull(test);
    }

    // TODO to ObjectFactory
    private RenderingDef makeRndDef(Pixels p) {
        RenderingDef r = new RenderingDef();
        r.setDefaultT(1);
        r.setDefaultZ(1);
        r.setPixels(p);

        RenderingModel m = new RenderingModel();
        m.setValue("test");
        r.setModel(m);

        QuantumDef qd = new QuantumDef();
        qd.setBitResolution(1);
        qd.setCdStart(1);
        qd.setCdEnd(1);

        r.setQuantization(qd);
        return r;
    }

    @Test(groups = "ticket:330")
    public void testPixelsIsFilled() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        Image i = factory.getUpdateService().saveAndReturnObject(p.getImage());
        p = i.getPrimaryPixels();

        IPixels pix = factory.getPixelsService();
        Pixels t = pix.retrievePixDescription(p.getId());
        testPixelsFilled(t);

        RenderingEngine re = factory.createRenderingEngine();
        re.lookupPixels(p.getId());
        t = re.getPixels();
        testPixelsFilled(t);
    }

    private void testPixelsFilled(Pixels t) {
        // assertTrue( t.sizeOfPlaneInfo() >= 0 );

        Collection<Channel> c = t.unmodifiableChannels();
        assertNotNull(c);
        assertTrue(c.size() > 0);

        for (Channel ch : c) {
            assertNotNull(ch.getLogicalChannel());
        }
    }

    @Test
    public void testPerformance() {
        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix.setSizeX(512);
        pix.setSizeY(512);
        pix.setSizeZ(1);
        pix.setSizeT(1);
        pix.setSizeC(1);
        pix = iUpdate.saveAndReturnObject(pix.getImage()).getPixels(0);
        RawPixelsStore raw = this.factory.createRawPixelsStore();
        raw.setPixelsId(pix.getId(), false);
        raw.calculateMessageDigest();

        long size = raw.getPlaneSize();
        byte[] data = new byte[RomioPixelBuffer.safeLongToInteger(size)];
        raw.setPlane(data, 0, 0, 0);
        raw.getPlane(0, 0, 0);
    }
}
