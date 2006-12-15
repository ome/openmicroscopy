/*
 * ome.server.itests.PixelsServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

// Java imports

// Third-party libraries
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

// Application-internal dependencies
import ome.api.IPixels;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.RenderingModel;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
@Test(groups = "integration")
public class PixelsServiceTest extends AbstractManagedContextTest {

    private static Log log = LogFactory.getLog(PixelsServiceTest.class);

    private IPixels pix;

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception {
        super.tearDown();
    }

    // =========================================================================

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
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
        r = (RenderingDef) iUpdate.saveAndReturnObject(r);

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
        p = factory.getUpdateService().saveAndReturnObject(p);

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

        PixelsDimensions pd = t.getPixelsDimensions();
        assertNotNull(pd);
        assertNotNull(pd.getSizeX());

        List c = t.getChannels();
        assertNotNull(c);
        assertTrue(c.size() > 0);

        for (Object object : c) {
            Channel ch = (Channel) object;
            assertNotNull(ch.getLogicalChannel());
        }
    }

}
