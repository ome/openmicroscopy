/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import ome.api.IPixels;
import ome.api.IUpdate;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "renderingengine", "broken" }
// Needs an ImporterFixture
)
public class RenderingEngineTest extends TestCase {

    private static Log log = LogFactory.getLog(RenderingEngineTest.class);

    /*
     * Pixels p = new Pixels(); AcquisitionContext ac = new
     * AcquisitionContext(); PhotometricInterpretation pi = new
     * PhotometricInterpretation(); AcquisitionMode mode = new
     * AcquisitionMode(); PixelsType pt = new PixelsType(); DimensionOrder dO =
     * new DimensionOrder(); PixelsDimensions pd = new PixelsDimensions(); Image
     * i = new Image(); Channel c = new Channel();
     */

    ServiceFactory sf;
    RenderingEngine re;
    RenderingEngine re2;
    IUpdate iUpdate;

    @Configuration(beforeTestMethod = true)
    public void setup() {
        sf = new ServiceFactory();
        iUpdate = sf.getUpdateService();
    }

    @Test
    public void test_simpleUsage() throws Exception {
        try {
            sf.getQueryService().get(Experimenter.class, 0L);
        } catch (Exception e) {
            // ignore ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }

        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix = iUpdate.saveAndReturnObject(pix);

        re.lookupPixels(pix.getId());
        re.load();

    }

    @Test
    public void test_simpleUsageWithRenderingDef() throws Exception {

        Pixels pix = ObjectFactory.createPixelGraph(null);
        RenderingDef def = ObjectFactory.createRenderingDef();
        pix.addRenderingDef(def);

        pix = iUpdate.saveAndReturnObject(pix);

        re.lookupPixels(pix.getId());
        re.lookupRenderingDef(pix.getId());
        re.load();

    }

    @Test
    public void test_differentReferences() throws Exception {
        assertTrue(re != re2);
    }

    private final static Long MAGE_PIXELS_54 = 54L;

    @Test(groups = { "magedb", "ticket:194" })
    public void testUnloadedDetails() throws Exception {
        re.lookupPixels(MAGE_PIXELS_54);
        re.lookupRenderingDef(MAGE_PIXELS_54);
        re.load();

        RenderingModel m = re.getModel();
        List families = re.getAvailableFamilies();
        Pixels pix = re.getPixels();
        test(m, families, pix);
    }

    @Test(groups = { "magedb", "ticket:194" })
    public void testUnloadedDetailsWithGetBean() throws Exception {
        RenderingEngine gotBean = sf.createRenderingEngine();

        gotBean.lookupPixels(MAGE_PIXELS_54);
        gotBean.lookupRenderingDef(MAGE_PIXELS_54);
        gotBean.load();

        RenderingModel m = gotBean.getModel();
        List families = gotBean.getAvailableFamilies();
        Pixels pix = gotBean.getPixels();
        test(m, families, pix);
    }

    @Test(groups = { "manualimport", "ticket:258" })
    public void testQuantumDefTransientObjectException() throws Exception {
        RenderingEngine re = sf.createRenderingEngine();
        re.lookupPixels(1L);
        re.lookupRenderingDef(1L);
        re.load();

        re.getModel();
        re.setQuantumStrategy(1);
    }

    // copied from server test
    @Test(groups = "ticket:330")
    public void testPixelsIsFilled() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = sf.getUpdateService().saveAndReturnObject(p);

        IPixels pix = sf.getPixelsService();
        Pixels t = pix.retrievePixDescription(p.getId());
        testPixelsFilled(t);

        RenderingEngine re = sf.createRenderingEngine();
        re.lookupPixels(p.getId());
        t = re.getPixels();
        testPixelsFilled(t);
    }

    private void testPixelsFilled(Pixels t) {
        // assertTrue( t.sizeOfPlaneInfo() >= 0 );

/*
        PixelsDimensions pd = t.getPixelsDimensions();
        assertNotNull(pd);
        assertNotNull(pd.getSizeX());
*/
        Collection<Channel> c = t.unmodifiableChannels();
        assertNotNull(c);
        assertTrue(c.size() > 0);

        for (Channel ch : c) {
            assertNotNull(ch.getLogicalChannel());
        }
    }

    // ~ Helpers
    // =========================================================================

    private void test(RenderingModel m, List<Family> families, Pixels pix) {
        assertNotNull(m);
        assertNotNull(pix);
        assertNotNull(families);
        assertTrue(families.size() > 0);

    }
}
