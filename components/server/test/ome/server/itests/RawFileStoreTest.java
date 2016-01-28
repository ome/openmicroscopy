/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import ome.api.RawFileStore;
import ome.model.core.OriginalFile;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 */
@Test(groups = "integration")
public class RawFileStoreTest extends AbstractManagedContextTest {

    @Test(groups = "ticket:1961")
    public void testClose() {
        OriginalFile file = new OriginalFile();
        file.setName("name");
        file.setPath("/tmp/path");
        file.setHash("");
        file.setSize(-1L);
        file.setMimetype("application/octet-stream");
        file = iUpdate.saveAndReturnObject(file);
        RawFileStore rfs = factory.createRawFileStore();
        rfs.setFileId(file.getId());
        rfs.write(new byte[]{0,1,2,3}, 0, 4);
        rfs.close();
        file = iQuery.get(OriginalFile.class, file.getId());
        assertFalse(file.getSize().equals(-1L)); // The should be updated
        assertFalse(file.getHash().equals("")); // These should be updated
    }
}
