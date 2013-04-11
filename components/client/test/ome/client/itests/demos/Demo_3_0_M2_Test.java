/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.demos;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.*;

import javax.sql.DataSource;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.core.Pixels;
import ome.model.enums.RenderingModel;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

@Test(
// "ignored" because it should only be run manually
groups = { "ignore", "manual", "client", "integration", "demo", "3.0", "3.0-M2" })
public class Demo_3_0_M2_Test extends TestCase {

    private static Logger TESTLOG = LoggerFactory.getLogger("TEST-"
            + Demo_3_0_M2_Test.class.getName());

    static int Xmax = 1024, Ymax = 1024, Zmax = 24, Tmax = 120, Cmax = 3;

    ServiceFactory sf;

    IQuery iQuery;

    IUpdate up;

    DataSource ds;

    SimpleJdbcTemplate jdbc;

    @Configuration(beforeTestClass = true)
    public void config() {
        TESTLOG.info("INIT");
        sf = new ServiceFactory("ome.client.test");
        iQuery = sf.getQueryService();
        up = sf.getUpdateService();

        ds = (DataSource) sf.getContext().getBean("dataSource");
        jdbc = new SimpleJdbcTemplate(ds);

        TESTLOG.info("PSQL/bug649");
        try {
            iQuery.get(Experimenter.class, 0L);
        } catch (Exception e) {
            // ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }

    }

    @Test(groups = { "ticket:193", "ticket:195" })
    public void testRenderingEngineDataSavedOnlyManuallys() throws Exception {
        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix.setSizeX(16);
        pix.setSizeY(16);
        pix.setSizeZ(1);
        pix.setSizeT(1);
        pix.setSizeC(1);
        pix = up.saveAndReturnObject(pix);

        RenderingEngine re = sf.createRenderingEngine();
        RenderingModel model = re.getModel();
        List<RenderingModel> models = re.getAvailableModels();
        boolean modified = false;
        for (RenderingModel newModel : models) {
            if (!newModel.getId().equals(model.getId())) {
                re.setModel(newModel);
                modified = true;
                break;
            }
        }
        assertTrue(modified);
        re.getDefaultT();
    }

}
