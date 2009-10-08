/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.io.File;

import ome.services.blitz.impl.MetadataStoreI;
import ome.services.roi.PopulateRoiJob;
import ome.services.util.Executor;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

@Test(groups = "integration")
public class MetadataStoreITest extends AbstractServantTest {

    File file() {
        File toplevel = new File(
                "components/tools/OmeroPy/src/omero/util/populate_roi.py");
        File relative = new File(
                "../tools/OmeroPy/src/omero/util/populate_roi.py");
        if (toplevel.exists()) {
            return toplevel;
        } else if (relative.exists()) {
            return relative;
        } else {
            throw new RuntimeException("huh? where are you?");
        }
    }

    /*
    BROKEN BY r5316
    @Test(groups = "ticket:1193")
    public void testPostProcess() throws Exception {
        setUp();
        Executor ex = (Executor) ctx.getBean("executor");
        PopulateRoiJob popRoi = new PopulateRoiJob(root_sf.getPrincipal(), ex,
                file());
        popRoi.init();
        popRoi.createJob();
        MetadataStoreI ms = new MetadataStoreI(be, popRoi);
        configure(ms, user_initializer);
        ms.setServiceFactory(user_sf);
        ParametersI p = new ParametersI().add("pixels", omero.rtypes.rlist(omero.rtypes.rlong(1)));
        assertFindByQuery(MetadataStoreI.plate_query, p);
    }
    */

}
