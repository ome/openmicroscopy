/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import ome.api.IDelete;
import ome.formats.importer.util.TinyImportFixture;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class DeleteServiceTest extends AbstractManagedContextTest {

    @Test
    public void testServiceLoadsOnClient() throws Exception {
        TinyImportFixture tiny = new TinyImportFixture(this.factory);
        tiny.setUp();
        tiny.doImport();
        Dataset d = tiny.getDataset();
        Image i = d.linkedImageList().get(0);
        IDelete srv = this.factory.getServiceByClass(IDelete.class);
        srv.deleteImage(i.getId(), true);
    }

}
