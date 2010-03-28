/*
 * ome.server.itests.ThumbnailServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

// Java imports

import java.util.Arrays;
import java.util.HashSet;

import ome.api.IQuery;
import ome.api.ThumbnailStore;
import ome.formats.importer.util.TinyImportFixture;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.parameters.Parameters;
import omeis.providers.re.RenderingEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ThumbnailServiceTest extends AbstractManagedContextTest {

    private static Log log = LogFactory.getLog(ThumbnailServiceTest.class);

    private ThumbnailStore tb;

    private IQuery qs;

    private Long pix1, pix2;

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Override
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception {
        super.setUp();
    }

    @Override
    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception {
        super.tearDown();
    }

    // =========================================================================

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        // ome.security.Utils.setUserAuth();
        tb = factory.createThumbnailService();
        qs = factory.getQueryService();
        pix1 = newPixels();
        pix2 = newPixels();
    }

    private long newPixels() throws Exception {
        return makePixels().getId();
    }


    @Test
    public void testSimpleCallsPostJta() throws Exception {
        RenderingEngine re = factory.createRenderingEngine();
        re.lookupPixels(pix1);
        re.load();
        re.resetDefaults();
        re.close();
        tb.setPixelsId(pix1);
        tb.getThumbnailSet(16, 16, new HashSet<Long>(Arrays.asList(pix1, pix2)));
    }

    // Currently this assumes that all renderingdefs are valid. This may
    // not be the case. Need better integration with bioformats-omero.
    // see ticket:218
    @Test(groups = { "ticket:410", "tickets:218", "broken" })
    public void testThumbnailsDirect() throws Exception {

        RenderingDef def = (RenderingDef) qs
                .findAllByQuery(
                        "from RenderingDef where pixels.sizeX > 8 and pixels.sizeY > 8"
                                + " and details.owner.id = :id and pixels.details.owner.id = :id",
                        new Parameters().addId(iAdmin.getEventContext()
                                .getCurrentUserId())).get(0);
        if (def != null) {
            Pixels p = qs.get(Pixels.class, def.getPixels().getId());

            tb.setPixelsId(p.getId());
            tb.setRenderingDefId(def.getId());
            tb.getThumbnailDirect(8, 8);
        }
    }

}
